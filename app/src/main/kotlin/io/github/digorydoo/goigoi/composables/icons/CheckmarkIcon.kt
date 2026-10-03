package io.github.digorydoo.goigoi.composables.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.filter.envDelay
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

private class Pt(val time: Float, val x: Float, val y: Float)

// This is duplicated in legacy Artist, but Paths are incompatible.
fun makeCheckmarkPath(cx: Float, cy: Float, r: Float, time: Float): Path {
    val pts = arrayOf(
        Pt(0.0f, cx - 0.65f * r, cy + 0.05f * r),
        Pt(0.7f, cx - 0.26f * r, cy + 0.48f * r),
        Pt(1.0f, cx + 0.67f * r, cy - 0.48f * r)
    )

    val path = Path()
    addAnimatedLine(pts, path, time)
    return path
}

private fun addAnimatedLine(pts: Array<Pt>, path: Path, time: Float) {
    var prevPt: Pt? = null

    for (pt in pts) {
        if (prevPt == null) {
            // This is the starting point.
            path.moveTo(pt.x, pt.y)
        } else if (time <= prevPt.time) {
            // This line segment is not yet drawn.
            break
        } else if (time >= pt.time) {
            // This line segment is fully drawn.
            path.lineTo(pt.x, pt.y)
        } else {
            // This line segment is partially drawn.
            val p = (time - prevPt.time) / (pt.time - prevPt.time) // 0..1
            val q = 1.0f - p
            path.lineTo(q * prevPt.x + p * pt.x, q * prevPt.y + p * pt.y)
        }

        prevPt = pt
    }
}

@Composable
fun CheckmarkIcon(
    modifier: Modifier = Modifier,
    animValue: Float = 1f, // 0..1
    size: Dp = 32.dp,
) {
    val bgColour = GoigoiTheme.colours.primary
    val markColour = GoigoiTheme.colours.onPrimary

    Canvas(modifier = modifier.size(size)) {
        val boundsFactor = envDelay(animValue, 0.0f, 0.8f)
        val insetPx = 1.dp.toPx()
        val markMinSizePx = 23.dp.toPx()

        val sz = this.size
        val width = sz.width * boundsFactor - 2 * insetPx
        val height = sz.height * boundsFactor - 2 * insetPx

        if (width <= 0f || height <= 0f) return@Canvas

        val cx = sz.width / 2f
        val cy = sz.height / 2f
        val r = 0.5f * min(width, height)

        if (r <= 0f) return@Canvas

        drawCircle(color = bgColour, radius = r, center = Offset(cx, cy))

        val markAnimValue = envDelay(animValue, 0.9f)
        if (markAnimValue <= 0f) return@Canvas

        val inset2 = floor(0.42f * r)
        val rect1Width = max(0f, sz.width - 2 * inset2)
        val markSize = max(rect1Width, markMinSizePx)
        val markR = 0.5f * markSize
        val path = makeCheckmarkPath(cx, cy, markR, markAnimValue)

        drawPath(
            path = path,
            color = markColour,
            style = Stroke(
                width = 0.2f * markR,
                cap = StrokeCap.Square,
                join = StrokeJoin.Miter,
            ),
        )
    }
}
