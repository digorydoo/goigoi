package io.github.digorydoo.goigoi.drawable

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ch.digorydoo.kutils.colour.Colour
import kotlin.math.min

class FabIconDrawable(
    private val iconName: IconName,
    private val colours: Colours,
    private val dims: Dimensions,
): AnimatedDrawable() {
    enum class IconName { NONE, PLAY, ARROW_RIGHT }

    interface Colours {
        val normal: Color
        val pressed: Color
        val shim: Color
        val glow: Color
        val icon: Color
    }

    interface Dimensions {
        val shimWidthPx: Int
        val glowRadiusPx: Float
        val outlinedIconStrokeWidthPx: Float
    }

    var glow = 0.0f

    private val glowPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val shimPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = colours.shim.toArgb()
    }

    private val bgndPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val filledIconPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = colours.icon.toArgb()
    }

    private val outlinedIconPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = dims.outlinedIconStrokeWidthPx
        color = colours.icon.toArgb()
    }

    override fun draw(canvas: Canvas) {
        val rect = animatedBounds

        val cx = rect.centerX().toFloat()
        val cy = rect.centerY().toFloat()
        var r = 0.5f * min(rect.width(), rect.height()).toFloat()
        canvas.drawCircle(cx, cy, r, shimPaint)

        r -= dims.shimWidthPx

        if (r > 0) {
            if (glow > 0.0f) {
                val glowArgb = Colour.mix(
                    Colour.fromARGB(colours.shim.toArgb()),
                    Colour.fromARGB(colours.glow.toArgb()),
                    glow
                ).toARGB()
                glowPaint.color = glowArgb
                glowPaint.setShadowLayer(dims.glowRadiusPx, 0.0f, 0.0f, glowPaint.color)
                canvas.drawCircle(cx, cy, r, glowPaint)
            }

            bgndPaint.color =
                if (state.contains(android.R.attr.state_pressed)) {
                    colours.pressed.toArgb()
                } else {
                    colours.normal.toArgb()
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
