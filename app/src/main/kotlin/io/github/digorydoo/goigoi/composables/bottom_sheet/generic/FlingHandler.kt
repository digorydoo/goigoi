package io.github.digorydoo.goigoi.composables.bottom_sheet.generic

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.withFrameNanos
import ch.digorydoo.kutils.math.clamp

class FlingHandler(
    private val scrollState: ScrollState,
    private val minSpeed: Float, // pixels per second
    private val maxSpeed: Float, // pixels per second
    private val stopVelocity: Float, // pixels per second
    private val deceleration: Float, // pixels per (second * second)
    private val getMinPos: () -> Int,
    private val getMaxPos: () -> Int,
    private val onFlingEnded: () -> Unit,
) {
    suspend fun fling(desiredSpeed: Float) {
        abort()

        if (scrollState.maxValue == Int.MAX_VALUE) {
            Log.w(TAG, "Fling suppressed, because layout has not been measured yet")
            return // should not happen in practice
        }

        if (desiredSpeed > -minSpeed && desiredSpeed < minSpeed) {
            // Too slow for an actual fling
            onFlingEnded()
            return
        }

        var speed = clamp(desiredSpeed, -maxSpeed, maxSpeed)
        var prevTime = 0L

        val minPos = getMinPos()
        val maxPos = getMaxPos()
        var borderReached = Int.MAX_VALUE

        try {
            scrollState.scroll {
                while (speed < -stopVelocity || speed > stopVelocity) {
                    withFrameNanos { time ->
                        if (prevTime != 0L) {
                            val dt = (time - prevTime) / 1e9f // convert nanos to seconds

                            speed =
                                if (speed > 0f) (speed - deceleration * dt).coerceAtLeast(0f)
                                else (speed + deceleration * dt).coerceAtMost(0f)

                            val distance = speed * dt
                            val consumed = scrollBy(distance)

                            if (scrollState.value <= minPos) {
                                borderReached = minPos
                                speed = 0f // abort
                            } else if (scrollState.value >= maxPos) {
                                borderReached = maxPos
                                speed = 0f // abort
                            } else if (consumed != distance) {
                                speed = 0f // abort
                            }
                        }
                        prevTime = time
                    }
                }
            }
        } catch (_: Exception) {
            // Fling aborted
        }

        if (borderReached < Int.MAX_VALUE) {
            // We probably have overshot. Bounce back!
            scrollState.animateScrollTo(borderReached)
        }

        onFlingEnded()
    }

    suspend fun abort() {
        try {
            // Invoking scrollTo should cancel a previous call to scroll()
            scrollState.scrollTo(scrollState.value)
        } catch (_: Exception) {
            Log.w(TAG, "Failed to abort fling. Is a scroll operation with higher priority already taking place?")
        }
    }

    companion object {
        private const val TAG = "FlingHandler"
    }
}
