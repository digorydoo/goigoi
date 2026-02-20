package io.github.digorydoo.goigoi.activity.prog_study.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.KeyLensPart
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import kotlin.math.min

class KeyLensDrawable(ctx: Context, private val keyDef: KeyDef): Drawable() {
    var selected: KeyLensPart? = null

    private val insetSize = DimUtils.dpToPx(1, ctx)
    private val iconSize = DimUtils.dpToPx(24.0f, ctx)

    private val bgndPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = ResUtils.getARGBFromAttr(R.attr.keyLensBgColour, ctx)
    }

    private val centreTextPaint = Paint().apply {
        color = ResUtils.getARGBFromAttr(R.attr.keyLensFgColour, ctx)
        style = Paint.Style.FILL
        textSize = DimUtils.dpToPx(22.0f, ctx)
        textAlign = Paint.Align.CENTER
    }

    private val smallTextPaint = Paint().apply {
        color = ResUtils.getARGBFromAttr(R.attr.keyLensFgColour, ctx)
        style = Paint.Style.FILL
        textSize = DimUtils.dpToPx(16.0f, ctx)
        textAlign = Paint.Align.CENTER
    }

    private val leftIcon = keyDef.leftIconResId?.let { ContextCompat.getDrawable(ctx, it) }
    private val rightIcon = keyDef.rightIconResId?.let { ContextCompat.getDrawable(ctx, it) }

    override fun draw(canvas: Canvas) {
        val bounds = copyBounds()
        bounds.inset(insetSize, insetSize)

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val r = 0.5f * min(bounds.width(), bounds.height())

        canvas.drawCircle(cx, cy, r, bgndPaint)

        if ((selected == null || selected == KeyLensPart.CENTRE) && keyDef.mainText.isNotEmpty()) {
            val yOffset = -centreTextPaint.descent() + 0.5f * (-centreTextPaint.ascent() + centreTextPaint.descent())
            canvas.drawText(keyDef.mainText, cx, cy + yOffset, centreTextPaint)
        }

        val smallTextYOffset = -smallTextPaint.descent() + 0.5f * (-smallTextPaint.ascent() + smallTextPaint.descent())
        val smallTextShift = 0.6f * r

        if (selected == null || selected == KeyLensPart.LEFT) {
            when {
                leftIcon != null -> drawIcon(leftIcon, cx - smallTextShift, cy, canvas)
                keyDef.leftText.isNotEmpty() -> canvas.drawText(
                    keyDef.leftText,
                    cx - smallTextShift,
                    cy + smallTextYOffset,
                    smallTextPaint
                )
            }
        }

        if (selected == null || selected == KeyLensPart.RIGHT) {
            when {
                rightIcon != null -> drawIcon(rightIcon, cx + smallTextShift, cy, canvas)
                keyDef.rightText.isNotEmpty() -> canvas.drawText(
                    keyDef.rightText,
                    cx + smallTextShift,
                    cy + smallTextYOffset,
                    smallTextPaint
                )
            }
        }

        if ((selected == null || selected == KeyLensPart.ABOVE) && keyDef.aboveText.isNotEmpty()) {
            canvas.drawText(keyDef.aboveText, cx, cy + smallTextYOffset - smallTextShift, smallTextPaint)
        }

        if ((selected == null || selected == KeyLensPart.BELOW) && keyDef.belowText.isNotEmpty()) {
            canvas.drawText(keyDef.belowText, cx, cy + smallTextYOffset + smallTextShift, smallTextPaint)
        }
    }

    private fun drawIcon(icon: Drawable, centreX: Float, centreY: Float, canvas: Canvas) {
        val left = (centreX - iconSize / 2).toInt()
        val top = (centreY - iconSize / 2).toInt()
        val right = (left + iconSize).toInt()
        val bottom = (top + iconSize).toInt()
        icon.setBounds(left, top, right, bottom)
        icon.setTintMode(PorterDuff.Mode.SRC_IN)
        icon.setTint(smallTextPaint.color)
        icon.draw(canvas)
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java; still needed for API < 29", ReplaceWith(""))
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}
