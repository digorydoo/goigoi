package io.github.digorydoo.goigoi.composables.icons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.cjk.dateToIntlStringLong
import ch.digorydoo.kutils.cjk.japaneseDayOfWeekAbbrev
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.composables.HintBalloon
import io.github.digorydoo.goigoi.composables.clickableNoRipple
import io.github.digorydoo.goigoi.legacy.spannable.FuriganaBuilder

@Composable
fun DayIconWithHintBalloon(day: Moment, progress: Float, animValue: Float, size: Dp = 32.dp) {
    val showBalloon = remember { mutableStateOf(false) }
    val intlDate = day.dateToIntlStringLong(true)

    HintBalloon(
        wrappedContent = {
            DayIcon(
                modifier = Modifier.clickableNoRipple { showBalloon.value = true },
                centreText = day.japaneseDayOfWeekAbbrev.toString(),
                progress = progress,
                animValue = animValue,
                size = size,
            )
        },
        lines = arrayOf(
            FuriganaBuilder.buildSpan(intlDate.ja),
            intlDate.en,
        ),
        open = showBalloon.value,
        onDismiss = { showBalloon.value = false },
    )
}
