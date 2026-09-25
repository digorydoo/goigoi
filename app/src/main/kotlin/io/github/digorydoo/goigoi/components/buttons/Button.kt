package io.github.digorydoo.goigoi.components.buttons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.providers.GoigoiTheme

/**
 * A rounded, elevated button.
 */
@Composable
private fun Button(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    enabled: Boolean = true,
    onClick: (() -> Unit)?,
) {
    val shape = remember { RoundedCornerShape(percent = 50) }
    val colours = GoigoiTheme.colours

    Surface(
        modifier = modifier
            .sizeIn(minWidth = 96.dp, minHeight = 40.dp)
            .width(IntrinsicSize.Max) // never wrap
            .height(IntrinsicSize.Min)
            .then(
                if (!enabled) Modifier.alpha(0.5f)
                else Modifier
            ),
        shape = shape,
        color = if (primary) colours.primary else colours.surface,
        shadowElevation = 2.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (onClick != null && enabled) Modifier.clickable(onClick = onClick)
                    else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
fun Button(
    text: String,
    modifier: Modifier = Modifier,
    uppercase: Boolean = true,
    primary: Boolean = true,
    enabled: Boolean = true,
    fontWeight: FontWeight? = null,
    textScaling: Float = 1f,
    onClick: (() -> Unit)?,
) {
    val colours = GoigoiTheme.colours
    val typography = GoigoiTheme.typography

    val style = remember(typography, textScaling) {
        typography.labelLarge.let {
            if (textScaling == 1f) it
            else it.copy(fontSize = it.fontSize * textScaling)
        }
    }

    Button(
        content = {
            Text(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                text = if (uppercase) text.uppercase() else text,
                color = if (primary) colours.onPrimary else colours.onSurface,
                style = style,
                fontWeight = fontWeight,
                softWrap = false,
            )
        },
        modifier = modifier,
        primary = primary,
        enabled = enabled,
        onClick = onClick
    )
}
