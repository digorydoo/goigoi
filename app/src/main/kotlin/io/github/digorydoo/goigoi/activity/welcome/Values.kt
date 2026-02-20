package io.github.digorydoo.goigoi.activity.welcome

import android.app.Activity
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.ScreenSize

class Values(a: Activity) {
    val topicImageMargin = DimUtils.dpToPx(24.0f, a.applicationContext)
    val minScrollTop = DimUtils.dpToPx(72, a.applicationContext)

    // FIXME remove this from attr since we have a function
    private val baseListLRMargin = ResUtils.getDimensionFromAttr(R.attr.mainActivityLRMargin, a)
    private val extraLRMargin = DimUtils.dpToPx(96.0f, a.applicationContext)
    private val furiganaFontSizeMinSmall = DimUtils.dpToPx(11.0f, a.applicationContext)
    private val furiganaFontSizeMinNormal = DimUtils.dpToPx(12.0f, a.applicationContext)

    fun listLRMargin(screenSize: ScreenSize, orientation: Orientation) = when {
        screenSize != ScreenSize.LARGE -> baseListLRMargin
        orientation == Orientation.PORTRAIT -> baseListLRMargin
        else -> baseListLRMargin + extraLRMargin
    }

    fun furiganaFontSizeMin(screenSize: ScreenSize) = when (screenSize) {
        ScreenSize.SMALL -> furiganaFontSizeMinSmall
        else -> furiganaFontSizeMinNormal
    }
}
