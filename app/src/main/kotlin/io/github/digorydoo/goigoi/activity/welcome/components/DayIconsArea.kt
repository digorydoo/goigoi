package io.github.digorydoo.goigoi.activity.welcome.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.components.icons.DayIcon
import kotlin.time.Duration.Companion.days

private const val DELAY_BETWEEN_ICONS_MILLIS = 80

private interface DayIconsAreaStyles {
    val marginBottom: Dp
}

@Composable
private fun getStyles(): DayIconsAreaStyles {
    val density = LocalDensity.current
    return remember(density) {
        object: DayIconsAreaStyles {
            override val marginBottom = 16.dp
        }
    }
}

@Composable
fun DayIconsArea(
    dailyProgress: Array<Float>, // index 0 is today, 1 is yesterday
    animateWithPreDelayMillis: Int?, // null = don't animate
) {
    val dims = getStyles()

    val anyPastProgress = dailyProgress.foldIndexed(false) { idx, result, progress ->
        result || (idx > 0 && progress > 0f)
    }

    if (!anyPastProgress) {
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = dims.marginBottom), horizontalArrangement = Arrangement.Center
    ) {
        var first = true
        val now = Moment.now()
        var preDelay = (animateWithPreDelayMillis ?: 0) + DELAY_BETWEEN_ICONS_MILLIS * dailyProgress.size

        for (idx in dailyProgress.size - 1 downTo 1) {
            val day = now - idx.days
            preDelay -= DELAY_BETWEEN_ICONS_MILLIS

            DayIcon(first, day, dailyProgress[idx], preDelay)

            first = false
        }
    }
}
