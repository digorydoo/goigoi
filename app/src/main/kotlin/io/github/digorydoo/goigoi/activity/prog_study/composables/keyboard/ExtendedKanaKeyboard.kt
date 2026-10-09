package io.github.digorydoo.goigoi.activity.prog_study.composables.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.core.prog_study.KeyActionHandler.Action
import io.github.digorydoo.goigoi.core.prog_study.KeyDef

enum class KanaMode { HIRAGANA, KATAKANA }

@Composable
fun ExtendedKanaKeyboard(mode: KanaMode, model: ProgStudyActivityModel, modifier: Modifier = Modifier) {
    val isHiragana = mode == KanaMode.HIRAGANA
    val transformsKeyDescr = stringResource(R.string.transforms_key)
    val backspaceKeyDescr = stringResource(R.string.backspace_key)
    val moveCaretLeftDescr = stringResource(R.string.move_caret_left)
    val moveCaretRightDescr = stringResource(R.string.move_caret_right)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            KeyButton(text = "", enabled = false, onClick = null)
            FiveWayButton(
                def = if (isHiragana) KeyDef.hiraganaVowelKey else KeyDef.katakanaVowelKey,
                onAction = model::onKeyBtnClicked
            )
            FiveWayButton(
                def = if (isHiragana) KeyDef.hiraganaKxKey else KeyDef.katakanaKxKey,
                onAction = model::onKeyBtnClicked
            )
            FiveWayButton(
                def = if (isHiragana) KeyDef.hiraganaSxKey else KeyDef.katakanaSxKey,
                onAction = model::onKeyBtnClicked
            )
            KeyButton(
                modifier = Modifier.semantics { contentDescription = backspaceKeyDescr },
                iconResId = R.drawable.ic_backspace_24dp,
                onClick = model::onBackspaceBtnClicked,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            KeyButton(
                modifier = Modifier.semantics { contentDescription = moveCaretLeftDescr },
                iconResId = R.drawable.ic_triangle_left_24dp,
                onClick = { model.onKeyBtnClicked("", Action.MOVE_CARET_LEFT) }
            )
            FiveWayButton(
                def = if (isHiragana) KeyDef.hiraganaTxKey else KeyDef.katakanaTxKey,
                onAction = model::onKeyBtnClicked,
            )
            FiveWayButton(
                def = if (isHiragana) KeyDef.hiraganaNxKey else KeyDef.katakanaNxKey,
                onAction = model::onKeyBtnClicked,
            )
            FiveWayButton(
                def = if (isHiragana) KeyDef.hiraganaHxKey else KeyDef.katakanaHxKey,
                onAction = model::onKeyBtnClicked,
            )
            KeyButton(
                modifier = Modifier.semantics { contentDescription = moveCaretRightDescr },
                iconResId = R.drawable.ic_triangle_right_24dp,
                onClick = { model.onKeyBtnClicked("", Action.MOVE_CARET_RIGHT) }
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            KeyButton(text = "", enabled = false, onClick = null)
            FiveWayButton(
                def = if (isHiragana) KeyDef.hiraganaMxKey else KeyDef.katakanaMxKey,
                onAction = model::onKeyBtnClicked,
            )
            FiveWayButton(
                def = if (isHiragana) KeyDef.hiraganaYxKey else KeyDef.katakanaYxKey,
                onAction = model::onKeyBtnClicked,
            )
            FiveWayButton(
                def = if (isHiragana) KeyDef.hiraganaRxKey else KeyDef.katakanaRxKey,
                onAction = model::onKeyBtnClicked,
            )
            KeyButton(text = "", enabled = false, onClick = null)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            KeyButton(text = "", enabled = false, onClick = null)
            FiveWayButton(
                modifier = Modifier.semantics { contentDescription = transformsKeyDescr },
                def = KeyDef.transformsKey,
                onAction = model::onKeyBtnClicked,
            )
            FiveWayButton(
                def = if (isHiragana) KeyDef.hiraganaWxKey else KeyDef.katakanaWxKey,
                onAction = model::onKeyBtnClicked,
            )
            FiveWayButton(
                def = KeyDef.punctuationsKey,
                onAction = model::onKeyBtnClicked,
            )
            EnterButton(model)
        }
    }
}
