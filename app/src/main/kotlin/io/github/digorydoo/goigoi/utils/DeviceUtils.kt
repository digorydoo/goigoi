package io.github.digorydoo.goigoi.utils

import android.app.Activity
import android.content.res.Configuration
import android.view.Surface
import android.view.Window
import android.view.WindowInsetsController
import kotlin.math.sqrt

enum class Orientation {
    PORTRAIT, LANDSCAPE_LEFT, LANDSCAPE_RIGHT, UNKNOWN
}

enum class ScreenSize {
    SMALL, // even smaller than Samsung Galaxy S5
    NORMAL, // Samsung Galaxy S5 and Samsung Galaxy S10
    LARGE // even larger than Samsung Galaxy S10
}

object DeviceUtils {
    private var cachedScreenSize: ScreenSize? = null

    fun getOrientation(activity: Activity): Orientation {
        val rotation = activity.display?.rotation ?: return Orientation.UNKNOWN

        // The default rotation is 90° on phones and 0° on tablets. Instead of trying to figure out
        // whether we're a phone or a tablet, we take the current orientation into account.

        val ctx = activity.applicationContext
        val cfg = ctx.resources.configuration ?: return Orientation.UNKNOWN
        val isLandscape = cfg.orientation == Configuration.ORIENTATION_LANDSCAPE

        return when {
            !isLandscape -> Orientation.PORTRAIT
            rotation == Surface.ROTATION_0 -> Orientation.LANDSCAPE_LEFT
            rotation == Surface.ROTATION_90 -> Orientation.LANDSCAPE_LEFT
            else -> Orientation.LANDSCAPE_RIGHT
        }
    }

    private fun getRawScreenSize(activity: Activity) = run {
        val bounds = activity.windowManager?.currentWindowMetrics?.bounds
        val x = bounds?.width() ?: 0
        val y = bounds?.height() ?: 0
        Pair(x, y)
    }

    fun getScreenSize(activity: Activity): ScreenSize {
        if (cachedScreenSize == null) {
            val ctx = activity.applicationContext
            val (x, y) = getRawScreenSize(activity)
            val dx = x.toDouble()
            val dy = y.toDouble()
            val d = sqrt(dx * dx + dy * dy)
            val diagDp = DimUtils.pxToDp(d.toInt(), ctx)

            // Samsung Galaxy S5:          dx=1080px dy=1920px diag= 734dp
            // Samsung Galaxy S10:         dx=1080px dy=2280px diag= 961dp
            // 7.0inch Tab Nexus 7 API 29: dx= 800px dy=1216px diag=1093dp
            cachedScreenSize = when {
                diagDp < 730 -> ScreenSize.SMALL
                diagDp < 1093 -> ScreenSize.NORMAL
                else -> ScreenSize.LARGE
            }
        }

        return cachedScreenSize ?: ScreenSize.NORMAL
    }

    /**
     * Sets the navigation bar to a light or dark theme.
     */
    fun setNavBarAppearance(dark: Boolean, window: Window) {
        val view = window.decorView

        val bit = if (dark) 0 else WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        val mask = WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        view.windowInsetsController?.setSystemBarsAppearance(bit, mask)
    }
}
