package io.github.digorydoo.goigoi.core.qa_factory

import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.getMockWordTobidasu
import io.github.digorydoo.goigoi.core.prog_study.QAKind
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer.Hint
import io.github.digorydoo.goigoi.core.stats.IWordStats
import io.github.digorydoo.goigoi.core.stats.StatsKey
import kotlin.test.*

internal class FakeWordStats: IWordStats {
    var totalSeenCount = 0
    var totalRating = 0f

    override fun getWordSeenCount(word: Word, key: StatsKey) = 0
    override fun getWordCorrectCount(word: Word, key: StatsKey) = 0
    override fun getWordWrongCount(word: Word, key: StatsKey) = 0
    override fun getWordTotalSeenCount(word: Word) = totalSeenCount
    override fun getWordTotalCorrectCount(word: Word) = 0
    override fun getWordTotalWrongCount(word: Word) = 0
    override fun getWordStudyProgress(word: Word) = 0f
    override fun getWordRating(word: Word, key: StatsKey) = 0f
    override fun getWordTotalRating(word: Word) = totalRating
    override fun getWordStudyMoment(word: Word) = null
    override fun getWordIsAsleep(word: Word) = false
    override fun getPhraseTotalSeenCount(word: Word, phraseIdx: Int) = 0
    override fun getSentenceTotalSeenCount(word: Word, sentenceIdx: Int) = 0
    override fun resetWordStatsExpensively(w: Word, u: Unyt, fakeNumCorrect: Int?, fakeNumWrong: Int?) {}
}

internal class QAFactoryTest {
    private val fakeStats = FakeWordStats()
    private val factory = QAFactory(fakeStats)
    private val tobidasu = getMockWordTobidasu()

    @Test
    fun `should set allowExtendedKeyboard to true when conditions are met`() {
        fakeStats.totalSeenCount = 5
        fakeStats.totalRating = 0.77f

        val qa = factory.createQA(
            word = tobidasu,
            kind = QAKind.SHOW_KANJI_ASK_KANA,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertTrue(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should set allowExtendedKeyboard to false when conditions aren't met`() {
        fakeStats.totalSeenCount = 4
        fakeStats.totalRating = 0.69f

        val qa = factory.createQA(
            word = tobidasu,
            kind = QAKind.SHOW_KANJI_ASK_KANA,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should have an empty questionAfterReveal when not asking anything`() {
        // questionAfterReveal should be not null if and only if user was asked something with an
        // empty gap to be filled in, which is obviously not the case if doesNotAskAnything is true.

        assertTrue(tobidasu.hint.en.isNotEmpty())

        QAKind.entries.filter { it.doesNotAskAnything }.forEach { kind ->
            val qa = factory.createQA(
                word = tobidasu,
                kind = kind,
                index = 0,
                showFuriganaWithQuestion = false,
            )
            val msg = "Kind $kind"
            assertNull(qa.questionAfterReveal, msg)
        }
    }

    @Test
    fun `should have an empty questionHint when not asking anything`() {
        // questionHint is a hint to make the question less ambiguous, but it is irrelevant when
        // we're just presenting a word, phrase or sentence as indicated by doesNotAskAnything.

        assertTrue(tobidasu.hint.en.isNotEmpty())

        QAKind.entries.filter { it.doesNotAskAnything }.forEach { kind ->
            val qa = factory.createQA(
                word = tobidasu,
                kind = kind,
                index = 0,
                showFuriganaWithQuestion = false,
            )
            val msg = "Kind $kind"
            val questionHint = qa.questionHint
            assertIs<OneOf.First<String, Hint>>(questionHint, msg)
            assertEquals("", questionHint.first, msg)
        }
    }

    @Test
    fun `should have an empty expectedAnswer when not asking anything`() {
        QAKind.entries.filter { it.doesNotAskAnything }.forEach { kind ->
            val qa = factory.createQA(
                word = tobidasu,
                kind = kind,
                index = 0,
                showFuriganaWithQuestion = false,
            )
            val msg = "Kind $kind"
            assertEquals("", qa.expectedAnswer, msg)
        }
    }

    @Test
    fun `should have an empty list of acceptable answers when not asking anything`() {
        assertTrue(tobidasu.acceptableAnswers.isNotEmpty())
        assertTrue(tobidasu.alternativeSpellings.isNotEmpty())

        QAKind.entries.filter { it.doesNotAskAnything }.forEach { kind ->
            val qa = factory.createQA(
                word = tobidasu,
                kind = kind,
                index = 0,
                showFuriganaWithQuestion = false,
            )
            val msg = "Kind $kind"
            assertEquals("", qa.acceptableAnswers.joinToString(", "), msg)
        }
    }

    @Test
    fun `should have empty kanjiOrKanaToReveal when not asking anything`() {
        // When doesNotAskAnything is true, we often use the TategakiText to display the word, phrase or sentence.
        // But that view leaves too little room for extras, therefore we don't want to reveal anything other than
        // the translation.

        assertTrue(tobidasu.kanji.isNotEmpty())
        assertTrue(tobidasu.kana.isNotEmpty())

        QAKind.entries.filter { it.doesNotAskAnything }.forEach { kind ->
            val qa = factory.createQA(
                word = tobidasu,
                kind = kind,
                index = 0,
                showFuriganaWithQuestion = false,
            )
            val msg = "Kind $kind"
            assertEquals("", qa.kanjiOrKanaToReveal, msg)
        }
    }

    @Test
    fun `should have empty hintToReveal when not asking anything`() {
        // No room for hint when TategakiText is involved.

        assertTrue(tobidasu.hint.en.isNotEmpty())

        QAKind.entries.filter { it.doesNotAskAnything }.forEach { kind ->
            val qa = factory.createQA(
                word = tobidasu,
                kind = kind,
                index = 0,
                showFuriganaWithQuestion = false,
            )
            val msg = "Kind $kind"
            assertEquals("", qa.hintToReveal, msg)
        }
    }

    @Test
    fun `should not show any explanation when not asking anything`() {
        // No room for explanation when TategakiText is involved.

        assertTrue(tobidasu.phrases[0].explanation.en.isNotEmpty())
        assertTrue(tobidasu.sentences[0].explanation.en.isNotEmpty())

        QAKind.entries.filter { it.doesNotAskAnything }.forEach { kind ->
            val qa = factory.createQA(
                word = tobidasu,
                kind = kind,
                index = 0,
                showFuriganaWithQuestion = false,
            )
            val msg = "Kind $kind"
            assertNull(qa.explanation, msg)
        }
    }

    @Test
    fun `should set presentWholeWords to false when not asking anything`() {
        // The flag is irrelevant when doesNotAskAnything is true. Still, it is better to be consistent.

        QAKind.entries.filter { it.doesNotAskAnything }.forEach { kind ->
            val qa = factory.createQA(
                word = tobidasu,
                kind = kind,
                index = 0,
                showFuriganaWithQuestion = false,
            )
            val msg = "Kind $kind"
            assertFalse(qa.presentWholeWords, msg)
        }
    }
}
