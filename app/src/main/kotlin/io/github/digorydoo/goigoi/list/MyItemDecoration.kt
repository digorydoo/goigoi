package io.github.digorydoo.goigoi.list

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import kotlin.math.roundToInt

class MyItemDecoration(ctx: Context): ItemDecoration() {
    private val dividerColour = ResUtils.getARGBFromAttr(R.attr.dividerColour, ctx)
    private val dividerStrokeWidth = DimUtils.dpToPx(1.0f, ctx)
    private val dividerTopMargin = DimUtils.dpToPx(8, ctx)
    private val dividerBottomMargin = DimUtils.dpToPx(8, ctx)
    private val subheaderTopMargin = DimUtils.dpToPx(16, ctx)

    private fun topMargin(item: AbstrListItem): Int {
        var m = when {
            item.topMargin > 0 -> item.topMargin
            item.viewType == ItemViewType.SUBHEADER -> subheaderTopMargin
            else -> 0
        }

        if (item.hasTopDivider) {
            m += dividerTopMargin
        }

        return m
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)
        val adapter = parent.adapter as MyListAdapter

        val paint = Paint().apply {
            color = dividerColour
            style = Paint.Style.STROKE
            strokeWidth = dividerStrokeWidth
        }

        val x1 = (parent.left + parent.paddingLeft).toFloat()
        val x2 = (parent.right - parent.paddingRight).toFloat()

        for (i in 0 ..< parent.childCount) {
            val child = parent.getChildAt(i)
            val pos = parent.getChildAdapterPosition(child)
            val item = adapter.itemAtPos(pos)

            if (item?.hasTopDivider == true) {
                val bounds = Rect()
                parent.getDecoratedBoundsWithMargins(child, bounds)

                val y = bounds.top +
                    child.translationY.roundToInt() +
                    dividerStrokeWidth +
                    topMargin(item)

                canvas.drawLine(x1 + item.lrMargin, y, x2 - item.lrMargin, y, paint)
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val pos = parent.getChildAdapterPosition(view)
        val adapter = parent.adapter as MyListAdapter
        val item = adapter.itemAtPos(pos)

        val lr = item?.lrMargin ?: 0

        outRect.apply {
            left = lr
            right = lr
            top = when {
                item == null -> 0
                item.hasTopDivider -> topMargin(item) + dividerBottomMargin
                else -> topMargin(item)
            }
            bottom = 0
        }
    }
}
