package io.github.digorydoo.goigoi.core.list

import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.stats.IUnytStats
import io.github.digorydoo.goigoi.core.stats.IWordStats

class ListItemDataFactory(private val delegate: Delegate) {
    interface Delegate {
        fun formatRelativeTime(m: Moment): String
    }

    fun createWordListItemData(word: Word, vocab: Vocabulary, stats: IWordStats): WordListItemData {
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

    fun createUnytListItemData(unyt: Unyt, vocab: Vocabulary, stats: IUnytStats): UnytListItemData {
        val isMyWordsUnyt = unyt == vocab.myWordsUnyt
        val studyMom = stats.getUnytStudyMoment(unyt)
        val studyMomentAsText = studyMom?.let { delegate.formatRelativeTime(it) }

        val progress: Float
        val rating: Float
        val asleep: Boolean

        if (isMyWordsUnyt) {
            progress = 0f
            rating = 0f
            asleep = false
        } else {
            asleep = stats.getUnytIsAsleep(unyt)

            if (asleep) {
                progress = 0f
                rating = 0f
            } else {
                // To compute the progress, we need to load the words of the unyt first. However, we must avoid
                // doing this for all unyts of a topic when a user navigates to a topic that he has never studied
                // yet, as this would have a performance impact. Luckily, a unyt will still have a non-null study
                // moment when its cache was invalidated after one of its words' stats have changed, so we have to
                // load those only.

                val maybeProgress = stats.getUnytStudyProgress(unyt)

                progress = if (studyMom != null &&
                    maybeProgress == 0f &&
                    unyt.numWordsLoaded == 0 &&
                    unyt.numWordsAvailable > 0
                ) {
                    vocab.loadUnytIfNecessary(unyt)
                    stats.getUnytStudyProgress(unyt)
                } else {
                    maybeProgress
                }

                rating = stats.getUnytRating(unyt)
            }
        }

        return UnytListItemData(
            name = unyt.name.withSystemLang,
            numWords = unyt.numWordsAvailable,
            studyMomentAsText = studyMomentAsText,
            isMyWordsUnyt = isMyWordsUnyt,
            progress = progress,
            rating = rating,
            asleep = asleep
        )
    }
}
