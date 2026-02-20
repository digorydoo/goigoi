package io.github.digorydoo.goigoi.bottom_sheet

import android.content.Context
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ScreenSize

class Values(ctx: Context) {
    val peekHeight = DimUtils.dpToPx(160, ctx)
    private val contentLRMargin1 = DimUtils.dpToPx(48, ctx)
    private val contentLRMargin2 = DimUtils.dpToPx(64, ctx)
    private val contentLRMargin3 = DimUtils.dpToPx(128, ctx)

    fun contentLRMargin(screenSize: ScreenSize, orientation: Orientation) = when (orientation) {
        Orientation.PORTRAIT -> when (screenSize) {
            ScreenSize.SMALL -> 0
            ScreenSize.NORMAL -> 0
            else -> contentLRMargin2
        }
        else -> when (screenSize) {
            ScreenSize.SMALL -> contentLRMargin1
            ScreenSize.NORMAL -> contentLRMargin2
            else -> contentLRMargin3
        }
    }
}
