package io.github.digorydoo.goigoi.core.prog_study

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.db.AcceptableAnswer
import io.github.digorydoo.goigoi.core.db.PhraseOrSentence
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Word
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

    private val tobidasu = Word().apply {
        primaryForm = FuriganaString("【飛：と】び【出：だ】す")
        romaji = "tobidasu"
        translation.en = "to rush out"
        hint.en = "v.t. & i."
        level = JLPTLevel.N3
        usuallyInKana = false
        acceptForTranslation.apply {
            // For the sake of this test, we'll accept more than we would in the actual app.
            // Also, the flags inaccurate/interchangeable have been tweaked for the sake of this test.
            // Note that the combination inaccurate = true, interchangeable = true is illegal and is not tested.
            add(AcceptableAnswer("駆けつける", inaccurate = false, interchangeable = false))
            add(AcceptableAnswer("かけつける", inaccurate = false, interchangeable = false))
            add(AcceptableAnswer("飛出す", inaccurate = false, interchangeable = true))
            add(AcceptableAnswer("出す", inaccurate = true, interchangeable = false))
            add(AcceptableAnswer("だす", inaccurate = true, interchangeable = false))
            add(AcceptableAnswer("トビダス", inaccurate = false, interchangeable = true))
            add(AcceptableAnswer("ラッシュ", inaccurate = true, interchangeable = false))
        }
        alternativeSpellings.add("飛出す")
        alternativeReadings.add("ざびだす") // not an actual reading, just added for the sake of this unit test
        phrases.add(
            PhraseOrSentence().apply {
                primaryForm = FuriganaString("【急：きゅう】に【飛：と】び【出：だ】す")
                romaji = "kyū ni tobidasu"
                translation.en = "to suddenly rush out"
                explanation.en = "sample explanation"
                level = JLPTLevel.N3
            }
        )
        sentences.add(
            PhraseOrSentence().apply {
                primaryForm = FuriganaString("【猫：ねこ】が【飛：と】び【出：だ】した。")
                romaji = "Neko ga tobidashita."
                translation.en = "A cat rushed out."
                level = JLPTLevel.N3
                explanation.en = "sample explanation"
                wordFormToAsk = FuriganaString("【飛：と】び【出：だ】した")
                acceptableAnswers.add(FuriganaString("【飛：とび】【出：だ】した"))
            }
        )
    }

    private val mazui = Word().apply {
        primaryForm = FuriganaString("【不味：まず】い")
        romaji = "mazui"
        translation.en = "unpleasant taste"
        hint.en = "i-adjective"
        level = JLPTLevel.N5
        usuallyInKana = true

        // マズい isn't a common spelling, but we add it for the sake of this test
        acceptForTranslation.add(AcceptableAnswer("マズい", inaccurate = false, interchangeable = true))
        alternativeSpellings.add("マズい")

        phrases.add(
            PhraseOrSentence().apply {
                primaryForm = FuriganaString("まずくて【食：た】べられない")
                romaji = "mazukute taberarenai"
                translation.en = "so bad-tasting you cannot eat it"
                level = JLPTLevel.N4
                wordFormToAsk = FuriganaString("まずくて")
                acceptableAnswers.apply {
                    add(FuriganaString("【不味：まず】くて"))
                    add(FuriganaString("マズくて"))
                }
            }
        )
    }

    private val egaku = Word().apply {
        primaryForm = FuriganaString("【描：えが】く")
        translation.en = "to draw"
        hint.en = "v.t."
        level = JLPTLevel.N3
        acceptForTranslation.apply {
            add(AcceptableAnswer("かく", inaccurate = false, interchangeable = false))
            add(AcceptableAnswer("塗り替える", inaccurate = true, interchangeable = false))
            add(AcceptableAnswer("ぬりかえる", inaccurate = true, interchangeable = false))
        }
        alternativeReadings.add("かく")
    }

    @Test
    fun `should create correct QA from SHOW_WORD_ASK_NOTHING when not usually in kana`() {
        val kind = QAKind.SHOW_WORD_ASK_NOTHING

        val qa = factory.createQA(
            word = tobidasu,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(tobidasu, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.question)
        assertEquals("飛び出す", qa.question.second.kanji)
        assertEquals("とびだす", qa.question.second.kana)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.questionAfterReveal)
        assertEquals("飛び出す", qa.questionAfterReveal.second.kanji)
        assertEquals("とびだす", qa.questionAfterReveal.second.kana)

        assertEquals("", (qa.questionHint as OneOf.First).first)
        assertEquals("", qa.expectedAnswer)
        assertEquals("", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("", qa.kanjiOrKanaToReveal)
        assertEquals("to rush out", qa.translationToReveal)
        assertEquals("", qa.hintToReveal)
        assertNull(qa.explanation)
        assertFalse(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_WORD_ASK_NOTHING when usually in kana`() {
        val kind = QAKind.SHOW_WORD_ASK_NOTHING

        val qa = factory.createQA(
            word = mazui,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(mazui, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)

        assertIs<OneOf.First<String, FuriganaString>>(qa.question)
        assertEquals("まずい", qa.question.first)

        assertIs<OneOf.First<String, FuriganaString>>(qa.questionAfterReveal)
        assertEquals("まずい", qa.questionAfterReveal.first)

        assertEquals("", (qa.questionHint as OneOf.First).first)
        assertEquals("", qa.expectedAnswer)
        assertEquals("", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("", qa.kanjiOrKanaToReveal)
        assertEquals("unpleasant taste", qa.translationToReveal)
        assertEquals("", qa.hintToReveal)
        assertNull(qa.explanation)
        assertFalse(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_KANJI_ASK_KANA`() {
        val kind = QAKind.SHOW_KANJI_ASK_KANA

        val qa = factory.createQA(
            word = egaku,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(egaku, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)
        assertEquals("描く", (qa.question as OneOf.First).first)
        assertNull(qa.questionAfterReveal)
        assertEquals("", (qa.questionHint as OneOf.First).first)
        assertEquals("えがく", qa.expectedAnswer)

        // かく comes from alternateReadings and is therefore an acceptable answer. Other entries from
        // acceptForTranslation are not acceptable here, because their kana don't match the kanji that is
        // presented to the user.
        assertEquals("かく", qa.acceptableAnswers.joinToString("、") { it.answer })

        assertEquals("", qa.kanjiOrKanaToReveal)
        assertEquals("to draw", qa.translationToReveal)
        assertEquals("v.t.", qa.hintToReveal)
        assertNull(qa.explanation)
        assertFalse(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_KANA_ASK_KANJI`() {
        val kind = QAKind.SHOW_KANA_ASK_KANJI

        val qa = factory.createQA(
            word = tobidasu,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(tobidasu, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)
        assertEquals("とびだす", (qa.question as OneOf.First).first)
        assertNull(qa.questionAfterReveal)
        assertEquals("", (qa.questionHint as OneOf.First).first)
        assertEquals("飛び出す", qa.expectedAnswer)
        assertEquals("飛出す", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("", qa.kanjiOrKanaToReveal)
        assertEquals("to rush out", qa.translationToReveal)
        assertEquals("v.t. & i.", qa.hintToReveal)
        assertNull(qa.explanation)
        assertFalse(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_ROMAJI_ASK_KANA`() {
        val kind = QAKind.SHOW_ROMAJI_ASK_KANA

        val qa = factory.createQA(
            word = tobidasu,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(tobidasu, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)
        assertEquals("tobidasu", (qa.question as OneOf.First).first)
        assertNull(qa.questionAfterReveal)
        assertEquals("", (qa.questionHint as OneOf.First).first)
        assertEquals("とびだす", qa.expectedAnswer)
        assertEquals("", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("飛び出す", qa.kanjiOrKanaToReveal)
        assertEquals("to rush out", qa.translationToReveal)
        assertEquals("v.t. & i.", qa.hintToReveal)
        assertNull(qa.explanation)
        assertFalse(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_TRANSLATION_ASK_KANA`() {
        val kind = QAKind.SHOW_TRANSLATION_ASK_KANA

        val qa = factory.createQA(
            word = tobidasu,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(tobidasu, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)
        assertEquals("to rush out", (qa.question as OneOf.First).first)
        assertNull(qa.questionAfterReveal)
        assertEquals("v.t. & i.", (qa.questionHint as OneOf.First).first)
        assertEquals("とびだす", qa.expectedAnswer)
        assertEquals("かけつける、だす、トビダス、ラッシュ", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("飛び出す", qa.kanjiOrKanaToReveal)
        assertEquals("", qa.translationToReveal)
        assertEquals("", qa.hintToReveal)
        assertNull(qa.explanation)
        assertFalse(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS`() {
        val kind = QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS

        val qa = factory.createQA(
            word = tobidasu,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(tobidasu, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)
        assertEquals("to rush out", (qa.question as OneOf.First).first)
        assertNull(qa.questionAfterReveal)
        assertEquals("v.t. & i.", (qa.questionHint as OneOf.First).first)
        assertEquals("飛び出す", qa.expectedAnswer)
        assertEquals("駆けつける、飛出す、出す", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("とびだす", qa.kanjiOrKanaToReveal)
        assertEquals("", qa.translationToReveal)
        assertEquals("", qa.hintToReveal)
        assertNull(qa.explanation)
        assertFalse(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR`() {
        val kind = QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR

        val qa = factory.createQA(
            word = tobidasu,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(tobidasu, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)

        assertIs<OneOf.First<String, FuriganaString>>(qa.question)
        assertEquals("to rush out", qa.question.first)

        assertNull(qa.questionAfterReveal)
        assertEquals("v.t. & i.", (qa.questionHint as OneOf.First).first)
        assertEquals("飛び出す", qa.expectedAnswer)
        assertEquals("駆けつける、飛出す、出す", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("とびだす", qa.kanjiOrKanaToReveal)
        assertEquals("", qa.translationToReveal)
        assertNull(qa.explanation)
        assertTrue(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_PHRASE_ASK_NOTHING`() {
        val kind = QAKind.SHOW_PHRASE_ASK_NOTHING

        val qa = factory.createQA(
            word = tobidasu,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(tobidasu, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.question)
        assertEquals("急に飛び出す", qa.question.second.kanji)
        assertEquals("きゅうにとびだす", qa.question.second.kana)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.questionAfterReveal)
        assertEquals("急に飛び出す", qa.questionAfterReveal.second.kanji)
        assertEquals("きゅうにとびだす", qa.questionAfterReveal.second.kana)

        assertEquals("", (qa.questionHint as OneOf.First).first)
        assertEquals("", qa.expectedAnswer)
        assertEquals("", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("", qa.kanjiOrKanaToReveal)
        assertEquals("to suddenly rush out", qa.translationToReveal)
        assertEquals("sample explanation", qa.explanation?.raw)
        assertFalse(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_PHRASE_ASK_WORD_KANA when wordFormToAsk is not set`() {
        val kind = QAKind.SHOW_PHRASE_ASK_WORD_KANA

        val qa = factory.createQA(
            word = tobidasu,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(tobidasu, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)
        assertEquals("", qa.word.phrases[qa.index].wordFormToAsk.raw)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.question)
        assertEquals("急に___", qa.question.second.kanji)
        assertEquals("きゅうに___", qa.question.second.kana)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.questionAfterReveal)
        assertEquals("急に飛び出す", qa.questionAfterReveal.second.kanji)
        assertEquals("きゅうにとびだす", qa.questionAfterReveal.second.kana)

        assertEquals("to suddenly rush out", (qa.questionHint as OneOf.First).first)
        assertEquals("とびだす", qa.expectedAnswer)
        assertEquals("トビダス", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("", qa.kanjiOrKanaToReveal)
        assertEquals("", qa.translationToReveal)
        assertEquals("sample explanation", qa.explanation?.raw)
        assertFalse(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_PHRASE_ASK_WORD_KANA when wordFormToAsk is set`() {
        val kind = QAKind.SHOW_PHRASE_ASK_WORD_KANA

        val qa = factory.createQA(
            word = mazui,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(mazui, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)
        assertEquals("まずくて", qa.word.phrases[qa.index].wordFormToAsk.raw)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.question)
        assertEquals("___食べられない", qa.question.second.kanji)
        assertEquals("___たべられない", qa.question.second.kana)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.questionAfterReveal)
        assertEquals("まずくて食べられない", qa.questionAfterReveal.second.kanji)
        assertEquals("まずくてたべられない", qa.questionAfterReveal.second.kana)

        assertEquals("so bad-tasting you cannot eat it", (qa.questionHint as OneOf.First).first)
        assertEquals("まずくて", qa.expectedAnswer)
        assertEquals("マズくて", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("", qa.kanjiOrKanaToReveal)
        assertEquals("", qa.translationToReveal)
        assertNull(qa.explanation?.raw)
        assertFalse(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_PHRASE_ASK_WORD_KANJI when wordFormToAsk is not set`() {
        val kind = QAKind.SHOW_PHRASE_ASK_WORD_KANJI

        val qa = factory.createQA(
            word = tobidasu,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(tobidasu, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)
        assertEquals("", qa.word.phrases[qa.index].wordFormToAsk.raw)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.question)
        assertEquals("急に___", qa.question.second.kanji)
        assertEquals("きゅうに___", qa.question.second.kana)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.questionAfterReveal)
        assertEquals("急に飛び出す", qa.questionAfterReveal.second.kanji)
        assertEquals("きゅうにとびだす", qa.questionAfterReveal.second.kana)

        assertEquals("to suddenly rush out", (qa.questionHint as OneOf.First).first)
        assertEquals("飛び出す", qa.expectedAnswer)
        assertEquals("飛出す", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("", qa.kanjiOrKanaToReveal)
        assertEquals("", qa.translationToReveal)
        assertEquals("sample explanation", qa.explanation?.raw)
        assertTrue(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_PHRASE_ASK_WORD_KANJI when wordFormToAsk is set`() {
        val kind = QAKind.SHOW_PHRASE_ASK_WORD_KANJI

        val qa = factory.createQA(
            word = mazui,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(mazui, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)
        assertEquals("まずくて", qa.word.phrases[qa.index].wordFormToAsk.raw)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.question)
        assertEquals("___食べられない", qa.question.second.kanji)
        assertEquals("___たべられない", qa.question.second.kana)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.questionAfterReveal)
        assertEquals("まずくて食べられない", qa.questionAfterReveal.second.kanji)
        assertEquals("まずくてたべられない", qa.questionAfterReveal.second.kana)

        assertEquals("so bad-tasting you cannot eat it", (qa.questionHint as OneOf.First).first)
        assertEquals("まずくて", qa.expectedAnswer)

        // Because マズくて does not include any furigana, FuriganaString will return it unchanged when trying to render
        // it in kanji. That's why it's also listed here as an acceptable answer even though we ask WORD_KANJI. That's
        // no problem, the user will simply not be able to produce that answer, and it isn't incorrect either.
        assertEquals("不味くて、マズくて", qa.acceptableAnswers.joinToString("、") { it.answer })

        assertEquals("", qa.kanjiOrKanaToReveal)
        assertEquals("", qa.translationToReveal)
        assertNull(qa.explanation?.raw)
        assertTrue(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA`() {
        val kind = QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA

        val qa = factory.createQA(
            word = tobidasu,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(tobidasu, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)

        assertIs<OneOf.First<String, FuriganaString>>(qa.question)
        assertEquals("to suddenly rush out", qa.question.first)

        assertNull(qa.questionAfterReveal)

        assertIs<OneOf.Second<String, Hint>>(qa.questionHint)
        assertEquals(Hint.PHRASE, qa.questionHint.second)

        assertEquals("きゅうにとびだす", qa.expectedAnswer)
        assertEquals("", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("", qa.kanjiOrKanaToReveal)
        assertEquals("", qa.translationToReveal)
        assertEquals("sample explanation", qa.explanation?.raw)
        assertFalse(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_SENTENCE_ASK_NOTHING`() {
        val kind = QAKind.SHOW_SENTENCE_ASK_NOTHING

        val qa = factory.createQA(
            word = tobidasu,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(tobidasu, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.question)
        assertEquals("猫が飛び出した。", qa.question.second.kanji)
        assertEquals("ねこがとびだした。", qa.question.second.kana)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.questionAfterReveal)
        assertEquals("猫が飛び出した。", qa.questionAfterReveal.second.kanji)
        assertEquals("ねこがとびだした。", qa.questionAfterReveal.second.kana)

        assertEquals("", (qa.questionHint as OneOf.First).first)
        assertEquals("", qa.expectedAnswer)
        assertEquals("", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("", qa.kanjiOrKanaToReveal)
        assertEquals("A cat rushed out.", qa.translationToReveal)
        assertEquals("sample explanation", qa.explanation?.raw)
        assertFalse(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_SENTENCE_ASK_WORD_KANA`() {
        val kind = QAKind.SHOW_SENTENCE_ASK_WORD_KANA

        val qa = factory.createQA(
            word = tobidasu,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(tobidasu, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.question)
        assertEquals("猫が___。", qa.question.second.kanji)
        assertEquals("ねこが___。", qa.question.second.kana)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.questionAfterReveal)
        assertEquals("猫が飛び出した。", qa.questionAfterReveal.second.kanji)
        assertEquals("ねこがとびだした。", qa.questionAfterReveal.second.kana)

        assertEquals("A cat rushed out.", (qa.questionHint as OneOf.First).first)
        assertEquals("とびだした", qa.expectedAnswer)
        assertEquals("", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("", qa.kanjiOrKanaToReveal)
        assertEquals("", qa.translationToReveal)
        assertEquals("sample explanation", qa.explanation?.raw)
        assertFalse(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

    @Test
    fun `should create correct QA from SHOW_SENTENCE_ASK_WORD_KANJI`() {
        val kind = QAKind.SHOW_SENTENCE_ASK_WORD_KANJI

        val qa = factory.createQA(
            word = tobidasu,
            kind = kind,
            index = 0,
            showFuriganaWithQuestion = false,
        )

        assertSame(tobidasu, qa.word)
        assertEquals(kind, qa.kind)
        assertEquals(0, qa.index)
        assertFalse(qa.showFuriganaWithQuestion)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.question)
        assertEquals("猫が___。", qa.question.second.kanji)
        assertEquals("ねこが___。", qa.question.second.kana)

        assertIs<OneOf.Second<String, FuriganaString>>(qa.questionAfterReveal)
        assertEquals("猫が飛び出した。", qa.questionAfterReveal.second.kanji)
        assertEquals("ねこがとびだした。", qa.questionAfterReveal.second.kana)

        assertEquals("A cat rushed out.", (qa.questionHint as OneOf.First).first)
        assertEquals("飛び出した", qa.expectedAnswer)
        assertEquals("飛出した", qa.acceptableAnswers.joinToString("、") { it.answer })
        assertEquals("", qa.kanjiOrKanaToReveal)
        assertEquals("", qa.translationToReveal)
        assertEquals("sample explanation", qa.explanation?.raw)
        assertTrue(qa.presentWholeWords)
        assertFalse(qa.allowExtendedKeyboard)
    }

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
}
