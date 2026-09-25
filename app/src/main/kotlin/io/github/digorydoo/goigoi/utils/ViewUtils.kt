package io.github.digorydoo.goigoi.utils

import android.view.View
import android.view.Window
import androidx.compose.ui.window.DialogWindowProvider

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
