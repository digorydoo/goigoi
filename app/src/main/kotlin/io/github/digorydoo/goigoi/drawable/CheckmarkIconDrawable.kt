package io.github.digorydoo.goigoi.drawable

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ch.digorydoo.kutils.filter.delay
import kotlin.math.floor
import kotlin.math.min

class CheckmarkIconDrawable(
    private val colours: Colours,
    private val dims: Dimensions,
): AnimatedDrawable() {
    interface Colours {
        val background: Color
        val mark: Color
    }

    interface Dimensions {
        val insetPx: Int
        val markMinSizePx: Int
    }

    private val bgndPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = colours.background.toArgb()
    }

    private val markPaint = Paint().apply {
        color = colours.mark.toArgb()
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
        strokeJoin = Paint.Join.MITER
    }

    override fun draw(canvas: Canvas) {
        val bounds = getAnimatedBounds(delay(animValue, 0.0f, 0.8f))
        bounds.inset(dims.insetPx, dims.insetPx)

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

        // Define rect2 as a rectangle with a size that's independent of the bounds

        val rect2 = Rect().apply {
            left = cx.toInt() - dims.markMinSizePx / 2
            right = left + dims.markMinSizePx
            top = cy.toInt() - dims.markMinSizePx / 2
            bottom = top + dims.markMinSizePx
        }

        // Take the larger of rect1 and rect2 and draw the checkmark in that rectangle

        val markRect = if (rect1.width() > rect2.width()) rect1 else rect2
        val path = Artist.makeCheckmarkPath(markRect, markAnimValue)
        markPaint.strokeWidth = 0.1f * markRect.width()
        canvas.drawPath(path, markPaint)
    }
}
