package io.github.digorydoo.goigoi.drawable

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.toRectF
import androidx.core.graphics.withClip
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.furigana.FuriganaBuilder
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import ch.digorydoo.kutils.colour.Colour

class LargeListItemBgnd(
    ctx: Context,
    primaryTextWithFurigana: String,
    private val secondaryText: String,
    private val bgndBitmap: Bitmap?,
    isLargeScreen: Boolean,
): AnimatedDrawable() {
    private val primaryText = FuriganaBuilder.buildSpan(primaryTextWithFurigana, canSeeFurigana = false)

    private val leftPadding = DimUtils.dpToPx(24.0f, ctx)
    private val primaryTextVDelta = DimUtils.dpToPx(48.0f, ctx)
    private val secondaryTextVDelta = DimUtils.dpToPx(24.0f, ctx)
    private val outerCornerSize = DimUtils.dpToPx(16.0f, ctx)
    private val textShadowSize = DimUtils.dpToPx(1.0f, ctx)

    private val bgndPaint = Paint().apply {
        style = Paint.Style.FILL
        color = ResUtils.getARGBFromRes(R.color.green_800, ctx)
    }

    private val primaryTextPaint = Paint().apply {
        style = Paint.Style.FILL
        color = ResUtils.getARGBFromRes(R.color.white, ctx)
        textSize = when (isLargeScreen) {
            true -> DimUtils.dpToPx(36.0f, ctx)
            else -> DimUtils.dpToPx(28.0f, ctx)
        }
    }

    private val secondaryTextPaint = Paint().apply {
        style = Paint.Style.FILL
        color = ResUtils.getARGBFromRes(R.color.white, ctx)
        textSize = when (isLargeScreen) {
            true -> DimUtils.dpToPx(16.0f, ctx)
            else -> DimUtils.dpToPx(13.0f, ctx)
        }
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
            canvas.drawRoundRect(dstR, outerCornerSize, outerCornerSize, bgndPaint)
        } else {
            val path = Path()
            path.addRoundRect(dstR, outerCornerSize, outerCornerSize, Path.Direction.CW)

            // We should probably use BitmapShader instead of withClip. Unfortunately, this would require us to load a
            // separate Bitmap that has the desired proportion, because you can't offset the bitmap with BitmapShader!

            canvas.withClip(path) {
                Artist.drawBitmapScaleToFit(bmp, dstR, bgndPaint, this, 0, 0)
            }
        }
    }

    private fun drawPrimaryText(r: RectF, canvas: Canvas) {
        val x = r.left + leftPadding
        val y = r.bottom - primaryTextVDelta
        primaryTextPaint.setShadowLayer(textShadowSize, 0.0f, 0.0f, Colour.black.toARGB())
        Artist.drawSpan(primaryText, x, y, primaryTextPaint, canvas)
        primaryTextPaint.clearShadowLayer()
    }

    private fun drawSecondaryText(r: RectF, canvas: Canvas) {
        val x = r.left + leftPadding
        val y = r.bottom - secondaryTextVDelta
        secondaryTextPaint.setShadowLayer(textShadowSize, 0.0f, 0.0f, Colour.black.toARGB())
        canvas.drawText(secondaryText, x, y, secondaryTextPaint)
        secondaryTextPaint.clearShadowLayer()
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "TopicListItemBg"
    }
}
