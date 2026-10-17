package io.github.digorydoo.goigoi.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.UserPrefs.ThemePref

object ResUtils {
    fun setActivityTheme(activity: Activity, lightThemeResId: Int? = null, darkThemeResId: Int? = null) {
        val ctx = activity.applicationContext
        val singletons = activity.getOrCreateSingletons()
        val prefs = singletons.prefs

        val darkMode = when (prefs.theme) {
            ThemePref.DARK -> true
            ThemePref.LIGHT -> false
            ThemePref.SYSTEM -> isSystemInDarkTheme(ctx)
        }

        val themeId =
            if (darkMode) darkThemeResId ?: R.style.MyDarkTheme
            else lightThemeResId ?: R.style.MyLightTheme

        activity.setTheme(themeId)
        ctx.theme.applyStyle(themeId, true)
        DeviceUtils.setNavBarAppearance(darkMode, activity.window)
    }

    private fun isSystemInDarkTheme(context: Context): Boolean {
        val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightMode == Configuration.UI_MODE_NIGHT_YES
    }

    fun getARGBFromRes(resId: Int, ctx: Context): Int {
        return ctx.getColor(resId)
    }

    fun getStringArray(resId: Int, ctx: Context): Array<String> {
        return ctx.resources.getStringArray(resId)
    }
}
