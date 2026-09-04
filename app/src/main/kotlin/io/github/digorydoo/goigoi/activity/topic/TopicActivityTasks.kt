package io.github.digorydoo.goigoi.activity.topic

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import io.github.digorydoo.goigoi.components.list.UnytListItemData
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.stats.Stats
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TopicActivityTasks(
    private val vocab: Vocabulary,
    private val stats: Stats,
    private val ctx: Context,
    private val lifecycleScope: LifecycleCoroutineScope,
) {
    private val itemDataMutex = Mutex()
    private val itemDataRequests = mutableMapOf<Unyt, Deferred<UnytListItemData>>()
    private val itemDataLoadingMutex = Mutex()

    private val unytWordsMutex = Mutex()
    private val unytWordsRequest = mutableMapOf<Unyt, Deferred<Unit>>()

    suspend fun getItemData(unyt: Unyt, isMyWordsUnyt: Boolean): UnytListItemData {
        // Whenever accessing itemDataRequest, we need to obtain the lock of itemDataMutex
        val deferred = itemDataMutex.withLock {
            // If we're already loading item data for the same unyt, getOrPut will just return the `Deferred` of
            // the ongoing request.
            itemDataRequests.getOrPut(unyt) {
                // The async extension function immediately returns with a `Deferred` that will later hold the result.
                lifecycleScope.async(Dispatchers.IO) {
                    // We want item loading to happen strictly one-by-one, as this looks better in the UI.
                    // This is achieved with another mutex.
                    itemDataLoadingMutex.withLock {
                        try {
                            // If we're loading the unyt's words, we need to wait.
                            unytWordsMutex.withLock { unytWordsRequest[unyt] }?.await()

                            // The c'tor of UnytListItemData is doing the heavy lifting of obtaining the stats.
                            // If stats cache is stale, it may even load the unyt.
                            UnytListItemData(unyt, isMyWordsUnyt, vocab, stats, ctx)
                        } finally {
                            itemDataMutex.withLock {
                                itemDataRequests -= unyt // same as remove(unyt), but no warning about unused result
                            }
                        }
                    }
                }
            }
        }
        return deferred.await()
    }

    private suspend fun loadUnytWordsIfNecessary(unyt: Unyt) {
        val deferred = unytWordsMutex.withLock {
            unytWordsRequest.getOrPut(unyt) {
                lifecycleScope.async(Dispatchers.IO) {
                    try {
                        // If we're loading item data for the same unyt, we need to wait.
                        itemDataMutex.withLock { itemDataRequests[unyt] }?.await()

                        // Even if item data was loading, we need to load the unyt's words, because loading item data
                        // does not need the words when stats are cached.
                        vocab.loadUnytIfNecessary(unyt)
                    } finally {
                        unytWordsMutex.withLock {
                            unytWordsRequest -= unyt
                        }
                    }
                }
            }
        }
        return deferred.await()
    }

    suspend fun fakeStats(unyt: Unyt, numCorrect: Int, numWrong: Int) {
        loadUnytWordsIfNecessary(unyt)
        withContext(Dispatchers.IO) {
            unyt.forEachWord { word ->
                stats.resetWordStatsExpensively(word, unyt, numCorrect, numWrong)
            }
        }
    }

    suspend fun resetStats(unyt: Unyt) {
        loadUnytWordsIfNecessary(unyt)
        withContext(Dispatchers.IO) {
            stats.resetUnytStatsExpensively(unyt)
        }
    }

    fun setSuperProgIdx(unyt: Unyt) {
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

        stats.setSuperProgressiveIdx(idx)
        Log.d(TAG, "superProgressiveIdx is now at $idx")
    }

    companion object {
        private const val TAG = "TopicActvTasks"
    }
}
