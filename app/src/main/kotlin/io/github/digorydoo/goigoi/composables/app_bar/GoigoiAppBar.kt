package io.github.digorydoo.goigoi.composables.app_bar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.ScreenSize

private interface GoigoiAppBarStyles {
    val appBarHeight: Dp

    val titleTextStyle: TextStyle
    val titlePaddingTop: Dp
    val titlePaddingStart: Dp
}

@Composable
private fun getStyles(): GoigoiAppBarStyles {
    val density = LocalDensity.current
    val typography = GoigoiTheme.typography
    val screenSize = DeviceProps.size
    val isPortrait = DeviceProps.isPortrait

    val small = when (screenSize) {
        ScreenSize.LARGE -> false
        ScreenSize.NORMAL -> !isPortrait
        ScreenSize.SMALL -> true
    }

    val statusBarHeightPx = WindowInsets.statusBars.getTop(density)
    val statusBarHeightDp = with(density) { statusBarHeightPx.toDp() }

    return remember(density) {
        object: GoigoiAppBarStyles {
            override val appBarHeight = statusBarHeightDp + (if (small) 48.dp else 56.dp)

            override val titleTextStyle = when (small) {
                true -> typography.appBarTitleSmall
                false -> typography.appBarTitle
            }

            override val titlePaddingTop = 1.dp
            override val titlePaddingStart = 16.dp
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoigoiAppBar(title: @Composable () -> Unit, onBack: () -> Unit) {
    val styles = getStyles()

    // The default implementation of the Android back button is to finish the current activity.
    // However, this would lead to inconsistent behaviour if the provided onBack callback does something else.
    // We can use BackHandler to make sure the same callback will be called.
    BackHandler(onBack = onBack)

    TopAppBar(
        modifier = Modifier.height(styles.appBarHeight),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_arrow_left_24dp),
                    contentDescription = null
                )
            }
        },
        title = title,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GoigoiTheme.colours.appBarContainer,
            titleContentColor = GoigoiTheme.colours.onAppBarContainer,
            navigationIconContentColor = GoigoiTheme.colours.onAppBarContainer
        )
    )
}

@Composable
fun GoigoiAppBar(title: String, onBack: () -> Unit) {
    val styles = getStyles()

    GoigoiAppBar(
        title = {
            Text(
                modifier = Modifier.padding(top = styles.titlePaddingTop, start = styles.titlePaddingStart),
                text = title,
                style = styles.titleTextStyle
            )
        },
        onBack
    )
}

@Composable
fun GoigoiAppBar(titleResId: Int, onBack: () -> Unit) {
    GoigoiAppBar(title = stringResource(titleResId), onBack)
}
