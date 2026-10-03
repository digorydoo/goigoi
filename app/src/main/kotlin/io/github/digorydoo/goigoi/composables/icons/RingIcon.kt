package io.github.digorydoo.goigoi.composables.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.filter.envDelay
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import kotlin.math.max
import kotlin.math.min

private const val RING_REL_STROKEWIDTH = 0.125f

@Composable
fun RingIcon(
    progress: Float, // 0..1
    modifier: Modifier = Modifier,
    animValue: Float = 1f, // 0..1
    size: Dp = 32.dp,
    drawBackground: Boolean = false,
    bgColour: Color = GoigoiTheme.colours.ringBackground,
    trailColour: Color = GoigoiTheme.colours.ringTrail,
) {
    val trackColour = GoigoiTheme.colours.ringTrack

    Canvas(modifier = modifier.size(size)) {
        val sz = this.size
        val cx = sz.width / 2f
        val cy = sz.height / 2f

        val sizeFactor = envDelay(animValue, 0.0f, 0.8f)
        val insetPx = 1.dp.toPx()

        val width = sz.width * sizeFactor - 2 * insetPx
        val height = sz.height * sizeFactor - 2 * insetPx

        if (width <= 0f || height <= 0f) return@Canvas

        val r = 0.5f * min(width, height)
        val minStrokeWidthPx = 3.dp.toPx()
        val trailStrokeWidth = max(r * RING_REL_STROKEWIDTH, minStrokeWidthPx)
        val r2 = r - trailStrokeWidth / 2.0f

        if (r2 <= 0f) return@Canvas

        val trackStrokeWidth = trailStrokeWidth * 0.8f

        if (drawBackground) {
            drawCircle(
                color = bgColour,
                radius = r,
                center = Offset(cx, cy),
                style = Fill,
            )
        }

        if (progress < 1f) {
            drawCircle(
                color = trackColour,
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = trackStrokeWidth),
            )
        }

        if (progress > 0.00001f) {
            drawArc(
                color = trailColour,
                startAngle = 270f,
                sweepAngle = max(0.0f, progress * 360.0f) * animValue,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(2 * r, 2 * r),
                style = Stroke(width = trailStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}
