package io.github.digorydoo.goigoi.components.menus

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.digorydoo.goigoi.R

class CtxMenuItemDefs<Action>(val iconResId: Int, val text: String, val action: Action, val debugOnly: Boolean)

@DslMarker
annotation class ContextMenuItemDsl

@ContextMenuItemDsl
class ContextMenuItemDefsBuilder<Action> {
    val items = mutableListOf<CtxMenuItemDefs<Action>>()

    @Composable
    fun item(iconResId: Int, textResId: Int, action: Action) =
        item(iconResId, stringResource(textResId), action)

    private fun item(iconResId: Int, text: String, action: Action): CtxMenuItemDefs<Action> {
        val item = CtxMenuItemDefs(
            iconResId = iconResId,
            text = text,
            action = action,
            debugOnly = false,
        )
        items.add(item)
        return item
    }

    fun debugItem(text: String, action: Action): CtxMenuItemDefs<Action> {
        val item = CtxMenuItemDefs(
            iconResId = R.drawable.ic_debug_white_24dp,
            text = "$text [DEBUG]",
            action = action,
            debugOnly = true,
        )
        items.add(item)
        return item
    }
}

@Composable
fun <Action> buildContextMenu(
    lambda: @Composable ContextMenuItemDefsBuilder<Action>.() -> Unit,
): List<CtxMenuItemDefs<Action>> {
    val builder = ContextMenuItemDefsBuilder<Action>()
    builder.lambda()
    return builder.items
}
