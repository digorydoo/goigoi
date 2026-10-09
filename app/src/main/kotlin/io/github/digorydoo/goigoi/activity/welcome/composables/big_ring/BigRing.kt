package io.github.digorydoo.goigoi.activity.welcome.composables.big_ring

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import io.github.digorydoo.goigoi.composables.HintBalloon
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.core.welcome.DailyProgressTracker.Companion.CHECKMARK_THRESHOLD
import io.github.digorydoo.goigoi.legacy.spannable.FuriganaBuilder
import kotlin.math.roundToInt

@Composable
fun BigRing(todaysProgress: Float, animValue: Float, modifier: Modifier = Modifier) {
    val showBalloon = remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val colours = GoigoiTheme.colours

    val textStyle = GoigoiTheme.typography.listItemPrimaryText.copy(
        platformStyle = PlatformTextStyle(includeFontPadding = false),
        fontFamily = FontFamily(Font(R.font.epson_tai_xing_shu_tib_2))
    )

    val contentDescr = stringResource(R.string.todays_progress) + ": ${(todaysProgress * 100).roundToInt()}%"

    val painter = remember(colours, density) {
        BigRingPainter(
            trailColour = colours.bigRingTrail,
            trackColour = colours.bigRingTrack,
            goalAchievedBgColour = colours.primary,
            tipBgColour = colours.bigRingTip,
            tipFgColour = colours.bigRingTrail,
            tipStrokeWidth = with(density) { 2.dp.toPx() },
        )
    }

    val now = Moment.now()
    val intlDate = now.dateToIntlStringLong(true)

    BoxWithConstraints(
        modifier = modifier.semantics { contentDescription = contentDescr },
        contentAlignment = Alignment.Center,
    ) {
        val ringSize = maxWidth
        val clickAreaSize = ringSize * 0.64f
        val fontSize = with(density) { (clickAreaSize * 0.7f).toSp() }

        Canvas(
            modifier = Modifier.size(ringSize)
        ) {
            painter.draw(todaysProgress = todaysProgress, animValue = animValue, drawScope = this)
        }

        HintBalloon(
            wrappedContent = {
                IconButton(
                    modifier = Modifier.size(clickAreaSize),
                    onClick = { showBalloon.value = true }
                ) {
                    Text(
                        text = now.japaneseDayOfWeekAbbrev.toString(),
                        color = when (todaysProgress >= CHECKMARK_THRESHOLD) {
                            false -> colours.onBackground
                            true -> lerp(colours.onBackground, colours.onPrimary, envDelay(animValue, 0.9f))
                        },
                        fontSize = fontSize,
                        style = textStyle,
                    )
                }
            },
            lines = arrayOf(
                FuriganaBuilder.buildSpan(intlDate.ja),
                intlDate.en,
                stringResource(R.string.todays_progress_percent)
                    .replace("\${N}", minOf(100, (todaysProgress * 100f).roundToInt()).toString()),
            ),
            open = showBalloon.value,
            onDismiss = { showBalloon.value = false },
        )
    }
}
