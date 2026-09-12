package io.github.digorydoo.goigoi.legacy.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.ResUtils

object MyDlgBuilder {
    fun showHintDlg(stringResId: Int, ctx: Context, onDismiss: (() -> Unit)? = null): AlertDialog {
        val wrapper = ResUtils.getDialogThemeWrapper(ctx)
        val builder = AlertDialog.Builder(wrapper)

        val message = ContextCompat.getString(ctx, stringResId)
        val gotIt = ContextCompat.getString(ctx, R.string.got_it)

        builder.setMessage(message)

        builder.setPositiveButton(gotIt) { dlg, _ ->
            dlg.dismiss()
            onDismiss?.invoke()
        }

        builder.setCancelable(false) // otherwise we would need to implement onDismiss
        return builder.create().apply { show() }
    }
}
