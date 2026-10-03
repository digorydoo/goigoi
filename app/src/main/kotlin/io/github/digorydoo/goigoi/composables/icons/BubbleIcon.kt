package io.github.digorydoo.goigoi.composables.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import kotlin.math.min
import kotlin.math.pow

private const val BUBBLE_FG2_THRESHOLD = 0.75f

enum class BubbleIconVariant { CIRCULAR, DIAMOND }

// FIXME get rid of this, move functionality into RingIcon and DiamondIcon, respectively
@Composable
fun BubbleIcon(
    rating: Float, // 0..1
    variant: BubbleIconVariant,
    modifier: Modifier = Modifier,
    animValue: Float = 1f, // 0..1
    size: Dp = 32.dp,
) {
    val backgroundColour = GoigoiTheme.colours.bubbleBackground
    val outlineColour = GoigoiTheme.colours.bubbleOutline
    val poorRatingColour = GoigoiTheme.colours.poorRating
    val goodRatingColour = GoigoiTheme.colours.primary

    Canvas(modifier = modifier.size(size)) {
        val sz = this.size
        val cx = sz.width / 2f
        val cy = sz.height / 2f

        var width = sz.width * animValue
        var height = sz.height * animValue

        if (variant == BubbleIconVariant.CIRCULAR) {
            val circularInsetPx = 2.dp.toPx()
            width -= 2 * circularInsetPx
            height -= 2 * circularInsetPx
        }

        val r = 0.5f * min(width, height)
        val strokeWidth = 1.dp.toPx()

        drawShape(variant, cx, cy, r, backgroundColour, Fill)
        drawShape(variant, cx, cy, r - strokeWidth, outlineColour, Stroke(width = strokeWidth))

        val innerDotColour = when {
            rating < BUBBLE_FG2_THRESHOLD -> poorRatingColour
            else -> lerp(
                poorRatingColour,
                goodRatingColour,
                (rating - BUBBLE_FG2_THRESHOLD) / (1.0f - BUBBLE_FG2_THRESHOLD)
            )
        }

        // If we used value linearly, the bubble would become large too quickly.
        // If we used value quadratic, the bubble would stay small too long.
        // Let's use something in between:
        val v = rating.pow(1.42f)

        // We also multiply by 0.96f, to make it obvious that we haven't reached 100% quite yet,
        // since for 100% we would use a CheckmarkIcon instead of a BubbleChart.
        val minBubbleSizePx = 2.dp.toPx()
        val r2 = minBubbleSizePx + (r - minBubbleSizePx) * v * 0.96f

        drawShape(variant, cx, cy, r2, innerDotColour, Fill)
    }
}

private fun DrawScope.drawShape(
    variant: BubbleIconVariant,
    cx: Float,
    cy: Float,
    r: Float,
    color: Color,
    style: DrawStyle,
) {
    when (variant) {
        BubbleIconVariant.CIRCULAR -> {
            drawCircle(radius = r, center = Offset(cx, cy), color = color, style = style)
        }
        BubbleIconVariant.DIAMOND -> {
            val path = Path().apply {
                moveTo(cx, cy - r)
                lineTo(cx + r, cy)
                lineTo(cx, cy + r)
                lineTo(cx - r, cy)
                close()
            }
            drawPath(path = path, color = color, style = style)
        }
    }
}
