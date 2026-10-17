package io.github.digorydoo.goigoi.core.stats

import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.study.Answer

interface IWordStats {
    fun getWordSeenCount(word: Word, key: StatsKey): Int
    fun getWordCorrectCount(word: Word, key: StatsKey): Int
    fun getWordWrongCount(word: Word, key: StatsKey): Int
    fun getWordTotalSeenCount(word: Word): Int
    fun getWordTotalCorrectCount(word: Word): Int
    fun getWordTotalWrongCount(word: Word): Int
    fun getWordStudyProgress(word: Word): Float
    fun getWordRating(word: Word, key: StatsKey): Float
    fun getWordTotalRating(word: Word): Float
    fun getWordStudyMoment(word: Word): Moment?
    fun getWordIsAsleep(word: Word): Boolean

    fun getPhraseTotalSeenCount(word: Word, phraseIdx: Int): Int
    fun getSentenceTotalSeenCount(word: Word, sentenceIdx: Int): Int

    fun resetWordStatsExpensively(w: Word, u: Unyt, fakeNumCorrect: Int?, fakeNumWrong: Int?)
}

interface IUnytStats {
    fun getUnytStudyMoment(unyt: Unyt): Moment?
    fun setUnytStudyMoment(unyt: Unyt)
    fun getUnytIsAsleep(unyt: Unyt): Boolean
    fun getUnytStudyProgress(unyt: Unyt): Float
    fun getUnytRating(unyt: Unyt): Float

    fun resetUnytStatsExpensively(unyt: Unyt)
}

interface ISuperProgressiveStats {
    val superProgressiveIdx: Int
    fun setSuperProgressiveIdx(idx: Int)
}

interface IDailyStats {
    fun getUserStudyCountOfDay(m: Moment): Float
    fun incUserStudyCountOfToday(reason: StatsKey)
}

interface IStatsEvents {
    fun notifyAppLaunch()
    fun notifyMainActivityResume()
    fun notifyUnytActivityLaunched(u: Unyt)
    fun notifyAnswer(w: Word, u: Unyt, key: StatsKey, answer: Answer)
}

interface IWordAndUnytStats: IWordStats, IUnytStats
interface IWordAndSuperProgressiveStats: IWordStats, ISuperProgressiveStats
interface IWordAndDailyStats: IWordStats, IDailyStats
interface IUnytAndDailyStats: IUnytStats, IDailyStats

interface IStats:
    IWordAndSuperProgressiveStats,
    IWordAndUnytStats,
    IWordAndDailyStats,
    IUnytAndDailyStats,
    IStatsEvents {

    fun export()
}
