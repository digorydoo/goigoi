package io.github.digorydoo.goigoi.components.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import io.github.digorydoo.goigoi.providers.GoigoiTheme

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
    horizontalPadding: Dp,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
    startContent: @Composable (() -> Unit)? = null,
    endContent: @Composable (() -> Unit)? = null,
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
                            onLongClick = { onLongPress.invoke() }
                        )
                    }
                    onClick != null -> Modifier.clickable(onClick = onClick)
                    else -> Modifier
                }
            )
            .padding(horizontal = horizontalPadding, vertical = styles.verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        startContent?.invoke()
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
                    color = GoigoiTheme.colours.onBackgroundSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        endContent?.invoke()
    }
}

@Composable
fun ListItem(
    iconResId: Int,
    primaryText: String,
    secondaryText: String = "",
    horizontalPadding: Dp,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
) {
    ListItem(
        primaryText = primaryText,
        secondaryText = secondaryText,
        horizontalPadding = horizontalPadding,
        onClick = onClick,
        onLongPress = onLongPress,
        startContent = {
            Icon(
                modifier = Modifier.padding(end = 24.dp),
                imageVector = ImageVector.vectorResource(iconResId),
                contentDescription = null,
                tint = GoigoiTheme.colours.decorativeIconTint
            )
        }
    )
}
