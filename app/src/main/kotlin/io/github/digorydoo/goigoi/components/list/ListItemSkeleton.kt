package io.github.digorydoo.goigoi.components.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.components.SkeletonSurface
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private interface ListItemSkeletonStyles {
    val minHeight: Dp
    val verticalPadding: Dp
    val textMaxWidth: Dp
    val primaryTextHeight: Dp
    val secondaryTextHeight: Dp
    val secondaryTextTopMargin: Dp
}

@Composable
private fun getStyles(): ListItemSkeletonStyles {
    val density = LocalDensity.current
    val typography = GoigoiTheme.typography

    return remember(density) {
        object: ListItemSkeletonStyles {
            override val minHeight = 56.dp
            override val verticalPadding = 8.dp
            override val textMaxWidth = 192.dp
            override val primaryTextHeight = with(density) { typography.listItemPrimaryText.fontSize.toDp() }
            override val secondaryTextHeight = with(density) { typography.listItemSecondaryText.fontSize.toDp() }
            override val secondaryTextTopMargin = 4.dp
        }
    }
}

@Composable
fun ListItemSkeleton(
    paddingLR: Dp,
    withLeftIcon: Boolean = false,
    iconSize: Dp = 24.dp,
    iconMarginEnd: Dp = 24.dp,
    iconShape: Shape = CircleShape,
    withSecondaryText: Boolean = true,
) {
    val styles = getStyles()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = styles.minHeight)
            .padding(horizontal = paddingLR, vertical = styles.verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (withLeftIcon) {
            SkeletonSurface(
                modifier = Modifier
                    .padding(end = iconMarginEnd) // outside size
                    .size(iconSize),
                shape = iconShape,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            SkeletonSurface(
                modifier = Modifier
                    .widthIn(max = styles.textMaxWidth)
                    .fillMaxWidth()
                    .height(styles.primaryTextHeight),
                shape = RectangleShape,
            )

            if (withSecondaryText) {
                SkeletonSurface(
                    modifier = Modifier
                        .widthIn(max = styles.textMaxWidth)
                        .fillMaxWidth()
                        .padding(top = styles.secondaryTextTopMargin) // outside height, outside contentColor
                        .height(styles.secondaryTextHeight),
                    shape = RectangleShape,
                )
            }
        }
    }
}
