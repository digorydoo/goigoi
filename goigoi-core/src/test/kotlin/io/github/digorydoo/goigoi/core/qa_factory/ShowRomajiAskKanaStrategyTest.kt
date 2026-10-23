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

internal class ShowRomajiAskKanaStrategyTest {
    @Test
    fun `should create the expected values for tobidasu`() {
        val tobidasu = getMockWordTobidasu()
        val data = QuestionData(tobidasu, QAKind.SHOW_ROMAJI_ASK_KANA, 0)
        val strategy = ShowRomajiAskKanaStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("tobidasu", question.first)

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
        assertEquals("飛び出す", kanjiOrKanaToReveal)

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
        val data = QuestionData(mazui, QAKind.SHOW_ROMAJI_ASK_KANA, 0)
        val strategy = ShowRomajiAskKanaStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("mazui", question.first)

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
        assertEquals("", kanjiOrKanaToReveal) // 不味い not shown, because mazui is usually written in kana

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
        val data = QuestionData(egaku, QAKind.SHOW_ROMAJI_ASK_KANA, 0)
        val strategy = ShowRomajiAskKanaStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("egaku", question.first)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("えがく", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("描く", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("to draw", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("v.t.", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation)
    }
}
