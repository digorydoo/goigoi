package io.github.digorydoo.goigoi.activity.topic.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.topic.TopicActivityModel
import io.github.digorydoo.goigoi.activity.topic.components.ConfirmAction.ConfirmResetStats
import io.github.digorydoo.goigoi.activity.topic.components.ConfirmAction.ConfirmSetSuperProgIdx
import io.github.digorydoo.goigoi.components.SimpleAlertDialog
import io.github.digorydoo.goigoi.components.app_bar.GoigoiAppBar
import io.github.digorydoo.goigoi.components.menus.UnytCtxAction.FAKE_AVG_STATS
import io.github.digorydoo.goigoi.components.menus.UnytCtxAction.FAKE_GOOD_STATS
import io.github.digorydoo.goigoi.components.menus.UnytCtxAction.FAKE_POOR_STATS
import io.github.digorydoo.goigoi.components.menus.UnytCtxAction.RESET_STATS
import io.github.digorydoo.goigoi.components.menus.UnytCtxAction.SET_SUPER_PROG_IDX
import io.github.digorydoo.goigoi.components.menus.UnytCtxMenu
import io.github.digorydoo.goigoi.core.db.Topic
import io.github.digorydoo.goigoi.core.db.Unyt

sealed interface ConfirmAction {
    class ConfirmResetStats(val unyt: Unyt): ConfirmAction
    class ConfirmSetSuperProgIdx(val unyt: Unyt): ConfirmAction
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicScreen(topic: Topic, model: TopicActivityModel, onUnytClicked: (Unyt) -> Unit, onBack: () -> Unit) {
    val unytOfMenu = remember { mutableStateOf(null as Unyt?) }
    val confirmAction = remember { mutableStateOf(null as ConfirmAction?) }
    val confirmResetStatsTemplate = stringResource(R.string.confirm_reset_unyt_progress_msg)
    val confirmSetSuperProgIdxMsg = stringResource(R.string.confirm_set_super_prog_idx_to_unyt_msg)
    val okResetStatsLabel = stringResource(R.string.confirm_reset_unyt_progress_ok)
    val okSetSuperProgIdxLabel = stringResource(R.string.confirm_set_super_prog_idx_to_unyt_ok)

    Scaffold(
        topBar = {
            GoigoiAppBar(
                title = topic.name.withSystemLangExcept("ja"), // use en if systemLang is ja
                onBack = onBack,
            )
        }
    ) { innerPadding ->
        TopicContent(
            modifier = Modifier.padding(innerPadding),
            topic = topic,
            model = model,
            onUnytClicked = onUnytClicked,
            onUnytLongPressed = { unytOfMenu.value = it }
        )

        unytOfMenu.value?.let { unyt ->
            UnytCtxMenu(
                onAction = { action ->
                    when (action) {
                        FAKE_GOOD_STATS -> model.fakeGoodStats(unyt)
                        FAKE_AVG_STATS -> model.fakeAvgStats(unyt)
                        FAKE_POOR_STATS -> model.fakePoorStats(unyt)
                        RESET_STATS -> confirmAction.value = ConfirmResetStats(unyt)
                        SET_SUPER_PROG_IDX -> confirmAction.value = ConfirmSetSuperProgIdx(unyt)
                    }

                    // TODO actions should have a callback so that the menu can stay open as long as the action runs
                    unytOfMenu.value = null
                },
                onDismissRequest = { unytOfMenu.value = null }
            )
        }

        confirmAction.value?.let { action ->
            when (action) {
                is ConfirmResetStats -> SimpleAlertDialog(
                    message = confirmResetStatsTemplate.replace("\${N}", "" + action.unyt.numWordsAvailable),
                    confirmLabel = okResetStatsLabel,
                    onConfirm = {
                        model.resetStats(action.unyt)
                        confirmAction.value = null
                    },
                    onDismissRequest = { confirmAction.value = null },
                )
                is ConfirmSetSuperProgIdx -> SimpleAlertDialog(
                    message = confirmSetSuperProgIdxMsg,
                    confirmLabel = okSetSuperProgIdxLabel,
                    onConfirm = {
                        model.setSuperProgIdx(action.unyt)
                        confirmAction.value = null
                    },
                    onDismissRequest = { confirmAction.value = null },
                )
            }
        }
    }
}
