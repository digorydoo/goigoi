package io.github.digorydoo.goigoi.utils

import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.Window
import androidx.compose.ui.window.DialogWindowProvider

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

/**
 * Can be used from a Dialog or ModalBottomSheet to access the window.
 */
fun View.findDialogWindow(): Window? {
    var parent = parent

    while (parent != null) {
        if (parent is DialogWindowProvider) {
            return parent.window
        }
        parent = parent.parent
    }

    return null
}
