package io.github.digorydoo.goigoi.activity.prog_study

import android.content.Context
import android.util.Log
import ch.digorydoo.kutils.cjk.JLPTLevel
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.core.db.KanjiIndex
import io.github.digorydoo.goigoi.core.prog_study.QAKind
import io.github.digorydoo.goigoi.core.prog_study.QAPicker
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer
import io.github.digorydoo.goigoi.core.prog_study.RoundsTracker
import io.github.digorydoo.goigoi.core.stats.Stats
import io.github.digorydoo.goigoi.core.study.Answer
import io.github.digorydoo.goigoi.core.study.StudyItemIterator
import io.github.digorydoo.goigoi.utils.restoreState
import io.github.digorydoo.goigoi.utils.saveState

class QAProvider(
    private val delegate: Delegate,
    isInTestLab: Boolean,
    kanjiIndex: KanjiIndex,
    stats: Stats,
) {
    interface Delegate {
        val canUseRomaji: Boolean
        val averageLevelOfWords: JLPTLevel
        fun createIterator(): StudyItemIterator
        fun ranOutOfWords()
    }

    class NoQAAvailableError: Exception()

    private var iterator: StudyItemIterator? = null
    private val rounds = RoundsTracker()
    private val qaPicker = QAPicker(isInTestLab, kanjiIndex, rounds, stats)

    private var _qa: QuestionAndAnswer? = null
    val qa get() = _qa!!

    fun start(state: ProgStudyState?): Boolean {
        require(iterator == null) { "iterator has already been created!" }

        val iterator = delegate.createIterator()
        this.iterator = iterator

        if (state == null) {
            return pickKindOrChangeWord()
        }

        iterator.restoreState(state.studyItemIteratorState)
        rounds.restoreState(state)
        _qa = QuestionAndAnswer(iterator.curWord, state.qaKind, state.qaIndex, state.questionHasFurigana)
        return true
    }

    fun saveState(outState: ProgStudyState) {
        iterator?.saveState(outState.studyItemIteratorState)
        rounds.saveState(outState)
        outState.qaKind = _qa?.kind ?: QAKind.SHOW_KANJI_ASK_KANA
        outState.qaIndex = _qa?.index ?: 0
        outState.questionHasFurigana = _qa?.questionHasFurigana ?: false
    }

    fun next() {
        val iterator = iterator ?: throw NoQAAvailableError()

        if (!iterator.hasNext()) {
            this.iterator = delegate.createIterator()
            delegate.ranOutOfWords() // shows a snack-bar
        } else {
            iterator.next()
        }

        if (!pickKindOrChangeWord()) {
            throw NoQAAvailableError()
        }
    }

    fun getSummary(ctx: Context) =
        ctx.getString(R.string.correct_wrong_counts)
            .replace("\${N}", "${iterator?.numCorrect ?: 0}")
            .replace("\${M}", "${iterator?.numWrong ?: 0}")

    fun notifyAnswer(answer: Answer) {
        iterator?.notifyAnswer(qa.kind.toStatsKey(), answer)
    }

    private fun pickKindOrChangeWord(): Boolean {
        val iterator = iterator ?: return false

        @Suppress("unused")
        for (i in 0 ..< 10) {
            val word = iterator.curWord
            _qa = qaPicker.getQA(word, delegate.averageLevelOfWords, delegate.canUseRomaji)

            if (_qa != null) break

            Log.w(TAG, "QAProvider found no available kind for word ${iterator.curWord.id}")
            if (!iterator.hasNext()) break
            iterator.next()
        }

        _qa?.let { rounds.aboutToShow(it.word, it.kind) }
        return _qa != null
    }

    companion object {
        private const val TAG = "QAProvider"
    }
}
