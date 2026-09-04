package io.github.digorydoo.goigoi.drawable

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ch.digorydoo.kutils.colour.Colour
import kotlin.math.min
import kotlin.math.pow

class BubbleIconDrawable(
    private val variant: Variant,
    private val value: Float, // 0..1
    private val colours: Colours,
    private val dims: Dimensions,
): AnimatedDrawable() {
    enum class Variant { CIRCULAR, DIAMOND }

    interface Colours {
        val poorRating: Color
        val goodRating: Color
        val background: Color
        val outline: Color
    }

    interface Dimensions {
        val circularInsetPx: Int
        val minBubbleSizePx: Float
        val outlineStrokeWidthPx: Float
    }

    private val bgndPaint = Paint().apply {
        isAntiAlias = true
        color = colours.background.toArgb()
        style = Paint.Style.FILL
    }

    private val innerDotPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val outlinePaint2 = Paint().apply {
        isAntiAlias = true
        color = colours.outline.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = dims.outlineStrokeWidthPx
    }

    override fun draw(canvas: Canvas) {
        val bounds = animatedBounds // always a new object

        if (variant == Variant.CIRCULAR) {
            bounds.inset(dims.circularInsetPx, dims.circularInsetPx)
        }

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val r = 0.5f * min(bounds.width(), bounds.height())

        drawShape(cx, cy, r, bgndPaint, canvas)
        drawShape(cx, cy, r - outlinePaint2.strokeWidth, outlinePaint2, canvas)

        val poorColour = colours.poorRating.toArgb()
        val goodColour = colours.goodRating.toArgb()

        if (value < BUBBLE_FG2_THRESHOLD) {
            innerDotPaint.color = poorColour
        } else {
            val rel = (value - BUBBLE_FG2_THRESHOLD) / (1.0f - BUBBLE_FG2_THRESHOLD)
            innerDotPaint.color = Colour.mixARGB(poorColour, goodColour, rel)
        }

        // If we used value linearly, the bubble would become large too quickly.
        // If we used value quadratic, the bubble would stay small too long.
        // Let's use something in between:
        val v = value.pow(1.42f)

        // We also multiply by 0.96f, to make it obvious that we haven't reached 100% quite yet,
        // since for 100% we would use a CheckmarkIcon instead of a BubbleChart.
        val r2 = dims.minBubbleSizePx + (r - dims.minBubbleSizePx) * v * 0.96f
        drawShape(cx, cy, r2, innerDotPaint, canvas)
    }

    private fun drawShape(cx: Float, cy: Float, r: Float, paint: Paint, canvas: Canvas) {
        when (variant) {
            Variant.CIRCULAR -> {
                canvas.drawCircle(cx, cy, r, paint)
            }
            Variant.DIAMOND -> {
                val path = Path().apply {
                    moveTo(cx, cy - r)
                    lineTo(cx + r, cy)
                    lineTo(cx, cy + r)
                    lineTo(cx - r, cy)
                    close()
                }
                canvas.drawPath(path, paint)
            }
        }
    }

    companion object {
        private const val BUBBLE_FG2_THRESHOLD = 0.75f
    }
}
