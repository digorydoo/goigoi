package io.github.digorydoo.goigoi.activity.prog_study.composables.qa

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.activity.prog_study.composables.AnimatedElement
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme

@Composable
fun HintToReveal(model: ProgStudyActivityModel) {
    val text = model.hintToReveal.collectAsState().value
    if (text.isEmpty()) return

    val pmode = model.presentationMode.collectAsState().value
    val qaKind = model.qaKind.collectAsState().value

    AnimatedElement(
        visible = when (pmode) {
            // When answer is trivial, revealed texts are shown during ANSWER_CHECK, otherwise in an extra step.
            PresentationMode.ANSWER_CHECK -> qaKind.doesNotAskAnything
            PresentationMode.REVEAL_TEXTS -> true
            else -> false
        },
        allowCollapse = pmode != PresentationMode.NOTHING,
    ) {
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = text,
            style = GoigoiTheme.typography.hint,
            color = GoigoiTheme.colours.secondaryOnBackground,
        )
    }
}
