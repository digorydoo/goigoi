package io.github.digorydoo.goigoi.study

import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.stats.Stats
import ch.digorydoo.kutils.flow.into
import ch.digorydoo.kutils.math.lerp
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class StudyItem private constructor(val word: Word, val score: Float) {
    var localNumCorrect = 0
    var localNumWrong = 0
    var localNumSkipped = 0

    companion object {
        fun create(word: Word, stats: Stats) =
            StudyItem(word, computeScore(word, stats))

        private fun computeScore(word: Word, stats: Stats): Float {
            val progress = stats.getWordStudyProgress(word)
            val rating = stats.getWordTotalRating(word)
            val rnd = Random.nextFloat()

            val jlptDifficulty = 1.0f - ((word.level?.toInt() ?: 0) / 5.0f) // N5 has difficulty 0, Nx has difficulty 1
            require(jlptDifficulty in 0.0f .. 1.0f) { "jlptDifficulty out of bounds: $jlptDifficulty" }

            fun relLength(s: String, maxLen: Int) =
                min(max(s.length - 1, 0), maxLen) / maxLen.toFloat()

            val relLength = relLength(word.kanji, 10)
            require(relLength in 0.0f .. 1.0f) { "relLength out of bounds: $relLength" }

            val score = when {
                progress <= 0.0f -> {
                    // This word hasn't studied yet. It should appear a bit later than those we've studied a few times,
                    // but it should be still before those we've studied quite a lot.
                    0.3f + 0.3f * (rnd into
                        { lerp(it, jlptDifficulty, 0.3f) } into
                        { lerp(it, relLength, 0.3f) })
                }
                progress < 1.0f -> {
                    // This word has been studied, but not very often. Make sure it appears in front.
                    (1.0f - progress) into
                        { lerp(it, rating, 0.3f) } into
                        { lerp(it, jlptDifficulty, 0.2f) } into
                        { lerp(it, relLength, 0.2f) } into
                        { lerp(it, rnd, 0.5f) } into
                        { 0.75f * it } // keep it in front of words with progress == 1, with some overlap
                }
                else -> {
                    // This word has been studied quite often. Push it back a little.
                    0.5f + 0.5f * (lerp(rating, rnd, 0.38f) into
                        { lerp(it, jlptDifficulty, 0.1f) } into
                        { lerp(it, relLength, 0.1f) })
                }
            }

            require(score in 0.0f .. 1.0f) { "score out of bounds: $score" }
            return score
        }
    }
}
