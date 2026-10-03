package io.github.digorydoo.goigoi.composables.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.filter.envDelay
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.vector.Vector2f
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import kotlin.math.max
import kotlin.math.min

private const val DIAMOND_REL_STROKEWIDTH = 0.125f

@Composable
fun DiamondIcon(
    progress: Float, // 0..1
    modifier: Modifier = Modifier,
    animValue: Float = 1f, // 0..1
    size: Dp = 32.dp,
    drawBackground: Boolean = false,
) {
    val trailColour = GoigoiTheme.colours.ringTrail
    val trackColour = GoigoiTheme.colours.ringTrack
    val bgColour = GoigoiTheme.colours.ringBackground

    Canvas(modifier = modifier.size(size)) {
        val sz = this.size
        val cx = sz.width / 2f
        val cy = sz.height / 2f

        val sizeFactor = envDelay(animValue, 0.0f, 0.8f)
        val width = sz.width * sizeFactor
        val height = sz.height * sizeFactor

        if (width <= 0f || height <= 0f) return@Canvas

        val r = 0.5f * min(width, height)
        val minStrokeWidthPx = 3.dp.toPx()
        val trailStrokeWidth = max(r * DIAMOND_REL_STROKEWIDTH, minStrokeWidthPx)
        val r2 = r - trailStrokeWidth / 2.0f

        if (r2 <= 0f) return@Canvas

        val trackStrokeWidth = trailStrokeWidth * 0.8f
        val shouldDrawTrail = progress > 0.00001f

        val corners = arrayOf(
            Vector2f(cx, cy - r),
            Vector2f(cx + r, cy),
            Vector2f(cx, cy + r),
            Vector2f(cx - r, cy),
        )

        val closedPath = makeClosedPath(corners)

        if (drawBackground) {
            drawPath(path = closedPath, color = bgColour, style = Fill)
        }

        drawPath(path = closedPath, color = trackColour, style = Stroke(width = trackStrokeWidth))

        if (shouldDrawTrail) {
            val openPath = makeOpenPath(corners, progress * animValue)

            drawPath(
                path = openPath,
                color = trailColour,
                style = Stroke(width = trailStrokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

private fun makeClosedPath(pts: Array<Vector2f>) = Path().apply {
    pts.forEachIndexed { i, pt ->
        if (i == 0) moveTo(pt.x, pt.y)
        else lineTo(pt.x, pt.y)
    }
    close()
}

private fun makeOpenPath(pts: Array<Vector2f>, rel: Float) = Path().apply {
    var prevPt = pts[0]
    moveTo(prevPt.x, prevPt.y)
    val stepSize = 1.0f / pts.size

    for (i in 1 .. pts.size) {
        val ri = i.toFloat() / pts.size
        val thisPt = pts.getOrNull(i) ?: pts[0]

        if (rel > ri) {
            lineTo(thisPt.x, thisPt.y)
        } else {
            val partial = 1.0f - clamp((ri - rel) / stepSize)
            val pr = lerp(prevPt, thisPt, partial)
            lineTo(pr.x, pr.y)
            break
        }

        prevPt = thisPt
    }
}
