package io.github.digorydoo.goigoi.activity.prefs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R.string
import io.github.digorydoo.goigoi.components.SheetHead
import io.github.digorydoo.goigoi.components.app_bar.GoigoiAppBar
import io.github.digorydoo.goigoi.components.list.ListItem
import io.github.digorydoo.goigoi.providers.DeviceProps
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.providers.Singletons
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ScreenSize

private interface PrefsScreenStyles {
    val contentPaddingLR: Dp
    val contentPaddingTop: Dp
    val contentPaddingBottom: Dp
}

@Composable
private fun getStyles(): PrefsScreenStyles {
    val themeColours = GoigoiTheme.colours
    val density = LocalDensity.current
    val screenSize = DeviceProps.size
    val orientation = DeviceProps.orientation

    return remember(themeColours, density, screenSize, orientation) {
        object: PrefsScreenStyles {
            override val contentPaddingLR = when (screenSize) {
                ScreenSize.LARGE -> 32.dp
                ScreenSize.NORMAL -> when (orientation) {
                    Orientation.PORTRAIT -> 24.dp
                    else -> 32.dp
                }
                ScreenSize.SMALL -> 16.dp
            }

            override val contentPaddingTop = when (screenSize) {
                ScreenSize.LARGE -> 16.dp
                ScreenSize.NORMAL -> when (orientation) {
                    Orientation.PORTRAIT -> 8.dp
                    else -> 0.dp
                }
                ScreenSize.SMALL -> 0.dp
            }

            override val contentPaddingBottom = when (screenSize) {
                ScreenSize.LARGE -> 24.dp
                ScreenSize.NORMAL -> when (orientation) {
                    Orientation.PORTRAIT -> 16.dp
                    else -> 8.dp
                }
                ScreenSize.SMALL -> 8.dp
            }
        }
    }
}

@Composable
fun PrefsScreen(
    onDarkModeChange: (Boolean) -> Unit,
    onAboutItemSelected: () -> Unit,
    onBack: () -> Unit,
) {
    val styles = getStyles()
    val prefs = Singletons.prefs
    val darkMode = prefs.darkMode

    Scaffold(
        topBar = {
            GoigoiAppBar(
                titleResId = string.preferences,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = styles.contentPaddingBottom)
        ) {
            SheetHead(modifier = Modifier.padding(bottom = styles.contentPaddingTop))
            ListItem(
                primaryText = stringResource(string.changeThemePrimary),
                secondaryText = stringResource(string.changeThemeSecondary),
                horizontalPadding = styles.contentPaddingLR,
                onClick = { onDarkModeChange(!darkMode) },
                endContent = {
                    Switch(
                        checked = darkMode,
                        onCheckedChange = onDarkModeChange
                    )
                }
            )
            ListItem(
                primaryText = stringResource(string.aboutAppPrimary),
                secondaryText = stringResource(string.aboutAppSecondary),
                horizontalPadding = styles.contentPaddingLR,
                onClick = onAboutItemSelected
            )
        }
    }
}
