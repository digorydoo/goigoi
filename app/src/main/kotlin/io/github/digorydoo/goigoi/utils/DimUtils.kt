package io.github.digorydoo.goigoi.utils

import android.app.Activity
import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt

@Suppress("unused", "MemberVisibilityCanBePrivate")
object DimUtils {
    private const val TAG = "DimUtils"

    fun fromAttr(attrResId: Int, activity: Activity) =
        ResUtils.getDimensionFromAttr(attrResId, activity)

    fun dpToPx(dp: Int, ctx: Context): Int {
        val metrics = ctx.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), metrics)
            .roundToInt()
    }

    fun dpToPx(dp: Float, ctx: Context): Float {
        val metrics = ctx.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics)
    }

    // NOTE: To correctly set the resulting value as a textSize, use:
    // textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
    fun spToPx(sp: Float, ctx: Context): Float {
        val metrics = ctx.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics)
    }

    fun pxToDp(px: Int, ctx: Context): Int {
        val oneDip = dpToPx(1.0f, ctx)
        return (px / oneDip).roundToInt()
    }

    fun pxToDp(px: Float, ctx: Context): Float {
        val oneDip = dpToPx(1.0f, ctx)
        return px / oneDip
    }

    fun mmToPx(mm: Int, ctx: Context): Int {
        val metrics = ctx.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mm.toFloat(), metrics)
            .roundToInt()
    }

    fun mmToPx(mm: Float, ctx: Context): Float {
        val metrics = ctx.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mm, metrics)
    }

    fun pxToMm(px: Int, ctx: Context): Int {
        val oneMm = mmToPx(1.0f, ctx)
        return (px / oneMm).roundToInt()
    }

    fun pxToMm(px: Float, ctx: Context): Float {
        val oneMm = mmToPx(1.0f, ctx)
        return px / oneMm
    }
}
