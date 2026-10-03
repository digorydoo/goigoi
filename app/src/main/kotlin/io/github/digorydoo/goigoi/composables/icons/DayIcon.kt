package io.github.digorydoo.goigoi.composables.icons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.core.welcome.DailyProgressTracker.Companion.CHECKMARK_THRESHOLD

@Composable
fun DayIcon(
    progress: Float,
    animValue: Float,
    modifier: Modifier = Modifier,
    centreText: String = "",
    size: Dp = 32.dp,
) {
    val density = LocalDensity.current
    val colours = GoigoiTheme.colours
    val fontSize = with(density) { (size * 0.42f).toSp() }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (progress < CHECKMARK_THRESHOLD) {
            RingIcon(progress = progress, animValue = animValue, size = size, drawBackground = true)
        } else {
            RingIcon(
                progress = progress,
                animValue = animValue,
                size = size,
                drawBackground = true,
                bgColour = colours.primary,
                trailColour = colours.primary,
            )
        }

        if (centreText.isNotEmpty()) {
            Text(
                // Japanese glyphs seem to extend below the baseline, so we need to compensate.
                modifier = Modifier.padding(bottom = 2.dp),
                text = centreText,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color =
                    if (progress < CHECKMARK_THRESHOLD) colours.onBackground
                    else colours.onPrimary,
            )
        }
    }
}
