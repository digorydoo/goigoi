package io.github.digorydoo.goigoi.drawable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import ch.digorydoo.kutils.colour.Colour
import kotlin.math.min

class FabIcon(ctx: Context, private val iconName: IconName): AnimatedDrawable() {
    enum class IconName { NONE, PLAY, ARROW_RIGHT }

    var glow = 0.0f

    private val btnNormalColour = ResUtils.getARGBFromRes(R.color.green_800, ctx)
    private val btnPressedColour = ResUtils.getARGBFromRes(R.color.green_700, ctx)
    private val shimColour = ResUtils.getColourFromAttr(R.attr.fabShimColour, ctx)
    private val glowColour = ResUtils.getColourFromAttr(R.attr.fabGlowColour, ctx)
    private val shimWidth = DimUtils.dpToPx(8, ctx)
    private val glowRadius = DimUtils.dpToPx(8.0f, ctx)

    private val glowPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val shimPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = shimColour.toARGB()
    }

    private val bgndPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val filledIconPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = ResUtils.getARGBFromRes(R.color.white, ctx)
    }

    private val outlinedIconPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = DimUtils.dpToPx(2.5f, ctx)
        color = ResUtils.getARGBFromRes(R.color.white, ctx)
    }

    override fun draw(canvas: Canvas) {
        val rect = animatedBounds

        val cx = rect.centerX().toFloat()
        val cy = rect.centerY().toFloat()
        var r = 0.5f * min(rect.width(), rect.height()).toFloat()
        canvas.drawCircle(cx, cy, r, shimPaint)

        r -= shimWidth

        if (r > 0) {
            if (glow > 0.0f) {
                glowPaint.color = Colour.mix(shimColour, glowColour, glow).toARGB()
                glowPaint.setShadowLayer(glowRadius, 0.0f, 0.0f, glowPaint.color)
                canvas.drawCircle(cx, cy, r, glowPaint)
            }

            bgndPaint.color =
                if (state.contains(android.R.attr.state_pressed)) {
                    btnPressedColour
                } else {
                    btnNormalColour
                }

            canvas.drawCircle(cx, cy, r, bgndPaint)

            when (iconName) {
                IconName.PLAY -> drawPlayIcon(canvas, cx, cy, r)
                IconName.ARROW_RIGHT -> drawArrowRightIcon(canvas, cx, cy, r)
                else -> Unit
            }
        }
    }

    private fun drawPlayIcon(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val d1 = r * 0.32f
        val d2 = 0.7f * d1

        canvas.drawPath(
            Path().apply {
                moveTo(cx - d2, cy - d1)
                lineTo(cx + d1, cy)
                lineTo(cx - d2, cy + d1)
            },
            filledIconPaint
        )
    }

    private fun drawArrowRightIcon(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val dx1 = r * 0.34f
        val dy = r * 0.34f

        canvas.drawLine(cx - dx1, cy, cx + dy, cy, outlinedIconPaint)

        canvas.drawPath(
            Path().apply {
                moveTo(cx, cy - dy)
                lineTo(cx + dx1, cy)
                lineTo(cx, cy + dy)
            },
            outlinedIconPaint
        )
    }
}
