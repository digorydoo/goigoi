package io.github.digorydoo.goigoi.components.list

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private interface ListSubheaderStyles {
    val dividerTopMargin: Dp
    val extraTopMarginWhenNoDivider: Dp
    val textTopMargin: Dp
    val textBottomMargin: Dp
    val textStyle: TextStyle
    val colour: Color
}

@Composable
private fun getStyles(): ListSubheaderStyles {
    val typography = GoigoiTheme.typography
    val themeColours = GoigoiTheme.colours

    return remember(themeColours, typography) {
        object: ListSubheaderStyles {
            override val dividerTopMargin = 16.dp
            override val extraTopMarginWhenNoDivider = 8.dp
            override val textTopMargin = 8.dp
            override val textBottomMargin = 4.dp
            override val textStyle = typography.listItemSecondaryText
            override val colour = themeColours.onBackgroundSecondary
        }
    }
}

@Composable
fun ListSubheader(text: String, textPaddingLR: Dp, hasTopDivider: Boolean = true) {
    val styles = getStyles()

    if (hasTopDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(top = styles.dividerTopMargin)
        )
    }

    Text(
        modifier = Modifier.padding(
            start = textPaddingLR,
            top = styles.textTopMargin + (if (hasTopDivider) 0.dp else styles.extraTopMarginWhenNoDivider),
            end = textPaddingLR,
            bottom = styles.textBottomMargin,
        ),
        text = text,
        style = styles.textStyle,
        color = styles.colour
    )
}
