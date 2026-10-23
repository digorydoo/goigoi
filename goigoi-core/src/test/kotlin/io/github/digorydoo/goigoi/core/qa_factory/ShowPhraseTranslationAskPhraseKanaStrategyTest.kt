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

internal class ShowPhraseTranslationAskPhraseKanaStrategyTest {
    @Test
    fun `should create the expected values for tobidasu`() {
        val tobidasu = getMockWordTobidasu()
        val data = QuestionData(tobidasu, QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA, 0)
        val strategy = ShowPhraseTranslationAskPhraseKanaStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("to suddenly rush out", question.first)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.Second<String, Hint>>(questionHint)
        assertEquals(Hint.PHRASE, questionHint.second)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("きゅうにとびだす", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("きゅうにラッシュアウトする", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("", kanjiOrKanaToReveal)

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
        val data = QuestionData(mazui, QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA, 0)
        val strategy = ShowPhraseTranslationAskPhraseKanaStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("so bad-tasting you cannot eat it", question.first)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.Second<String, Hint>>(questionHint)
        assertEquals(Hint.PHRASE, questionHint.second)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("まずくてたべられない", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("マズくてたべられない", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("", kanjiOrKanaToReveal)

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
        val data = QuestionData(egaku, QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA, 0)
        val strategy = ShowPhraseTranslationAskPhraseKanaStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.First<String, FuriganaString>>(question)
        assertEquals("to paint a picture", question.first)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.Second<String, Hint>>(questionHint)
        assertEquals(Hint.PHRASE, questionHint.second)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("えをえがく", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("えをかく", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation)
    }
}
