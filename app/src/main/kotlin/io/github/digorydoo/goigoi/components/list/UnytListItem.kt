package io.github.digorydoo.goigoi.components.list

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.cjk.Unicode
import io.github.digorydoo.goigoi.R.drawable
import io.github.digorydoo.goigoi.R.string
import io.github.digorydoo.goigoi.components.icons.BubbleIcon
import io.github.digorydoo.goigoi.components.icons.RingIcon
import io.github.digorydoo.goigoi.components.icons.ZzzIcon
import io.github.digorydoo.goigoi.drawable.BubbleIconDrawable
import io.github.digorydoo.goigoi.drawable.RingIconDrawable
import io.github.digorydoo.goigoi.utils.DiamondShape

private const val ANIM_DURATION_MILLIS = 300

private interface UnytListItemStyles {
    val iconSize: Dp
    val iconMarginEnd: Dp
}

@Composable
private fun getStyles(): UnytListItemStyles {
    val density = LocalDensity.current

    return remember(density) {
        object: UnytListItemStyles {
            override val iconSize = 48.dp
            override val iconMarginEnd = 20.dp
        }
    }
}

@Composable
fun UnytListItem(
    data: UnytListItemData?, // will render a skeleton if null
    paddingLR: Dp,
    onClick: (() -> Unit)? = null,
    onLongPress: (() -> Unit)? = null,
) {
    val styles = getStyles()
    val secondaryTextTemplate = stringResource(string.n_words)

    Crossfade(
        targetState = data == null,
        animationSpec = tween(durationMillis = ANIM_DURATION_MILLIS)
    ) { dataIsNull ->
        if (dataIsNull) {
            ListItemSkeleton(
                paddingLR,
                withLeftIcon = true,
                iconSize = styles.iconSize,
                iconMarginEnd = styles.iconMarginEnd,
                iconShape = DiamondShape,
                withSecondaryText = true
            )
        } else {
            require(data != null)
            var secondaryText = secondaryTextTemplate.replace("\${N}", "${data.numWords}")

            if (data.studyMomentAsText != null) {
                secondaryText += "${Unicode.EN_SPACE}${Unicode.TRIANG_RIGHT}${Unicode.EN_SPACE}${data.studyMomentAsText}"
            }

            if (data.isMyWordsUnyt) {
                ListItem(
                    iconResId = drawable.ic_bubbles_24dp,
                    primaryText = data.name,
                    secondaryText = secondaryText,
                    horizontalPadding = paddingLR,
                    onClick = onClick,
                    onLongPress = onLongPress,
                )
            } else {
                ListItem(
                    primaryText = data.name,
                    secondaryText = secondaryText,
                    horizontalPadding = paddingLR,
                    onClick = onClick,
                    onLongPress = onLongPress,
                    startContent = {
                        when {
                            data.asleep -> ZzzIcon(
                                modifier = Modifier.padding(end = styles.iconMarginEnd),
                                size = styles.iconSize
                            )
                            data.progress < 1f -> RingIcon(
                                data.progress,
                                RingIconDrawable.Variant.DIAMOND,
                                modifier = Modifier.padding(end = styles.iconMarginEnd),
                                size = styles.iconSize
                            )
                            else -> BubbleIcon(
                                data.rating,
                                BubbleIconDrawable.Variant.DIAMOND,
                                modifier = Modifier.padding(end = styles.iconMarginEnd),
                                size = styles.iconSize
                            )
                        }
                    }
                )
            }
        }
    }
}
