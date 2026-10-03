package io.github.digorydoo.goigoi.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme

private const val BADGE_TEXT_MAX_LENGTH = 15
private val BADGE_HORIZONTAL_PADDING = 8.dp
private val BADGE_VERTICAL_PADDING = 1.dp

@Composable
fun GoigoiBadge(text: String, modifier: Modifier = Modifier) {
    val colours = GoigoiTheme.colours
    val typography = GoigoiTheme.typography
    val density = LocalDensity.current
    val textSizeDp = with(density) { typography.badge.fontSize.toDp() }

    val truncatedText =
        if (text.length <= BADGE_TEXT_MAX_LENGTH - 1) text
        else text.substring(0, BADGE_TEXT_MAX_LENGTH - 1) + "…"

    Surface(
        modifier = modifier.widthIn(min = textSizeDp + BADGE_VERTICAL_PADDING * 2),
        shape = RoundedCornerShape(percent = 50),
        color = colours.primary,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = BADGE_HORIZONTAL_PADDING, vertical = BADGE_VERTICAL_PADDING),
            text = truncatedText,
            maxLines = 1,
            style = typography.badge,
            color = colours.onPrimary
        )
    }
}
