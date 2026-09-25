package io.github.digorydoo.goigoi.legacy.drawable

import android.content.Context
import androidx.compose.ui.graphics.Color
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils

object IconBuilder {
    fun getCheckmarkIconDrawable(ctx: Context): CheckmarkIconDrawable {
        val colours = object: CheckmarkIconDrawable.Colours {
            override val background = Color(ResUtils.getARGBFromRes(R.color.green_800, ctx))
            override val mark = Color(ResUtils.getARGBFromRes(R.color.white, ctx))
        }
        val dims = object: CheckmarkIconDrawable.Dimensions {
            override val insetPx = DimUtils.dpToPx(1, ctx)
            override val markMinSizePx = DimUtils.dpToPx(23, ctx)
        }
        return CheckmarkIconDrawable(colours, dims)
    }

    fun getFlashIconDrawable(ctx: Context): FlashIconDrawable {
        val colours = object: FlashIconDrawable.Colours {
            override val background = Color(ResUtils.getARGBFromAttr(R.attr.flashBgColour, ctx))
            override val foreground = Color(ResUtils.getARGBFromAttr(R.attr.flashFgColour, ctx))
        }
        val dims = object: FlashIconDrawable.Dimensions {
            override val insetPx = DimUtils.dpToPx(1, ctx)
        }
        return FlashIconDrawable(colours, dims)
    }
}
