package io.github.digorydoo.goigoi.activity.welcome.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import ch.digorydoo.kutils.cjk.FuriganaString
import io.github.digorydoo.goigoi.R.drawable
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel.MyWordsUnytItem
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel.TopicItem
import io.github.digorydoo.goigoi.components.Highlightable
import io.github.digorydoo.goigoi.components.list.ListItem
import io.github.digorydoo.goigoi.components.list.UnytListItem
import io.github.digorydoo.goigoi.core.db.Topic
import io.github.digorydoo.goigoi.core.db.Unyt.Companion.MIN_NUM_WORDS_FOR_STUDY
import io.github.digorydoo.goigoi.utils.withStudyLang

@Composable
fun TopicsAndMyWordsList(
    model: WelcomeActivityModel,
    paddingLR: Dp,
    dividerMargin: Dp,
    topics: List<Topic>,
    onTopicClicked: (Topic) -> Unit,
    onMyWordsUnytClicked: () -> Unit,
) {
    val myWordsData = model.myWordsData.collectAsState().value
    val highlightedItem = model.highlightedItem.collectAsState().value

    HorizontalDivider(modifier = Modifier.padding(top = dividerMargin, bottom = dividerMargin))

    for (topic in topics) {
        Highlightable(
            highlightOnce = highlightedItem is TopicItem && highlightedItem.topic == topic,
            onAnimationCompleted = { model.setHighlightedItem(null) },
        ) {
            ListItem(
                iconResId = drawable.ic_local_library_black_24dp,
                primaryText = FuriganaString(topic.name.withStudyLang).kanji,
                secondaryText = topic.name.withSystemLangExcept("ja"), // use en if systemLang is ja
                horizontalPadding = paddingLR,
                onClick = { onTopicClicked(topic) }
            )
        }
    }

    if (myWordsData.numWords > MIN_NUM_WORDS_FOR_STUDY) {
        HorizontalDivider(modifier = Modifier.padding(top = dividerMargin, bottom = dividerMargin))

        Highlightable(
            highlightOnce = highlightedItem is MyWordsUnytItem,
            onAnimationCompleted = { model.setHighlightedItem(null) },
        ) {
            UnytListItem(data = myWordsData, paddingLR = paddingLR, onClick = onMyWordsUnytClicked)
        }
    }
}
