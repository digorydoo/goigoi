package io.github.digorydoo.goigoi.composables.bottom_sheet.generic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.findDialogWindow

@Composable
fun StatusBarAboveBottomSheet(modifier: Modifier = Modifier) {
    val view = LocalView.current

    DisposableEffect(view) {
        val window = view.findDialogWindow()
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }

        if (controller != null) {
            val orig = controller.isAppearanceLightStatusBars
            controller.isAppearanceLightStatusBars = false
            onDispose {
                controller.isAppearanceLightStatusBars = orig
            }
        } else {
            onDispose {}
        }
    }

    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsTopHeight(WindowInsets.statusBars)
            .background(GoigoiTheme.colours.statusBarAboveBottomSheet)
    )
}
