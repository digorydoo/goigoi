package io.github.digorydoo.goigoi.activity.unyt

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import io.github.digorydoo.goigoi.composables.list.WordListItemData
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.stats.Stats
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class UnytActivityTasks(
    private val unyt: Unyt,
    private val vocab: Vocabulary,
    private val stats: Stats,
    private val lifecycleScope: LifecycleCoroutineScope,
) {
    private val itemDataMutex = Mutex()
    private val itemDataRequests = mutableMapOf<Word, Deferred<WordListItemData>>()
    private val itemDataLoadingMutex = Mutex()

    suspend fun getItemData(word: Word): WordListItemData {
        // Whenever accessing itemDataRequest, we need to obtain the lock of itemDataMutex
        val deferred = itemDataMutex.withLock {
            // If we're already loading item data for the same word, getOrPut will just return the `Deferred` of
            // the ongoing request.
            itemDataRequests.getOrPut(word) {
                // The async extension function immediately returns with a `Deferred` that will later hold the result.
                lifecycleScope.async(Dispatchers.IO) {
                    // We want item loading to happen strictly one-by-one, as this looks better in the UI.
                    // This is achieved with another mutex.
                    itemDataLoadingMutex.withLock {
                        try {
                            WordListItemData.create(word, vocab, stats)
                        } finally {
                            itemDataMutex.withLock {
                                itemDataRequests -= word // same as remove(word), but no warning about unused result
                            }
                        }
                    }
                }
            }
        }
        return deferred.await()
    }

    suspend fun fakeStats(word: Word, numCorrect: Int, numWrong: Int) {
        withContext(Dispatchers.IO) {
            stats.resetWordStatsExpensively(word, unyt, numCorrect, numWrong)
        }
    }

    suspend fun resetStats(word: Word) {
        withContext(Dispatchers.IO) {
            stats.resetWordStatsExpensively(word, unyt, null, null)
        }
    }

    fun setSuperProgIdx(word: Word) {
        val idx = vocab.allWordFilenames.indexOf(word.filename)

        if (idx < 0) {
            Log.e(TAG, "Cannot determine index of word with file ${word.filename}")
            return
        }

        stats.setSuperProgressiveIdx(idx)
        Log.d(TAG, "superProgressiveIdx is now at $idx")
    }

    companion object {
        private const val TAG = "UnytActvTasks"
    }
}
