package io.github.digorydoo.goigoi.drawable

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ch.digorydoo.kutils.filter.delay
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.vector.Vector2f
import kotlin.math.max
import kotlin.math.min

class RingIconDrawable(
    private val variant: Variant,
    private val value: Float, // 0..1
    private val colours: Colours,
    private val dims: Dimensions,
): AnimatedDrawable() {
    enum class Variant { CIRCULAR, DIAMOND }

    interface Colours {
        val trail: Color
        val track: Color
    }

    interface Dimensions {
        val circularInsetSizePx: Int
        val minStrokeWidthPx: Float
    }

    private val trackPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = colours.track.toArgb()
    }

    private val trailPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = colours.trail.toArgb()
    }

    override fun draw(canvas: Canvas) {
        val bounds = getAnimatedBounds(delay(animValue, 0.0f, 0.8f))

        if (variant == Variant.CIRCULAR) {
            bounds.inset(dims.circularInsetSizePx, dims.circularInsetSizePx)
        }

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val r = 0.5f * min(bounds.width(), bounds.height())
        val strokeWidth = max(r * RING_REL_STROKEWIDTH, dims.minStrokeWidthPx)
        val r2 = r - strokeWidth / 2.0f

        trackPaint.strokeWidth = strokeWidth * 0.8f
        trailPaint.strokeWidth = strokeWidth
        val shouldDrawTrail = value > 0.00001f

        when (variant) {
            Variant.CIRCULAR -> drawCircularRing(cx, cy, r2, shouldDrawTrail, canvas)
            Variant.DIAMOND -> drawDiamondRing(cx, cy, r2, shouldDrawTrail, canvas)
        }
    }

    private fun drawCircularRing(cx: Float, cy: Float, r: Float, shouldDrawTrail: Boolean, canvas: Canvas) {
        canvas.drawCircle(cx, cy, r, trackPaint)

        if (shouldDrawTrail) {
            val phi = max(0.0f, value * 360.0f) * animValue
            val left = cx - r
            val top = cy - r
            val right = cx + r
            val bottom = cy + r

            canvas.drawArc(left, top, right, bottom, 270f, phi, false, trailPaint)
        }
    }

    private fun drawDiamondRing(cx: Float, cy: Float, r: Float, shouldDrawTrail: Boolean, canvas: Canvas) {
        val corners = getDiamondCorners(cx, cy, r)
        canvas.drawPath(makeClosedPath(corners), trackPaint)

        if (shouldDrawTrail) {
            canvas.drawPath(makeOpenPath(corners, value * animValue), trailPaint)
        }
    }

    private fun getDiamondCorners(cx: Float, cy: Float, r: Float) = arrayOf(
        Vector2f(cx, cy - r),
        Vector2f(cx + r, cy),
        Vector2f(cx, cy + r),
        Vector2f(cx - r, cy),
    )

    private fun makeClosedPath(pts: Array<Vector2f>) = Path().apply {
        pts.forEachIndexed { i, pt ->
            if (i == 0) moveTo(pt.x, pt.y)
            else lineTo(pt.x, pt.y)
        }
        close()
    }

    private fun makeOpenPath(pts: Array<Vector2f>, rel: Float) = Path().apply {
        var prevPt = pts[0]
        moveTo(prevPt.x, prevPt.y)
        val stepSize = 1.0f / pts.size

        for (i in 1 .. pts.size) {
            val ri = i.toFloat() / pts.size
            val thisPt = pts.getOrNull(i) ?: pts[0]

            if (rel > ri) {
                lineTo(thisPt.x, thisPt.y)
            } else {
                val partial = 1.0f - clamp((ri - rel) / stepSize)
                val pr = lerp(prevPt, thisPt, partial)
                lineTo(pr.x, pr.y)
                break
            }

            prevPt = thisPt
        }
    }

    companion object {
        private const val RING_REL_STROKEWIDTH = 0.125f
    }
}
