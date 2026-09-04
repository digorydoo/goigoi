package io.github.digorydoo.goigoi.activity.topic.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.activity.topic.TopicActivityModel
import io.github.digorydoo.goigoi.activity.topic.TopicActivityModel.Subheader
import io.github.digorydoo.goigoi.activity.topic.TopicActivityModel.UnytInfo
import io.github.digorydoo.goigoi.components.Highlightable
import io.github.digorydoo.goigoi.components.SheetHead
import io.github.digorydoo.goigoi.components.list.ListSubheader
import io.github.digorydoo.goigoi.components.list.UnytListItem
import io.github.digorydoo.goigoi.core.db.Topic
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.providers.DeviceProps
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ScreenSize

private interface TopicContentStyles {
    val contentPaddingLR: Dp
    val contentPaddingBottom: Dp
}

@Composable
private fun getStyles(): TopicContentStyles {
    val themeColours = GoigoiTheme.colours
    val density = LocalDensity.current
    val screenSize = DeviceProps.size
    val orientation = DeviceProps.orientation

    return remember(themeColours, density, screenSize, orientation) {
        object: TopicContentStyles {
            override val contentPaddingLR = when (screenSize) {
                ScreenSize.LARGE -> 32.dp
                ScreenSize.NORMAL -> when (orientation) {
                    Orientation.PORTRAIT -> 24.dp
                    else -> 32.dp
                }
                ScreenSize.SMALL -> 16.dp
            }

            override val contentPaddingBottom = when (screenSize) {
                ScreenSize.LARGE -> 24.dp
                ScreenSize.NORMAL -> when (orientation) {
                    Orientation.PORTRAIT -> 16.dp
                    else -> 8.dp
                }
                ScreenSize.SMALL -> 8.dp
            }
        }
    }
}

@Composable
fun TopicContent(
    topic: Topic,
    model: TopicActivityModel,
    onUnytClicked: (Unyt) -> Unit,
    onUnytLongPressed: (Unyt) -> Unit,
    modifier: Modifier = Modifier,
) {
    val styles = getStyles()
    val list = model.list.collectAsState().value

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = styles.contentPaddingBottom),
    ) {
        item {
            SheetHead()
            TopicTitle(topic, styles.contentPaddingLR)
        }

        itemsIndexed(list) { idx, item ->
            when (item) {
                is Subheader -> ListSubheader(
                    text = item.text,
                    textPaddingLR = styles.contentPaddingLR,
                    hasTopDivider = idx > 0
                )
                is UnytInfo -> {
                    // Make sure composition identifies elements by the unyt (which is unique in the list)
                    key(item.unyt) {
                        val highlightedUnyt = model.highlightedUnyt.collectAsState().value

                        Highlightable(
                            highlightOnce = highlightedUnyt == item.unyt,
                            onAnimationCompleted = { model.setHighlightedUnyt(null) },
                        ) {
                            UnytListItem(
                                item.data,
                                styles.contentPaddingLR,
                                onClick = { onUnytClicked(item.unyt) },
                                onLongPress = { onUnytLongPressed(item.unyt) }
                            )
                        }
                    }
                }
            }
        }
    }
}
