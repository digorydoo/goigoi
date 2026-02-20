package io.github.digorydoo.goigoi.study

import android.util.Log
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Word
import kotlin.random.Random

class MyWordsMaintainer(private val delegate: Delegate, private val myWordsUnyt: Unyt) {
    interface Delegate {
        fun getWordStudyProgress(word: Word): Float
        fun getWordTotalRating(word: Word): Float
    }

    fun onAnswerCorrect(word: Word) {
        val progress = delegate.getWordStudyProgress(word)
        val rating = if (progress < 1.0f) 0.0f else delegate.getWordTotalRating(word)

        if (progress < 1.0f || rating < MIN_RATING_FOR_REMOVAL_FROM_MY_WORDS) return

        if (myWordsUnyt.numWordsLoaded >= MIN_UNYT_SIZE_FOR_REMOVE && myWordsUnyt.hasWordWithId(word.id)) {
            Log.d(TAG, "Removing word from My Words unyt: ${word.id}, progress=$progress, rating=$rating")
            myWordsUnyt.removeAllWithSameId(word)
        }
    }

    fun onAnswerWrong(word: Word) {
        if (myWordsUnyt.hasWordWithId(word.id)) {
            Log.d(TAG, "Moving word to the top of My Words unyt: ${word.id}")
            myWordsUnyt.removeAllWithSameId(word)
            myWordsUnyt.add(0, word) // put it back at first index
        } else if (wouldCauseAmbiguity(word)) {
            Log.d(TAG, "Not adding word to My Words unyt to prevent ambiguities: ${word.id}")
        } else if (myWordsUnyt.numWordsLoaded < IDEAL_UNYT_SIZE) {
            Log.d(TAG, "Adding word to My Words unyt: ${word.id}")
            myWordsUnyt.add(0, word) // put it back at first index
        } else if (myWordsUnyt.numWordsLoaded > 0 && Random.nextFloat() <= PROBABILITY_FOR_REPLACE) {
            val last = myWordsUnyt.last()
            Log.d(TAG, "Replacing last in My Words unyt (${last.id}) with ${word.id}")
            myWordsUnyt.removeAllWithSameId(last)
            myWordsUnyt.add(0, word)
        }
    }

    fun onNextWord(newWord: Word) {
        if (myWordsUnyt.hasWordWithId(newWord.id)) {
            Log.d(TAG, "Word is already in the My Words unyt")
        } else {
            val progress = delegate.getWordStudyProgress(newWord)
            val rating = delegate.getWordTotalRating(newWord)

            if (progress >= 1.0f && rating >= MIN_RATING_FOR_REMOVAL_FROM_MY_WORDS) {
                Log.d(TAG, "Not adding word to My Words unyt, because progress=$progress, rating=$rating")
            } else if (wouldCauseAmbiguity(newWord)) {
                Log.d(TAG, "Not adding word to My Words unyt to prevent ambiguities")
            } else if (myWordsUnyt.numWordsLoaded < IDEAL_UNYT_SIZE) {
                Log.d(TAG, "My words unyt has empty slot (loaded ${myWordsUnyt.numWordsLoaded}), adding word")
                myWordsUnyt.add(0, newWord)
            } else {
                val toBeRemoved = findWordToRemoveFromMyWordsUnyt()

                if (toBeRemoved == null) {
                    Log.d(TAG, "Checked My Words unyt if any words could be removed, but found none")
                } else {
                    val rmProgress = delegate.getWordStudyProgress(toBeRemoved)
                    val rmRating = delegate.getWordTotalRating(toBeRemoved)
                    Log.d(
                        TAG,
                        "Removing word from My Words unyt: ${toBeRemoved.id}, progress=$rmProgress, rating=$rmRating"
                    )
                    myWordsUnyt.removeAllWithSameId(toBeRemoved)

                    if (wouldCauseAmbiguity(newWord)) {
                        Log.d(TAG, "Not adding word to My Words unyt to prevent ambiguities")
                    } else {
                        Log.d(TAG, "Adding word to My Words unyt")
                        myWordsUnyt.add(0, newWord)
                    }
                }
            }
        }
    }

    fun wouldCauseAmbiguity(word: Word): Boolean {
        return myWordsUnyt.any { other ->
            word.id == other.id || word.links.any { it.wordId == other.id && it.canCauseAmbiguity }
        }
    }

    private fun findWordToRemoveFromMyWordsUnyt(): Word? {
        var toBeRemoved: Word? = null

        myWordsUnyt.forEachWord { w ->
            val shouldRemove = when {
                toBeRemoved != null -> false
                delegate.getWordStudyProgress(w) < 1.0f -> false
                delegate.getWordTotalRating(w) < MIN_RATING_FOR_REMOVAL_FROM_MY_WORDS -> false
                else -> true
            }

            if (shouldRemove) {
                toBeRemoved = w
            }
        }

        return toBeRemoved
    }

    companion object {
        const val MIN_RATING_FOR_REMOVAL_FROM_MY_WORDS = 0.8f

        private const val TAG = "MyWordsMaintainer"
        private const val IDEAL_UNYT_SIZE = 50
        private const val MIN_UNYT_SIZE_FOR_REMOVE = 25 // not too high, because no new words can be added when full
        private const val PROBABILITY_FOR_REPLACE = 0.2f
    }
}
