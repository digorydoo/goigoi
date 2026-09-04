package io.github.digorydoo.goigoi.components.icons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.cjk.dateToIntlStringLong
import ch.digorydoo.kutils.cjk.japaneseDayOfWeekAbbrev
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.components.HintBalloon
import io.github.digorydoo.goigoi.drawable.RingIconDrawable.Variant
import io.github.digorydoo.goigoi.furigana.FuriganaBuilder
import io.github.digorydoo.goigoi.providers.DeviceProps
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ScreenSize
import io.github.digorydoo.goigoi.utils.clickableNoRipple

private const val ANIM_DURATION = 200

private interface DayIconStyles {
    val spacing: Dp
    val innerMargin: Dp
}

@Composable
private fun getStyles(): DayIconStyles {
    val density = LocalDensity.current
    val screenSize = DeviceProps.size
    val orientation = DeviceProps.orientation

    return remember(density) {
        object: DayIconStyles {
            override val spacing = when (screenSize) {
                ScreenSize.LARGE -> 24.dp
                ScreenSize.NORMAL -> when (orientation) {
                    Orientation.PORTRAIT -> 16.dp
                    else -> 24.dp
                }
                ScreenSize.SMALL -> 16.dp
            }

            override val innerMargin = 8.dp
        }
    }
}

@Composable
fun DayIcon(
    first: Boolean,
    day: Moment,
    progress: Float,
    animateWithPreDelayMillis: Int?,
) {
    val showBalloon = remember { mutableStateOf(false) }
    val dims = getStyles()
    val intlDate = day.dateToIntlStringLong(true)

    HintBalloon(
        wrappedContent = {
            Column(
                modifier = Modifier
                    .padding(
                        // Apply spacing to start and end in order to get the tip of the HintBalloon in the centre
                        start = if (first) 0.dp else dims.spacing / 2,
                        end = if (first) 0.dp else dims.spacing / 2,
                    )
                    .clickableNoRipple { showBalloon.value = true },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (animateWithPreDelayMillis != null) {
                    AnimatedDayIcon(
                        progress,
                        delayMillis = animateWithPreDelayMillis,
                        durationMillis = ANIM_DURATION,
                    )
                } else {
                    RingIcon(progress, Variant.CIRCULAR)
                }

                Text(
                    modifier = Modifier.padding(top = dims.innerMargin),
                    text = day.japaneseDayOfWeekAbbrev.toString(),
                    style = GoigoiTheme.typography.listItemSecondaryText
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
