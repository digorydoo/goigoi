package io.github.digorydoo.goigoi.components.menus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.components.SimpleAlertDialog
import io.github.digorydoo.goigoi.components.menus.WordCtxAction.*
import io.github.digorydoo.goigoi.core.db.Word
import kotlinx.coroutines.launch

private sealed interface ConfirmWordAction
private class ConfirmWordResetStats(val word: Word): ConfirmWordAction
private class ConfirmWordSetSuperProgIdx(val word: Word): ConfirmWordAction

private enum class WordCtxAction {
    ADD_TO_MY_WORDS,
    REMOVE_FROM_MY_WORDS,
    RESET_STATS,
    FAKE_GOOD_STATS,
    FAKE_AVG_STATS,
    FAKE_POOR_STATS,
    SET_SUPER_PROG_IDX,
}

interface WordCtxMenuModel {
    suspend fun fakeGoodStats(word: Word)
    suspend fun fakeAvgStats(word: Word)
    suspend fun fakePoorStats(word: Word)
    suspend fun resetStats(word: Word)

    fun addToMyWords(word: Word)
    fun removeFromMyWords(word: Word)

    fun setSuperProgIdx(word: Word)
}

@Composable
fun WordCtxMenu(
    model: WordCtxMenuModel,
    word: Word,
    isWordInMyWords: Boolean,
    onDismissRequest: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val confirmActionState = remember { mutableStateOf(null as ConfirmWordAction?) }
    val confirmAction = confirmActionState.value
    val confirmResetStatsMsg = stringResource(R.string.confirm_reset_word_progress_msg)
    val confirmSetSuperProgIdxMsg = stringResource(R.string.confirm_set_super_prog_idx_to_word_msg)
    val okResetStatsLabel = stringResource(R.string.confirm_reset_word_progress_ok)
    val okSetSuperProgIdxLabel = stringResource(R.string.confirm_set_super_prog_idx_ok)

    val items = buildMenuDefs {
        item(R.drawable.ic_reset_white_24dp, R.string.item_reset_word_progress, RESET_STATS)
        debugItem("Fake good stats", FAKE_GOOD_STATS)
        debugItem("Fake average stats", FAKE_AVG_STATS)
        debugItem("Fake poor stats", FAKE_POOR_STATS)
        debugItem("Set super prog idx", SET_SUPER_PROG_IDX)

        if (isWordInMyWords) {
            item(R.drawable.ic_trashcan_24dp, R.string.item_remove_word, REMOVE_FROM_MY_WORDS)
        } else {
            item(R.drawable.ic_add_24dp, R.string.item_add_word, ADD_TO_MY_WORDS)
        }
    }

    if (confirmAction == null) {
        MenuDialog(
            items = items,
            onAction = { action ->
                when (action) {
                    FAKE_GOOD_STATS -> scope.launch {
                        model.fakeGoodStats(word) // suspends until finished
                        onDismissRequest()
                    }
                    FAKE_AVG_STATS -> scope.launch {
                        model.fakeAvgStats(word)
                        onDismissRequest()
                    }
                    FAKE_POOR_STATS -> scope.launch {
                        model.fakePoorStats(word)
                        onDismissRequest()
                    }
                    RESET_STATS -> confirmActionState.value = ConfirmWordResetStats(word)
                    SET_SUPER_PROG_IDX -> confirmActionState.value = ConfirmWordSetSuperProgIdx(word)
                    ADD_TO_MY_WORDS -> {
                        model.addToMyWords(word)
                        onDismissRequest()
                    }
                    REMOVE_FROM_MY_WORDS -> {
                        model.removeFromMyWords(word)
                        onDismissRequest()
                    }
                }
            },
            onDismissRequest = onDismissRequest
        )
    } else {
        when (confirmAction) {
            is ConfirmWordResetStats -> SimpleAlertDialog(
                message = confirmResetStatsMsg,
                confirmLabel = okResetStatsLabel,
                onConfirm = {
                    scope.launch {
                        model.resetStats(confirmAction.word)
                        onDismissRequest()
                    }
                },
                onDismissRequest = onDismissRequest,
            )
            is ConfirmWordSetSuperProgIdx -> SimpleAlertDialog(
                message = confirmSetSuperProgIdxMsg,
                confirmLabel = okSetSuperProgIdxLabel,
                onConfirm = {
                    scope.launch {
                        model.setSuperProgIdx(confirmAction.word)
                        onDismissRequest()
                    }
                },
                onDismissRequest = onDismissRequest,
            )
        }
    }
}
