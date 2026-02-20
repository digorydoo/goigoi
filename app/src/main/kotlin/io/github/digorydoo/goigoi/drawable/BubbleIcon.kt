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
import kotlin.math.pow

class BubbleIcon(
    ctx: Context,
    private val variant: Variant,
    private val value: Float, // 0..1
): AnimatedDrawable() {
    enum class Variant { CIRCULAR, DIAMOND }

    private val poorColour = ResUtils.getARGBFromAttr(R.attr.bubbleFgColour, ctx)
    private val goodColour = ResUtils.getARGBFromRes(R.color.green_800, ctx)

    private val circularInsetWidth = DimUtils.dpToPx(2, ctx)
    private val minBubbleSize = DimUtils.dpToPx(2.0f, ctx)

    private val bgndPaint = Paint().apply {
        isAntiAlias = true
        color = ResUtils.getARGBFromAttr(R.attr.bubbleBgColour, ctx)
        style = Paint.Style.FILL
    }

    private val innerDotPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val outlinePaint2 = Paint().apply {
        isAntiAlias = true
        color = ResUtils.getARGBFromAttr(R.attr.bubbleOutlineColour, ctx)
        style = Paint.Style.STROKE
        strokeWidth = DimUtils.dpToPx(1.0f, ctx)
    }

    override fun draw(canvas: Canvas) {
        val bounds = animatedBounds // always a new object

        if (variant == Variant.CIRCULAR) {
            bounds.inset(circularInsetWidth, circularInsetWidth)
        }

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val r = 0.5f * min(bounds.width(), bounds.height())

        drawShape(cx, cy, r, bgndPaint, canvas)
        drawShape(cx, cy, r - outlinePaint2.strokeWidth, outlinePaint2, canvas)

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
        val r2 = minBubbleSize + (r - minBubbleSize) * v * 0.96f
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
