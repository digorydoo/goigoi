package io.github.digorydoo.goigoi.activity.prog_study.composables.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.cjk.isKana
import ch.digorydoo.kutils.cjk.isSmallKana
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.utils.ScreenSize

private interface FixedKeysStyles {
    val keySpacing: Dp
    val horizActionRowMarginTop: Dp
}

@Composable
private fun getStyles(): FixedKeysStyles {
    val density = LocalDensity.current
    val screenSize = DeviceProps.size

    return remember(density) {
        object: FixedKeysStyles {
            override val keySpacing = when (screenSize) {
                ScreenSize.LARGE -> 8.dp
                ScreenSize.NORMAL -> 4.dp
                ScreenSize.SMALL -> 2.dp
            }
            override val horizActionRowMarginTop = 4.dp
        }
    }
}

@Composable
fun FixedKeysKeyboard(model: ProgStudyActivityModel, modifier: Modifier = Modifier) {
    val styles = getStyles()
    val fixedKeys = model.fixedKeys.collectAsState().value
    val backspaceKeyDescr = stringResource(R.string.backspace_key)

    val size = remember(fixedKeys) {
        when {
            fixedKeys.size > 4 -> KeyButtonSize.NORMAL
            fixedKeys.all { it.isKana() } -> KeyButtonSize.NORMAL
            else -> {
                val maxLen = if (fixedKeys.isEmpty()) 0 else fixedKeys.maxOf { it.length }
                when (maxLen) {
                    1 -> KeyButtonSize.XXLARGE
                    2 -> KeyButtonSize.XLARGE
                    3 -> KeyButtonSize.LARGE
                    else -> KeyButtonSize.NORMAL
                }
            }
        }
    }

    Column(modifier = modifier) {
        FlowRow(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(styles.keySpacing),
            verticalArrangement = Arrangement.spacedBy(styles.keySpacing),
        ) {
            for (keyText in fixedKeys) {
                KeyButton(
                    text = keyText,
                    size = size,
                    smallText = keyText.length == 1 && keyText[0].isSmallKana(),
                    onClick = { model.onKeyBtnClicked(keyText) }
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = styles.horizActionRowMarginTop),
            horizontalArrangement = Arrangement.spacedBy(styles.keySpacing),
        ) {
            KeyButton(
                modifier = Modifier.semantics { contentDescription = backspaceKeyDescr },
                iconResId = R.drawable.ic_backspace_24dp,
                onClick = model::onBackspaceBtnClicked,
            )
            EnterButton(model)
        }
    }
}
