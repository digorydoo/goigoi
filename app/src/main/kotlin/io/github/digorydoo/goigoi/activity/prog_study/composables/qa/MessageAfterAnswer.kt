package io.github.digorydoo.goigoi.activity.prog_study.composables.qa

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.activity.prog_study.composables.AnimatedElement
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.core.study.Answer

@Composable
fun MessageAfterAnswer(model: ProgStudyActivityModel) {
    val pmode = model.presentationMode.collectAsState().value
    val colours = GoigoiTheme.colours
    val typography = GoigoiTheme.typography

    val qaKind = model.qaKind.collectAsState().value
    if (qaKind.doesNotAskAnything) return

    val correctness = model.answerCorrectness.collectAsState().value
    if (correctness != Answer.CORRECT_EXCEPT_KANA_SIZE) return

    val text = stringResource(R.string.answer_correct_except_kana_size)

    AnimatedElement(
        visible = pmode == PresentationMode.ANSWER_CHECK,
        allowCollapse = pmode != PresentationMode.NOTHING,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                imageVector = ImageVector.vectorResource(R.drawable.ic_point_right_black_24dp),
                contentDescription = null,
                tint = GoigoiTheme.colours.decorativeIconTint
            )
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp),
                text = text,
                color = colours.onBackground,
                fontSize = typography.listItemPrimaryText.fontSize,
                fontStyle = FontStyle.Italic,
            )
        }
    }
}
