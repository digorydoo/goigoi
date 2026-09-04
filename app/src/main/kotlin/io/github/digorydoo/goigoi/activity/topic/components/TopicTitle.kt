package io.github.digorydoo.goigoi.activity.topic.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.cjk.FuriganaString
import io.github.digorydoo.goigoi.core.db.Topic
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.withStudyLang

private interface TopicTitleStyles {
    val textStyle: TextStyle
    val textColour: Color
    val marginBottom: Dp
}

@Composable
private fun getStyles(): TopicTitleStyles {
    val colours = GoigoiTheme.colours
    val typography = GoigoiTheme.typography
    val density = LocalDensity.current

    return remember(colours, density) {
        object: TopicTitleStyles {
            override val textStyle = typography.bigContentTitle
            override val textColour = colours.onBackground
            override val marginBottom = 8.dp
        }
    }
}

@Composable
fun TopicTitle(topic: Topic, contentPaddingLR: Dp) {
    val styles = getStyles()

    Text(
        modifier = Modifier.padding(start = contentPaddingLR, end = contentPaddingLR, bottom = styles.marginBottom),
        text = FuriganaString(topic.name.withStudyLang).kanji,
        color = styles.textColour,
        style = styles.textStyle,
    )
}
