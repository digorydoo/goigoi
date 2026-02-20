package io.github.digorydoo.goigoi.drawable

import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import io.github.digorydoo.goigoi.utils.AndroidMathUtils.scaleRect

abstract class AnimatedDrawable: Drawable() {
    var animValue: Float = 1.0f

    protected val animatedBounds: Rect
        get() = getAnimatedBounds(animValue)

    protected fun getAnimatedBounds(boundsAnimValue: Float): Rect {
        val r = copyBounds()
        scaleRect(r, boundsAnimValue)
        return r
    }

    final override fun setAlpha(alpha: Int) {}
    final override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java; still needed for API < 29", ReplaceWith(""))
    final override fun getOpacity() = PixelFormat.TRANSLUCENT
}
