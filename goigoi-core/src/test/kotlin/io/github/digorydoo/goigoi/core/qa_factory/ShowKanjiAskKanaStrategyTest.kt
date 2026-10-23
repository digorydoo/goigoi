package io.github.digorydoo.goigoi.core.qa_factory

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.getMockWordEgaku
import io.github.digorydoo.goigoi.core.getMockWordMazui
import io.github.digorydoo.goigoi.core.getMockWordTobidasu
import io.github.digorydoo.goigoi.core.prog_study.QAKind
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer.Hint
import io.github.digorydoo.goigoi.core.qa_factory.QAFactory.QuestionData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

internal class ShowKanjiAskKanaStrategyTest {
    @Test
    fun `should create the expected values for tobidasu`() {
        val tobidasu = getMockWordTobidasu()
        val data = QuestionData(tobidasu, QAKind.SHOW_KANJI_ASK_KANA, 0)
        val strategy = ShowKanjiAskKanaStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("飛び出す", question.first)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("とびだす", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("to rush out", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("v.t. & i.", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation)
    }

    @Test
    fun `should create the expected values for mazui`() {
        val mazui = getMockWordMazui()
        val data = QuestionData(mazui, QAKind.SHOW_KANJI_ASK_KANA, 0)
        val strategy = ShowKanjiAskKanaStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("不味い", question.first)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("まずい", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("unpleasant taste", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("i-adjective", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation)
    }

    @Test
    fun `should create the expected values for egaku`() {
        val egaku = getMockWordEgaku()
        val data = QuestionData(egaku, QAKind.SHOW_KANJI_ASK_KANA, 0)
        val strategy = ShowKanjiAskKanaStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("描く", question.first)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("えがく", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("かく", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("to draw", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("v.t.", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation)
    }
}
