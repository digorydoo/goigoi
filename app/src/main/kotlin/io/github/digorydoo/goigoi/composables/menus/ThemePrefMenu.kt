package io.github.digorydoo.goigoi.composables.menus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.composables.list.ListItem
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.UserPrefs.ThemePref
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private val HORIZONTAL_PADDING = 16.dp
private val VERTICAL_PADDING = 8.dp

@Composable
fun ThemePrefMenu(
    selected: ThemePref,
    onSelectAndDismiss: (newPref: ThemePref) -> Unit,
    onCancel: () -> Unit,
) {
    val choice = remember { mutableStateOf<ThemePref?>(null) }
    val scope = rememberCoroutineScope()

    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
        onDismissRequest = onCancel,
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = GoigoiTheme.colours.surface,
        ) {
            Column(modifier = Modifier.padding(vertical = VERTICAL_PADDING)) {
                for (prefValue in ThemePref.entries) {
                    ListItem(
                        primaryText = when (prefValue) {
                            ThemePref.DARK -> stringResource(R.string.darkTheme)
                            ThemePref.LIGHT -> stringResource(R.string.lightTheme)
                            ThemePref.SYSTEM -> stringResource(R.string.systemTheme)
                        },
                        horizontalPadding = HORIZONTAL_PADDING,
                        onClick = {
                            if (choice.value == null) {
                                choice.value = prefValue

                                scope.launch {
                                    delay(100.milliseconds) // let the user see the radio button icon change
                                    onSelectAndDismiss(prefValue)
                                }
                            }
                        },
                        endContent = {
                            Icon(
                                imageVector = ImageVector.vectorResource(
                                    if (prefValue == (choice.value ?: selected)) R.drawable.ic_radio_btn_checked_24dp
                                    else R.drawable.ic_radio_btn_unchecked_24dp
                                ),
                                contentDescription = null,
                                tint = GoigoiTheme.colours.onSurface,
                            )
                        }
                    )
                }
            }
        }
    }
}
