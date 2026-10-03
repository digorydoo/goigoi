package io.github.digorydoo.goigoi.composables.menus

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val SELECT_DELAY = 42.milliseconds // let the user see the checkmark switch places before dismiss

@Composable
fun <Value> MenuPopup(
    expanded: Boolean,
    items: List<MenuItemDefs<Value>>,
    currentValue: Value,
    onItemSelected: (Value) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = GoigoiTheme.colours.surface,
        tonalElevation = 0.dp, // disable Material 3 tonal elevation (I don't like it)
    ) {
        items.forEach { item ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.text,
                        style = GoigoiTheme.typography.listItemPrimaryText,
                        color = GoigoiTheme.colours.onBackground
                    )
                },
                leadingIcon = {
                    if (currentValue == item.action) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_check_black_24dp),
                            contentDescription = null,
                            tint = GoigoiTheme.colours.onSurface
                        )
                    }
                },
                trailingIcon = {
                    if (item.iconResId != null) {
                        Icon(
                            imageVector = ImageVector.vectorResource(item.iconResId),
                            contentDescription = null,
                            tint = GoigoiTheme.colours.decorativeIconTint
                        )
                    }
                },
                onClick = {
                    onItemSelected(item.action)
                    scope.launch {
                        delay(SELECT_DELAY)
                        onDismissRequest()
                    }
                },
            )
        }
    }
}
