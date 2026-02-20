package io.github.digorydoo.goigoi.activity.unyt

import android.app.Activity
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ScreenSize

class Values(a: Activity) {
    val firstItemTopMargin = DimUtils.dpToPx(16, a)

    private val startBtnRightMarginSmall = DimUtils.dpToPx(24, a)
    private val startBtnRightMarginMedium = DimUtils.dpToPx(32, a)
    private val startBtnRightMarginLarge = DimUtils.dpToPx(48, a)

    private val startBtnBottomMarginSmall = DimUtils.dpToPx(16, a)
    private val startBtnBottomMarginMedium = DimUtils.dpToPx(24, a)
    private val startBtnBottomMarginLarge = DimUtils.dpToPx(128, a)

    fun startBtnRightMargin(screenSize: ScreenSize, orientation: Orientation) = when {
        screenSize == ScreenSize.LARGE -> startBtnRightMarginLarge
        orientation == Orientation.PORTRAIT -> startBtnRightMarginSmall // small when portrait
        else -> startBtnRightMarginMedium
    }

    fun startBtnBottomMargin(screenSize: ScreenSize, orientation: Orientation) = when {
        screenSize == ScreenSize.LARGE -> startBtnBottomMarginLarge
        orientation == Orientation.PORTRAIT -> startBtnBottomMarginMedium
        else -> startBtnBottomMarginSmall // small when landscape
    }
}
