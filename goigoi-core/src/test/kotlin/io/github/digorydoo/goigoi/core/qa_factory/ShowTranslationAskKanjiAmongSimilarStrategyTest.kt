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
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ShowTranslationAskKanjiAmongSimilarStrategyTest {
    @Test
    fun `should create the expected values for tobidasu`() {
        val tobidasu = getMockWordTobidasu()
        val data = QuestionData(tobidasu, QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR, 0)
        val strategy = ShowTranslationAskKanjiAmongSimilarStrategy()

        assertTrue(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("to rush out", question.first)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("v.t. & i.", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("飛び出す", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("飛出す、出す、駆けつける", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("とびだす", kanjiOrKanaToReveal)

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
        val data = QuestionData(mazui, QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR, 0)
        val strategy = ShowTranslationAskKanjiAmongSimilarStrategy()

        assertTrue(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("unpleasant taste", question.first)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("i-adjective", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("不味い", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("まずい", kanjiOrKanaToReveal)

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
        val data = QuestionData(egaku, QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR, 0)
        val strategy = ShowTranslationAskKanjiAmongSimilarStrategy()

        assertTrue(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("to draw", question.first)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("v.t.", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("描く", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("塗り替える", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("えがく", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation)
    }
}
