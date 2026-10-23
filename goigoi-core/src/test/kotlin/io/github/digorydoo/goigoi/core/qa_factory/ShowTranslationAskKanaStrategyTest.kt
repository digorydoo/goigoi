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

internal class ShowTranslationAskKanaStrategyTest {
    @Test
    fun `should create the expected values for tobidasu`() {
        val tobidasu = getMockWordTobidasu()
        val data = QuestionData(tobidasu, QAKind.SHOW_TRANSLATION_ASK_KANA, 0)
        val strategy = ShowTranslationAskKanaStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("to rush out", question.first)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("v.t. & i.", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("とびだす", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals(
            "だす、かけつける、ラッシュアウトする",
            acceptableAnswers.joinToString("、") { it.answer }
        )

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("飛び出す", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation)
    }

    @Test
    fun `should create the expected values for mazui`() {
        val mazui = getMockWordMazui()
        val data = QuestionData(mazui, QAKind.SHOW_TRANSLATION_ASK_KANA, 0)
        val strategy = ShowTranslationAskKanaStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("unpleasant taste", question.first)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("i-adjective", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("まずい", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("マズい", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("", kanjiOrKanaToReveal) // not revealed, because word is usually written in kana

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation)
    }

    @Test
    fun `should create the expected values for egaku`() {
        val egaku = getMockWordEgaku()
        val data = QuestionData(egaku, QAKind.SHOW_TRANSLATION_ASK_KANA, 0)
        val strategy = ShowTranslationAskKanaStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("to draw", question.first)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("v.t.", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("えがく", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("かく、ぬりかえる", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("描く", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation)
    }
}
