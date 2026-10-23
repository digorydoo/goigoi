package io.github.digorydoo.goigoi.core.qa_factory

import ch.digorydoo.kutils.cjk.JLPTLevel
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.prog_study.QAKind
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer
import io.github.digorydoo.goigoi.core.stats.IWordStats

class QAFactory(private val stats: IWordStats) {
    class QuestionData(val word: Word, kind: QAKind, index: Int) {
        val presentWholeWords = when (kind) {
            QAKind.SHOW_KANJI_ASK_KANA -> false
            QAKind.SHOW_KANA_ASK_KANJI -> false
            QAKind.SHOW_ROMAJI_ASK_KANA -> false
            QAKind.SHOW_TRANSLATION_ASK_KANA -> false
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> true
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS -> false
            QAKind.SHOW_WORD_ASK_NOTHING -> false
            QAKind.SHOW_PHRASE_ASK_NOTHING -> false
            QAKind.SHOW_SENTENCE_ASK_NOTHING -> false
            QAKind.SHOW_PHRASE_ASK_WORD_KANA -> false
            QAKind.SHOW_PHRASE_ASK_WORD_KANJI -> true
            QAKind.SHOW_SENTENCE_ASK_WORD_KANA -> false
            QAKind.SHOW_SENTENCE_ASK_WORD_KANJI -> true
            QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> false
        }

        val phrase = word.phrases.getOrNull(index)
        val sentence = word.sentences.getOrNull(index)

        val phrasePrimaryFormInParts = phrase?.primaryFormInParts(word)
        val sentencePrimaryFormInParts = sentence?.primaryFormInParts(word)
    }

    private val showKanaAskKanji = ShowKanaAskKanjiStrategy()
    private val showKanjiAskKana = ShowKanjiAskKanaStrategy()
    private val showPhraseAskNothing = ShowPhraseAskNothingStrategy()
    private val showPhraseAskWordKana = ShowPhraseAskWordKanaStrategy()
    private val showPhraseAskWordKanji = ShowPhraseAskWordKanjiStrategy()
    private val showPhraseTranslationAskPhraseKana = ShowPhraseTranslationAskPhraseKanaStrategy()
    private val showRomajiAskKana = ShowRomajiAskKanaStrategy()
    private val showSentenceAskNothing = ShowSentenceAskNothingStrategy()
    private val showSentenceAskWordKana = ShowSentenceAskWordKanaStrategy()
    private val showSentenceAskWordKanji = ShowSentenceAskWordKanjiStrategy()
    private val showTranslationAskKana = ShowTranslationAskKanaStrategy()
    private val showTranslationAskKanjiAmongSimilar = ShowTranslationAskKanjiAmongSimilarStrategy()
    private val showTranslationAskKanjiAmongWords = ShowTranslationAskKanjiAmongWordsStrategy()
    private val showWordAskNothing = ShowWordAskNothingStrategy()

    fun createQA(
        word: Word,
        kind: QAKind,
        index: Int,
        showFuriganaWithQuestion: Boolean,
    ): QuestionAndAnswer {
        val strategy = when (kind) {
            QAKind.SHOW_KANJI_ASK_KANA -> showKanjiAskKana
            QAKind.SHOW_KANA_ASK_KANJI -> showKanaAskKanji
            QAKind.SHOW_ROMAJI_ASK_KANA -> showRomajiAskKana
            QAKind.SHOW_TRANSLATION_ASK_KANA -> showTranslationAskKana
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS -> showTranslationAskKanjiAmongWords
            QAKind.SHOW_WORD_ASK_NOTHING -> showWordAskNothing
            QAKind.SHOW_PHRASE_ASK_NOTHING -> showPhraseAskNothing
            QAKind.SHOW_SENTENCE_ASK_NOTHING -> showSentenceAskNothing
            QAKind.SHOW_PHRASE_ASK_WORD_KANJI -> showPhraseAskWordKanji
            QAKind.SHOW_SENTENCE_ASK_WORD_KANJI -> showSentenceAskWordKanji
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> showTranslationAskKanjiAmongSimilar
            QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> showPhraseTranslationAskPhraseKana
            QAKind.SHOW_PHRASE_ASK_WORD_KANA -> showPhraseAskWordKana
            QAKind.SHOW_SENTENCE_ASK_WORD_KANA -> showSentenceAskWordKana
        }

        val data = QuestionData(word, kind, index)
        val question = strategy.getQuestion(data)

        val allowExtendedKeyboard = when {
            data.presentWholeWords -> false // we're going to use FIXED_KEYS
            word.level == JLPTLevel.N5 -> false // N5 learners might be too confused by full kana keyboard
            kind == QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> false // answers would get too ambiguous
            stats.getWordTotalSeenCount(word) < MIN_SEEN_COUNT_FOR_EXTENDED_KEYBOARD -> false // FIXED_KEYS is easier
            stats.getWordTotalRating(word) < MIN_RATING_FOR_EXTENDED_KEYBOARD -> false
            else -> true
        }

        return QuestionAndAnswer(
            word = word,
            kind = kind,
            index = index,
            showFuriganaWithQuestion = showFuriganaWithQuestion,
            question = question,
            questionAfterReveal = strategy.getQuestionAfterReveal(data),
            questionHint = strategy.getQuestionHint(data),
            expectedAnswer = strategy.getExpectedAnswer(data),
            acceptableAnswers = strategy.getAcceptableAnswers(data),
            kanjiOrKanaToReveal = strategy.getKanjiOrKanaToReveal(data),
            translationToReveal = strategy.getTranslationToReveal(data),
            hintToReveal = strategy.getHintToReveal(data),
            explanation = strategy.getExplanation(data),
            presentWholeWords = data.presentWholeWords,
            allowExtendedKeyboard = allowExtendedKeyboard,
        )
    }

    companion object {
        private const val MIN_SEEN_COUNT_FOR_EXTENDED_KEYBOARD = 5
        private const val MIN_RATING_FOR_EXTENDED_KEYBOARD = 0.77f
    }
}
