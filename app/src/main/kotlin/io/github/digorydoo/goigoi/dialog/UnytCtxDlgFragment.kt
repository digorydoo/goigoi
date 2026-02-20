package io.github.digorydoo.goigoi.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.utils.ResUtils

class UnytCtxDlgFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val ctx = requireContext()
        val vocab = Vocabulary.getSingleton(ctx)
        val activity = requireActivity()

        val args = requireArguments()
        val unytId = args.getString("unytId")!!
        val unyt = vocab.findUnytById(unytId)!!

        val wrapper = ResUtils.getDialogThemeWrapper(ctx)
        val builder = AlertDialog.Builder(wrapper)

        val menu = UnytCtxMenu(unyt).apply {
            createItems(ctx)
            callback = activity as? UnytCtxMenu.Callback
        }

        val items = menu.items
            .map { item -> item.text }
            .toTypedArray()

        builder.setItems(items) { _: DialogInterface?, itemId: Int ->
            menu.itemSelected(itemId, ctx)
        }

        return builder.create()
    }

    companion object {
        /**
         * The fragment's TAG is also used with FragmentManager.findFragmentByTag, so make sure it's unique.
         */
        const val TAG = "UnytCtxDlgFragment"

        fun createNewFragment(unyt: Unyt): DialogFragment {
            val fragment: DialogFragment = UnytCtxDlgFragment()

            val args = Bundle()
            args.putString("unytId", unyt.id)
            fragment.arguments = args

            return fragment
        }
    }
}
