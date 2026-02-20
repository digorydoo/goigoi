package io.github.digorydoo.goigoi.utils

import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View

fun View.addHapticFeedback() {
    isHapticFeedbackEnabled = true

    setOnTouchListener { v, event ->
        val effect = when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> HapticFeedbackConstants.VIRTUAL_KEY
            MotionEvent.ACTION_UP -> HapticFeedbackConstants.VIRTUAL_KEY_RELEASE
            else -> null
        }

        effect?.let { performHapticFeedback(it) }

        if (event?.actionMasked == MotionEvent.ACTION_UP) {
            v.performClick() // call onClick listener
        }

        true // eat event
    }
}
