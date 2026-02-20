package io.github.digorydoo.goigoi.dialog

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.stats.Stats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max

class UnytCtxMenu(val unyt: Unyt) {
    enum class Action {
        FAKE_GOOD_STATS,
        FAKE_AVG_STATS,
        FAKE_POOR_STATS,
        RESET_STATS,
        SET_SUPER_PROGRESSIVE_IDX,
    }

    interface Callback {
        fun onUnytCtxMenuAction(action: Action)
    }

    class Item(val action: Action, val text: String)

    val items = ArrayList<Item>()
    var callback: Callback? = null

    fun createItems(ctx: Context) {
        items.clear()

        addItem(Action.RESET_STATS, R.string.item_reset_unyt_progress, ctx)

        if (BuildConfig.DEBUG) {
            addItem(Action.FAKE_GOOD_STATS, "Fake good stats [DEBUG]")
            addItem(Action.FAKE_AVG_STATS, "Fake average stats [DEBUG]")
            addItem(Action.FAKE_POOR_STATS, "Fake poor stats [DEBUG]")
            addItem(Action.SET_SUPER_PROGRESSIVE_IDX, "Set super prog idx [DEBUG]")
        }
    }

    private fun addItem(action: Action, strResId: Int, ctx: Context) {
        items.add(Item(action, ctx.getString(strResId)))
    }

    private fun addItem(action: Action, text: String) {
        items.add(Item(action, text))
    }

    fun itemSelected(itemId: Int, ctx: Context) {
        doAction(items[itemId].action, ctx)
    }

    private fun doAction(action: Action, ctx: Context) {
        when (action) {
            Action.FAKE_GOOD_STATS -> fakeStats(action, 5, 1, ctx)
            Action.FAKE_AVG_STATS -> fakeStats(action, 4, 2, ctx)
            Action.FAKE_POOR_STATS -> fakeStats(action, 3, 20, ctx)
            Action.RESET_STATS -> resetStats(ctx)
            Action.SET_SUPER_PROGRESSIVE_IDX -> setSuperProgressiveIdx(ctx)
        }
    }

    private fun fakeStats(action: Action, numCorrect: Int, numWrong: Int, ctx: Context) {
        val stats = Stats.getSingleton(ctx)

        val vocab = Vocabulary.getSingleton(ctx)
        vocab.loadUnytIfNecessary(unyt, ctx)

        CoroutineScope(Dispatchers.IO).apply {
            launch {
                // Here we're in the new thread.

                unyt.forEachWord { word ->
                    stats.resetWordStatsExpensively(word, unyt, numCorrect, numWrong)
                }

                // The callback will update the Views, so we need to call it on the UI thread (== main looper).
                Handler(Looper.getMainLooper()).post {
                    callback?.onUnytCtxMenuAction(action)
                }
            }
        }
    }

    private fun resetStats(ctx: Context) {
        val numWordsInUnit = max(unyt.numWordsLoaded, unyt.numWordsAvailable)

        var msg = ctx.getString(R.string.confirm_reset_unyt_progress_msg)
        msg = msg.replace("\${N}", "" + numWordsInUnit)

        val okLabel = ctx.getString(R.string.confirm_reset_unyt_progress_ok)
        val cancelLabel = ctx.getString(android.R.string.cancel)

        MyDlgBuilder.showTwoWayDlg(msg, okLabel, cancelLabel, ctx) { confirm ->
            if (confirm) {
                val stats = Stats.getSingleton(ctx)

                val vocab = Vocabulary.getSingleton(ctx)
                vocab.loadUnytIfNecessary(unyt, ctx)

                CoroutineScope(Dispatchers.IO).apply {
                    launch {
                        // Here we're in the new thread.
                        stats.resetUnytStatsExpensively(unyt)

                        // Call the callback on the UI thread!
                        Handler(Looper.getMainLooper()).post {
                            callback?.onUnytCtxMenuAction(Action.RESET_STATS)
                        }
                    }
                }
            }
        }
    }

    private fun setSuperProgressiveIdx(ctx: Context) {
        val vocab = Vocabulary.getSingleton(ctx)
        val filename = unyt.wordFilenames.getOrNull(0)

        if (filename == null) {
            Log.e(TAG, "The unyt ${unyt.id} appears to be empty")
            return
        }

        val idx = vocab.allWordFilenames.indexOf(filename)

        if (idx < 0) {
            Log.e(TAG, "Cannot determine index of word with file $filename")
            return
        }

        val stats = Stats.getSingleton(ctx)
        stats.setSuperProgressiveIdx(idx)
        Log.d(TAG, "superProgressiveIdx is now at $idx")
    }

    companion object {
        private const val TAG = "UnytCtxMenu"
    }
}
