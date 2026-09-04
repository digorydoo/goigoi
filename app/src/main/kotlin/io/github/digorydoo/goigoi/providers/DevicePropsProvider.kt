package io.github.digorydoo.goigoi.providers

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.digorydoo.goigoi.utils.DeviceUtils
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ScreenSize

@Immutable
private data class DevicePropsData(
    val size: ScreenSize = ScreenSize.NORMAL,
    val orientation: Orientation = Orientation.PORTRAIT,
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

}

@Composable
fun DevicePropsProvider(activity: Activity, content: @Composable () -> Unit) {
    val data = DevicePropsData(
        size = DeviceUtils.getScreenSize(activity),
        orientation = DeviceUtils.getOrientation(activity)
    )
    CompositionLocalProvider(LocalDevicePropsData provides data) {
        content()
    }
}
