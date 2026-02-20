package io.github.digorydoo.goigoi.view

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView
import io.github.digorydoo.goigoi.utils.DimUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.math.abs

/**
 * This ScrollView may be used as the root view of a BottomSheet whose contents can become longer than the height of the
 * screen. To get correct touch behaviour at the area to the left and right of the sheet (landscape), set a padding to
 * this ScrollView rather than using the BottomSheet's maxWidth property.
 */
class BottomSheetScrollView: ScrollView {
    var dialog: BottomSheetDialog? = null

    private var touchDown = PointF(0.0f, 0.0f)
    private var maybeClick = false
    private var shouldIgnoreDrag = false
    private var shouldDismiss = false

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) {
        // setup(ctx, attrs)
    }

    constructor(ctx: Context, attrs: AttributeSet, defStyle: Int): super(ctx, attrs, defStyle) {
        // setup(ctx, attrs)
    }

    override fun onTouchEvent(evt: MotionEvent?): Boolean {
        when (evt?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touchDown = PointF(evt.rawX, evt.rawY)
                maybeClick = true
                shouldIgnoreDrag = isCloseToSheetTop(evt)
                shouldDismiss = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = DimUtils.pxToMm(abs(touchDown.x - evt.rawX), context)
                val dy = DimUtils.pxToMm(abs(touchDown.y - evt.rawY), context)

                if (dx > MAX_CLICK_DISTANCE || dy > MAX_CLICK_DISTANCE) {
                    maybeClick = false
                }

                if (shouldIgnoreDrag) {
                    // This fixes a problem that there was a thin area near the top of the sheet where the user could
                    // scroll the sheet content without dragging the sheet.
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                if (maybeClick && isOutsideContent(evt)) {
                    shouldDismiss = true
                    performClick()
                    return true // event was handled
                }
            }
        }

        return super.onTouchEvent(evt)
    }

    override fun performClick(): Boolean {
        if (shouldDismiss) {
            dialog?.dismiss()
        }

        return super.performClick()
    }

    private fun getRelativePos(evt: MotionEvent): PointF {
        val topLeft = intArrayOf(0, 0).apply { getLocationOnScreen(this) }
        val x = evt.rawX - topLeft[0] // don't use evt.x since it may be relative in some cases
        val y = evt.rawY - topLeft[1]
        return PointF(x, y)
    }

    private fun isOutsideContent(evt: MotionEvent): Boolean {
        val pos = getRelativePos(evt)
        val r = RectF(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            measuredWidth - paddingRight.toFloat(),
            measuredHeight - paddingBottom.toFloat()
        )
        return !r.contains(pos.x, pos.y)
    }

    private fun isCloseToSheetTop(evt: MotionEvent): Boolean {
        if (scrollY > 0) {
            // When the sheet is scrolled, the top is above the screen area.
            return false
        } else {
            val pos = getRelativePos(evt)
            val d = DimUtils.mmToPx(DEAD_TOP_DISTANCE, context)
            return pos.y <= d
        }
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "BottomSheetScrollView"

        private const val MAX_CLICK_DISTANCE = 2 // millimeter
        private const val DEAD_TOP_DISTANCE = 6 // millimeter
    }
}
