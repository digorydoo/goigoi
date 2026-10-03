package io.github.digorydoo.goigoi.activity.unyt.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.activity.unyt.UnytActivityModel
import io.github.digorydoo.goigoi.activity.unyt.UnytActivityModel.WordInfo
import io.github.digorydoo.goigoi.composables.Highlightable
import io.github.digorydoo.goigoi.composables.SheetHead
import io.github.digorydoo.goigoi.composables.list.WordListItem
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.ScreenSize

private interface UnytContentStyles {
    val contentHorizPadding: Dp
    val contentPaddingBottom: Dp
}

@Composable
private fun getStyles(): UnytContentStyles {
    val themeColours = GoigoiTheme.colours
    val density = LocalDensity.current
    val screenSize = DeviceProps.size
    val isPortrait = DeviceProps.isPortrait

    return remember(themeColours, density, screenSize, isPortrait) {
        object: UnytContentStyles {
            override val contentHorizPadding = when (screenSize) {
                ScreenSize.LARGE -> 32.dp
                ScreenSize.NORMAL -> if (isPortrait) 24.dp else 32.dp
                ScreenSize.SMALL -> 16.dp
            }
            override val contentPaddingBottom = 72.dp // large, because FAB overlaps content
        }
    }
}

@Composable
fun UnytContent(
    model: UnytActivityModel,
    onWordClicked: (WordInfo) -> Unit,
    onWordLongPressed: (WordInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val styles = getStyles()
    val list = model.list.collectAsState().value
    val listDisplayMode = model.listDisplayMode.collectAsState().value

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = styles.contentPaddingBottom),
    ) {
        item {
            SheetHead()
            UnytTopArea(model, styles.contentHorizPadding)
        }

        items(list) { item ->
            when (item) {
                is WordInfo -> {
                    key(item.word) {
                        val highlightedWord = model.highlightedWord.collectAsState().value

                        Highlightable(
                            highlightOnce = highlightedWord == item.word,
                            onAnimationCompleted = { model.setHighlightedWord(null) },
                        ) {
                            WordListItem(
                                item.data,
                                listDisplayMode,
                                showJLPTLevel = model.unytIsMyWords,
                                horizontalPadding = styles.contentHorizPadding,
                                onClick = { onWordClicked(item) },
                                onLongPress = { onWordLongPressed(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}
