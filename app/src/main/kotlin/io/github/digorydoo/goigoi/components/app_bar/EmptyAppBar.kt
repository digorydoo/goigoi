package io.github.digorydoo.goigoi.components.app_bar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import io.github.digorydoo.goigoi.providers.GoigoiTheme

/**
 * Workaround to get a reasonable status bar colour for activities that have no AppBar.
 * FIXME This doesn't fully work, i.e. foreground colour is not propagated to status bar!
 * We probably need to remove this component and do it differently. But styles for legacy actitivies come in the way!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmptyAppBar(onBack: () -> Unit) {
    BackHandler(onBack = onBack)

    val density = LocalDensity.current
    val statusBarHeightPx = WindowInsets.statusBars.getTop(density)
    val statusBarHeightDp = with(density) { statusBarHeightPx.toDp() }

    TopAppBar(
        modifier = Modifier.height(statusBarHeightDp),
        title = {},
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GoigoiTheme.colours.statusBar, // works
            titleContentColor = GoigoiTheme.colours.onStatusBar, // doesn't work
        )
    )
}
