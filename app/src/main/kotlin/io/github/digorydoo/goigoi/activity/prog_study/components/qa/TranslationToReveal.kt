package io.github.digorydoo.goigoi.activity.prog_study.components.qa

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.activity.prog_study.components.AnimatedElement
import io.github.digorydoo.goigoi.core.study.Answer
import io.github.digorydoo.goigoi.providers.GoigoiTheme

@Composable
fun TranslationToReveal(model: ProgStudyActivityModel) {
    val text = model.translationToReveal.collectAsState().value
    if (text.isEmpty()) return

    val mode = model.presentationMode.collectAsState().value
    val answerCorrectness = model.answerCorrectness.collectAsState().value

    AnimatedElement(visible = mode == PresentationMode.ANSWER_CHECK && answerCorrectness != Answer.WRONG) {
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = text,
            style = GoigoiTheme.typography.hint,
            color = GoigoiTheme.colours.onBackground,
        )
    }
}
