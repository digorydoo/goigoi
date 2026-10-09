package io.github.digorydoo.goigoi.activity.welcome.composables.big_ring

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import ch.digorydoo.kutils.filter.envDelay
import io.github.digorydoo.goigoi.core.welcome.DailyProgressTracker.Companion.CHECKMARK_THRESHOLD
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class BigRingPainter(
    private val trailColour: Color,
    private val trackColour: Color,
    private val goalAchievedBgColour: Color,
    private val tipBgColour: Color,
    private val tipFgColour: Color,
    private val tipStrokeWidth: Float,
) {
    fun draw(todaysProgress: Float, animValue: Float, drawScope: DrawScope) {
        val sz = drawScope.size
        val width = sz.width
        val height = sz.height
        if (width <= 0f || height <= 0f) return

        val r = 0.5f * min(width, height)
        if (r <= 0f) return

        val goalAchieved = todaysProgress >= CHECKMARK_THRESHOLD

        val phi =
            if (goalAchieved) 360f * animValue
            else max(0.0f, todaysProgress * 360.0f) * animValue

        val tipAnimValue =
            if (goalAchieved) envDelay(animValue, 0.96f)
            else 0f

        val cx = sz.width / 2f
        val cy = sz.height / 2f

        val w = r * REL_TRAIL_WIDTH
        val r2 = r - w / 2.0f

        if (goalAchieved) {
            val bgAnimValue = envDelay(animValue, 0.92f)

            drawScope.drawCircle(
                color = goalAchievedBgColour.copy(alpha = bgAnimValue),
                radius = r2,
                center = Offset(cx, cy),
                style = Fill,
            )
        }

        drawScope.drawCircle(
            color = trackColour,
            radius = r2,
            center = Offset(cx, cy),
            style = Stroke(width = w * 0.8f),
        )

        if (phi > 0f) {
            val mixedTrailColour = lerp(trailColour, goalAchievedBgColour, animValue)

            drawScope.drawArc(
                color = mixedTrailColour,
                startAngle = 270f,
                sweepAngle = phi,
                useCenter = false,
                topLeft = Offset(cx - r2, cy - r2),
                size = Size(2 * r2, 2 * r2),
                style = Stroke(
                    width = w,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }

        if (tipAnimValue < 1.0f && phi > 0f) {
            drawTip(
                cx = cx,
                cy = cy,
                r = r,
                phi = phi,
                animValue = tipAnimValue,
                scope = drawScope,
            )
        }
    }

    private fun drawTip(
        cx: Float,
        cy: Float,
        r: Float,
        phi: Float,
        animValue: Float,
        scope: DrawScope,
    ) {
        val angleCorrection = 0.1f * REL_TRAIL_WIDTH
        val centreCorrection = 0.5f * REL_TRAIL_WIDTH * r

        val psi = angleCorrection + (phi - 90) * 2f * PI / 360f
        val tipX = (cx + (r - centreCorrection) * cos(psi)).toFloat()
        val tipY = (cy + (r - centreCorrection) * sin(psi)).toFloat()

        val chi = psi + PI / 2f
        val phase1 = PI / 2f
        val phase2 = 0.99f

        val s1 = 0.91f * REL_TRAIL_WIDTH * r * (1f - animValue)
        val s2 = s1 * 0.48f
        val s3 = s1 * 0.33f
        val s4 = s1 * 0.05f

        val tipPath = Path().apply {
            moveTo(
                (tipX + s2 * cos(chi - phase1)).toFloat(),
                (tipY + s2 * sin(chi - phase1)).toFloat(),
            )
            lineTo(
                (tipX + s1 * cos(chi)).toFloat(),
                (tipY + s1 * sin(chi)).toFloat(),
            )
            lineTo(
                (tipX + s2 * cos(chi + phase1)).toFloat(),
                (tipY + s2 * sin(chi + phase1)).toFloat(),
            )
            lineTo(
                (tipX + s3 * cos(chi + PI - phase2)).toFloat(),
                (tipY + s3 * sin(chi + PI - phase2)).toFloat(),
            )
            lineTo(
                (tipX + s4 * cos(chi + PI)).toFloat(),
                (tipY + s4 * sin(chi + PI)).toFloat(),
            )
            lineTo(
                (tipX + s3 * cos(chi + PI + phase2)).toFloat(),
                (tipY + s3 * sin(chi + PI + phase2)).toFloat(),
            )
            close()
        }

        val phase3 = 0.3f
        val s5 = s1 * 0.6f

        val trianglePath = Path().apply {
            moveTo(
                (tipX + s5 * cos(chi - phase3)).toFloat(),
                (tipY + s5 * sin(chi - phase3)).toFloat(),
            )
            lineTo(
                (tipX + s1 * cos(chi)).toFloat(),
                (tipY + s1 * sin(chi)).toFloat(),
            )
            lineTo(
                (tipX + s5 * cos(chi + phase3)).toFloat(),
                (tipY + s5 * sin(chi + phase3)).toFloat(),
            )
            close()
        }

        scope.drawPath(path = tipPath, color = tipBgColour, style = Fill)
        scope.drawPath(path = trianglePath, color = tipFgColour, style = Fill)

        scope.drawPath(
            path = tipPath,
            color = tipFgColour,
            style = Stroke(
                width = tipStrokeWidth,
                cap = StrokeCap.Butt,
                join = StrokeJoin.Miter,
            ),
        )
    }

    companion object {
        private const val REL_TRAIL_WIDTH = 0.2f
    }
}
