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

internal class ShowPhraseAskWordKanjiStrategyTest {
    @Test
    fun `should create the expected values for tobidasu`() {
        val tobidasu = getMockWordTobidasu()
        val data = QuestionData(tobidasu, QAKind.SHOW_PHRASE_ASK_WORD_KANJI, 0)
        val strategy = ShowPhraseAskWordKanjiStrategy()

        assertTrue(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.Second<String, FuriganaString>>(question)
        assertEquals("急に___", question.second.kanji)
        assertEquals("きゅうに___", question.second.kana)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertIs<OneOf.Second<String, FuriganaString>>(questionAfterReveal)
        assertEquals("急に飛び出す", questionAfterReveal.second.kanji)
        assertEquals("きゅうにとびだす", questionAfterReveal.second.kana)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("to suddenly rush out", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("飛び出す", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("飛出す", acceptableAnswers.joinToString("、") { it.answer })

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
        val data = QuestionData(mazui, QAKind.SHOW_PHRASE_ASK_WORD_KANJI, 0)
        val strategy = ShowPhraseAskWordKanjiStrategy()

        assertTrue(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.Second<String, FuriganaString>>(question)
        assertEquals("___食べられない", question.second.kanji)
        assertEquals("___たべられない", question.second.kana)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertIs<OneOf.Second<String, FuriganaString>>(questionAfterReveal)
        assertEquals("まずくて食べられない", questionAfterReveal.second.kanji)
        assertEquals("まずくてたべられない", questionAfterReveal.second.kana)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("so bad-tasting you cannot eat it", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("まずくて", expectedAnswer)

        val acceptableAnswers = strategy.getAcceptableAnswers(data)
        assertEquals("不味くて", acceptableAnswers.joinToString("、") { it.answer })

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
        val data = QuestionData(egaku, QAKind.SHOW_PHRASE_ASK_WORD_KANJI, 0)
        val strategy = ShowPhraseAskWordKanjiStrategy()

        assertTrue(data.presentWholeWords)

        val question = strategy.getQuestion(data)
        assertIs<OneOf.Second<String, FuriganaString>>(question)
        assertEquals("絵を___", question.second.kanji)
        assertEquals("えを___", question.second.kana)

        val questionAfterReveal = strategy.getQuestionAfterReveal(data)
        assertIs<OneOf.Second<String, FuriganaString>>(questionAfterReveal)
        assertEquals("絵を描く", questionAfterReveal.second.kanji)
        assertEquals("えをえがく", questionAfterReveal.second.kana)

        val questionHint = strategy.getQuestionHint(data)
        assertIs<OneOf.First<String, Hint>>(questionHint)
        assertEquals("to paint a picture", questionHint.first)

        val expectedAnswer = strategy.getExpectedAnswer(data)
        assertEquals("描く", expectedAnswer)

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
