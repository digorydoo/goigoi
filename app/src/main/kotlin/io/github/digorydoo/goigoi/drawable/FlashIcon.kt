package io.github.digorydoo.goigoi.drawable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import ch.digorydoo.kutils.filter.delay
import kotlin.math.min

class FlashIcon(ctx: Context): AnimatedDrawable() {
    private val insetSize = DimUtils.dpToPx(1, ctx)

    private val bgndPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = ResUtils.getARGBFromAttr(R.attr.flashBgColour, ctx)
    }

    private val flashPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = ResUtils.getARGBFromAttr(R.attr.flashFgColour, ctx)
    }

    override fun draw(canvas: Canvas) {
        val bounds = getAnimatedBounds(delay(animValue, 0.0f, 0.8f))
        bounds.inset(insetSize, insetSize)

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val r = 0.5f * min(bounds.width(), bounds.height())

        canvas.drawCircle(cx, cy, r, bgndPaint)

        val path = Path()
        var idx = 0

        while (idx < pts.size) {
            val relX = pts[idx++]
            val relY = pts[idx++]
            val x = bounds.left + relX * bounds.width()
            val y = bounds.top + relY * bounds.height()

            if (idx <= 2) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        path.close()

        canvas.drawPath(path, flashPaint)
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "FlashIcon"

        val pts = arrayOf(
            0.45f, 0.85f,
            0.46f, 0.55f,
            0.35f, 0.55f,
            0.39f, 0.21f,
            0.69f, 0.21f,
            0.56f, 0.45f,
            0.69f, 0.45f
        )
    }
}
