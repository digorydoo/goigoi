package io.github.digorydoo.goigoi.drawable

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toRectF

class SheetHeadDrawable(
    private val colours: Colours,
    private val dims: Dimensions,
): AnimatedDrawable() {
    interface Colours {
        val appBar: Color
    }

    interface Dimensions {
        val cornerSizePx: Int
        val headHeightPx: Int
    }

    override fun getIntrinsicHeight() = dims.headHeightPx

    override fun draw(canvas: Canvas) {
        val paint = Paint().apply {
            style = Paint.Style.FILL
            color = colours.appBar.toArgb()
        }

        val r = copyBounds().toRectF()
        val cornerSize = dims.cornerSizePx.toFloat()

        val path = Path().apply {
            moveTo(r.left, r.top)
            lineTo(r.right, r.top)
            lineTo(r.right, r.bottom)
            arcTo(
                r.right - 2 * cornerSize,
                r.bottom - cornerSize,
                r.right,
                r.bottom + cornerSize,
                0.0f, // startAngle (degrees)
                -90.0f, // sweepAngle (degrees)
                false // forceMoveTo
            )
            lineTo(r.right - cornerSize, r.bottom - cornerSize) // should be here already
            lineTo(r.left + cornerSize, r.bottom - cornerSize)
            arcTo(
                r.left,
                r.bottom - cornerSize,
                r.left + 2 * cornerSize,
                r.bottom + cornerSize,
                -90.0f, // startAngle (degrees)
                -90.0f, // sweepAngle (degrees)
                false // forceMoveTo
            )
            lineTo(r.left, r.bottom) // should be here already
            close()
        }

        canvas.drawPath(path, paint)
    }
}
