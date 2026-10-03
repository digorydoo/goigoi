package io.github.digorydoo.goigoi.activity.prog_study.composables.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme

@Composable
fun EnterButton(model: ProgStudyActivityModel) {
    val enterKeyDescr = stringResource(R.string.enter_key)
    val colours = GoigoiTheme.colours
    val currentAnswer = model.currentAnswer.collectAsState().value
    val prefill = model.answerPrefill.collectAsState().value
    val badAnswer = currentAnswer.isEmpty() || currentAnswer.length <= prefill.length

    KeyButton(
        modifier = Modifier.semantics { contentDescription = enterKeyDescr },
        iconResId = R.drawable.ic_enter_24dp,
        background =
            if (badAnswer) colours.surface
            else colours.primary,
        color =
            if (badAnswer) colours.onSurface
            else colours.onPrimary,
        onClick = model::onEnterBtnClicked,
    )
}
