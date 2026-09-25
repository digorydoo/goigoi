package io.github.digorydoo.goigoi.activity.prog_study.components.qa

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalDensity
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.activity.prog_study.components.AnimatedElement
import io.github.digorydoo.goigoi.core.study.Answer

@Composable
fun CorrectedAnswer(model: ProgStudyActivityModel) {
    val mode = model.presentationMode.collectAsState().value
    val acceptableAnswers = model.acceptableAnswers.collectAsState().value
    val answerCorrectness = model.answerCorrectness.collectAsState().value

    val density = LocalDensity.current
    val fontSize = with(density) { ANSWER_FONT_SIZE.toSp() }

    // Ideally, we would show the acceptable answer that is closest to the given answer.
    // For now, just show the first acceptable answer.
    val correctedAnswer = when (answerCorrectness) {
        Answer.CORRECT, Answer.SKIP, Answer.NONE, Answer.TRIVIAL -> ""
        Answer.WRONG, Answer.CORRECT_EXCEPT_KANA_SIZE -> when {
            mode == PresentationMode.QUESTION -> ""
            else -> acceptableAnswers.firstOrNull() ?: ""
        }
    }

    AnimatedElement(visible = mode == PresentationMode.ANSWER_CHECK) {
        Text(text = correctedAnswer, fontSize = fontSize)
    }
}
