package io.github.digorydoo.goigoi.composables.menus

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.github.digorydoo.goigoi.R

class MenuItemDefs<Action>(val iconResId: Int?, val text: String, val action: Action, val debugOnly: Boolean)

@DslMarker
annotation class MenuItemDsl

@MenuItemDsl
class MenuItemDefsBuilder<Action> {
    val items = mutableListOf<MenuItemDefs<Action>>()

    @Composable
    fun item(iconResId: Int? = null, textResId: Int, action: Action) =
        item(iconResId, stringResource(textResId), action)

    private fun item(iconResId: Int?, text: String, action: Action): MenuItemDefs<Action> {
        val item = MenuItemDefs(
            iconResId = iconResId,
            text = text,
            action = action,
            debugOnly = false,
        )
        items.add(item)
        return item
    }

    fun debugItem(text: String, action: Action): MenuItemDefs<Action> {
        val item = MenuItemDefs(
            iconResId = R.drawable.ic_debug_white_24dp,
            text = "$text [DEBUG]",
            action = action,
            debugOnly = true,
        )
        items.add(item)
        return item
    }
}

// FIXME buildMenuDefs's lambda should not be compsable; store textResId in defs!
//  Then, buildMenuDefs should have a dependencies argument and call remember(dependencies) { ... }
@Composable
fun <Action> buildMenuDefs(
    lambda: @Composable MenuItemDefsBuilder<Action>.() -> Unit,
): List<MenuItemDefs<Action>> {
    val builder = MenuItemDefsBuilder<Action>()
    builder.lambda()
    return builder.items
}
