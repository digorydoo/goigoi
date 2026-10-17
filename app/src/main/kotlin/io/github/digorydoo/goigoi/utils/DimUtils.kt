package io.github.digorydoo.goigoi.utils

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt

@Deprecated("Use LocalDensity instead")
object DimUtils {
    fun dpToPx(dp: Float, ctx: Context): Float {
        val metrics = ctx.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics)
    }

    fun pxToDp(px: Int, ctx: Context): Int {
        val oneDip = dpToPx(1.0f, ctx)
        return (px / oneDip).roundToInt()
    }

    // fun mmToPx(mm: Float, ctx: Context): Float {
    //     val metrics = ctx.resources.displayMetrics
    //     return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mm, metrics)
    // }
    //
    // fun pxToMm(px: Float, ctx: Context): Float {
    //     val oneMm = mmToPx(1.0f, ctx)
    //     return px / oneMm
    // }
}
