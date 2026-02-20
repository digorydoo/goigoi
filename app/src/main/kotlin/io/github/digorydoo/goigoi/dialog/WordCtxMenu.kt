package io.github.digorydoo.goigoi.dialog

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.stats.Stats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WordCtxMenu(private val unyt: Unyt, private val word: Word) {
    enum class Action {
        ADD_TO_MY_WORDS,
        REMOVE_FROM_MY_WORDS,
        RESET_STATS,
        FAKE_GOOD_STATS,
        FAKE_AVG_STATS,
        FAKE_POOR_STATS,
        SET_SUPER_PROGRESSIVE_IDX,
    }

    interface Callback {
        fun onWordCtxMenuAction(action: Action)
    }

    // Note: The image resource will only be used if the item appears in the WordInfoBottomSheet, not inside the
    // context menu!
    class Item(val action: Action, val imgResId: Int, val text: String)

    val items = ArrayList<Item>()
    var callback: Callback? = null

    fun createItems(ctx: Context) {
        val vocab = Vocabulary.getSingleton(ctx)
        val myWordsUnyt = vocab.myWordsUnyt
        vocab.loadUnytIfNecessary(myWordsUnyt, ctx)

        items.clear()

        addItem(Action.RESET_STATS, R.drawable.ic_reset_white_24dp, R.string.item_reset_word_progress, ctx)

        if (BuildConfig.DEBUG) {
            addItem(Action.FAKE_GOOD_STATS, R.drawable.ic_debug_white_24dp, "Fake good stats [DEBUG]")
            addItem(Action.FAKE_AVG_STATS, R.drawable.ic_debug_white_24dp, "Fake average stats [DEBUG]")
            addItem(Action.FAKE_POOR_STATS, R.drawable.ic_debug_white_24dp, "Fake poor stats [DEBUG]")
            addItem(Action.SET_SUPER_PROGRESSIVE_IDX, R.drawable.ic_debug_white_24dp, "Set super prog idx [DEBUG]")
        }

        if (myWordsUnyt.hasWordWithId(word.id)) {
            addItem(Action.REMOVE_FROM_MY_WORDS, R.drawable.ic_trashcan_24dp, R.string.item_remove_word, ctx)
        } else {
            addItem(Action.ADD_TO_MY_WORDS, R.drawable.ic_add_black_24dp, R.string.item_add_word, ctx)
        }
    }

    private fun addItem(action: Action, imgResId: Int, strResId: Int, ctx: Context) {
        items.add(Item(action, imgResId, ctx.getString(strResId)))
    }

    private fun addItem(action: Action, imgResId: Int, text: String) {
        items.add(Item(action, imgResId, text))
    }

    fun itemSelected(itemId: Int, ctx: Context) {
        doAction(items[itemId].action, ctx)
    }

    fun doAction(action: Action, ctx: Context) {
        when (action) {
            Action.ADD_TO_MY_WORDS -> addToMyWords(ctx)
            Action.REMOVE_FROM_MY_WORDS -> removeFromMyWords(ctx)
            Action.RESET_STATS -> resetStats(ctx)
            Action.FAKE_GOOD_STATS -> fakeStats(action, 5, 1, ctx)
            Action.FAKE_AVG_STATS -> fakeStats(action, 4, 2, ctx)
            Action.FAKE_POOR_STATS -> fakeStats(action, 3, 20, ctx)
            Action.SET_SUPER_PROGRESSIVE_IDX -> setSuperProgressiveIdx(ctx)
        }
    }

    private fun addToMyWords(ctx: Context) {
        val vocab = Vocabulary.getSingleton(ctx)
        val myWordsUnyt = vocab.myWordsUnyt

        myWordsUnyt.add(0, word) // put it in front

        if (unyt == myWordsUnyt) {
            callback?.onWordCtxMenuAction(Action.ADD_TO_MY_WORDS)
        }
    }

    private fun removeFromMyWords(ctx: Context) {
        val vocab = Vocabulary.getSingleton(ctx)
        val myWordsUnyt = vocab.myWordsUnyt

        myWordsUnyt.removeAllWithSameId(word)

        if (unyt == myWordsUnyt) {
            callback?.onWordCtxMenuAction(Action.REMOVE_FROM_MY_WORDS)
        }
    }

    private fun resetStats(ctx: Context) {
        val msg = ctx.getString(R.string.confirm_reset_word_progress_msg)
        val okLabel = ctx.getString(R.string.confirm_reset_word_progress_ok)
        val cancelLabel = ctx.getString(android.R.string.cancel)

        MyDlgBuilder.showTwoWayDlg(msg, okLabel, cancelLabel, ctx) { confirm ->
            if (confirm) {
                val stats = Stats.getSingleton(ctx)

                CoroutineScope(Dispatchers.IO).apply {
                    launch {
                        // We're in another Thread.
                        stats.resetWordStatsExpensively(word, unyt, null, null)

                        // Call the callback on the UI thread!
                        Handler(Looper.getMainLooper()).post {
                            callback?.onWordCtxMenuAction(Action.RESET_STATS)
                        }
                    }
                }
            }
        }
    }

    private fun fakeStats(action: Action, numCorrect: Int, numWrong: Int, ctx: Context) {
        val stats = Stats.getSingleton(ctx)

        CoroutineScope(Dispatchers.IO).apply {
            launch {
                // We're in another Thread.
                stats.resetWordStatsExpensively(word, unyt, numCorrect, numWrong)

                // Call the callback on the UI thread!
                Handler(Looper.getMainLooper()).post {
                    callback?.onWordCtxMenuAction(action)
                }
            }
        }
    }

    private fun setSuperProgressiveIdx(ctx: Context) {
        val vocab = Vocabulary.getSingleton(ctx)
        val idx = vocab.allWordFilenames.indexOf(word.filename)

        if (idx < 0) {
            Log.e(TAG, "Cannot determine index of word ${word.id} with file ${word.filename}")
            return
        }

        val stats = Stats.getSingleton(ctx)
        stats.setSuperProgressiveIdx(idx)
        Log.d(TAG, "superProgressiveIdx is now at $idx")
    }

    companion object {
        private const val TAG = "WordCtxMenu"
    }
}
