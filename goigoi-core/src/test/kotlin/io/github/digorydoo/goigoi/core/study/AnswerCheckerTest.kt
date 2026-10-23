package io.github.digorydoo.goigoi.core.study

import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.db.AcceptableAnswer
import io.github.digorydoo.goigoi.core.db.AnswerAccuracy
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.db.WordHint
import io.github.digorydoo.goigoi.core.prog_study.QAKind
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AnswerCheckerTest {
    private val answerChecker = AnswerChecker()

    private fun createQA(
        expectedAnswer: String = "",
        acceptableAnswers: List<AcceptableAnswer> = emptyList(),
        kind: QAKind = QAKind.SHOW_KANJI_ASK_KANA,
        word: Word = Word(),
    ): QuestionAndAnswer {
        return QuestionAndAnswer(
            word = word,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
            question = OneOf.First(""),
            questionAfterReveal = null,
            questionHint = OneOf.First(""),
            expectedAnswer = expectedAnswer,
            acceptableAnswers = acceptableAnswers,
            kanjiOrKanaToReveal = "",
            translationToReveal = "",
            hintToReveal = "",
            explanation = null,
            presentWholeWords = false,
            allowExtendedKeyboard = false,
        )
    }

    @Test
    fun `should return TRIVIAL when QA kind does not ask anything`() {
        val qa = createQA(kind = QAKind.SHOW_WORD_ASK_NOTHING)
        val result = answerChecker.check(qa, "anything")
        assertEquals(Answer.TRIVIAL, result)
    }

    @Test
    fun `should return CORRECT when answer matches expected answer exactly`() {
        val qa = createQA(expectedAnswer = "たべる")
        val result = answerChecker.check(qa, "たべる")
        assertEquals(Answer.CORRECT, result)
    }

    @Test
    fun `should return CORRECT when answer matches an accurate acceptable answer`() {
        val qa = createQA(
            expectedAnswer = "食べる",
            acceptableAnswers = listOf(AcceptableAnswer("たべる", AnswerAccuracy.CORRECT))
        )
        val result = answerChecker.check(qa, "たべる")
        assertEquals(Answer.CORRECT, result)
    }

    @Test
    fun `should return CORRECT_BUT_INACCURATE when answer matches an inaccurate acceptable answer`() {
        val qa = createQA(
            expectedAnswer = "食べる",
            acceptableAnswers = listOf(AcceptableAnswer("たべる", AnswerAccuracy.INACCURATE))
        )
        val result = answerChecker.check(qa, "たべる")
        assertEquals(Answer.CORRECT_BUT_INACCURATE, result)
    }

    @Test
    fun `should return CORRECT_EXCEPT_KANA_SIZE when user enters wrong kana size for expected answer`() {
        val qa = createQA(expectedAnswer = "きょう")
        val result = answerChecker.check(qa, "きよう")
        assertEquals(Answer.CORRECT_EXCEPT_KANA_SIZE, result)
    }

    @Test
    fun `should return CORRECT_EXCEPT_KANA_SIZE when user enters wrong kana size for accurate acceptable answer`() {
        val qa = createQA(
            expectedAnswer = "今日",
            acceptableAnswers = listOf(AcceptableAnswer("きょう", AnswerAccuracy.CORRECT))
        )
        val result = answerChecker.check(qa, "きよう")
        assertEquals(Answer.CORRECT_EXCEPT_KANA_SIZE, result)
    }

    @Test
    fun `should return CORRECT_EXCEPT_KANA_SIZE when user enters wrong kana size for inaccurate acceptable answer`() {
        val qa = createQA(
            expectedAnswer = "今日",
            acceptableAnswers = listOf(AcceptableAnswer("きょう", AnswerAccuracy.CORRECT))
        )
        val result = answerChecker.check(qa, "きよう")
        assertEquals(Answer.CORRECT_EXCEPT_KANA_SIZE, result)
    }

    @Test
    fun `should return CORRECT when answer differs only by kind of kana for expected answer`() {
        val qa = createQA(expectedAnswer = "あ")
        val result = answerChecker.check(qa, "ア")
        assertEquals(Answer.CORRECT, result)
    }

    @Test
    fun `should return CORRECT when answer differs only by kind of kana for acceptable answer`() {
        val qa = createQA(
            expectedAnswer = "部屋",
            acceptableAnswers = listOf(AcceptableAnswer("へや", AnswerAccuracy.CORRECT))
        )
        val result = answerChecker.check(qa, "ヘヤ")
        assertEquals(Answer.CORRECT, result)
    }

    @Test
    fun `should return CORRECT when user omitted suru or o suru from expected answer`() {
        val qaSuru = createQA(expectedAnswer = "掃除する")
        assertEquals(Answer.CORRECT, answerChecker.check(qaSuru, "掃除"))

        val qaWoSuru = createQA(expectedAnswer = "勉強をする")
        assertEquals(Answer.CORRECT, answerChecker.check(qaWoSuru, "勉強"))
    }

    @Test
    fun `should return CORRECT when user omitted suru from acceptable answer`() {
        val qa = createQA(
            expectedAnswer = "掃除",
            acceptableAnswers = listOf(AcceptableAnswer("掃除する", AnswerAccuracy.CORRECT))
        )
        assertEquals(Answer.CORRECT, answerChecker.check(qa, "掃除"))
    }

    @Test
    fun `should return CORRECT when user added suru to a suru noun`() {
        val word = Word().apply { hint2 = WordHint.NOUN_SURU }
        val qa = createQA(
            word = word,
            expectedAnswer = "掃除",
            kind = QAKind.SHOW_TRANSLATION_ASK_KANA
        )
        assertEquals(Answer.CORRECT, answerChecker.check(qa, "掃除する"))
    }

    @Test
    fun `should return CORRECT_BUT_INACCURATE when user added o suru instead of suru to a suru noun`() {
        val word = Word().apply { hint2 = WordHint.NOUN_SURU }
        val qa = createQA(
            word = word,
            expectedAnswer = "掃除する",
            kind = QAKind.SHOW_TRANSLATION_ASK_KANA
        )
        assertEquals(Answer.CORRECT_BUT_INACCURATE, answerChecker.check(qa, "掃除をする"))
    }

    @Test
    fun `should not apply suru addition check when kind involves phrases or sentences`() {
        val word = Word().apply { hint2 = WordHint.NOUN_SURU }

        val qa1 = createQA(
            word = word,
            expectedAnswer = "掃除", // not really a phrase, though
            kind = QAKind.SHOW_PHRASE_ASK_WORD_KANA
        )
        assertEquals(Answer.WRONG, answerChecker.check(qa1, "掃除する"))

        val qa2 = createQA(
            word = word,
            expectedAnswer = "掃除しません",
            kind = QAKind.SHOW_PHRASE_ASK_WORD_KANA
        )
        assertEquals(Answer.WRONG, answerChecker.check(qa2, "掃除する"))
        assertEquals(Answer.WRONG, answerChecker.check(qa2, "掃除しませんする"))
    }

    @Test
    fun `should not apply suru addition check when word is not a suru word`() {
        val word = Word()
        val qa = createQA(
            word = word,
            expectedAnswer = "猫",
            kind = QAKind.SHOW_TRANSLATION_ASK_KANA
        )
        assertEquals(Answer.WRONG, answerChecker.check(qa, "猫する"))
    }

    @Test
    fun `should return CORRECT when answer is missing special characters present in expected answer`() {
        val qa = createQA(expectedAnswer = "〜こんにちは。")
        assertEquals(Answer.CORRECT, answerChecker.check(qa, "こんにちは"))
    }

    @Test
    fun `should return CORRECT when answer is missing special characters present in acceptable answer`() {
        val qa = createQA(
            expectedAnswer = "ごちそうさまでした",
            acceptableAnswers = listOf(
                AcceptableAnswer("〜ご馳走様でした。", AnswerAccuracy.CORRECT)
            )
        )
        assertEquals(Answer.CORRECT, answerChecker.check(qa, "ご馳走様でした"))
    }

    @Test
    fun `should return WRONG when answer is completely wrong`() {
        val qa = createQA(expectedAnswer = "いぬ")
        assertEquals(Answer.WRONG, answerChecker.check(qa, "ねこ"))
    }
}
