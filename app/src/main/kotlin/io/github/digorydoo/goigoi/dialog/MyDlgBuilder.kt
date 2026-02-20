package io.github.digorydoo.goigoi.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.ResUtils

object MyDlgBuilder {
    fun showTwoWayDlg(
        message: String,
        confirmBtnCaption: String,
        denyBtnCaption: String,
        ctx: Context,
        reply: (confirm: Boolean) -> Unit,
    ): AlertDialog {
        val wrapper = ResUtils.getDialogThemeWrapper(ctx)
        val builder = AlertDialog.Builder(wrapper)
        builder.setMessage(message)

        builder.setPositiveButton(confirmBtnCaption) { dlg, _ ->
            dlg.dismiss()
            reply(true)
        }
        builder.setNegativeButton(denyBtnCaption) { dlg, _ ->
            dlg.dismiss()
            reply(false)
        }

        return builder.create().apply { show() }
    }

    fun showRateUsDlg(ctx: Context, reply: (confirm: Boolean) -> Unit): AlertDialog {
        val message = ContextCompat.getString(ctx, R.string.rate_us_message)
        val okBtnCaption = ContextCompat.getString(ctx, R.string.rate_us_btn)
        val cancelBtnCaption = ContextCompat.getString(ctx, R.string.not_now_btn)
        return showTwoWayDlg(message, okBtnCaption, cancelBtnCaption, ctx, reply)
    }

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
