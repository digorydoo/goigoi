package io.github.digorydoo.goigoi.activity.unyt.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.components.menus.MenuPopup
import io.github.digorydoo.goigoi.components.menus.buildMenuDefs
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.UserPrefs.WordListItemMode

@Composable
fun DisplayModeOverflowBtn(
    availableModes: List<WordListItemMode>,
    currentMode: WordListItemMode,
    onModeSelected: (WordListItemMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expanded = remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { expanded.value = true }) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_more_vert_black_24dp),
                contentDescription = null,
                tint = GoigoiTheme.colours.onBackground,
            )
        }

        val items = buildMenuDefs {
            for (mode in availableModes) {
                item(
                    textResId = when (mode) {
                        WordListItemMode.SHOW_KANA -> R.string.show_kana
                        WordListItemMode.SHOW_ROMAJI -> R.string.show_romaji
                        WordListItemMode.SHOW_TRANSLATION -> R.string.show_translation
                    },
                    action = mode,
                )
            }
        }

        MenuPopup(
            expanded = expanded.value,
            items,
            currentMode,
            onItemSelected = { onModeSelected(it) },
            onDismissRequest = { expanded.value = false }
        )
    }
}
