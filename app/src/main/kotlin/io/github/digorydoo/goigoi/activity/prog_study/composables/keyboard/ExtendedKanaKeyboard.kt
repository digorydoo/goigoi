package io.github.digorydoo.goigoi.activity.prog_study.composables.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.core.prog_study.KeyDef

enum class KanaMode { HIRAGANA, KATAKANA }

val KANA_KEYBOARD_BUTTON_WIDTH = 56.dp

@Composable
fun ExtendedKanaKeyboard(mode: KanaMode, model: ProgStudyActivityModel, modifier: Modifier = Modifier) {
    val isHiragana = mode == KanaMode.HIRAGANA
    val invisibleBtnWidth = KANA_KEYBOARD_BUTTON_WIDTH
    val transformsKeyDescr = stringResource(R.string.transforms_key)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Spacer(modifier = Modifier.width(invisibleBtnWidth))
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
            BackspaceButton(model)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
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
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
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
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Spacer(modifier = Modifier.width(invisibleBtnWidth))
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
