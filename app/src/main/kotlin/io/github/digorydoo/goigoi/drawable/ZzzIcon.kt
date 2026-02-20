package io.github.digorydoo.goigoi.drawable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import kotlin.math.min

class ZzzIcon(ctx: Context): AnimatedDrawable() {
    private val insetSize = DimUtils.dpToPx(1, ctx)

    private val bgPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = ResUtils.getARGBFromAttr(R.attr.zzzBgColour, ctx)
    }

    private val fgPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
        strokeJoin = Paint.Join.MITER
        color = ResUtils.getARGBFromAttr(R.attr.zzzFgColour, ctx)
    }

    override fun draw(canvas: Canvas) {
        val bounds = animatedBounds
        bounds.inset(insetSize, insetSize)

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val r = 0.5f * min(bounds.width(), bounds.height())

        canvas.drawCircle(cx + 0.1f * r, cy - 0.38f * r, r * 0.6f, bgPaint)
        canvas.drawCircle(cx - 0.3f * r, cy + 0.20f * r, r * 0.6f * 0.6f, bgPaint)
        canvas.drawCircle(cx + 0.0f * r, cy + 0.60f * r, r * 0.6f * 0.6f * 0.6f, bgPaint)
        canvas.drawCircle(cx - 0.1f * r, cy + 0.86f * r, r * 0.6f * 0.6f * 0.6f * 0.6f, bgPaint)

        drawZ(cx + 0.1f * r, cy - 0.38f * r, r * 0.6f, canvas)
        drawZ(cx - 0.3f * r, cy + 0.20f * r, r * 0.6f * 0.6f, canvas)
        drawZ(cx + 0.0f * r, cy + 0.60f * r, r * 0.6f * 0.6f * 0.6f, canvas)
    }

    private fun drawZ(cx: Float, cy: Float, r: Float, canvas: Canvas) {
        val path = Path().apply {
            moveTo(cx - 0.4f * r, cy - 0.4f * r)
            lineTo(cx + 0.4f * r, cy - 0.4f * r)
            lineTo(cx - 0.4f * r, cy + 0.4f * r)
            lineTo(cx + 0.4f * r, cy + 0.4f * r)
        }

        fgPaint.strokeWidth = 0.2f * r
        canvas.drawPath(path, fgPaint)
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "ZzzIcon"
    }
}
