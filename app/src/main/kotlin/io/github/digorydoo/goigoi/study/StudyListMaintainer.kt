package io.github.digorydoo.goigoi.study

import android.util.Log
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.stats.Stats
import io.github.digorydoo.goigoi.study.MyWordsMaintainer.Companion.MIN_RATING_FOR_REMOVAL_FROM_MY_WORDS
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

class StudyListMaintainer(
    private val delegate: Delegate,
    private val list: MutableList<StudyItem>,
    private val pool: StudyItemPool,
    private val superProgressive: Boolean,
    private val stats: Stats,
) {
    interface Delegate {
        fun loadWordFile(filename: String): Word?
        fun wouldCauseAmbiguityWithMyWordsUnyt(word: Word): Boolean
    }

    private var roundsSinceAddMoreWords = MIN_ROUNDS_FOR_ADD_MORE_WORDS

    // @return true if the word was removed from the list, false otherwise
    fun onAnswerCorrect(word: Word, streak: Int): Boolean {
        roundsSinceAddMoreWords++

        if (!superProgressive) {
            return false
        }

        val filenameAtHead = pool.filenameAtHead
        val progress = stats.getWordStudyProgress(word)
        val rating = if (progress < 1.0f) 0.0f else stats.getWordTotalRating(word)
        val removed: Boolean

        if (progress < 1.0f || rating < MIN_RATING_FOR_REMOVAL_FROM_MY_WORDS) {
            removed = false
        } else if (list.size < MIN_LIST_SIZE_FOR_REMOVE) {
            Log.d(TAG, "Not removing word, because list size is ${list.size}")
            removed = false
        } else if (word.filename == filenameAtHead) {
            Log.d(TAG, "Not removing word, because it is still the super progressive head")
            removed = false
        } else {
            Log.d(TAG, "Removing word from study list: ${word.id}, progress=$progress, rating=$rating")
            list.removeAll { it.word.id == word.id }
            removed = true
        }

        val (add, reason) = when {
            list.size >= IDEAL_LIST_SIZE -> Pair(
                false, // don't add new word, list is too large
                "list size is ${list.size} >= $IDEAL_LIST_SIZE"
            )
            list.size < MIN_LIST_SIZE_FOR_REMOVE -> Pair(
                true, // add new word, because otherwise list would not change any more
                "list size is only ${list.size} < $MIN_LIST_SIZE_FOR_REMOVE"
            )
            roundsSinceAddMoreWords < MIN_ROUNDS_FOR_ADD_MORE_WORDS -> Pair(
                false, // don't add new words, too early
                "roundsSinceAddMoreWords is only $roundsSinceAddMoreWords < $MIN_ROUNDS_FOR_ADD_MORE_WORDS"
            )
            streak < MIN_STREAK_FOR_ADD_MORE_WORDS -> Pair(
                false, // don't add new word, streak not reached yet
                "streak is $streak < $MIN_STREAK_FOR_ADD_MORE_WORDS"
            )
            else -> Pair(
                true, // add new word, streak allows it
                "roundsSinceAddMoreWords is $roundsSinceAddMoreWords, streak is $streak"
            )
        }

        if (add) {
            Log.d(TAG, "Adding more words, because $reason")
            val numNewInList = list.count { stats.getWordStudyProgress(it.word) < 1.0f }

            if (numNewInList > MAX_NEW_IN_LIST_IDEALLY) {
                Log.d(TAG, "Forcing pick from past, because there are already $numNewInList new words in list")
                pool.addOneToListFromFarPast(list, IDEAL_LIST_SIZE)
            } else {
                pool.addOneToListFromHeadOrPast(list, IDEAL_LIST_SIZE)
            }

            roundsSinceAddMoreWords = 0
            ensureSuperProgressiveHeadIsInListAndEarly() // in order not to keep pushing it back
        } else {
            Log.d(TAG, "Not adding more words, because $reason")
        }

        return removed
    }

    fun onAnswerWrong() {
        roundsSinceAddMoreWords++

        if (!superProgressive) {
            return
        }

        // The answer was wrong, so we don't want to burden the user with yet another new word. However, we can add
        // one from the past if there is room.

        val (add, reason) = when {
            list.size >= IDEAL_LIST_SIZE -> Pair(
                false, // don't add new word, list is too large
                "list size is ${list.size} >= $IDEAL_LIST_SIZE"
            )
            roundsSinceAddMoreWords < MIN_ROUNDS_FOR_ADD_MORE_WORDS -> Pair(
                false, // don't add new words, too early
                "roundsSinceAddMoreWords is only $roundsSinceAddMoreWords < $MIN_ROUNDS_FOR_ADD_MORE_WORDS"
            )
            else -> Pair(
                true, // add new word
                "roundsSinceAddMoreWords is $roundsSinceAddMoreWords"
            )
        }

        if (add) {
            pool.addOneToListFromFarPast(list, IDEAL_LIST_SIZE)
            roundsSinceAddMoreWords = 0
        } else {
            Log.d(TAG, "Not adding more words, because $reason")
        }
    }

    fun pushBack(item: StudyItem, answer: Answer) {
        val localNumSkipped = item.localNumSkipped

        val step = when (answer) {
            Answer.CORRECT,
            Answer.CORRECT_EXCEPT_KANA_SIZE,
            -> stepWhenAnswerWasCorrect(item)

            Answer.WRONG -> stepWhenAnswerWasWrong(item)
            Answer.TRIVIAL -> stepWhenAnswerWasTrivial(item)
            Answer.SKIP -> (10.0f + (10 * localNumSkipped)).roundToInt()
            Answer.NONE -> 0
        }

        if (step <= 0) {
            Log.w(TAG, "Unexpected step value: $step")
        } else {
            Log.d(TAG, "Putting item back by $step")
            list.remove(item)
            list.add(min(step, list.size), item)
        }

        if (superProgressive) {
            if (pool.advanceHeadIfPossible(list)) {
                ensureSuperProgressiveHeadIsInListAndEarly()
            }
        }
    }

    private fun stepWhenAnswerWasCorrect(item: StudyItem): Int {
        val seenCount = stats.getWordTotalSeenCount(item.word)
        val correctCount = stats.getWordTotalCorrectCount(item.word)
        val localNumCorrect = item.localNumCorrect
        val score = item.score
        val rnd = Random.nextFloat()

        val step = when {
            seenCount <= 1 -> 1 + (5 * rnd)
            correctCount <= 1 -> 6.0f + (6 * rnd)
            correctCount <= 2 -> 9.0f + (7 * score) + (7 * rnd) + localNumCorrect
            correctCount <= 3 -> 12.0f + (9 * score) + (9 * rnd) + (2 * localNumCorrect)
            correctCount <= 4 -> 13.0f + (10 * score) + (10 * rnd) + (3 * localNumCorrect)
            correctCount <= 5 -> 14.0f + (11 * score) + (11 * rnd) + (4 * localNumCorrect)
            else -> 15.0f + (12 * score) + (12 * rnd) + (5 * localNumCorrect)
        }

        return limitStepForWordAtHead(step.roundToInt(), item.word)
    }

    private fun stepWhenAnswerWasWrong(item: StudyItem): Int {
        val seenCount = stats.getWordTotalSeenCount(item.word)
        val correctCount = stats.getWordTotalCorrectCount(item.word)
        val rnd = Random.nextFloat()

        val step = when {
            seenCount <= 1 -> 1 + (3 * rnd)
            correctCount <= 2 -> 2.0f + (2 * rnd)
            correctCount <= 3 -> 3.0f + (3 * rnd)
            correctCount <= 4 -> 4.0f + (4 * rnd)
            correctCount <= 5 -> 5.0f + (5 * rnd)
            else -> 5.0f + (4 * item.score) + (5 * rnd)
        }

        return limitStepForWordAtHead(step.roundToInt(), item.word)
    }

    private fun stepWhenAnswerWasTrivial(item: StudyItem): Int {
        val seenCount = stats.getWordTotalSeenCount(item.word)
        val rnd = Random.nextFloat()

        if (seenCount > 2) {
            // QAProvider shows each phrase and sentence once with trivial answer. Make sure the word does not
            // reappear too soon when this happens.
            val step = 11.0f + (7 * item.score) + (7 * rnd)
            return limitStepForWordAtHead(step.roundToInt(), item.word)
        } else {
            // The word is still fairly new to the user, so don't push it too far.
            val maxStep = (4.0f + (4 * rnd)).roundToInt()
            var step = 1

            while (step < list.size - 1 && step < maxStep) {
                val nextItem = list[step]
                val nextSeenCount = stats.getWordTotalSeenCount(nextItem.word)

                if (nextSeenCount == 0) {
                    // The answer for nextItem will probably be trivial (except when it picked
                    // SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR). Since the answer for the item we're moving probably
                    // won't be trivial, it's better to leave it here, because we want to avoid long rows of trivial
                    // answers.
                    break
                } else {
                    step++
                }
            }

            return step
        }
    }

    private fun limitStepForWordAtHead(step: Int, word: Word) =
        if (superProgressive && pool.isHeadAt(word)) {
            Log.d(TAG, "Limiting the push step of ${word.id} since it's the super progressive head")
            min(step, MAX_STEP_FOR_WORD_AT_HEAD)
        } else {
            step
        }

    private fun ensureSuperProgressiveHeadIsInListAndEarly() {
        require(superProgressive)
        val filename = pool.filenameAtHead

        if (filename == null) {
            Log.w(TAG, "There is no word at super prog head")
            return
        }

        val itemAtHead = list.find { it.word.filename == filename }
        val wordAtHead = itemAtHead?.word ?: delegate.loadWordFile(filename)

        if (wordAtHead == null) {
            Log.w(TAG, "Failed to load word ahead: $filename")
            return
        }

        if (itemAtHead != null) {
            val idxOfItemAtHead = list.indexOfFirst { it == itemAtHead }
            require(idxOfItemAtHead >= 0) // because itemAtHead comes from list

            if (idxOfItemAtHead <= POS_FOR_NEW_WORD) {
                Log.d(TAG, "Leaving the super prog head (${wordAtHead.id}) at pos $idxOfItemAtHead")
            } else {
                Log.d(TAG, "Moving the super prog head (${wordAtHead.id}) to pos $POS_FOR_NEW_WORD")
                list.removeAll { it == itemAtHead }
                list.add(min(POS_FOR_NEW_WORD, list.size), itemAtHead)
            }
        } else {
            Log.d(TAG, "Adding the super prog head (${wordAtHead.id}) to the study list")
            list.add(min(POS_FOR_NEW_WORD, list.size), StudyItem.create(wordAtHead, stats))
        }

        if (list.size > IDEAL_LIST_SIZE) {
            val last = list.last()
            Log.d(TAG, "Study list has become too large, dropping the last item: ${last.word.id}")
            list.remove(last)
        }
    }

    fun initForNormalMode(unyt: Unyt) {
        require(!superProgressive)
        require(list.isEmpty())

        unyt.forEachWord { w -> list.add(StudyItem.create(w, stats)) }
        list.sortWith { w1, w2 -> w1.score.compareTo(w2.score) }
    }

    fun initForSuperProgressiveMode(myWordsUnyt: Unyt) {
        require(superProgressive)
        require(list.isEmpty())

        myWordsUnyt.forEachWord { list.add(StudyItem.create(it, stats)) }

        if (list.size < MIN_INITIAL_LIST_SIZE) {
            val numNewToAdd = min(MAX_NEW_IN_LIST_IDEALLY, MIN_INITIAL_LIST_SIZE - list.size)

            if (numNewToAdd > 0) {
                Log.d(TAG, "List size is only ${list.size}. Trying to add words from head with max rating.")
                pool.fillListFromHead(
                    list,
                    list.size + numNewToAdd,
                    maxRatingAllowed = MIN_RATING_FOR_REMOVAL_FROM_MY_WORDS,
                )
            }

            if (list.size < MIN_INITIAL_LIST_SIZE) {
                Log.d(TAG, "List size is only ${list.size}. Trying to add words from the past.")
                pool.fillListFromRecentPast(list, MIN_INITIAL_LIST_SIZE)

                if (list.size < MIN_INITIAL_LIST_SIZE) {
                    Log.d(TAG, "List size is only ${list.size}. Trying to add more words from head with max rating.")
                    pool.fillListFromHead(
                        list,
                        MIN_INITIAL_LIST_SIZE,
                        maxRatingAllowed = MIN_RATING_FOR_REMOVAL_FROM_MY_WORDS,
                    )

                    if (list.size < MIN_INITIAL_LIST_SIZE) {
                        Log.d(TAG, "List size is only ${list.size}. Trying to add words from head without max rating.")
                        pool.fillListFromHead(
                            list,
                            MIN_INITIAL_LIST_SIZE,
                            maxRatingAllowed = Float.POSITIVE_INFINITY, // no limit
                        )

                        if (list.size < MIN_INITIAL_LIST_SIZE) {
                            Log.w(TAG, "List is still too short: ${list.size} < $MIN_INITIAL_LIST_SIZE")
                        }
                    }
                }
            }
        }

        list.sortWith { w1, w2 -> w1.score.compareTo(w2.score) }
        ensureSuperProgressiveHeadIsInListAndEarly()
    }

    companion object {
        private const val TAG = "StudyListMaintainer"
        private const val IDEAL_LIST_SIZE = 50
        private const val MIN_INITIAL_LIST_SIZE = 25
        private const val MIN_LIST_SIZE_FOR_REMOVE = 10
        const val POS_FOR_NEW_WORD = 5
        private const val MAX_STEP_FOR_WORD_AT_HEAD = 11
        private const val MIN_ROUNDS_FOR_ADD_MORE_WORDS = 15
        private const val MIN_STREAK_FOR_ADD_MORE_WORDS = POS_FOR_NEW_WORD + 4
        private const val MAX_NEW_IN_LIST_IDEALLY = 5
    }
}
