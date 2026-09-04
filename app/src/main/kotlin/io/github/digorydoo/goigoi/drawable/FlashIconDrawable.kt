package io.github.digorydoo.goigoi.drawable

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ch.digorydoo.kutils.filter.delay
import kotlin.math.min

class FlashIconDrawable(
    private val colours: Colours,
    private val dims: Dimensions,
): AnimatedDrawable() {
    interface Colours {
        val background: Color
        val foreground: Color
    }

    interface Dimensions {
        val insetPx: Int
    }

    private val bgndPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = colours.background.toArgb()
    }

    private val flashPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = colours.foreground.toArgb()
    }

    override fun draw(canvas: Canvas) {
        val bounds = getAnimatedBounds(delay(animValue, 0.0f, 0.8f))
        bounds.inset(dims.insetPx, dims.insetPx)

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
