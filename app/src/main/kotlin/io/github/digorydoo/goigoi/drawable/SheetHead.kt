package io.github.digorydoo.goigoi.drawable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.toRectF
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils

class SheetHead(ctx: Context): AnimatedDrawable() {
    private val cornerSize = DimUtils.dpToPx(24, ctx)
    private val headHeight = cornerSize + DimUtils.dpToPx(8, ctx)
    private val appBarColour = ResUtils.getARGBFromAttr(R.attr.appBarColour, ctx)

    // This would be the content background colour, but we use alpha instead (we simply don't draw these regions).
    // private val contentColour = ResUtils.getARGBFromAttr(R.attr.topicSheetHeadColour, ctx)

    override fun getIntrinsicHeight() = headHeight

    override fun draw(canvas: Canvas) {
        val paint = Paint().apply {
            style = Paint.Style.FILL
            color = appBarColour
        }

        val r = copyBounds().toRectF()

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
