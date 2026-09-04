package io.github.digorydoo.goigoi.components.menus

import androidx.compose.runtime.Composable
import io.github.digorydoo.goigoi.R.drawable
import io.github.digorydoo.goigoi.R.string
import io.github.digorydoo.goigoi.components.menus.WordCtxAction.*

enum class WordCtxAction {
    ADD_TO_MY_WORDS,
    REMOVE_FROM_MY_WORDS,
    RESET_STATS,
    FAKE_GOOD_STATS,
    FAKE_AVG_STATS,
    FAKE_POOR_STATS,
    SET_SUPER_PROGRESSIVE_IDX,
}

@Composable
fun WordCtxMenu(
    isWordInMyWords: Boolean,
    onAction: (WordCtxAction) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val items = buildContextMenu {
        item(drawable.ic_reset_white_24dp, string.item_reset_word_progress, RESET_STATS)
        debugItem("Fake good stats", FAKE_GOOD_STATS)
        debugItem("Fake average stats", FAKE_AVG_STATS)
        debugItem("Fake poor stats", FAKE_POOR_STATS)
        debugItem("Set super prog idx", SET_SUPER_PROGRESSIVE_IDX)

        if (isWordInMyWords) {
            item(drawable.ic_trashcan_24dp, string.item_remove_word, REMOVE_FROM_MY_WORDS)
        } else {
            item(drawable.ic_add_black_24dp, string.item_add_word, ADD_TO_MY_WORDS)
        }
    }

    ContextMenu(
        items = items,
        onAction = onAction,
        onDismissRequest = onDismissRequest
    )
}
