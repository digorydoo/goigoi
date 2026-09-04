package io.github.digorydoo.goigoi.drawable

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toRectF
import androidx.core.graphics.withClip
import ch.digorydoo.kutils.colour.Colour
import io.github.digorydoo.goigoi.furigana.FuriganaBuilder

class BigStudyBtnDrawable(
    private val colours: Colours,
    private val dimensions: Dimensions,
    primaryTextWithFurigana: String,
    private val secondaryText: String,
    private val bgndBitmap: Bitmap?,
): AnimatedDrawable() {
    interface Colours {
        val background: Color
        val text: Color
    }

    interface Dimensions {
        val leftPaddingPx: Float
        val primaryTextVDeltaPx: Float
        val secondaryTextVDeltaPx: Float
        val outerCornerSizePx: Float
        val textShadowSizePx: Float
        val primaryTextSizePx: Float
        val secondaryTextSizePx: Float
    }

    private val primaryText = FuriganaBuilder.buildSpan(primaryTextWithFurigana, canSeeFurigana = false)

    private val bgndPaint = Paint().apply {
        style = Paint.Style.FILL
        color = colours.background.toArgb()
    }

    private val primaryTextPaint = Paint().apply {
        style = Paint.Style.FILL
        color = colours.text.toArgb()
        textSize = dimensions.primaryTextSizePx
    }

    private val secondaryTextPaint = Paint().apply {
        style = Paint.Style.FILL
        color = colours.text.toArgb()
        textSize = dimensions.secondaryTextSizePx
    }

    override fun draw(canvas: Canvas) {
        val r = copyBounds().toRectF()
        drawBackground(r, canvas)
        drawPrimaryText(r, canvas)
        drawSecondaryText(r, canvas)
    }

    private fun drawBackground(r: RectF, canvas: Canvas) {
        val dstR = RectF(r)
        val bmp = bgndBitmap

        if (bmp == null) {
            canvas.drawRoundRect(dstR, dimensions.outerCornerSizePx, dimensions.outerCornerSizePx, bgndPaint)
        } else {
            val path = Path()
            path.addRoundRect(dstR, dimensions.outerCornerSizePx, dimensions.outerCornerSizePx, Path.Direction.CW)

            // We should probably use BitmapShader instead of withClip. Unfortunately, this would require us to load a
            // separate Bitmap that has the desired proportion, because you can't offset the bitmap with BitmapShader!

            canvas.withClip(path) {
                Artist.drawBitmapScaleToFit(bmp, dstR, bgndPaint, this, 0, 0)
            }
        }
    }

    private fun drawPrimaryText(r: RectF, canvas: Canvas) {
        val x = r.left + dimensions.leftPaddingPx
        val y = r.bottom - dimensions.primaryTextVDeltaPx
        primaryTextPaint.setShadowLayer(dimensions.textShadowSizePx, 0.0f, 0.0f, Colour.black.toARGB())
        Artist.drawSpan(primaryText, x, y, primaryTextPaint, canvas)
        primaryTextPaint.clearShadowLayer()
    }

    private fun drawSecondaryText(r: RectF, canvas: Canvas) {
        val x = r.left + dimensions.leftPaddingPx
        val y = r.bottom - dimensions.secondaryTextVDeltaPx
        secondaryTextPaint.setShadowLayer(dimensions.textShadowSizePx, 0.0f, 0.0f, Colour.black.toARGB())
        canvas.drawText(secondaryText, x, y, secondaryTextPaint)
        secondaryTextPaint.clearShadowLayer()
    }
}
