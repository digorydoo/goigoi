package io.github.digorydoo.goigoi.composables.list

import android.content.Context
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.stats.Stats
import io.github.digorydoo.goigoi.utils.formatRelativeTime

class UnytListItemData private constructor(
    val name: String,
    val numWords: Int,
    val studyMomentAsText: String?, // null = not studied yet
    val isMyWordsUnyt: Boolean,
    val progress: Float, // null = do not display progress
    val rating: Float, // null = do not display rating
    val asleep: Boolean, // true = not studied for a long time
) {
    companion object {
        fun createEmpty() = UnytListItemData(
            name = "",
            numWords = 0,
            studyMomentAsText = null,
            isMyWordsUnyt = false,
            progress = 0f,
            rating = 0f,
            asleep = false
        )

        fun create(
            unyt: Unyt,
            isMyWordsUnyt: Boolean,
            vocab: Vocabulary,
            stats: Stats,
            ctx: Context,
        ): UnytListItemData {
            val studyMom = stats.getUnytStudyMoment(unyt)
            val studyMomentAsText = studyMom?.formatRelativeTime(ctx)

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
}
