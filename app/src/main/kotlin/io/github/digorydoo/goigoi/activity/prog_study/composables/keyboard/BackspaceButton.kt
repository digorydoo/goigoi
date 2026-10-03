package io.github.digorydoo.goigoi.activity.prog_study.composables.keyboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel

@Composable
fun BackspaceButton(model: ProgStudyActivityModel) {
    val backspaceKeyDescr = stringResource(R.string.backspace_key)
    KeyButton(
        modifier = Modifier.semantics { contentDescription = backspaceKeyDescr },
        iconResId = R.drawable.ic_backspace_24dp,
        onClick = model::onBackspaceBtnClicked,
    )
}
