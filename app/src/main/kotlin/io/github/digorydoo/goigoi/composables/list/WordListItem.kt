package io.github.digorydoo.goigoi.composables.list

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.composables.icons.BubbleIcon
import io.github.digorydoo.goigoi.composables.icons.BubbleIconVariant
import io.github.digorydoo.goigoi.composables.icons.RingIcon
import io.github.digorydoo.goigoi.composables.icons.ZzzIcon
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.UserPrefs.WordListItemMode

private const val ANIM_DURATION_MILLIS = 300

private interface WordListItemStyles {
    val iconSize: Dp
    val iconMarginEnd: Dp
}

@Composable
private fun getStyles(): WordListItemStyles {
    val density = LocalDensity.current

    return remember(density) {
        object: WordListItemStyles {
            override val iconSize = 48.dp
            override val iconMarginEnd = 20.dp
        }
    }
}

@Composable
fun WordListItem(
    data: WordListItemData?, // will render a skeleton if null
    mode: WordListItemMode,
    showJLPTLevel: Boolean,
    horizontalPadding: Dp,
    onClick: () -> Unit,
    onLongPress: (() -> Unit)? = null,
) {
    val styles = getStyles()

    // We can't use the targetState parameter inside the lambda, because for some odd reason the lambda may be called
    // with data == null when targetState would indicate it is not null, causing a crash.
    @SuppressLint("UnusedCrossfadeTargetStateParameter")
    Crossfade(
        targetState = data == null,
        animationSpec = tween(durationMillis = ANIM_DURATION_MILLIS)
    ) { _ ->
        if (data == null) {
            ListItemSkeleton(
                horizontalPadding,
                withLeftIcon = true,
                iconSize = styles.iconSize,
                iconMarginEnd = styles.iconMarginEnd,
                iconShape = CircleShape,
                withSecondaryText = true
            )
        } else {
            val primaryText: String
            val secondaryText: String

            when (mode) {
                WordListItemMode.SHOW_KANA -> {
                    if (data.usuallyInKana) {
                        primaryText = data.kana
                        secondaryText = data.kanji
                    } else {
                        primaryText = data.kanji
                        secondaryText = data.kana
                    }
                }
                WordListItemMode.SHOW_ROMAJI -> {
                    primaryText = if (data.usuallyInKana) data.kana else data.kanji
                    secondaryText = data.romaji
                }
                WordListItemMode.SHOW_TRANSLATION -> {
                    primaryText = if (data.usuallyInKana) data.kana else data.kanji
                    secondaryText = data.translation
                }
            }

            ListItem(
                primaryText = primaryText,
                secondaryText = secondaryText,
                horizontalPadding = horizontalPadding,
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
                            modifier = Modifier.padding(end = styles.iconMarginEnd),
                            size = styles.iconSize
                        )
                        else -> BubbleIcon(
                            data.rating,
                            BubbleIconVariant.CIRCULAR,
                            modifier = Modifier.padding(end = styles.iconMarginEnd),
                            size = styles.iconSize
                        )
                    }
                },
                endContent = {
                    if (showJLPTLevel) {
                        // The badge collides visually with UnytActivity's FAB
                        // GoigoiBadge(text = data.level.toPrettyString())

                        // Display it with a Text instead
                        Text(
                            modifier = Modifier
                                .align(Alignment.Top)
                                .padding(end = 4.dp), // align with UnytActivity's overflow btn
                            text = data.level.toPrettyString(),
                            style = GoigoiTheme.typography.listItemSecondaryText,
                            color = GoigoiTheme.colours.secondaryOnBackground,
                        )
                    }
                }
            )
        }
    }
}
