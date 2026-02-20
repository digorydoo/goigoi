package io.github.digorydoo.goigoi.bottom_sheet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.FragmentManager
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.dialog.WordCtxMenu
import io.github.digorydoo.goigoi.dialog.WordCtxMenu.Action
import io.github.digorydoo.goigoi.utils.DeviceUtils
import io.github.digorydoo.goigoi.view.BottomSheetScrollView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WordInfoBottomSheet: BottomSheetDialogFragment() {
    private lateinit var bindings: Bindings
    private lateinit var values: Values
    private lateinit var unyt: Unyt
    private lateinit var word: Word

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val ctx = requireContext()

        val args = arguments
        val wordId = args?.getString("wordId")!!
        val unytId = args.getString("unytId")!!

        val vocab = Vocabulary.getSingleton(ctx)

        unyt = vocab.findUnytById(unytId)!!
        vocab.loadUnytIfNecessary(unyt, ctx)

        word = vocab.findWordById(wordId)!!

        val rootView = inflater.inflate(R.layout.word_info_bottom_sheet, container, false) as BottomSheetScrollView
        bindings = Bindings(rootView)
        values = Values(ctx)

        val activity = requireActivity()
        val screenSize = DeviceUtils.getScreenSize(activity)
        val orient = DeviceUtils.getOrientation(activity)

        values.contentLRMargin(screenSize, orient).let { p ->
            rootView.setPadding(p, 0, p, 0)
        }

        rootView.dialog = dialog as? BottomSheetDialog

        setupBehaviour(rootView)

        val delegate = object: SheetContent.Delegate {
            override fun onWordCtxMenuAction(action: Action) {
                (activity as? WordCtxMenu.Callback)?.onWordCtxMenuAction(action)
            }

            override fun dismissSheet() {
                dismiss()
            }

            override fun showInBrowser(uri: Uri) {
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

        val sheetContent = SheetContent(bindings, word, unyt, delegate)
        sheetContent.insertContent(inflater, ctx)
        return rootView
    }

    private fun setupBehaviour(scrollView: ScrollView) {
        (dialog as? BottomSheetDialog)?.let { dlg ->
            dlg.behavior.apply {
                peekHeight = values.peekHeight
                isFitToContents = true // true=make sheet as high as its content
                skipCollapsed = true // true=immediately close sheet on fling down
            }

            scrollView.viewTreeObserver.addOnScrollChangedListener {
                // Prevent bottom sheet from being dragged while our nested scrollView
                // is not at its top.
                dlg.behavior.isDraggable = scrollView.scrollY == 0
            }
        }
    }

    companion object {
        private const val TAG = "WordInfoBottomSheet"

        fun show(unyt: Unyt, word: Word, mgr: FragmentManager, onClose: (() -> Unit)?) {
            if (mgr.findFragmentByTag(TAG) != null) {
                // Do not open another bottom sheet when it is already open. We may come here when
                // the user double-clicks the infoBtn on ProgStudyActivity, etc. Call onClose from a
                // handler to make sure the order of calls stays the same.

                if (onClose != null) {
                    Handler(Looper.getMainLooper()).post {
                        onClose()
                    }
                }
            } else {
                if (onClose != null) {
                    // Add a listener to detect when the sheet is closed.

                    mgr.addOnBackStackChangedListener(
                        object: FragmentManager.OnBackStackChangedListener {
                            var count = 0

                            override fun onBackStackChanged() {
                                // We come here twice: once when the sheet is opened,
                                // once when it is closed.
                                if (++count >= 2) {
                                    mgr.removeOnBackStackChangedListener(this)
                                    onClose()
                                }
                            }
                        }
                    )
                }

                val fta = mgr.beginTransaction()
                fta.addToBackStack(null)
                val sheet = createNewSheet(unyt, word)
                sheet.show(fta, TAG) // also commits the transaction
            }
        }

        private fun createNewSheet(unyt: Unyt, word: Word): WordInfoBottomSheet {
            val sheet = WordInfoBottomSheet()
            val args = Bundle()
            args.putString("unytId", unyt.id)
            args.putString("wordId", word.id)
            sheet.arguments = args
            return sheet
        }
    }
}
