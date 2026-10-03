package io.github.digorydoo.goigoi.activity.welcome.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.cjk.FuriganaString
import io.github.digorydoo.goigoi.R.drawable
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel.MyWordsUnytItem
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel.TopicItem
import io.github.digorydoo.goigoi.composables.Highlightable
import io.github.digorydoo.goigoi.composables.list.ListItem
import io.github.digorydoo.goigoi.composables.list.UnytListItem
import io.github.digorydoo.goigoi.core.db.Topic
import io.github.digorydoo.goigoi.core.db.Unyt.Companion.MIN_NUM_WORDS_FOR_STUDY
import io.github.digorydoo.goigoi.utils.withStudyLang

@Composable
fun TopicsAndMyWordsList(
    model: WelcomeActivityModel,
    horizPadding: Dp,
    topics: List<Topic>,
    onTopicClicked: (Topic) -> Unit,
    onMyWordsUnytClicked: () -> Unit,
) {
    val myWordsData = model.myWordsData.collectAsState().value
    val highlightedItem = model.highlightedItem.collectAsState().value
    val dividerMargin = 8.dp

    for (topic in topics) {
        Highlightable(
            highlightOnce = highlightedItem is TopicItem && highlightedItem.topic == topic,
            onAnimationCompleted = { model.setHighlightedItem(null) },
        ) {
            ListItem(
                iconResId = drawable.ic_local_library_black_24dp,
                primaryText = FuriganaString(topic.name.withStudyLang).kanji,
                secondaryText = topic.name.withSystemLangExcept("ja"), // use en if systemLang is ja
                horizontalPadding = horizPadding,
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
            UnytListItem(data = myWordsData, paddingLR = horizPadding, onClick = onMyWordsUnytClicked)
        }
    }
}
