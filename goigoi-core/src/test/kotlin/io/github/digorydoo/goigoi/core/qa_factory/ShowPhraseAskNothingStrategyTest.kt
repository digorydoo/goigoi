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

internal class ShowPhraseAskNothingStrategyTest {
    @Test
    fun `should create the expected values for tobidasu`() {
        val tobidasu = getMockWordTobidasu()
        val data = QuestionData(tobidasu, QAKind.SHOW_PHRASE_ASK_NOTHING, 0)
        val strategy = ShowPhraseAskNothingStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.Second<String, FuriganaString>>(question)
        assertEquals("急に飛び出す", question.second.kanji)
        assertEquals("きゅうにとびだす", question.second.kana)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("to suddenly rush out", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation)
    }

    @Test
    fun `should create the expected values for mazui`() {
        val mazui = getMockWordMazui()
        val data = QuestionData(mazui, QAKind.SHOW_PHRASE_ASK_NOTHING, 0)
        val strategy = ShowPhraseAskNothingStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.Second<String, FuriganaString>>(question)
        assertEquals("まずくて食べられない", question.second.kanji)
        assertEquals("まずくてたべられない", question.second.kana)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("so bad-tasting you cannot eat it", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation)
    }

    @Test
    fun `should create the expected values for egaku`() {
        val egaku = getMockWordEgaku()
        val data = QuestionData(egaku, QAKind.SHOW_PHRASE_ASK_NOTHING, 0)
        val strategy = ShowPhraseAskNothingStrategy()

        assertFalse(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.Second<String, FuriganaString>>(question)
        assertEquals("絵を描く", question.second.kanji)
        assertEquals("えをえがく", question.second.kana)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertNull(questionAfterReveal)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("to paint a picture", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation)
    }
}
