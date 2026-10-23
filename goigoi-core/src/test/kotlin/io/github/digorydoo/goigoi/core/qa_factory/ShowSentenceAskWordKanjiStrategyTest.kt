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

internal class ShowSentenceAskWordKanjiStrategyTest {
    @Test
    fun `should create the expected values for tobidasu`() {
        val tobidasu = getMockWordTobidasu()
        val data = QuestionData(tobidasu, QAKind.SHOW_SENTENCE_ASK_WORD_KANJI, 0)
        val strategy = ShowSentenceAskWordKanjiStrategy()

        assertTrue(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.Second<String, FuriganaString>>(question)
        assertEquals("猫が___。", question.second.kanji)
        assertEquals("ねこが___。", question.second.kana)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertIs<OneOf.Second<String, FuriganaString>>(questionAfterReveal)
        assertEquals("猫が飛び出した。", questionAfterReveal.second.kanji)
        assertEquals("ねこがとびだした。", questionAfterReveal.second.kana)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("A cat rushed out.", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("飛び出した", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("飛出した", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertEquals("sample explanation", explanation?.raw)
    }

    @Test
    fun `should create the expected values for mazui`() {
        val mazui = getMockWordMazui()
        val data = QuestionData(mazui, QAKind.SHOW_SENTENCE_ASK_WORD_KANJI, 0)
        val strategy = ShowSentenceAskWordKanjiStrategy()

        assertTrue(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.Second<String, FuriganaString>>(question)
        assertEquals("この料理は ___。", question.second.kanji)
        assertEquals("このりょうりは ___。", question.second.kana)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertIs<OneOf.Second<String, FuriganaString>>(questionAfterReveal)
        assertEquals("この料理は まずい。", questionAfterReveal.second.kanji)
        assertEquals("このりょうりは まずい。", questionAfterReveal.second.kana)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("This dish tastes bad.", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("まずい", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation?.raw)
    }

    @Test
    fun `should create the expected values for egaku`() {
        val egaku = getMockWordEgaku()
        val data = QuestionData(egaku, QAKind.SHOW_SENTENCE_ASK_WORD_KANJI, 0)
        val strategy = ShowSentenceAskWordKanjiStrategy()

        assertTrue(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.Second<String, FuriganaString>>(question)
        assertEquals("将来の夢を心に___いる。", question.second.kanji)
        assertEquals("しょうらいのゆめをこころに___いる。", question.second.kana)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertIs<OneOf.Second<String, FuriganaString>>(questionAfterReveal)
        assertEquals("将来の夢を心に描いている。", questionAfterReveal.second.kanji)
        assertEquals("しょうらいのゆめをこころにえがいている。", questionAfterReveal.second.kana)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("I'm picturing my future dreams in my mind.", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("描いて", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("", acceptableAnswers.joinToString("、") { it.answer })

        val kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data)
        assertEquals("", kanjiOrKanaToReveal)

        val translationToReveal = strategy.getTranslationToReveal(data)
        assertEquals("", translationToReveal)

        val hintToReveal = strategy.getHintToReveal(data)
        assertEquals("", hintToReveal)

        val explanation = strategy.getExplanation(data)
        assertNull(explanation?.raw)
    }
}
