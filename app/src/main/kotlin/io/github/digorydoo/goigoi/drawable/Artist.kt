package io.github.digorydoo.goigoi.drawable

import android.graphics.*
import android.text.SpannableString
import android.text.style.ReplacementSpan
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object Artist {
    class TextExtent(val width: Float, val ascent: Float, val descent: Float)

    /**
     * Draws the SpannableString on the canvas. This currently only works for ReplacementSpans,
     * and we currently ignore str.getSpanFlags.
     */
    fun drawSpan(str: SpannableString, left: Float, yBaseLine: Float, p: Paint, canvas: Canvas) {
        val spans = str.getSpans(0, str.length, ReplacementSpan::class.java)
        var i = 0
        var x = left
        val metrics = Paint.FontMetricsInt()

        for (span in spans) {
            val start = str.getSpanStart(span)
            val end = str.getSpanEnd(span)

            if (start < 0 || end < 0) continue

            if (i < start) {
                canvas.drawText(str, i, start, x, yBaseLine, p)
                x += p.measureText(str, i, start)
            }

            val width = span.getSize(p, str, start, end, metrics)
            val top = yBaseLine + metrics.ascent
            val bottom = yBaseLine + metrics.descent
            span.draw(canvas, str, start, end, x, top.toInt(), yBaseLine.toInt(), bottom.toInt(), p)

            x += width
            i = end
        }

        if (i < str.length) {
            canvas.drawText(str, i, str.length, x, yBaseLine, p)
        }
    }

    /**
     * Measures the given spannable string.
     * @return An instance of TextExtent
     */
    fun measureSpan(str: SpannableString, p: Paint): TextExtent {
        val spans = str.getSpans(0, str.length, ReplacementSpan::class.java)
        var i = 0
        var width = 0.0f
        var minAscent = 0.0f
        var maxDescent = 0.0f
        val metrics = Paint.FontMetricsInt()

        for (span in spans) {
            val start = str.getSpanStart(span)
            val end = str.getSpanEnd(span)

            if (start < 0 || end < 0) continue

            if (i < start) {
                width += p.measureText(str, i, start)
                minAscent = min(minAscent, p.fontMetrics.ascent)
                maxDescent = max(maxDescent, p.fontMetrics.descent)
            }

            width += span.getSize(p, str, start, end, metrics)
            minAscent = min(minAscent, metrics.ascent.toFloat())
            maxDescent = max(maxDescent, metrics.descent.toFloat())
            i = end
        }

        if (i < str.length) {
            width += p.measureText(str, i, str.length)
            minAscent = min(minAscent, p.fontMetrics.ascent)
            maxDescent = max(maxDescent, p.fontMetrics.descent)
        }

        return TextExtent(width, minAscent, maxDescent)
    }

    fun drawBitmapScaleToFit(
        bmp: Bitmap,
        dstR: RectF,
        p: Paint,
        canvas: Canvas,
        srcOffsetX: Int,
        srcOffsetY: Int,
    ) {
        if (dstR.width() <= 0 || dstR.height() <= 0 || bmp.width <= 0 || bmp.height <= 0) {
            return
        }

        val srcRatio = bmp.width / bmp.height
        val dstRatio = dstR.width() / dstR.height()

        val srcR = if (srcRatio < dstRatio) {
            val sy = dstR.height() / (dstR.width() / bmp.width)
            val top = (0.5f * bmp.height - 0.5f * sy).toInt()
            val bottom = top + sy.toInt()
            Rect(0, top, bmp.width, bottom)
        } else {
            val sx = dstR.width() / (dstR.height() / bmp.height)
            val left = (0.5f * bmp.width - 0.5f * sx).toInt()
            val right = left + sx.toInt()
            Rect(left, 0, right, bmp.height)
        }

        srcR.offset(srcOffsetX, srcOffsetY)
        canvas.drawBitmap(bmp, srcR, dstR, p)
    }

    fun makeCheckmarkPath(bounds: Rect, time: Float): Path {
        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val r = 0.5f * min(bounds.width(), bounds.height())

        val pts = arrayOf(
            Pt(0.0f, cx - 0.65f * r, cy + 0.05f * r),
            Pt(0.7f, cx - 0.26f * r, cy + 0.48f * r),
            Pt(1.0f, cx + 0.67f * r, cy - 0.48f * r)
        )

        val path = Path()
        addAnimatedLine(pts, path, time)
        return path
    }

    private fun addAnimatedLine(pts: Array<Pt>, path: Path, time: Float) {
        var prevPt: Pt? = null

        for (pt in pts) {
            if (prevPt == null) {
                // This is the starting point.
                path.moveTo(pt.x, pt.y)
            } else if (time <= prevPt.time) {
                // This line segment is not yet drawn.
                break
            } else if (time >= pt.time) {
                // This line segment is fully drawn.
                path.lineTo(pt.x, pt.y)
            } else {
                // This line segment is partially drawn.
                val p = (time - prevPt.time) / (pt.time - prevPt.time) // 0..1
                val q = 1.0f - p
                path.lineTo(q * prevPt.x + p * pt.x, q * prevPt.y + p * pt.y)
            }

            prevPt = pt
        }
    }

    fun addAnimatedArc(arcs: Array<Arc>, path: Path, time: Float, bounds: Rect, connect: Boolean) {
        var fromArc: Arc? = null
        var toArc: Arc? = null

        for (a in arcs) {
            if (a.time <= time) {
                fromArc = a
            } else {
                toArc = a
                break
            }
        }

        if (fromArc == null) {
            // This set of arcs don't show yet
            return
        }

        var x1: Float
        var y1: Float
        var x2: Float
        var y2: Float
        val start: Float
        val end: Float
        val radialShift: Float
        val angularShift: Float

        if (toArc == null) {
            // This set of arcs have completed animation.
            x1 = bounds.left + bounds.width() * fromArc.left
            y1 = bounds.top + bounds.height() * fromArc.top
            x2 = bounds.left + bounds.width() * fromArc.right
            y2 = bounds.top + bounds.height() * fromArc.bottom
            start = fromArc.start
            end = fromArc.end
            radialShift = fromArc.radialShift
            angularShift = fromArc.angularShift
        } else {
            // Interpolate between fromArc and toArc.
            val p = (time - fromArc.time) / (toArc.time - fromArc.time) // 0..1
            val q = 1.0f - p
            x1 = bounds.left + bounds.width() * (q * fromArc.left + p * toArc.left)
            y1 = bounds.top + bounds.height() * (q * fromArc.top + p * toArc.top)
            x2 = bounds.left + bounds.width() * (q * fromArc.right + p * toArc.right)
            y2 = bounds.top + bounds.height() * (q * fromArc.bottom + p * toArc.bottom)
            start = q * fromArc.start + p * toArc.start
            end = q * fromArc.end + p * toArc.end
            radialShift = q * fromArc.radialShift + p * toArc.radialShift
            angularShift = q * fromArc.angularShift + p * toArc.angularShift
        }

        if (radialShift != 0.0f) {
            val phi = 0.5f * (start + end) * Math.PI / 180.0f
            val r = 0.5f * max(bounds.width(), bounds.height())
            val xShift = (radialShift * r * cos(phi)).toFloat()
            val yShift = (radialShift * r * sin(phi)).toFloat()
            x1 += xShift
            y1 += yShift
            x2 += xShift
            y2 += yShift
        }

        var sweep = end - start

        if (sweep <= 0) {
            return
        } else if (sweep >= 360.0f) {
            sweep = 359.999f
        }

        path.arcTo(x1, y1, x2, y2, start + angularShift, sweep, !connect)
    }

    class Pt(var time: Float, var x: Float, var y: Float)

    class Arc(
        var time: Float,
        var left: Float,
        var top: Float,
        var right: Float,
        var bottom: Float,
        var radialShift: Float,
        var start: Float,
        var end: Float,
        var angularShift: Float,
    )
}
