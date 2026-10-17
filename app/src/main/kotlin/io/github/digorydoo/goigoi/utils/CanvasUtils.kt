package io.github.digorydoo.goigoi.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.Rect
import android.graphics.RectF
import android.text.SpannableString
import android.text.style.ReplacementSpan

/**
 * Draws the SpannableString on the canvas. This currently only works for ReplacementSpans,
 * and we currently ignore str.getSpanFlags.
 */
fun Canvas.drawSpan(str: SpannableString, left: Float, yBaseLine: Float, p: Paint) {
    val spans = str.getSpans(0, str.length, ReplacementSpan::class.java)
    var i = 0
    var x = left
    val metrics = FontMetricsInt()

    for (span in spans) {
        val start = str.getSpanStart(span)
        val end = str.getSpanEnd(span)

        if (start < 0 || end < 0) continue

        if (i < start) {
            drawText(str, i, start, x, yBaseLine, p)
            x += p.measureText(str, i, start)
        }

        val width = span.getSize(p, str, start, end, metrics)
        val top = yBaseLine + metrics.ascent
        val bottom = yBaseLine + metrics.descent
        span.draw(this, str, start, end, x, top.toInt(), yBaseLine.toInt(), bottom.toInt(), p)

        x += width
        i = end
    }

    if (i < str.length) {
        drawText(str, i, str.length, x, yBaseLine, p)
    }
}

fun Canvas.drawBitmapScaleToFit(
    bmp: Bitmap,
    dstR: RectF,
    p: Paint,
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
    drawBitmap(bmp, srcR, dstR, p)
}
