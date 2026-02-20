package io.github.digorydoo.goigoi.stats

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.study.Answer
import java.io.OutputStream

class Stats private constructor(ctx: Context) {
    interface Exportable {
        fun exportTo(dst: OutputStream)
    }

    private val generalStatsFile = GeneralStatsFile(ctx.filesDir)
    private val wordStatsFile = WordStatsFile(ctx.filesDir)
    private val unytStatsFile = UnytStatsFile(ctx.filesDir)

    private val vocab = Vocabulary.getSingleton(ctx)

    val launchCount get() = generalStatsFile.launchCount

    fun notifyAppLaunch() {
        generalStatsFile.notifyAppLaunch()
    }

    fun notifyMainActivityResume() {
        generalStatsFile.notifyMainActivityResume()
    }

    fun notifyUnytActivityLaunched(u: Unyt) {
        unytStatsFile.notifyUnytActivityLaunched(u)
    }

    fun getUserStudyCountOfDay(m: Moment) =
        generalStatsFile.getUserStudyCountOfDay(m)

    fun incUserStudyCountOfToday(reason: StatsKey) {
        generalStatsFile.incUserStudyCountOfToday(reason)
    }

    fun hasHintBeenShown(hintDlgKey: HintDlgKey) =
        generalStatsFile.hasHintBeenShown(hintDlgKey)

    fun didShowHint(hintDlgKey: HintDlgKey) {
        generalStatsFile.didShowHint(hintDlgKey)
    }

    val superProgressiveIdx get() = generalStatsFile.superProgressiveIdx

    fun setSuperProgressiveIdx(idx: Int) {
        generalStatsFile.setSuperProgressiveIdx(idx)
    }

    fun notifyAnswer(w: Word, u: Unyt, key: StatsKey, answer: Answer) {
        wordStatsFile.notifyAnswer(w, key, answer)
        unytStatsFile.setStudyMoment(u)
        incUserStudyCountOfToday(key)
    }

    fun getWordSeenCount(word: Word, key: StatsKey) =
        wordStatsFile.getSeenCount(word, key)

    fun getWordCorrectCount(word: Word, key: StatsKey) =
        wordStatsFile.getCorrectCount(word, key)

    fun getWordTotalSeenCount(word: Word) =
        wordStatsFile.getTotalSeenCount(word)

    fun getWordTotalCorrectCount(word: Word) =
        wordStatsFile.getTotalCorrectCount(word)

    private fun getWordTotalWrongCount(word: Word) =
        wordStatsFile.getTotalWrongCount(word)

    fun getWordStudyProgress(word: Word): Float =
        wordStatsFile.getStudyProgress(word)

    fun getWordRating(word: Word, key: StatsKey) =
        wordStatsFile.getRating(word, key)

    fun getWordTotalRating(word: Word): Float =
        wordStatsFile.getTotalRating(word)

    fun getUnytStudyMoment(unyt: Unyt) =
        unytStatsFile.getStudyMoment(unyt)

    fun setUnytStudyMoment(unyt: Unyt) {
        unytStatsFile.setStudyMoment(unyt)
    }

    fun getUnytStudyProgress(unyt: Unyt): Float {
        val cached = unytStatsFile.getStudyProgress(unyt)

        if (cached != null) {
            return cached
        }

        if (unyt.numWordsLoaded == 0) {
            // We can't tell until the words of the unyt have been loaded.
            return 0.0f
        }

        val progress = computeUnytStudyProgress(unyt)
        unytStatsFile.setStudyProgress(unyt, progress) // store new value in cache
        return progress
    }

    fun getUnytRating(unyt: Unyt): Float {
        val cached = unytStatsFile.getRating(unyt)

        if (cached != null) {
            return cached
        }

        if (unyt.numWordsLoaded == 0) {
            // We can't tell until the words of the unyt have been loaded.
            return 0.0f
        }

        val rating = computeUnytRating(unyt)
        unytStatsFile.setRating(unyt, rating) // store new value in cache
        return rating
    }

    private fun computeUnytStudyProgress(unyt: Unyt): Float {
        val numWords = unyt.numWordsLoaded

        if (numWords <= 0) {
            return 0.0f
        }

        var sum = 0f
        var minProgress = Float.MAX_VALUE

        unyt.forEachWord { w ->
            val p = getWordStudyProgress(w)
            sum += p

            if (p < minProgress) {
                minProgress = p
            }
        }

        if (sum <= 0) {
            return 0.0f
        }

        // Add additional weight to the word with the least progress value
        val avg = sum / numWords.toFloat()
        val p = 0.2f
        val q = 1.0f - p
        return p * minProgress + q * avg // 0..1
    }

    private fun computeUnytRating(unyt: Unyt): Float {
        var count = 0
        var sum = 0f
        var minRating = Float.MAX_VALUE

        unyt.forEachWord { w ->
            // There's no need to check the seen count, since the unyt's rating shouldn't be
            // displayed as long as there are still words with low seen counts.

            val r = getWordTotalRating(w)
            sum += r
            count++

            if (r < minRating) {
                minRating = r
            }
        }

        if (count <= 0) {
            return 0.0f
        }

        // Add additional weight to the word with the least rating

        val avg = sum / count.toFloat()
        val p = 0.2f
        val q = 1.0f - p
        val result = p * minRating + q * avg

        // Force the rating below 0.98 if there is any word with a rating less than 0.98

        return if (result >= 0.98f && minRating < 0.98f) {
            0.97f
        } else {
            result // 0..1
        }
    }

    // Shown on BottomSheet
    fun getDebugInfo(word: Word) =
        "Progress: ${wordStatsFile.getStudyProgress(word)} (based on total correct)\n" +
            "Rating: ${getWordTotalRating(word)}, " +
            "seen: ${getWordTotalSeenCount(word)}, " +
            "correct: ${getWordTotalCorrectCount(word)}, " +
            "wrong: ${getWordTotalWrongCount(word)}\n" +
            "\n" +
            StatsKey.entries.flatMap { statsKey ->
                val rating = wordStatsFile.getRating(word, statsKey)
                val seenCount = wordStatsFile.getSeenCount(word, statsKey)
                val correctCount = wordStatsFile.getCorrectCount(word, statsKey)
                val wrongCount = wordStatsFile.getWrongCount(word, statsKey)
                listOf(
                    statsKey.name,
                    "    Rating: $rating, seen: $seenCount, correct: $correctCount, wrong: $wrongCount",
                )
            }.joinToString("\n")

    // Shown on BottomSheet
    fun getDebugInfo(unyt: Unyt) =
        """
        Study moment ${getUnytStudyMoment(unyt)?.formatAsZoneAgnosticDateTime()}
        """.trimIndent()

    /**
     * Called by SplashActivity to pre-fill KeyValueFile's in-memory cache and UnytStatsFile's
     * cached values with some data.
     */
    fun prefillCaches() {
        var toGo = 30

        for (t in vocab.topics) {
            for (u in t.unyts) {
                // None of the unyt ratings are going to be looked up on first launch as study
                // progress values will still be 0. On later launches, UnytStatsFile should have the
                // ratings ready in a single value. So, let's just pre-fill the progress values.
                getUnytStudyProgress(u)
                toGo--

                if (toGo <= 0) {
                    // We stop here to make sure the first launch does not take forever
                    return
                }
            }
        }
    }

    // This may take a while, so be sure to always call this from a coroutine!
    // FIXME: wordStatsFile and unytStatsFile are not Thread-safe!
    fun resetUnytStatsExpensively(unyt: Unyt) {
        // Do not use unyt.forEachWord, as this would lock the unyt for the entire loop!
        unyt.getWordsShallowClone().forEach { w ->
            wordStatsFile.reset(w)

            // Try to prevent from running out of memory

            try {
                Thread.sleep(10)
            } catch (_: InterruptedException) {
                // ignore
            } finally {
                System.gc()
            }
        }

        unytStatsFile.setStudyProgress(unyt, null)
        unytStatsFile.setRating(unyt, null)
    }

    // This may take a while, so be sure to always call this from a coroutine!
    // FIXME: wordStatsFile and unytStatsFile are not Thread-safe!
    fun resetWordStatsExpensively(w: Word, u: Unyt, fakeNumCorrect: Int?, fakeNumWrong: Int?) {
        wordStatsFile.reset(w)
        unytStatsFile.setStudyProgress(u, null) // clear cache
        unytStatsFile.setRating(u, null) // clear cache

        // Note: The order with which we apply fake stats is important!

        if (fakeNumWrong != null) {
            (0 ..< fakeNumWrong).forEach {
                notifyAnswer(w, u, StatsKey.FLIPTHRU, Answer.WRONG)
            }
        }

        if (fakeNumCorrect != null) {
            (0 ..< fakeNumCorrect).forEach {
                notifyAnswer(w, u, StatsKey.FLIPTHRU, Answer.CORRECT)
            }
        }
    }

    // Called from SplashActivity if active.
    @Suppress("unused")
    fun export(resolver: ContentResolver) {
        export(resolver, generalStatsFile, EXPORTED_GENERAL_STATS_FILE_NAME)
        export(resolver, unytStatsFile, EXPORTED_UNYT_STATS_FILE_NAME)
        export(resolver, wordStatsFile, EXPORTED_WORD_STATS_FILE_NAME)
    }

    private fun export(resolver: ContentResolver, stats: Exportable, filename: String) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "text/plain")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

        if (uri == null) {
            Log.w(TAG, "Failed to export: $filename")
            return
        }

        resolver.openOutputStream(uri)?.use { out ->
            stats.exportTo(out)
        }

        Log.w(TAG, "Successfully exported: $filename") // a warning, because this should not be normally active
    }

    companion object {
        private const val TAG = "Stats"
        private const val EXPORTED_GENERAL_STATS_FILE_NAME = "goigoi-general-stats.txt"
        private const val EXPORTED_UNYT_STATS_FILE_NAME = "goigoi-unyt-stats.txt"
        private const val EXPORTED_WORD_STATS_FILE_NAME = "goigoi-word-stats.txt"

        private var singleton: Stats? = null

        fun getSingleton(ctx: Context) =
            singleton ?: Stats(ctx).also { singleton = it }

        fun hasSingleton() = singleton != null
    }
}
