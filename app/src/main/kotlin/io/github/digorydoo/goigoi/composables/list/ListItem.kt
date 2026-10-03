package io.github.digorydoo.goigoi.composables.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme

private interface ListItemStyles {
    val minHeight: Dp
    val verticalPadding: Dp
    val secondaryTextTopMargin: Dp
}

@Composable
private fun getStyles(): ListItemStyles {
    val density = LocalDensity.current

    return remember(density) {
        object: ListItemStyles {
            override val minHeight = 56.dp
            override val verticalPadding = 8.dp
            override val secondaryTextTopMargin = 4.dp
        }
    }
}

@Composable
fun ListItem(
    primaryText: String,
    secondaryText: String = "",
    horizontalPadding: Dp = 0.dp, // ignored if both paddingStart and paddingEnd are specified
    paddingStart: Dp = horizontalPadding,
    paddingEnd: Dp = horizontalPadding,
    onClick: (() -> Unit)? = null, // not clickable when null
    enabled: Boolean = onClick != null, // useful when state changes dynamically, to allow ripple effect to finish
    onLongPress: (() -> Unit)? = null,
    startContent: @Composable (RowScope.() -> Unit)? = null,
    endContent: @Composable (RowScope.() -> Unit)? = null,
) {
    val styles = getStyles()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = styles.minHeight)
            .then(
                when {
                    onClick != null && onLongPress != null -> {
                        Modifier.combinedClickable(
                            onClick = { onClick.invoke() },
                            onLongClick = { onLongPress.invoke() },
                            enabled = enabled,
                        )
                    }
                    onClick != null -> Modifier.clickable(onClick = onClick, enabled = enabled)
                    else -> Modifier
                }
            )
            .padding(start = paddingStart, end = paddingEnd)
            .padding(vertical = styles.verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        startContent?.invoke(this)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = primaryText,
                style = GoigoiTheme.typography.listItemPrimaryText,
                color = GoigoiTheme.colours.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (secondaryText.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = styles.secondaryTextTopMargin),
                    text = secondaryText,
                    style = GoigoiTheme.typography.listItemSecondaryText,
                    color = GoigoiTheme.colours.secondaryOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        endContent?.invoke(this)
    }
}

@Composable
fun ListItem(
    iconResId: Int?,
    primaryText: String,
    secondaryText: String = "",
    horizontalPadding: Dp = 0.dp, // ignored if both paddingStart and paddingEnd are specified
    paddingStart: Dp = horizontalPadding,
    paddingEnd: Dp = horizontalPadding,
    iconMarginEnd: Dp = 24.dp,
    onClick: (() -> Unit)? = null, // not clickable when null
    enabled: Boolean = onClick != null, // useful when state changes dynamically, to allow ripple effect to finish
    onLongPress: (() -> Unit)? = null,
) {
    ListItem(
        primaryText = primaryText,
        secondaryText = secondaryText,
        horizontalPadding = horizontalPadding,
        paddingStart = paddingStart,
        paddingEnd = paddingEnd,
        onClick = onClick,
        enabled = enabled,
        onLongPress = onLongPress,
        startContent = {
            if (iconResId != null) {
                Icon(
                    modifier = Modifier.padding(end = iconMarginEnd),
                    imageVector = ImageVector.vectorResource(iconResId),
                    contentDescription = null,
                    tint = GoigoiTheme.colours.decorativeIconTint
                )
            }
        }
    )
}

@Composable
fun ListItem(
    iconFromChar: Char,
    primaryText: String,
    secondaryText: String = "",
    horizontalPadding: Dp = 0.dp, // ignored if both paddingStart and paddingEnd are specified
    paddingStart: Dp = horizontalPadding,
    paddingEnd: Dp = horizontalPadding,
    iconMarginEnd: Dp = 24.dp,
    onClick: (() -> Unit)? = null, // not clickable when null
    enabled: Boolean = onClick != null, // useful when state changes dynamically, to allow ripple effect to finish
    onLongPress: (() -> Unit)? = null,
) {
    val density = LocalDensity.current

    // We need to specify the textSize in sp, but we actually don't want the icon to scale with font scaling
    val iconSizeSp = with(density) { (24.dp.toPx() / fontScale).toSp() }

    ListItem(
        primaryText = primaryText,
        secondaryText = secondaryText,
        horizontalPadding = horizontalPadding,
        paddingStart = paddingStart,
        paddingEnd = paddingEnd,
        onClick = onClick,
        enabled = enabled,
        onLongPress = onLongPress,
        startContent = {
            Text(
                modifier = Modifier.padding(end = iconMarginEnd),
                text = iconFromChar.toString(),
                style = GoigoiTheme.typography.iconFromChar,
                fontSize = iconSizeSp,
            )
        }
    )
}
