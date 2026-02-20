package io.github.digorydoo.goigoi.helper

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import io.github.digorydoo.goigoi.utils.DimUtils
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.decel
import kotlin.math.abs
import kotlin.math.sign

class MyGestureDetector(ctx: Context) {
    private enum class GestureKind {
        NONE, FLING_LEFT, FLING_RIGHT, FLING_UP, FLING_DOWN, SINGLE_TAP
    }

    private enum class ScrollKind {
        NONE, UNDER_THRESHOLD1, UNDER_THRESHOLD2, HORIZ, VERT
    }

    var canFlingLeft = false
    var canFlingRight = false
    var canFlingUp = false
    var canFlingDown = false

    var onTouch: ((event: MotionEvent) -> Boolean)? = null
    var onFlingLeft: (() -> Unit)? = null
    var onFlingRight: (() -> Unit)? = null
    var onFlingUp: (() -> Unit)? = null
    var onFlingDown: (() -> Unit)? = null
    var onScroll: ((dx: Float, dy: Float) -> Unit)? = null
    var onSingleTap: (() -> Unit)? = null
    var onLetGo: (() -> Unit)? = null

    private val stdDetector: GestureDetector = GestureDetector(ctx, Listener())
    private val oneMillimeter = DimUtils.pxToMm(1.0f, ctx)
    private var detectedGesture = GestureKind.NONE
    private var scrollDetected = ScrollKind.NONE
    private var maybeQuietFling = GestureKind.NONE

    fun attachTo(view: View) {
        view.setOnTouchListener { v: View, event: MotionEvent ->
            if (onTouch?.let { it(event) } != true) {
                // We need to call performClick here when a click was detected, rather than just
                // handle the action of the click.

                if (handleTouchEvent(event)) {
                    v.performClick()
                }
            }

            true // true=consume event
        }
        view.setOnClickListener {
            onSingleTap?.let { it() }
        }
    }

    private fun handleTouchEvent(e: MotionEvent): Boolean {
        var isSingleTap = false
        detectedGesture = GestureKind.NONE
        stdDetector.onTouchEvent(e)

        if (e.action == MotionEvent.ACTION_UP) {
            if (!callCallbacks(detectedGesture)) {
                when {
                    detectedGesture == GestureKind.SINGLE_TAP -> {
                        isSingleTap = true
                    }
                    scrollDetected == ScrollKind.UNDER_THRESHOLD1 -> {
                        isSingleTap = true
                    }
                    maybeQuietFling != GestureKind.NONE -> {
                        if (!callCallbacks(maybeQuietFling)) {
                            onLetGo?.let { it() }
                        }
                    }
                    else -> {
                        onLetGo?.let { it() }
                    }
                }
            }

            scrollDetected = ScrollKind.NONE
            maybeQuietFling = GestureKind.NONE
        }

        return isSingleTap
    }

    private fun callCallbacks(gesture: GestureKind): Boolean {
        when (gesture) {
            GestureKind.FLING_LEFT -> {
                if (canFlingLeft) {
                    onFlingLeft?.let { it() }
                } else {
                    onLetGo?.let { it() }
                }
                return true
            }
            GestureKind.FLING_RIGHT -> {
                if (canFlingRight) {
                    onFlingRight?.let { it() }
                } else {
                    onLetGo?.let { it() }
                }
                return true
            }
            GestureKind.FLING_UP -> {
                if (canFlingUp) {
                    onFlingUp?.let { it() }
                } else {
                    onLetGo?.let { it() }
                }
                return true
            }
            GestureKind.FLING_DOWN -> {
                if (canFlingDown) {
                    onFlingDown?.let { it() }
                } else {
                    onLetGo?.let { it() }
                }
                return true
            }
            else -> return false
        }
    }

    private fun handleFling(e1: MotionEvent, e2: MotionEvent, vxPx: Float, vyPx: Float) {
        val dxm = (e2.x - e1.x) * oneMillimeter
        val dym = (e2.y - e1.y) * oneMillimeter

        val vx = vxPx * oneMillimeter
        val vy = vyPx * oneMillimeter

        when (scrollDetected) {
            ScrollKind.HORIZ -> {
                if (abs(dxm) >= FLING_MIN_DISTANCE && abs(vx) >= FLING_SPEED_THRESHOLD) {
                    detectedGesture = if (vx < 0) {
                        GestureKind.FLING_LEFT
                    } else {
                        GestureKind.FLING_RIGHT
                    }
                }
            }
            ScrollKind.VERT -> {
                if (abs(dym) >= FLING_MIN_DISTANCE && abs(vy) >= FLING_SPEED_THRESHOLD) {
                    detectedGesture = if (vy < 0) {
                        GestureKind.FLING_UP
                    } else {
                        GestureKind.FLING_DOWN
                    }
                }
            }
            else -> Unit
        }
    }

    private fun handleScrollEvent(e1: MotionEvent, e2: MotionEvent) {
        var dx = e2.x - e1.x
        var dy = e2.y - e1.y

        val dxm = dx * oneMillimeter
        val dym = dy * oneMillimeter

        // Under threshold 1, card does not move yet.

        if (scrollDetected == ScrollKind.NONE || scrollDetected == ScrollKind.UNDER_THRESHOLD1) {
            if (abs(dxm) < SCROLL_THRESHOLD1 && abs(dym) < SCROLL_THRESHOLD1) {
                scrollDetected = ScrollKind.UNDER_THRESHOLD1
                return
            } else {
                scrollDetected = ScrollKind.UNDER_THRESHOLD2
            }
        }

        // Under threshold 2, direction may still change.

        if (scrollDetected == ScrollKind.UNDER_THRESHOLD2) {
            scrollDetected = when {
                abs(dxm) >= SCROLL_THRESHOLD2 -> ScrollKind.HORIZ
                abs(dym) >= SCROLL_THRESHOLD2 -> ScrollKind.VERT
                else -> ScrollKind.UNDER_THRESHOLD2
            }
        }

        when {
            scrollDetected == ScrollKind.HORIZ -> dy = 0.0f
            scrollDetected == ScrollKind.VERT -> dx = 0.0f
            dx > dy -> dy = 0.0f
            dx < dy -> dx = 0.0f
        }

        // When card is (slowly) dragged a long way, we call it a quiet fling.

        maybeQuietFling = when (scrollDetected) {
            ScrollKind.HORIZ -> when {
                dxm <= -QUIET_FLING_THRESHOLD -> GestureKind.FLING_LEFT
                dxm >= QUIET_FLING_THRESHOLD -> GestureKind.FLING_RIGHT
                else -> GestureKind.NONE
            }
            ScrollKind.VERT -> when {
                dym <= -QUIET_FLING_THRESHOLD -> GestureKind.FLING_UP
                dym >= QUIET_FLING_THRESHOLD -> GestureKind.FLING_DOWN
                else -> GestureKind.NONE
            }
            else -> GestureKind.NONE
        }

        // If fling is disabled, we still move the card slightly.

        if ((!canFlingLeft && dx < 0) || (!canFlingRight && dx > 0)) {
            dx = scrollDeltaWhenDisallowed(dx)
        }

        if ((!canFlingUp && dy < 0) || (!canFlingDown && dy > 0)) {
            dy = scrollDeltaWhenDisallowed(dy)
        }

        onScroll?.let { it(dx, dy) }
    }

    private fun scrollDeltaWhenDisallowed(motionDelta: Float): Float {
        val delta = DISALLOW_FLING_DELTA / oneMillimeter
        val rel = decel(clamp(abs(motionDelta) * 0.2f / delta), 1.5f)
        return (motionDelta.sign * rel * delta)
    }

    private inner class Listener: GestureDetector.OnGestureListener {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, vxPx: Float, vyPx: Float): Boolean {
            if (e1 != null) handleFling(e1, e2, vxPx, vyPx)
            return false
        }

        override fun onLongPress(e: MotionEvent) {}

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
            if (e1 != null) handleScrollEvent(e1, e2)
            return true
        }

        override fun onShowPress(e: MotionEvent) {}

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            detectedGesture = GestureKind.SINGLE_TAP
            return true
        }
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "MyGestureDetector"

        private const val FLING_SPEED_THRESHOLD = 180.0f // millimeters per second
        private const val FLING_MIN_DISTANCE = 12.0f // millimeters
        private const val QUIET_FLING_THRESHOLD = 24.0f // millimeters
        private const val SCROLL_THRESHOLD1 = 2.0f // millimeters until scroll starts
        private const val SCROLL_THRESHOLD2 = 4.2f // millimeters until direction is locked
        private const val DISALLOW_FLING_DELTA = 5.0f // millimeters
    }
}
