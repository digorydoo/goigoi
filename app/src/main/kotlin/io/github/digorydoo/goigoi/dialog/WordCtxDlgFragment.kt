package io.github.digorydoo.goigoi.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.utils.ResUtils

class WordCtxDlgFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val ctx = requireContext()
        val vocab = Vocabulary.getSingleton(ctx)
        val activity = requireActivity()

        val args = requireArguments()
        val wordId = args.getString("wordId")!!
        val unytId = args.getString("unytId")!!

        val word = vocab.findWordById(wordId)!!
        val unyt = vocab.findUnytById(unytId)!!

        val wrapper = ResUtils.getDialogThemeWrapper(ctx)
        val builder = AlertDialog.Builder(wrapper)

        val menu = WordCtxMenu(unyt, word).apply {
            createItems(ctx)
            callback = activity as? WordCtxMenu.Callback
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
        const val TAG = "WordCtxDlgFragment"

        fun createNewFragment(word: Word, unyt: Unyt): DialogFragment {
            val fragment: DialogFragment = WordCtxDlgFragment()

            val args = Bundle()
            args.putString("wordId", word.id)
            args.putString("unytId", unyt.id)
            fragment.arguments = args

            return fragment
        }
    }
}
