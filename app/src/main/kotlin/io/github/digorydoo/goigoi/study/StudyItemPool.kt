package io.github.digorydoo.goigoi.study

import android.util.Log
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.stats.Stats
import io.github.digorydoo.goigoi.study.MyWordsMaintainer.Companion.MIN_RATING_FOR_REMOVAL_FROM_MY_WORDS
import io.github.digorydoo.goigoi.study.StudyListMaintainer.Companion.POS_FOR_NEW_WORD
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.randomIntBiasedTowardsEnd
import kotlin.random.Random

class StudyItemPool(
    private val delegate: Delegate,
    private val allWordFilenames: List<String>,
    private val stats: Stats,
) {
    interface Delegate {
        fun loadWordFile(filename: String): Word?
        fun wouldCauseAmbiguityWithMyWordsUnyt(word: Word): Boolean
    }

    private val superProgressiveIdx get() = stats.superProgressiveIdx
    val filenameAtHead get() = allWordFilenames.getOrNull(superProgressiveIdx)

    fun isHeadAt(word: Word) =
        word.filename.isNotEmpty() && word.filename == filenameAtHead

    fun advanceHeadIfPossible(list: List<StudyItem>): Boolean {
        var didAdvance = false
        var idx = superProgressiveIdx

        while (idx < allWordFilenames.size) {
            val word = findOrLoadWordAtIdx(idx, list) ?: break
            val progress = stats.getWordStudyProgress(word)
            val rating = stats.getWordTotalRating(word)

            if (progress < 1.0f || rating < MIN_RATING_FOR_REMOVAL_FROM_MY_WORDS) {
                // Advancing cannot continue. The word will be grabbed from the pool with the normal mechanism.
                break
            }

            Log.d(TAG, "Word at $idx is ${word.id}, advancing since progress=$progress, rating=$rating")
            stats.setSuperProgressiveIdx(++idx)
            didAdvance = true
        }

        if (didAdvance) {
            Log.d(TAG, "Advanced superProgressiveIdx to $idx")
        } else {
            Log.d(TAG, "superProgressiveIdx stays at $idx")
        }

        return didAdvance
    }

    private fun findOrLoadWordAtIdx(idx: Int, list: List<StudyItem>): Word? {
        val filename = allWordFilenames.getOrNull(idx)

        if (filename == null) {
            Log.w(TAG, "There is no word at index $idx")
            return null
        }

        val item = list.find { it.word.filename == filename }
        val word = item?.word ?: delegate.loadWordFile(filename)

        if (word == null) {
            Log.w(TAG, "Failed to load word: $filename")
            return null
        }

        return word
    }

    fun addOneToListFromHeadOrPast(list: MutableList<StudyItem>, maxListSize: Int) {
        val idx = superProgressiveIdx
        val recentIdx = idx - NUM_WORDS_CONSIDERED_RECENT

        if (recentIdx <= MAX_SEEK_AHEAD) {
            // Too few words to pick from past
            addOneToListFromHead(list, maxListSize)
            return
        }

        // The probability of picking a word from past increases with the index until some maximum is reached.
        val probability = MAX_PROBABILITY_TO_PICK_WORD_FROM_PAST *
            clamp(idx.toFloat() / INDEX_OF_MAX_PROBABILITY_TO_PICK_WORD_FROM_PAST)

        if (Random.nextFloat() > probability) {
            Log.d(TAG, "Rolled the dice at probability $probability and got: fetching from words ahead")
            addOneToListFromHead(list, maxListSize)
        } else {
            Log.d(TAG, "Rolled the dice at probability $probability and got: adding one from far past")
            addOneToListFromFarPast(list, maxListSize)
        }
    }

    fun fillListFromHead(list: MutableList<StudyItem>, maxListSize: Int, maxRatingAllowed: Float) {
        addWords(
            list,
            maxListSize = maxListSize,
            startIdx = superProgressiveIdx, // not +1, in case the word at head is missing (ambiguity, etc.)
            maxRatingAllowed = maxRatingAllowed,
            addInFront = false,
            pickOneOnly = false,
        )
    }

    fun fillListFromRecentPast(list: MutableList<StudyItem>, maxListSize: Int) {
        val recentIdx = superProgressiveIdx - NUM_WORDS_CONSIDERED_RECENT

        if (recentIdx <= MAX_SEEK_AHEAD) {
            Log.d(TAG, "Refusing to fetch from past, because not enough past words available")
            return
        }

        addWords(
            list,
            maxListSize = maxListSize,
            startIdx = recentIdx - MAX_SEEK_AHEAD, // i.e. we're not reaching the words considered recent
            // Only include words that barely passed the rating for removal.
            maxRatingAllowed = MIN_RATING_FOR_REMOVAL_FROM_MY_WORDS + 0.1f,
            addInFront = false,
            pickOneOnly = false,
        )
    }

    private fun addOneToListFromHead(list: MutableList<StudyItem>, maxListSize: Int) {
        addWords(
            list,
            maxListSize = maxListSize,
            startIdx = superProgressiveIdx, // not +1, in case the word at head is missing (ambiguity, etc.)
            maxRatingAllowed = Float.POSITIVE_INFINITY, // no limit, but we're looking for the best candidate
            addInFront = true,
            pickOneOnly = true, // find best candidate
        )
    }

    fun addOneToListFromFarPast(list: MutableList<StudyItem>, maxListSize: Int) {
        val recentIdx = superProgressiveIdx - NUM_WORDS_CONSIDERED_RECENT

        if (recentIdx <= MAX_SEEK_AHEAD) {
            Log.d(TAG, "Refusing to fetch from past, because not enough past words available")
            return
        }

        val startIdx = randomIntBiasedTowardsEnd(recentIdx - MAX_SEEK_AHEAD, 0.42f) // larger bias = closer to recent

        addWords(
            list,
            maxListSize = maxListSize,
            startIdx = startIdx,
            maxRatingAllowed = Float.POSITIVE_INFINITY, // no limit, but we're looking for the best candidate
            addInFront = true,
            pickOneOnly = true, // find best candidate
        )
    }

    private fun addWords(
        list: MutableList<StudyItem>,
        maxListSize: Int,
        startIdx: Int,
        maxRatingAllowed: Float,
        addInFront: Boolean,
        pickOneOnly: Boolean,
    ) {
        if (list.size >= maxListSize) {
            Log.d(TAG, "Not adding more words, because the list has already ${list.size} words")
            return
        }

        Log.d(TAG, "Fetching words starting at idx=$startIdx, pickOneOnly=$pickOneOnly")

        fun listHas(wordFilename: String) = list.any { it.word.filename == wordFilename }

        fun wouldCauseAmbiguity(w: Word): Boolean {
            val internalAmbiguity = list.any { other ->
                w.id == other.word.id || w.links.any { it.wordId == other.word.id && it.canCauseAmbiguity }
            }
            return internalAmbiguity || delegate.wouldCauseAmbiguityWithMyWordsUnyt(w)
        }

        var added = 0

        fun add(w: Word) {
            if (addInFront) {
                list.add(POS_FOR_NEW_WORD, StudyItem.create(w, stats))
            } else {
                list.add(StudyItem.create(w, stats))
            }
            added++
        }

        var idx = startIdx
        var alreadyInList = 0
        var alreadyLearnt = 0
        var skippedAmbiguity = 0
        var failed = 0

        class Candidate(val word: Word, val idx: Int, val progress: Float, val rating: Float)

        var candidatesSeen = 0
        var candidate: Candidate? = null
        val startMillis = System.currentTimeMillis()

        while (list.size < maxListSize) {
            val filename = allWordFilenames.getOrNull(idx++)

            if (filename == null) {
                Log.w(TAG, "Aborting, because no more words available, idx=${idx - 1}")
                break
            } else if (listHas(filename)) {
                alreadyInList++
            } else {
                val word = delegate.loadWordFile(filename)

                if (word == null) {
                    Log.w(TAG, "Could not load word file: $filename")
                    failed++
                } else {
                    val progress = stats.getWordStudyProgress(word)
                    val rating = if (progress < 1.0f) 0.0f else stats.getWordTotalRating(word)

                    if (progress >= 1.0f && rating >= maxRatingAllowed) {
                        alreadyLearnt++
                    } else if (wouldCauseAmbiguity(word)) {
                        skippedAmbiguity++
                    } else if (pickOneOnly) {
                        candidatesSeen++
                        val isBetter = candidate == null || progress < candidate.progress || rating < candidate.rating

                        if (isBetter) {
                            candidate = Candidate(word, idx - 1, progress, rating)
                        }

                        if (candidate.progress < 1.0f) break
                    } else {
                        add(word)
                    }
                }

                // Limit seek ahead, to prevent reaching recent words when fetching from the past.
                if (idx - startIdx >= MAX_SEEK_AHEAD) break

                // Limit time spent for searching candidates.
                if (candidate != null) {
                    if (System.currentTimeMillis() - startMillis > MAX_MILLIS_FOR_SEEK_AHEAD) break
                }
            }
        }

        if (pickOneOnly) {
            require(added == 0)

            if (candidate == null) {
                Log.d(TAG, "No suitable candidates found")
                return
            }

            add(candidate.word)
            Log.d(TAG, "Picked word at idx ${candidate.idx} among $candidatesSeen candidates")
            Log.d(TAG, "   lvl=${candidate.word.level}, progress=${candidate.progress}, rating=${candidate.rating}")
        } else {
            require(candidate == null)
            Log.d(TAG, "Filled $added slots")
        }

        val numSkipped = alreadyInList + alreadyLearnt + skippedAmbiguity + failed

        if (numSkipped > 0) {
            val reasons = arrayOf(
                Pair(alreadyInList, "already in study list"),
                Pair(alreadyLearnt, "already learnt"),
                Pair(skippedAmbiguity, "ambiguous"),
                Pair(failed, "FAILED"),
            ).mapNotNull { (count, msg) ->
                when (count) {
                    0 -> null
                    numSkipped -> msg
                    else -> "$count $msg"
                }
            }
            Log.d(TAG, "   skipped $numSkipped: " + reasons.joinToString(" + "))
        }
    }

    companion object {
        private const val TAG = "StudyItemPool"
        private const val MAX_SEEK_AHEAD = 100
        private const val NUM_WORDS_CONSIDERED_RECENT = 100
        private const val INDEX_OF_MAX_PROBABILITY_TO_PICK_WORD_FROM_PAST = 2300 // end of N3, close to start of N2
        private const val MAX_PROBABILITY_TO_PICK_WORD_FROM_PAST = 0.21f
        private const val MAX_MILLIS_FOR_SEEK_AHEAD = 150L // milliseconds
    }
}
