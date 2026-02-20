package io.github.digorydoo.goigoi.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Surface
import android.view.Window
import android.view.WindowInsetsController
import ch.digorydoo.kutils.colour.Colour
import kotlin.math.ceil
import kotlin.math.sqrt

enum class Orientation {
    PORTRAIT, LANDSCAPE_LEFT, LANDSCAPE_RIGHT, UNKNOWN
}

enum class ScreenSize {
    SMALL, // even smaller than Samsung Galaxy S5
    NORMAL, // Samsung Galaxy S5 and Samsung Galaxy S10
    LARGE // even larger than Samsung Galaxy S10
}

enum class NavBarPos {
    BOTTOM, LEFT, RIGHT, PERMANENT
}

object DeviceUtils {
    private const val TAG = "DeviceUtils"

    private var cachedIsInTestLab: Boolean? = null
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
     * Determines the height of the status bar. Note that the implementation is far from ideal,
     * but there does not seem to exist a better alternative at the moment.
     * https://stackoverflow.com/questions/3407256/height-of-status-bar-in-android
     * @return the height of the status bar, in device pixels
     */
    @Deprecated("Try to avoid having to know the height of the status bar ")
    fun getStatusBarHeight(ctx: Context): Int {
        val res = ctx.resources
        // FIXME can i use R.android.dimen.status_bar_height here?
        val resId = res.getIdentifier("status_bar_height", "dimen", "android")
        return if (resId > 0) {
            res.getDimensionPixelSize(resId)
        } else {
            ceil(24 * res.displayMetrics.density).toInt()
        }
    }

    fun getStatusBarColour(window: Window): Colour {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val view = window.decorView
            val drw = view.background as? ColorDrawable
            return Colour.fromARGB(drw?.color ?: Color.BLACK)
        } else {
            @Suppress("DEPRECATION")
            return ResUtils.getColourFromAttr(android.R.attr.statusBarColor, window.context)
        }
    }

    fun getNavigationBarColour(window: Window): Colour {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // FIXME This is the colour of the status bar
            val view = window.decorView
            val drw = view.background as? ColorDrawable
            return Colour.fromARGB(drw?.color ?: Color.BLACK)
        } else {
            @Suppress("DEPRECATION")
            return ResUtils.getColourFromAttr(android.R.attr.navigationBarColor, window.context)
        }
    }

    /**
     * Sets the status bar to a light or dark theme.
     */
    fun setStatusBarAppearance(colour: Int, dark: Boolean, window: Window) {
        val view = window.decorView

        val bit = if (dark) 0 else WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        val mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        view.windowInsetsController?.setSystemBarsAppearance(bit, mask)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            view.setBackgroundColor(colour)
        } else {
            @Suppress("DEPRECATION")
            window.statusBarColor = colour
        }
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

    /**
     * Determines the position of the nav bar. Note that figuring out the right time to call this
     * method may be tricky, as onConfigurationChange is not called on Activity when orientation
     * changes from LANDSCAPE_LEFT to LANDSCAPE_RIGHT directly. In general, you should try to avoid
     * having to know the position of the navbar in the first place.
     */
    @Deprecated("Try not to depend on the position of the nav bar")
    fun getNavBarPos(activity: Activity): NavBarPos {
        val ctx = activity.applicationContext

        if (!ResUtils.hasOnscreenNavBar(ctx)) {
            return NavBarPos.PERMANENT
        }

        val res = ctx.resources
        val cfg = res.configuration
        val dm = res.displayMetrics
        val canMove = dm.widthPixels != dm.heightPixels && cfg.smallestScreenWidthDp < 600

        if (!canMove || dm.widthPixels < dm.heightPixels) {
            return NavBarPos.BOTTOM
        }

        return when (getOrientation(activity)) {
            Orientation.LANDSCAPE_RIGHT -> NavBarPos.LEFT
            Orientation.LANDSCAPE_LEFT -> NavBarPos.RIGHT
            else -> NavBarPos.BOTTOM
        }
    }

    /**
     * Checks whether the device runs in Google Play Console Test Lab.
     */
    fun isInTestLab(ctx: Context): Boolean {
        if (cachedIsInTestLab == null) {
            try {
                cachedIsInTestLab = Settings.System.getString(ctx.contentResolver, "firebase.test.lab") == "true"
            } catch (e: Exception) {
                Log.d(TAG, "isInTestLab failed: ${e.message}")
                cachedIsInTestLab = false
            }
        }

        return cachedIsInTestLab ?: false
    }
}
