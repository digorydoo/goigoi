package io.github.digorydoo.goigoi.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import android.widget.ArrayAdapter
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.helper.UserPrefs
import ch.digorydoo.kutils.colour.Colour

object ResUtils {
    private const val TAG = "ResUtils"

    fun setActivityTheme(activity: Activity, lightThemeResId: Int? = null, darkThemeResId: Int? = null) {
        val ctx = activity.applicationContext
        val prefs = UserPrefs.getSingleton(ctx)

        val themeId = if (prefs.darkMode) {
            darkThemeResId ?: R.style.MyDarkTheme
        } else {
            lightThemeResId ?: R.style.MyLightTheme
        }

        activity.setTheme(themeId)
        ctx.theme.applyStyle(themeId, true)
        DeviceUtils.setNavBarAppearance(prefs.darkMode, activity.window)
    }

    /**
     * Determines whether the navigation bar is onscreen.
     * See also: DeviceUtils.getNavBarPos
     */
    fun hasOnscreenNavBar(ctx: Context): Boolean {
        /*
        Alternative, not sure if this would be better:
            val vcf = ViewConfiguration.get(ctx)

            if (vcf.hasPermanentMenuKey()) {
                return false
            }
        */
        val res = ctx.resources
        // FIXME can I use R.android.bool config_shownavigationBar here?
        val id = res.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && res.getBoolean(id)
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

    fun getDrawableFromAttr(attrResId: Int, ctx: Context): Drawable? {
        val resId = getTypedValueResId(attrResId, ctx, R.drawable.ic_flash_on_black_24dp)
        return ContextCompat.getDrawable(ctx, resId)
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

    fun getColourFromAttr(attrId: Int, ctx: Context): Colour {
        return Colour.fromARGB(getARGBFromAttr(attrId, ctx))
    }

    fun getDimensionFromAttr(attrResId: Int, activity: Activity): Float {
        val dm = activity.resources.displayMetrics
        val ctx = activity.applicationContext
        return getTypedValue(attrResId, ctx)?.getDimension(dm) ?: 0.0f
    }

    fun getStringArray(resId: Int, ctx: Context): Array<String> {
        return ctx.resources.getStringArray(resId)
    }

    @Suppress("unused")
    fun createArrayAdapter(
        textArrayResId: Int,
        activity: Activity,
    ): ArrayAdapter<CharSequence> {
        val adapter = ArrayAdapter.createFromResource(
            activity, textArrayResId, android.R.layout.simple_spinner_item // standard
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // standard
        return adapter
    }

    fun createArrayAdapter(
        textArray: Array<String?>,
        activity: Activity,
    ): ArrayAdapter<CharSequence?> {
        val adapter = ArrayAdapter<CharSequence?>(
            activity, android.R.layout.simple_spinner_item,  // standard
            textArray
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // standard
        return adapter
    }
}
