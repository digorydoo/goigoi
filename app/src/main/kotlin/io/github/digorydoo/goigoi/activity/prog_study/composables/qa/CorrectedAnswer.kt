package io.github.digorydoo.goigoi.activity.prog_study.composables.qa

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalDensity
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.activity.prog_study.composables.AnimatedElement
import io.github.digorydoo.goigoi.core.study.Answer

@Composable
fun CorrectedAnswer(model: ProgStudyActivityModel) {
    val qaKind = model.qaKind.collectAsState().value
    if (qaKind.doesNotAskAnything) return

    val pmode = model.presentationMode.collectAsState().value
    val expectedAnswer = model.expectedAnswer.collectAsState().value
    val answerCorrectness = model.answerCorrectness.collectAsState().value

    val density = LocalDensity.current
    val fontSize = with(density) { ANSWER_FONT_SIZE.toSp() }

    val correctedAnswer = when (answerCorrectness) {
        Answer.CORRECT, Answer.NONE, Answer.TRIVIAL -> ""
        Answer.WRONG, Answer.CORRECT_BUT_INACCURATE, Answer.CORRECT_EXCEPT_KANA_SIZE -> when {
            pmode == PresentationMode.QUESTION -> ""
            else -> expectedAnswer
        }
    }

    AnimatedElement(
        visible = correctedAnswer.isNotEmpty() &&
            (pmode == PresentationMode.ANSWER_CHECK || pmode == PresentationMode.REVEAL_TEXTS),
        allowCollapse = pmode != PresentationMode.NOTHING || correctedAnswer.isEmpty(),
    ) {
        Text(text = correctedAnswer, fontSize = fontSize)
    }
}
