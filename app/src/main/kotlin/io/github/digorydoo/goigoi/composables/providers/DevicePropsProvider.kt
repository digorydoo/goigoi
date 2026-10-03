package io.github.digorydoo.goigoi.composables.providers

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.utils.DeviceUtils
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ScreenSize

@Immutable
private data class DevicePropsData(
    val size: ScreenSize = ScreenSize.NORMAL,
    val orientation: Orientation = Orientation.PORTRAIT,
    val widthDp: Dp = 0.dp,
    val heightDp: Dp = 0.dp,
)

private val LocalDevicePropsData = staticCompositionLocalOf { DevicePropsData() }

object DeviceProps {
    val size
        @Composable
        @ReadOnlyComposable
        get() = LocalDevicePropsData.current.size

    val orientation
        @Composable
        @ReadOnlyComposable
        get() = LocalDevicePropsData.current.orientation

    val isPortrait
        @Composable
        @ReadOnlyComposable
        get() = LocalDevicePropsData.current.orientation.let { it == Orientation.PORTRAIT || it == Orientation.UNKNOWN }

    @Suppress("unused")
    val isLandscape @Composable get() = !isPortrait

    @Suppress("unused")
    val isLeftLandscape
        @Composable
        @ReadOnlyComposable
        get() = LocalDevicePropsData.current.orientation.let { it == Orientation.LANDSCAPE_LEFT }

    val widthDp
        @Composable
        @ReadOnlyComposable
        get() = LocalDevicePropsData.current.widthDp

    @Suppress("unused")
    val heightDp
        @Composable
        @ReadOnlyComposable
        get() = LocalDevicePropsData.current.heightDp
}

@Composable
fun DevicePropsProvider(activity: Activity, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val bounds = activity.windowManager.currentWindowMetrics.bounds
    val widthDp = with(density) { bounds.width().toDp() }
    val heightDp = with(density) { bounds.height().toDp() }

    val data = DevicePropsData(
        size = DeviceUtils.getScreenSize(activity),
        orientation = DeviceUtils.getOrientation(activity),
        widthDp = widthDp,
        heightDp = heightDp
    )

    CompositionLocalProvider(LocalDevicePropsData provides data) {
        content()
    }
}
