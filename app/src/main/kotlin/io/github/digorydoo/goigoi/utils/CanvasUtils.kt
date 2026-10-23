package io.github.digorydoo.goigoi.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF

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
