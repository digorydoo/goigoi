package io.github.digorydoo.goigoi.drawable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import ch.digorydoo.kutils.filter.delay
import kotlin.math.floor
import kotlin.math.min

class CheckmarkIcon(ctx: Context): AnimatedDrawable() {
    private val insetSize = DimUtils.dpToPx(1, ctx)
    private val markMinSize = DimUtils.dpToPx(23, ctx)

    private val bgndPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = ResUtils.getARGBFromRes(R.color.green_800, ctx)
    }

    private val markPaint = Paint().apply {
        color = ResUtils.getARGBFromRes(R.color.white, ctx)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
        strokeJoin = Paint.Join.MITER
    }

    override fun draw(canvas: Canvas) {
        val bounds = getAnimatedBounds(delay(animValue, 0.0f, 0.8f))
        bounds.inset(insetSize, insetSize)

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val r = 0.5f * min(bounds.width(), bounds.height())

        canvas.drawCircle(cx, cy, r, bgndPaint)

        // Determine whether the checkmark will be drawn

        val markAnimValue = delay(animValue, 0.9f)

        if (markAnimValue < 0) {
            return
        }

        // Define rect1 as a rectangle of a size proportional to the unanimated bounds

        val rect1 = copyBounds()
        val inset2 = floor(0.42f * r).toInt()
        rect1.inset(inset2, inset2)

        // Define rect2 as a rectangle with a size that's independent from the bounds

        val rect2 = Rect().apply {
            left = cx.toInt() - markMinSize / 2
            right = left + markMinSize
            top = cy.toInt() - markMinSize / 2
            bottom = top + markMinSize
        }

        // Take the larger of rect1 and rect2 and draw the checkmark in that rectangle

        val markRect = if (rect1.width() > rect2.width()) rect1 else rect2
        val path = Artist.makeCheckmarkPath(markRect, markAnimValue)
        markPaint.strokeWidth = 0.1f * markRect.width()
        canvas.drawPath(path, markPaint)
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "CheckmarkIcon"
    }
}
