package io.github.digorydoo.goigoi.composables.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import kotlin.math.min

@Composable
fun ZzzIcon(
    modifier: Modifier = Modifier,
    animValue: Float = 1f, // 0..1
    size: Dp = 32.dp,
) {
    val bgColour = GoigoiTheme.colours.dimmedDecorativeIconBackground
    val fgColour = GoigoiTheme.colours.onDimmedDecorativeIconBackground

    Canvas(modifier = modifier.size(size)) {
        val insetPx = 1.dp.toPx()

        val sz = this.size
        val width = sz.width * animValue - 2 * insetPx
        val height = sz.height * animValue - 2 * insetPx

        if (width <= 0f || height <= 0f) return@Canvas

        val cx = sz.width / 2f
        val cy = sz.height / 2f
        val r = 0.5f * min(width, height)

        if (r <= 0f) return@Canvas

        val r1 = r * 0.6f
        val r2 = r1 * 0.6f
        val r3 = r2 * 0.6f
        val r4 = r3 * 0.6f

        val cx1 = cx + 0.1f * r
        val cy1 = cy - 0.38f * r

        val cx2 = cx - 0.3f * r
        val cy2 = cy + 0.20f * r

        val cx3 = cx + 0.0f * r
        val cy3 = cy + 0.60f * r

        val cx4 = cx - 0.1f * r
        val cy4 = cy + 0.86f * r

        drawCircle(color = bgColour, radius = r1, center = Offset(cx1, cy1))
        drawCircle(color = bgColour, radius = r2, center = Offset(cx2, cy2))
        drawCircle(color = bgColour, radius = r3, center = Offset(cx3, cy3))
        drawCircle(color = bgColour, radius = r4, center = Offset(cx4, cy4))

        drawZ(cx1, cy1, r1, fgColour)
        drawZ(cx2, cy2, r2, fgColour)
        drawZ(cx3, cy3, r3, fgColour)
    }
}

private fun DrawScope.drawZ(cx: Float, cy: Float, r: Float, colour: Color) {
    val path = Path().apply {
        moveTo(cx - 0.4f * r, cy - 0.4f * r)
        lineTo(cx + 0.4f * r, cy - 0.4f * r)
        lineTo(cx - 0.4f * r, cy + 0.4f * r)
        lineTo(cx + 0.4f * r, cy + 0.4f * r)
    }

    drawPath(
        path = path,
        color = colour,
        style = Stroke(
            width = 0.2f * r,
            cap = StrokeCap.Square,
            join = StrokeJoin.Miter,
        ),
    )
}
