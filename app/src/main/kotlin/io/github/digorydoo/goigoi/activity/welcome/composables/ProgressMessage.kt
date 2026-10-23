package io.github.digorydoo.goigoi.activity.welcome.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel
import io.github.digorydoo.goigoi.composables.HintBalloon
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.composables.text.FuriganaText

@Composable
fun ProgressMessage(model: WelcomeActivityModel, horizPadding: Dp) {
    val showBalloon = remember { mutableStateOf(false) }

    val ja = model.progressMsgJa.collectAsState().value
    val translation = model.progressMsgTranslation.collectAsState().value

    HintBalloon(
        wrappedContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showBalloon.value = true }
                    .padding(top = 8.dp, bottom = 8.dp)
                    .padding(horizontal = horizPadding),
                contentAlignment = Alignment.TopStart,
            ) {
                FuriganaText(
                    text = ja,
                    textFontSize = GoigoiTheme.typography.listItemPrimaryText.fontSize,
                    color = GoigoiTheme.colours.onBackground,
                    furiganaFontSizeFactor = 0.6f
                )
            }
        },
        lines = arrayOf(translation),
        open = showBalloon.value,
        onDismiss = { showBalloon.value = false },
    )
}
