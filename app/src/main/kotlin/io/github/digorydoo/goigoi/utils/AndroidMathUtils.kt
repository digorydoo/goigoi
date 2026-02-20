package io.github.digorydoo.goigoi.utils

import android.graphics.Rect

/**
 * Android-specific math utils. Our main MathUtils class is in kutils.
 */
object AndroidMathUtils {
    /**
     * Scales an Android Rect structure with the origin at its centre.
     */
    fun scaleRect(r: Rect, factor: Float) {
        val aw = (r.width() * factor).toInt()
        val ah = (r.height() * factor).toInt()
        val cx = ((r.left + r.right) / 2.0f).toInt()
        val cy = ((r.top + r.bottom) / 2.0f).toInt()
        r.left = cx - (aw / 2.0f).toInt()
        r.right = r.left + aw
        r.top = cy - (ah / 2.0f).toInt()
        r.bottom = r.top + ah
    }
}
