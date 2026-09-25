package io.github.digorydoo.goigoi.activity.prog_study.components.qa

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.activity.prog_study.components.AnimatedElement
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer
import io.github.digorydoo.goigoi.providers.GoigoiTheme

@Composable
fun QuestionHint(model: ProgStudyActivityModel, enterDelayIdx: Int) {
    val hint = model.questionHint.collectAsState().value
    val mode = model.presentationMode.collectAsState().value

    val hintText = when (hint) {
        is OneOf.First -> hint.first
        is OneOf.Second -> when (hint.second) {
            QuestionAndAnswer.Hint.PHRASE -> stringResource(R.string.asking_phrase)
        }
    }

    if (hintText.isEmpty()) return // don't occupy space

    AnimatedElement(
        visible = mode != PresentationMode.NOTHING && mode != PresentationMode.BEFORE_QUESTION,
        enterDelayIdx = enterDelayIdx,
    ) {
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = hintText,
            style = GoigoiTheme.typography.hint,
            color = GoigoiTheme.colours.secondaryOnBackground,
        )
    }
}
