package io.github.digorydoo.goigoi.core.prog_study

import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.goigoi.core.db.KanjiIndex
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.stats.Stats
import io.github.digorydoo.goigoi.core.study.Answer
import io.github.digorydoo.goigoi.core.study.StudyItemIterator

class QAProvider(
    private val iterator: StudyItemIterator,
    kanjiIndex: KanjiIndex,
    private val rounds: RoundsTracker,
    private val stats: Stats,
    private val vocab: Vocabulary,
) {
    class NoQAAvailableError: Exception()

    private val qaPicker = QAPicker(kanjiIndex, rounds, stats)
    lateinit var qa: QuestionAndAnswer; private set

    fun start(initQA: QuestionAndAnswer?) {
        if (initQA == null) {
            qa = nextQA()
        } else {
            qa = initQA
        }
    }

    fun next() {
        if (!iterator.hasNext()) {
            throw Exception("Ran out of words even though iterator should be on repeat")
        } else {
            iterator.next()
        }

        qa = nextQA()
    }

    fun notifyAnswer(answer: Answer) {
        iterator.notifyAnswer(qa.kind.toStatsKey(), answer)
    }

    private fun nextQA(): QuestionAndAnswer {
        val canUseRomaji = stats.superProgressiveIdx < MAX_SUPER_PROGRESSIVE_IDX_FOR_ROMAJI
        val avgLevel = vocab.myWordsUnyt.averageLevelOfWords() ?: JLPTLevel.N5

        @Suppress("unused")
        for (i in 0 ..< 10) {
            val word = iterator.curWord
            val newQA = qaPicker.getQA(word, avgLevel, canUseRomaji)

            if (newQA != null) {
                rounds.aboutToShow(newQA.word, newQA.kind)
                return newQA
            }

            Log.warn(TAG, "QAProvider found no available kind for word ${iterator.curWord.id}")
            if (iterator.hasNext()) iterator.next()
            else break
        }

        throw NoQAAvailableError()
    }

    companion object {
        private val TAG = Log.Tag("QAProvider")
        private const val MAX_SUPER_PROGRESSIVE_IDX_FOR_ROMAJI = 100 // still well into N5
    }
}
