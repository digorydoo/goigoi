package io.github.digorydoo.goigoi.composables.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.composables.SimpleAlertDialog
import io.github.digorydoo.goigoi.composables.menus.UnytCtxAction.FAKE_AVG_STATS
import io.github.digorydoo.goigoi.composables.menus.UnytCtxAction.FAKE_GOOD_STATS
import io.github.digorydoo.goigoi.composables.menus.UnytCtxAction.FAKE_POOR_STATS
import io.github.digorydoo.goigoi.composables.menus.UnytCtxAction.RESET_STATS
import io.github.digorydoo.goigoi.composables.menus.UnytCtxAction.SET_SUPER_PROG_IDX
import io.github.digorydoo.goigoi.core.db.Unyt
import kotlinx.coroutines.launch

private sealed interface ConfirmUnytAction
private class ConfirmUnytResetStats(val unyt: Unyt): ConfirmUnytAction
private class ConfirmUnytSetSuperProgIdx(val unyt: Unyt): ConfirmUnytAction

enum class UnytCtxAction {
    FAKE_GOOD_STATS,
    FAKE_AVG_STATS,
    FAKE_POOR_STATS,
    RESET_STATS,
    SET_SUPER_PROG_IDX,
}

interface UnytCtxMenuModel {
    suspend fun fakeGoodStats(unyt: Unyt)
    suspend fun fakeAvgStats(unyt: Unyt)
    suspend fun fakePoorStats(unyt: Unyt)
    suspend fun resetStats(unyt: Unyt)
    fun setSuperProgIdx(unyt: Unyt)
}

@Composable
fun UnytCtxMenu(
    model: UnytCtxMenuModel,
    unyt: Unyt,
    onDismissRequest: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val confirmActionState = remember { mutableStateOf(null as ConfirmUnytAction?) }
    val confirmAction = confirmActionState.value

    val confirmResetStatsTemplate = stringResource(R.string.confirm_reset_unyt_progress_msg)
    val confirmSetSuperProgIdxMsg = stringResource(R.string.confirm_set_super_prog_idx_to_unyt_msg)
    val okResetStatsLabel = stringResource(R.string.confirm_reset_unyt_progress_ok)
    val okSetSuperProgIdxLabel = stringResource(R.string.confirm_set_super_prog_idx_ok)

    val items = buildMenuDefs {
        item(R.drawable.ic_reset_white_24dp, R.string.item_reset_unyt_progress, RESET_STATS)
        debugItem("Fake good stats", FAKE_GOOD_STATS)
        debugItem("Fake average stats", FAKE_AVG_STATS)
        debugItem("Fake poor stats", FAKE_POOR_STATS)
        debugItem("Set super prog idx", SET_SUPER_PROG_IDX)
    }

    if (confirmAction == null) {
        MenuDialog(
            items = items,
            onAction = { action ->
                when (action) {
                    FAKE_GOOD_STATS -> scope.launch {
                        model.fakeGoodStats(unyt) // suspends until finished
                        onDismissRequest()
                    }
                    FAKE_AVG_STATS -> scope.launch {
                        model.fakeAvgStats(unyt)
                        onDismissRequest()
                    }
                    FAKE_POOR_STATS -> scope.launch {
                        model.fakePoorStats(unyt)
                        onDismissRequest()
                    }
                    RESET_STATS -> confirmActionState.value = ConfirmUnytResetStats(unyt)
                    SET_SUPER_PROG_IDX -> confirmActionState.value = ConfirmUnytSetSuperProgIdx(unyt)
                }
            },
            onDismissRequest = onDismissRequest
        )
    } else {
        when (confirmAction) {
            is ConfirmUnytResetStats -> SimpleAlertDialog(
                message = confirmResetStatsTemplate.replace("\${N}", "" + confirmAction.unyt.numWordsAvailable),
                confirmLabel = okResetStatsLabel,
                onConfirm = {
                    scope.launch {
                        model.resetStats(confirmAction.unyt)
                        onDismissRequest()
                    }
                },
                onDismissRequest = onDismissRequest,
            )
            is ConfirmUnytSetSuperProgIdx -> SimpleAlertDialog(
                message = confirmSetSuperProgIdxMsg,
                confirmLabel = okSetSuperProgIdxLabel,
                onConfirm = {
                    scope.launch {
                        model.setSuperProgIdx(confirmAction.unyt)
                        onDismissRequest()
                    }
                },
                onDismissRequest = onDismissRequest,
            )
        }
    }
}
