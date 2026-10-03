package io.github.digorydoo.goigoi.activity.unyt.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.unyt.UnytActivityModel
import io.github.digorydoo.goigoi.composables.icons.UnytIcon
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme

private interface UnytTopAreaStyles {
    val chartMarginTop: Dp
    val chartMarginBottom: Dp
    val chartSize: Dp
    val overflowBtnMarginTop: Dp
    val overflowBtnMarginEnd: Dp
    val dividerMarginTop: Dp
    val dividerMarginBottom: Dp
    val textMarginTop: Dp
}

@Composable
private fun getStyles(hasChart: Boolean): UnytTopAreaStyles {
    val density = LocalDensity.current
    val isPortrait = DeviceProps.isPortrait

    return remember(density, hasChart) {
        object: UnytTopAreaStyles {
            override val chartMarginTop = 16.dp
            override val chartMarginBottom = 8.dp
            override val chartSize = if (isPortrait) 128.dp else 72.dp
            override val overflowBtnMarginTop = if (isPortrait || !hasChart) 4.dp else 8.dp
            override val overflowBtnMarginEnd = if (isPortrait || !hasChart) 8.dp else 24.dp
            override val dividerMarginTop = if (isPortrait) 24.dp else 8.dp
            override val dividerMarginBottom = if (isPortrait) 0.dp else 16.dp
            override val textMarginTop = if (isPortrait || !hasChart) 16.dp else 0.dp
        }
    }
}

@Composable
fun UnytTopArea(model: UnytActivityModel, contentHorizPadding: Dp) {
    val hasChart = !model.unytIsMyWords
    val styles = getStyles(hasChart)
    val unytProgress = model.unytProgress.collectAsState().value
    val unytRating = model.unytRating.collectAsState().value
    val numWordsInUnyt = model.numWordsInUnyt.collectAsState().value
    val numWordsAsString = stringResource(R.string.n_words).replace("\${N}", "$numWordsInUnyt")
    val currentMode = model.listDisplayMode.collectAsState().value
    val availableModes = model.availableListDisplayModes
    val isPortrait = DeviceProps.isPortrait

    if (isPortrait || !hasChart) {
        if (hasChart) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = styles.chartMarginTop, bottom = styles.chartMarginBottom),
                contentAlignment = Alignment.Center,
            ) {
                UnytIcon(
                    size = styles.chartSize,
                    progress = unytProgress,
                    rating = unytRating,

                    // The unyt is asleep when its study date is too long in the past. Thus, when the unyt is asleep,
                    // all of its words must be asleep, too. But the contrary is not true: If the unyt is not asleep,
                    // there may still be some words that are. We do not base the unyt's asleep state on the asleep
                    // state of its words, because computing those would be too inefficient in TopicActivity. To avoid
                    // confusing the user, it's best not to show the asleep state at all in the chart here.
                    asleep = false,
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(top = styles.dividerMarginTop, bottom = styles.dividerMarginBottom)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                modifier = Modifier
                    .padding(start = contentHorizPadding)
                    .padding(top = styles.textMarginTop),
                text = numWordsAsString,
                color = GoigoiTheme.colours.secondaryOnBackground,
                style = GoigoiTheme.typography.listItemSecondaryText,
            )
            Spacer(modifier = Modifier.weight(1f))
            DisplayModeOverflowBtn(
                modifier = Modifier.padding(top = styles.overflowBtnMarginTop, end = styles.overflowBtnMarginEnd),
                availableModes = availableModes,
                currentMode = currentMode,
                onModeSelected = { model.setListDisplayMode(it) }
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // this ensures we can use fillMaxHeight from child Column
        ) {
            UnytIcon(
                modifier = Modifier.padding(
                    start = contentHorizPadding,
                    top = styles.chartMarginTop,
                    bottom = styles.chartMarginBottom
                ),
                progress = unytProgress,
                rating = unytRating,
                asleep = false, // see above
                size = styles.chartSize,
            )
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = styles.overflowBtnMarginTop),
                horizontalAlignment = Alignment.End
            ) {
                DisplayModeOverflowBtn(
                    modifier = Modifier.padding(end = maxOf(0.dp, contentHorizPadding - styles.overflowBtnMarginEnd)),
                    availableModes = availableModes,
                    currentMode = currentMode,
                    onModeSelected = { model.setListDisplayMode(it) }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier.padding(end = contentHorizPadding, bottom = styles.chartMarginBottom),
                    text = numWordsAsString,
                    color = GoigoiTheme.colours.secondaryOnBackground,
                    style = GoigoiTheme.typography.listItemSecondaryText,
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = styles.dividerMarginTop, bottom = styles.dividerMarginBottom)
        )
    }
}
