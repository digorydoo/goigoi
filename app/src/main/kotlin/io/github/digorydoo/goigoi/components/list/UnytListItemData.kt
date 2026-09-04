package io.github.digorydoo.goigoi.components.list

import android.content.Context
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.stats.Stats
import io.github.digorydoo.goigoi.drawable.IconBuilder
import io.github.digorydoo.goigoi.utils.formatRelativeTime
import kotlin.time.Duration.Companion.days

class UnytListItemData {
    val name: String
    val numWords: Int
    val studyMomentAsText: String? // null = not studied yet
    val isMyWordsUnyt: Boolean
    val progress: Float // null = do not display progress
    val rating: Float // null = do not display rating
    val asleep: Boolean // true = not studied for a long time

    constructor() {
        name = ""
        numWords = 0
        studyMomentAsText = null
        isMyWordsUnyt = false
        progress = 0f
        rating = 0f
        asleep = false
    }

    constructor(unyt: Unyt, isMyWordsUnyt: Boolean, vocab: Vocabulary, stats: Stats, ctx: Context) {
        name = unyt.name.withSystemLang
        numWords = unyt.numWordsAvailable
        this.isMyWordsUnyt = isMyWordsUnyt

        val studyMom = stats.getUnytStudyMoment(unyt)
        studyMomentAsText = studyMom?.formatRelativeTime(ctx)

        if (isMyWordsUnyt) {
            progress = 0f
            rating = 0f
            asleep = false
        } else {
            val minDat = Moment.now() - IconBuilder.DAYS_BEFORE_ZZZ.days
            asleep = studyMom != null && studyMom < minDat

            if (asleep) {
                progress = 0f
                rating = 0f
            } else {
                // To compute the progress, we need to load the words of the unyt first. However, we must avoid doing
                // this for all unyts of a topic when a user navigates to a topic that he has never studied yet, as this
                // would have a performance impact. Luckily, a unyt will still have a non-null study moment when its
                // cache was invalidated after one of its words' stats have changed, so we have to load those only.

                val maybeProgress = stats.getUnytStudyProgress(unyt)

                if (studyMom != null &&
                    maybeProgress == 0f &&
                    unyt.numWordsLoaded == 0 &&
                    unyt.numWordsAvailable > 0
                ) {
                    vocab.loadUnytIfNecessary(unyt)
                    progress = stats.getUnytStudyProgress(unyt)
                } else {
                    progress = maybeProgress
                }

                rating = stats.getUnytRating(unyt)
            }
        }
    }
}
