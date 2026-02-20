package io.github.digorydoo.goigoi.activity.topic

import android.app.Activity
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.ScreenSize

class Values(a: Activity) {
    val listItemTopMargin = DimUtils.dpToPx(8, a) // e.g. see topic "さらに" (has no subheader)
    val listItemTopMarginWhenSubheader = DimUtils.dpToPx(24, a) // e.g. see topic "JLPT N5 - N4" (has subheader)

    // FIXME remove this from attr as we now have a function
    private val listLRMargin = ResUtils.getDimensionFromAttr(R.attr.topicActivityLRMargin, a)
    private val extraLRMargin = DimUtils.dpToPx(96.0f, a)

    fun gapSize(screenSize: ScreenSize, orientation: Orientation) = when {
        screenSize != ScreenSize.LARGE -> listLRMargin
        orientation == Orientation.PORTRAIT -> listLRMargin
        else -> listLRMargin + extraLRMargin
    }
}
