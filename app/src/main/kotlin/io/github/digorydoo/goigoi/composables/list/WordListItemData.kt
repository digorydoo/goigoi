package io.github.digorydoo.goigoi.composables.list

import ch.digorydoo.kutils.cjk.JLPTLevel
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.stats.Stats

class WordListItemData private constructor(
    val kanji: String,
    val kana: String,
    val usuallyInKana: Boolean,
    val romaji: String,
    val translation: String,
    val level: JLPTLevel,
    val progress: Float, // null = do not display progress
    val rating: Float, // null = do not display rating
    val asleep: Boolean, // true = not studied for a long time
    val isInMyWords: Boolean,
) {
    companion object {
        fun create(word: Word, vocab: Vocabulary, stats: Stats): WordListItemData {
            val asleep = stats.getWordIsAsleep(word)
            return WordListItemData(
                kanji = word.kanji,
                kana = word.kana,
                usuallyInKana = word.usuallyInKana,
                romaji = word.romaji,
                translation = word.translation.withSystemLang,
                level = word.level ?: JLPTLevel.Nx,
                progress = if (asleep) 0f else stats.getWordStudyProgress(word),
                rating = if (asleep) 0f else stats.getWordTotalRating(word),
                asleep = stats.getWordIsAsleep(word),
                isInMyWords = vocab.myWordsUnyt.hasWordWithId(word.id)
            )
        }
    }
}
