package io.github.digorydoo.goigoi.components.menus

import androidx.compose.runtime.Composable
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.components.menus.UnytCtxAction.FAKE_AVG_STATS
import io.github.digorydoo.goigoi.components.menus.UnytCtxAction.FAKE_GOOD_STATS
import io.github.digorydoo.goigoi.components.menus.UnytCtxAction.FAKE_POOR_STATS
import io.github.digorydoo.goigoi.components.menus.UnytCtxAction.RESET_STATS
import io.github.digorydoo.goigoi.components.menus.UnytCtxAction.SET_SUPER_PROG_IDX

enum class UnytCtxAction {
    FAKE_GOOD_STATS,
    FAKE_AVG_STATS,
    FAKE_POOR_STATS,
    RESET_STATS,
    SET_SUPER_PROG_IDX,
}

@Composable
fun UnytCtxMenu(
    onAction: (UnytCtxAction) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val items = buildContextMenu {
        item(R.drawable.ic_reset_white_24dp, R.string.item_reset_unyt_progress, RESET_STATS)
        debugItem("Fake good stats", FAKE_GOOD_STATS)
        debugItem("Fake average stats", FAKE_AVG_STATS)
        debugItem("Fake poor stats", FAKE_POOR_STATS)
        debugItem("Set super prog idx", SET_SUPER_PROG_IDX)
    }

    ContextMenu(
        items = items,
        onAction = onAction,
        onDismissRequest = onDismissRequest
    )
}
