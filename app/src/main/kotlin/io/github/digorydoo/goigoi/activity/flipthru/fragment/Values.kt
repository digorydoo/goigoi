package io.github.digorydoo.goigoi.activity.flipthru.fragment

import android.content.Context
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils

class Values(ctx: Context) {
    val furiganaFontSizeBase = DimUtils.dpToPx(12.0f, ctx)
    val romajiFontSize = DimUtils.dpToPx(17.0f, ctx)

    val romajiTextColour = ResUtils.getARGBFromAttr(R.attr.mySecondaryTextColour, ctx)
    val romajiSoloTextColour = ResUtils.getARGBFromAttr(R.attr.myPrimaryTextColour, ctx)
}
