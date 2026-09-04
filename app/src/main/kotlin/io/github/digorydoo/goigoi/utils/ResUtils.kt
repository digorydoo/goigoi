package io.github.digorydoo.goigoi.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.view.ContextThemeWrapper
import ch.digorydoo.kutils.colour.Colour
import io.github.digorydoo.goigoi.R

object ResUtils {
    private const val TAG = "ResUtils"

    fun setActivityTheme(activity: Activity, lightThemeResId: Int? = null, darkThemeResId: Int? = null) {
        val ctx = activity.applicationContext
        val prefs = SingletonHolder.prefs

        val themeId = if (prefs.darkMode) {
            darkThemeResId ?: R.style.MyDarkTheme
        } else {
            lightThemeResId ?: R.style.MyLightTheme
        }

        activity.setTheme(themeId)
        ctx.theme.applyStyle(themeId, true)
        DeviceUtils.setNavBarAppearance(prefs.darkMode, activity.window)
    }

    fun getDialogThemeWrapper(ctx: Context): ContextThemeWrapper {
        val v = TypedValue()
        ctx.theme.resolveAttribute(R.attr.myDialogStyle, v, true)
        return ContextThemeWrapper(ctx, v.resourceId)
    }

    private fun getTypedValue(attrResId: Int, ctx: Context): TypedValue? {
        try {
            val v = TypedValue()
            val theme = ctx.theme

            // NOTE: resolveAttribute would fail if applyStyle was not called in setActivityTheme.
            // If you encounter problems, the following could be a workaround:
            // val theme = getContextThemeWrapper(ctx).theme

            if (!theme.resolveAttribute(attrResId, v, true)) {
                throw Exception("resolveAttribute failed")
            }

            return v
        } catch (e: Exception) {
            Log.d(TAG, "getTypedValue failed: ${e.message}")
            return null
        }
    }

    private fun getTypedValueResId(attrResId: Int, ctx: Context, fallback: Int): Int {
        return getTypedValue(attrResId, ctx)
            .takeIf { it?.resourceId != 0 }
            ?.resourceId
            ?: fallback
    }

    fun getARGBFromRes(resId: Int, ctx: Context): Int {
        return ctx.getColor(resId)
    }

    fun getARGBFromAttr(attrResId: Int, ctx: Context): Int {
        val resId = getTypedValueResId(attrResId, ctx, R.color.opacity_black_1f)
        return ctx.getColor(resId)
    }

    fun getColourFromRes(resId: Int, ctx: Context): Colour {
        return Colour.fromARGB(getARGBFromRes(resId, ctx))
    }

    fun getDimensionFromAttr(attrResId: Int, activity: Activity): Float {
        val dm = activity.resources.displayMetrics
        val ctx = activity.applicationContext
        return getTypedValue(attrResId, ctx)?.getDimension(dm) ?: 0.0f
    }

    fun getStringArray(resId: Int, ctx: Context): Array<String> {
        return ctx.resources.getStringArray(resId)
    }
}
