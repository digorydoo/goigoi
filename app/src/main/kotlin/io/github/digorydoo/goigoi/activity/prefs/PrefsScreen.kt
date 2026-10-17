package io.github.digorydoo.goigoi.activity.prefs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.composables.SheetHead
import io.github.digorydoo.goigoi.composables.app_bar.GoigoiAppBar
import io.github.digorydoo.goigoi.composables.list.ListItem
import io.github.digorydoo.goigoi.composables.menus.ThemePrefMenu
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.utils.ScreenSize
import io.github.digorydoo.goigoi.utils.UserPrefs
import io.github.digorydoo.goigoi.utils.UserPrefs.ThemePref

@Composable
fun PrefsScreen(
    prefs: UserPrefs, // TODO should not pass UserPrefs here, but a Model instead
    onDarkModeChange: (ThemePref) -> Unit,
    onAboutItemSelected: () -> Unit,
    onBack: () -> Unit,
) {
    val showThemeMenu = remember { mutableStateOf(false) }

    val screenSize = DeviceProps.size
    val isPortrait = DeviceProps.isPortrait

    val contentHorizPadding = when (screenSize) {
        ScreenSize.LARGE -> 32.dp
        ScreenSize.NORMAL -> if (isPortrait) 24.dp else 32.dp
        ScreenSize.SMALL -> 16.dp
    }

    val contentPaddingTop = when (screenSize) {
        ScreenSize.LARGE -> 16.dp
        ScreenSize.NORMAL -> if (isPortrait) 8.dp else 0.dp
        ScreenSize.SMALL -> 0.dp
    }

    val contentPaddingBottom = when (screenSize) {
        ScreenSize.LARGE -> 24.dp
        ScreenSize.NORMAL -> if (isPortrait) 16.dp else 8.dp
        ScreenSize.SMALL -> 8.dp
    }

    Scaffold(
        topBar = {
            GoigoiAppBar(
                titleResId = R.string.preferences,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = contentPaddingBottom)
        ) {
            SheetHead(modifier = Modifier.padding(bottom = contentPaddingTop))

            ListItem(
                primaryText = stringResource(R.string.changeTheme),
                secondaryText = when (prefs.theme) {
                    ThemePref.DARK -> stringResource(R.string.darkTheme)
                    ThemePref.LIGHT -> stringResource(R.string.lightTheme)
                    ThemePref.SYSTEM -> stringResource(R.string.systemTheme)
                },
                horizontalPadding = contentHorizPadding,
                onClick = { showThemeMenu.value = true },
            )

            if (showThemeMenu.value) {
                ThemePrefMenu(
                    selected = prefs.theme,
                    onSelectAndDismiss = { newValue ->
                        showThemeMenu.value = false

                        if (newValue != prefs.theme) {
                            onDarkModeChange(newValue)
                        }
                    },
                    onCancel = {
                        showThemeMenu.value = false
                    }
                )
            }

            ListItem(
                primaryText = stringResource(R.string.aboutAppPrimary),
                secondaryText = stringResource(R.string.aboutAppSecondary),
                horizontalPadding = contentHorizPadding,
                onClick = onAboutItemSelected
            )
        }
    }
}
