package io.github.digorydoo.goigoi.drawable

import android.content.Context
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.stats.Stats
import ch.digorydoo.kutils.utils.Moment
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object IconBuilder {
    private const val DAYS_BEFORE_ZZZ = 99

    fun makeWordIcon(ctx: Context, word: Word): AnimatedDrawable {
        val stats = Stats.getSingleton(ctx)
        val progress = stats.getWordStudyProgress(word)

        if (progress < 1.0f) {
            return RingIcon(ctx, RingIcon.Variant.CIRCULAR, progress)
        }

        val rating = stats.getWordTotalRating(word)
        return BubbleIcon(ctx, BubbleIcon.Variant.CIRCULAR, rating)
    }

    fun makeUnytIcon(ctx: Context, unyt: Unyt): AnimatedDrawable {
        val stats = Stats.getSingleton(ctx)

        var progress = stats.getUnytStudyProgress(unyt)

        if (progress == 0.0f && unyt.numWordsLoaded == 0 && unyt.numWordsAvailable > 0) {
            // To compute the progress, we need to load the words of the unyt first. However, we
            // must avoid doing this for all unyts of a topic when a user navigates to a topic that
            // he has never studied yet, as this would have a performance impact. Luckily, a unyt
            // will still have a non-null study moment when its cache was invalidated after one of
            // its words' stats have changed, so we load those only.

            if (stats.getUnytStudyMoment(unyt) != null) {
                val vocab = Vocabulary.getSingleton(ctx)
                vocab.loadUnytIfNecessary(unyt, ctx)
                progress = stats.getUnytStudyProgress(unyt)
            }
        }

        if (progress < 1.0f) {
            return RingIcon(ctx, RingIcon.Variant.DIAMOND, progress)
        }

        val studyDat = stats.getUnytStudyMoment(unyt)
        val minDat = Moment.now() - DAYS_BEFORE_ZZZ.toDuration(DurationUnit.DAYS)

        if (studyDat != null && studyDat < minDat) {
            return ZzzIcon(ctx)
        }

        val rating = stats.getUnytRating(unyt)
        return BubbleIcon(ctx, BubbleIcon.Variant.DIAMOND, rating)
    }
}
