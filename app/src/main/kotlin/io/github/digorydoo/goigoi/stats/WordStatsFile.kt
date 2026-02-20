package io.github.digorydoo.goigoi.stats

import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.study.Answer
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.lerp
import java.io.File
import java.io.OutputStream
import kotlin.math.min

class WordStatsFile(dir: File): Stats.Exportable {
    private val file: RawStatsFile = RawStatsFile(dir, FILE_NAME, FILE_VERSION)

    override fun exportTo(dst: OutputStream) =
        file.exportTo(dst)

    fun notifyAnswer(word: Word, key: StatsKey, answer: Answer) {
        when (answer) {
            Answer.CORRECT -> {
                incInt("$key.$ANSWER_CORRECT_ID", word, 1)
                setRecentAnswer(key, word, 1)
                updateCumulRating(word, correct = true)
            }
            Answer.WRONG -> {
                incInt("$key.$ANSWER_WRONG_ID", word, 1)
                setRecentAnswer(key, word, 0)
                updateCumulRating(word, correct = false)
            }
            Answer.CORRECT_EXCEPT_KANA_SIZE, // counted as neither correct nor wrong
            Answer.TRIVIAL, // word, phrase or sentence just presented
            Answer.SKIP, // user skipped word in FlipThru
            Answer.NONE, // shouldn't happen
            -> Unit
        }

        incInt("$key.$WORD_SEEN_ID", word, 1)
    }

    fun getCorrectCount(word: Word, key: StatsKey): Int {
        return getInt("$key.$ANSWER_CORRECT_ID", word, 0)
    }

    fun getTotalCorrectCount(word: Word) =
        StatsKey.entries.sumOf { getCorrectCount(word, it) }

    fun getTotalWrongCount(word: Word) =
        StatsKey.entries.sumOf { getWrongCount(word, it) }

    fun getWrongCount(word: Word, key: StatsKey): Int {
        return getInt("$key.$ANSWER_WRONG_ID", word, 0)
    }

    fun getSeenCount(word: Word, key: StatsKey): Int {
        val seen = getInt("$key.$WORD_SEEN_ID", word, 0)
        if (seen > 0) return seen

        // Backward compatibility
        val oldSeen = getCorrectCount(word, key) + getWrongCount(word, key)
        if (oldSeen <= 0) return 0
        setInt("$key.$WORD_SEEN_ID", word, oldSeen)
        return oldSeen
    }

    fun getStudyProgress(word: Word): Float {
        // Progress is solely based on correct count.
        val relCorrect = clamp(getTotalCorrectCount(word).toFloat() / STUDY_PROGRESS_COUNT)
        if (relCorrect > 0) return relCorrect

        // If the above yielded 0, approach the first progress step with the seen count.
        return clamp(getTotalSeenCount(word).toFloat() / STUDY_PROGRESS_COUNT) / (STUDY_PROGRESS_COUNT + 1)
    }

    fun getTotalSeenCount(word: Word) =
        StatsKey.entries.sumOf { getSeenCount(word, it) }

    fun getRating(word: Word, key: StatsKey): Float {
        val numCorrect = getCorrectCount(word, key)
        val numWrong = getWrongCount(word, key)
        val numAnswers = numCorrect + numWrong

        if (numAnswers <= 0) {
            return 0.0f
        }

        val avg = numCorrect.toFloat() / numAnswers.toFloat() // average for this StatsKey only
        val recentAnswers = getRecentAnswers(word, key) // recent answers for this StatsKey

        val weightedSum = if (recentAnswers.isEmpty()) {
            // The stats file must be missing data, because we know numAnswers > 0.
            avg
        } else {
            var p = 1.0f
            var sum = p * avg
            var sumOfWeights = p

            p = WEIGHT_OF_FIRST_RECENT_ANSWER

            for (answer in recentAnswers) {
                if (answer >= 0) {
                    sum += answer * p
                    sumOfWeights += p
                    p *= 0.88f
                }
            }

            sum / sumOfWeights
        }

        // Adjust the rating if the number of answers is low.
        val relevance = min(1.0f, numAnswers.toFloat() / MIN_ANSWERS_FOR_TOP_RATING)
        val adjustedRating = lerp(MIN_RATING_WHEN_TOO_FEW_ANSWERS, weightedSum, relevance)

        val result = if (adjustedRating < weightedSum) {
            // e.g. there was just one answer, and it was correct
            adjustedRating
        } else {
            // e.g. there was just one answer, and it was wrong
            (weightedSum + adjustedRating) / 2.0f
        }

        // Clamping should not be necessary, but I do it as a safety catch should something be wrong.
        return clamp(result)
    }

    fun getTotalRating(word: Word): Float {
        var sum = 0.0f
        var totalNumAnswers = 0

        StatsKey.entries.forEach { key ->
            val numAnswers = getCorrectCount(word, key) + getWrongCount(word, key)

            if (numAnswers > 0) {
                sum += numAnswers * getRating(word, key)
                totalNumAnswers += numAnswers
            }
        }

        if (totalNumAnswers <= 0) return 0.0f

        val weightedSum = sum / totalNumAnswers
        val cumulRating = getCumulRating(word)
        return lerp(weightedSum, cumulRating, WEIGHT_OF_CUMUL)
    }

    private fun getRecentAnswers(word: Word, key: StatsKey) =
        recentAnswerIds(key).map { id -> getInt(id, word, -1) }

    private fun setRecentAnswer(key: StatsKey, word: Word, newVal: Int) {
        val ids = recentAnswerIds(key)
        for (i in ids.indices.reversed()) {
            if (i > 0) {
                setInt(ids[i], word, getInt(ids[i - 1], word, -1))
            } else {
                setInt(ids[i], word, newVal)
            }
        }
    }

    private fun getCumulRating(word: Word): Float {
        val key = getKey(CUMUL_RATING_ID, word)
        return file.getFloat(key) ?: MIN_RATING_WHEN_TOO_FEW_ANSWERS
    }

    private fun updateCumulRating(word: Word, correct: Boolean) {
        val key = getKey(CUMUL_RATING_ID, word)
        val cur = getCumulRating(word)
        val ratingOfAnswer = if (correct) 1.0f else 0.0f
        val updateRate = if (correct) CUMUL_UPDATE_RATE_WHEN_CORRECT else CUMUL_UPDATE_RATE_WHEN_WRONG
        val new = lerp(cur, ratingOfAnswer, updateRate)
        file.setFloat(key, new)
    }

    fun reset(w: Word) {
        StatsKey.entries.forEach { key ->
            file.remove(getKey("$key.$WORD_SEEN_ID", w))
            file.remove(getKey("$key.$ANSWER_CORRECT_ID", w))
            file.remove(getKey("$key.$ANSWER_WRONG_ID", w))
            file.remove(getKey(CUMUL_RATING_ID, w))

            recentAnswerIds(key).forEach { id ->
                file.remove(getKey(id, w))
            }
        }
    }

    private fun getInt(id: String, word: Word, defaultVal: Int): Int {
        val key = getKey(id, word)
        return file.getInt(key) ?: defaultVal
    }

    private fun setInt(id: String, word: Word, value: Int) {
        val key = getKey(id, word)
        file.setInt(key, value)
    }

    private fun incInt(id: String, word: Word, defaultVal: Int, maxVal: Int? = null) {
        val key = getKey(id, word)
        file.incInt(key, defaultVal, maxVal)
    }

    private fun getKey(counterId: String, word: Word): String {
        return word.id + "." + counterId
    }

    companion object {
        private const val FILE_NAME = "word"
        private const val FILE_VERSION = 6

        private const val WORD_SEEN_ID = "seen"
        private const val ANSWER_CORRECT_ID = "ok"
        private const val ANSWER_WRONG_ID = "no"

        private const val ANSWER_CYCLE_ID = "Z"
        private const val ANSWER_CYCLE_SIZE = 3

        private const val CUMUL_RATING_ID = "K"
        private const val CUMUL_UPDATE_RATE_WHEN_CORRECT = 0.12f
        private const val CUMUL_UPDATE_RATE_WHEN_WRONG = 0.07f
        private const val WEIGHT_OF_CUMUL = 0.8f
        private const val WEIGHT_OF_FIRST_RECENT_ANSWER = 0.55f

        private const val STUDY_PROGRESS_COUNT = 3
        private const val MIN_ANSWERS_FOR_TOP_RATING = 5
        private const val MIN_RATING_WHEN_TOO_FEW_ANSWERS = 0.64f

        private fun recentAnswerIds(key: StatsKey) =
            Array(ANSWER_CYCLE_SIZE) { "$key.$ANSWER_CYCLE_ID.$it" }
    }
}
