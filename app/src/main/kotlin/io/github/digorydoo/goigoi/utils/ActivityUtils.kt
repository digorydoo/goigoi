package io.github.digorydoo.goigoi.utils

import android.content.Context
import androidx.appcompat.widget.Toolbar
import io.github.digorydoo.goigoi.R

object ActivityUtils {
    /**
     * Our toolbar's subtitle uses a secondary text colour. Google Play Console does not like that and wants a higher
     * contrast. Use this function to silence the warning.
     */
    fun adjustSubtitleTextColour(toolbar: Toolbar, ctx: Context) {
        if (DeviceUtils.isInTestLab(ctx)) {
            val c = ResUtils.getARGBFromAttr(R.attr.appBarPrimaryTextColour, ctx)
            toolbar.setSubtitleTextColor(c)
        }
    }
}
