package io.github.digorydoo.goigoi.activity.welcome.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.cjk.dateToIntlStringLong
import ch.digorydoo.kutils.cjk.japaneseDayOfWeekAbbrev
import ch.digorydoo.kutils.filter.envDelay
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.R.string
import io.github.digorydoo.goigoi.composables.HintBalloon
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.core.welcome.DailyProgressTracker.Companion.CHECKMARK_THRESHOLD
import io.github.digorydoo.goigoi.legacy.spannable.FuriganaBuilder
import io.github.digorydoo.goigoi.utils.ScreenSize
import kotlin.math.*

private const val REL_TRAIL_WIDTH = 0.162f

@Composable
fun BigRing(todaysProgress: Float, animValue: Float) {
    val showBalloon = remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val colours = GoigoiTheme.colours
    val screenSize = DeviceProps.size
    val isPortrait = DeviceProps.isPortrait

    val ringSize = when (screenSize) {
        ScreenSize.LARGE -> 256.dp
        ScreenSize.NORMAL -> if (isPortrait) 224.dp else 192.dp
        ScreenSize.SMALL -> 192.dp
    }

    val marginTop = when (screenSize) {
        ScreenSize.LARGE -> 32.dp
        ScreenSize.NORMAL -> if (isPortrait) 32.dp else 8.dp
        ScreenSize.SMALL -> 8.dp
    }

    val buttonSize = ringSize * 0.64f
    val fontSize = with(density) { (buttonSize * 0.7f).toSp() }

    val textStyle = GoigoiTheme.typography.listItemPrimaryText.copy(
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        fontFamily = FontFamily(Font(R.font.epson_tai_xing_shu_tib_2))
    )

    val contentDescr = stringResource(string.todays_progress) + ": ${(todaysProgress * 100).roundToInt()}%"

    val now = Moment.now()
    val intlDate = now.dateToIntlStringLong(true)

    Box(
        modifier = Modifier
            .semantics { contentDescription = contentDescr }
            .fillMaxWidth()
            .padding(top = marginTop, bottom = marginTop + 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.size(ringSize)
        ) {
            drawBigRing(
                todaysProgress = todaysProgress,
                animValue = animValue,
                trailColour = colours.bigRingTrail,
                trackColour = colours.ringTrack,
                checkmarkColour = colours.primary,
                tipBgColour = colours.bigRingTip,
                tipFgColour = colours.bigRingTrail,
                tipStrokeWidth = 2.dp.toPx(),
            )
        }
        HintBalloon(
            wrappedContent = {
                IconButton(
                    modifier = Modifier.size(buttonSize),
                    onClick = { showBalloon.value = true }
                ) {
                    Text(
                        text = now.japaneseDayOfWeekAbbrev.toString(),
                        color = colours.onBackground,
                        fontSize = fontSize,
                        style = textStyle,
                    )
                }
            },
            lines = arrayOf(
                FuriganaBuilder.buildSpan(intlDate.ja),
                intlDate.en,
            ),
            open = showBalloon.value,
            onDismiss = { showBalloon.value = false },
        )
    }
}

private fun DrawScope.drawBigRing(
    todaysProgress: Float,
    animValue: Float,
    trailColour: Color,
    trackColour: Color,
    checkmarkColour: Color,
    tipBgColour: Color,
    tipFgColour: Color,
    tipStrokeWidth: Float,
) {
    val insetSizePx = 1.dp.toPx()

    val sz = this.size
    val width = sz.width - 2 * insetSizePx
    val height = sz.height - 2 * insetSizePx

    if (width <= 0f || height <= 0f) return

    val cx = sz.width / 2f
    val cy = sz.height / 2f
    val r = 0.5f * min(width, height)

    if (r <= 0f) return

    val goalAchieved = todaysProgress >= CHECKMARK_THRESHOLD

    val phi =
        if (goalAchieved) 360f * animValue
        else max(0.0f, todaysProgress * 360.0f) * animValue

    var tipAnimValue = 0.0f

    if (goalAchieved) {
        tipAnimValue = envDelay(animValue, 0.96f)
    }

    drawTrackAndTrail(
        cx = cx,
        cy = cy,
        r = r,
        phi = phi,
        trackColour = trackColour,
        trailColour = trailColour,
        checkmarkColour = checkmarkColour,
        animValue = tipAnimValue,
    )

    if (tipAnimValue < 1.0f && phi > 0f) {
        drawTip(
            cx = cx,
            cy = cy,
            r = r,
            phi = phi,
            animValue = tipAnimValue,
            bgColour = tipBgColour,
            fgColour = tipFgColour,
            strokeWidth = tipStrokeWidth,
        )
    }
}

private fun DrawScope.drawTrackAndTrail(
    cx: Float,
    cy: Float,
    r: Float,
    phi: Float,
    trackColour: Color,
    trailColour: Color,
    checkmarkColour: Color,
    animValue: Float,
) {
    val ringRadius = r + r * REL_TRAIL_WIDTH / 2.0f
    val w = ringRadius * REL_TRAIL_WIDTH
    val r2 = ringRadius - w / 2.0f

    drawCircle(
        color = trackColour,
        radius = r2,
        center = Offset(cx, cy),
        style = Stroke(width = w * 0.8f),
    )

    if (phi > 0f) {
        val mixedTrailColour = lerp(trailColour, checkmarkColour, animValue)
        drawArc(
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
}

private fun DrawScope.drawTip(
    cx: Float,
    cy: Float,
    r: Float,
    phi: Float,
    animValue: Float,
    bgColour: Color,
    fgColour: Color,
    strokeWidth: Float,
) {
    val angleCorrection = 0.2f * REL_TRAIL_WIDTH
    val centreCorrection = 0.04f * REL_TRAIL_WIDTH * r

    val psi = angleCorrection + (phi - 90) * 2f * PI / 360f
    val tipX = (cx + (r - centreCorrection) * cos(psi)).toFloat()
    val tipY = (cy + (r - centreCorrection) * sin(psi)).toFloat()

    val chi = psi + PI / 2f
    val phase1 = PI / 2f
    val phase2 = 0.99f

    val size = 0.94f * REL_TRAIL_WIDTH * r * (1f - animValue)
    val s2 = size * 0.48f
    val s3 = size * 0.33f
    val s4 = size * 0.05f

    val tipPath = Path().apply {
        moveTo(
            (tipX + s2 * cos(chi - phase1)).toFloat(),
            (tipY + s2 * sin(chi - phase1)).toFloat(),
        )
        lineTo(
            (tipX + size * cos(chi)).toFloat(),
            (tipY + size * sin(chi)).toFloat(),
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
    val s5 = size * 0.6f

    val trianglePath = Path().apply {
        moveTo(
            (tipX + s5 * cos(chi - phase3)).toFloat(),
            (tipY + s5 * sin(chi - phase3)).toFloat(),
        )
        lineTo(
            (tipX + size * cos(chi)).toFloat(),
            (tipY + size * sin(chi)).toFloat(),
        )
        lineTo(
            (tipX + s5 * cos(chi + phase3)).toFloat(),
            (tipY + s5 * sin(chi + phase3)).toFloat(),
        )
        close()
    }

    drawPath(path = tipPath, color = bgColour, style = Fill)
    drawPath(path = trianglePath, color = fgColour, style = Fill)

    drawPath(
        path = tipPath,
        color = fgColour,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Butt,
            join = StrokeJoin.Miter,
        ),
    )
}
