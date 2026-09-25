package io.github.digorydoo.goigoi.core.prog_study

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.cjk.isHiragana
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.db.PhraseOrSentence
import io.github.digorydoo.goigoi.core.db.PhraseOrSentence.SplitPrimaryForm
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.stats.Stats

// This class is immutable
class QuestionAndAnswer private constructor(
    val word: Word,
    val kind: QAKind,
    val index: Int,
    val showFuriganaWithQuestion: Boolean,
    val question: OneOf<String, FuriganaString>,

    @Deprecated("Only for legacy activity")
    val questionWithoutFurigana: String,

    val questionAfterReveal: OneOf<String, FuriganaString>?, // null = no change
    val questionHint: OneOf<String, Hint>,
    val answers: List<String>,
    val kanjiOrKanaToReveal: String,
    val translationToReveal: String,
    val hintToReveal: String,
    val explanation: FuriganaString?,
    val presentWholeWords: Boolean,
    val allowExtendedKeyboard: Boolean,
) {
    enum class Hint { PHRASE }
    enum class FontType { DEFAULT, BOLD_HIRAGANA, BOLD_KATAKANA, PENCIL, CALLIGRAPHY }

    @Deprecated("Only for legacy activity")
    var fontType = FontType.DEFAULT // will be set by Choreographer

    @Deprecated("Only for legacy activity")
    var furiganaRelVOffset = 0.0f // dito

    companion object {
        private const val GAP = "___"
        private const val GAP_WITH_EMPTY_FURIGANA = "$GAP【：】" // to preserve the height of the furigana

        fun create(
            word: Word,
            stats: Stats,
            kind: QAKind,
            index: Int,
            showFuriganaWithQuestion: Boolean,
        ): QuestionAndAnswer {
            val phrase = word.phrases.getOrNull(index)
            val sentence = word.sentences.getOrNull(index)

            val phrasePrimaryFormInParts = phrase?.primaryFormInParts(word)
            val sentencePrimaryFormInParts = sentence?.primaryFormInParts(word)

            val presentWholeWords = getPresentWholeWords(kind)

            val question: OneOf<String, FuriganaString> = getQuestion(
                word,
                kind,
                phrase,
                sentence,
                phrasePrimaryFormInParts,
                sentencePrimaryFormInParts,
                // When we're not presenting whole words, i.e. allowing the user to type in, we do not ask for the
                // word form suffix (e.g. 〜ます, 〜ました, etc.) in order to make the answer less ambiguous.
                gapIncludesSuffix = presentWholeWords,
            )

            val allowExtendedKeyboard = when {
                presentWholeWords -> false // whole words need FIXED_KEYS
                word.level == JLPTLevel.N5 -> false // N5 learners have rōmaji instead
                kind == QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> false // answers would get too ambiguous
                stats.getWordTotalSeenCount(word) < 5 -> false // FIXED_KEYS is easier
                stats.getWordTotalRating(word) < 0.77f -> false
                else -> true
            }

            return QuestionAndAnswer(
                word = word,
                kind = kind,
                index = index,
                showFuriganaWithQuestion = showFuriganaWithQuestion,
                question = question,
                questionWithoutFurigana = when (question) {
                    is OneOf.First -> question.first
                    is OneOf.Second -> question.second.kanji
                },
                questionAfterReveal = getQuestionAfterReveal(word, kind, phrase, sentence),
                questionHint = getQuestionHint(word, kind, phrase, sentence),
                answers = getAnswers(
                    word,
                    kind,
                    phrase,
                    phrasePrimaryFormInParts,
                    sentencePrimaryFormInParts,
                    includeSuffix = presentWholeWords,
                ),
                kanjiOrKanaToReveal = getKanjiOrKanaToReveal(word, kind, phrase),
                translationToReveal = getTranslationToReveal(word, kind, phrase, sentence),
                hintToReveal = getHintToReveal(word, kind),
                explanation = getExplanation(kind, phrase, sentence),
                presentWholeWords = presentWholeWords,
                allowExtendedKeyboard = allowExtendedKeyboard,
            )
        }

        private fun getQuestion(
            word: Word,
            kind: QAKind,
            phrase: PhraseOrSentence?,
            sentence: PhraseOrSentence?,
            phrasePrimaryFormInParts: SplitPrimaryForm?,
            sentencePrimaryFormInParts: SplitPrimaryForm?,
            gapIncludesSuffix: Boolean,
        ): OneOf<String, FuriganaString> = when (kind) {
            QAKind.SHOW_KANJI_ASK_KANA -> OneOf.First(word.kanji)
            QAKind.SHOW_KANA_ASK_KANJI -> OneOf.First(word.kana)
            QAKind.SHOW_ROMAJI_ASK_KANA -> OneOf.First(word.romaji)
            QAKind.SHOW_TRANSLATION_ASK_KANA -> OneOf.First(word.translation.withSystemLang)

            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
            -> OneOf.First(word.translation.withSystemLang)

            QAKind.SHOW_WORD_ASK_NOTHING -> when {
                word.usuallyInKana -> OneOf.First(word.kana)
                else -> OneOf.Second(word.primaryForm)
            }

            QAKind.SHOW_PHRASE_ASK_NOTHING -> OneOf.Second(phrase!!.primaryForm)
            QAKind.SHOW_SENTENCE_ASK_NOTHING -> OneOf.Second(sentence!!.primaryForm)

            QAKind.SHOW_PHRASE_ASK_WORD_KANA,
            QAKind.SHOW_PHRASE_ASK_WORD_KANJI,
            -> OneOf.Second(
                phrasePrimaryFormInParts!!.let { phrase ->
                    val gap = if (phrase.wordStem.kanji != phrase.wordStem.kana) GAP_WITH_EMPTY_FURIGANA else GAP
                    if (gapIncludesSuffix) {
                        FuriganaString(phrase.begin.raw + gap + phrase.end.raw)
                    } else {
                        FuriganaString(phrase.begin.raw + gap + phrase.wordSuffix + phrase.end.raw)
                    }
                }
            )

            QAKind.SHOW_SENTENCE_ASK_WORD_KANA,
            QAKind.SHOW_SENTENCE_ASK_WORD_KANJI,
            -> OneOf.Second(
                sentencePrimaryFormInParts!!.let { sentence ->
                    val gap = if (sentence.wordStem.kanji != sentence.wordStem.kana) GAP_WITH_EMPTY_FURIGANA else GAP
                    if (gapIncludesSuffix) {
                        FuriganaString(sentence.begin.raw + gap + sentence.end.raw)
                    } else {
                        FuriganaString(sentence.begin.raw + gap + sentence.wordSuffix + sentence.end.raw)
                    }
                }
            )

            QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> OneOf.First(phrase!!.translation.withSystemLang)
        }

        private fun getQuestionAfterReveal(
            word: Word,
            kind: QAKind,
            phrase: PhraseOrSentence?,
            sentence: PhraseOrSentence?,
        ): OneOf<String, FuriganaString>? = when (kind) {
            QAKind.SHOW_WORD_ASK_NOTHING -> when {
                word.usuallyInKana -> OneOf.First(word.kana)
                else -> OneOf.Second(word.primaryForm)
            }

            QAKind.SHOW_PHRASE_ASK_NOTHING,
            QAKind.SHOW_PHRASE_ASK_WORD_KANA,
            QAKind.SHOW_PHRASE_ASK_WORD_KANJI,
            -> OneOf.Second(phrase!!.primaryForm)

            QAKind.SHOW_SENTENCE_ASK_NOTHING,
            QAKind.SHOW_SENTENCE_ASK_WORD_KANA,
            QAKind.SHOW_SENTENCE_ASK_WORD_KANJI,
            -> OneOf.Second(sentence!!.primaryForm)

            else -> null // no change
        }

        private fun getQuestionHint(
            word: Word,
            kind: QAKind,
            phrase: PhraseOrSentence?,
            sentence: PhraseOrSentence?,
        ): OneOf<String, Hint> = when (kind) {
            QAKind.SHOW_KANJI_ASK_KANA,
            QAKind.SHOW_KANA_ASK_KANJI,
            QAKind.SHOW_ROMAJI_ASK_KANA,
            QAKind.SHOW_WORD_ASK_NOTHING,
            QAKind.SHOW_PHRASE_ASK_NOTHING,
            QAKind.SHOW_SENTENCE_ASK_NOTHING,
            -> OneOf.First("")

            QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
            -> OneOf.Second(Hint.PHRASE)

            QAKind.SHOW_TRANSLATION_ASK_KANA,
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
            -> OneOf.First(word.hintsWithSystemLang)

            QAKind.SHOW_PHRASE_ASK_WORD_KANA,
            QAKind.SHOW_PHRASE_ASK_WORD_KANJI,
            -> OneOf.First(phrase?.translation?.withSystemLang ?: "")

            QAKind.SHOW_SENTENCE_ASK_WORD_KANA,
            QAKind.SHOW_SENTENCE_ASK_WORD_KANJI,
            -> OneOf.First(sentence?.translation?.withSystemLang ?: "")
        }

        private fun getAnswers(
            word: Word,
            kind: QAKind,
            phrase: PhraseOrSentence?,
            phrasePrimaryFormInParts: SplitPrimaryForm?,
            sentencePrimaryFormInParts: SplitPrimaryForm?,
            includeSuffix: Boolean,
        ) = when (kind) {
            QAKind.SHOW_KANJI_ASK_KANA,
            QAKind.SHOW_ROMAJI_ASK_KANA,
            QAKind.SHOW_TRANSLATION_ASK_KANA,
            -> mutableListOf(word.kana).apply { addAll(word.synonyms.map { it.kana }) }

            QAKind.SHOW_KANA_ASK_KANJI,
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
            -> mutableListOf(word.kanji).apply { addAll(word.synonyms.map { it.kanji }) }

            QAKind.SHOW_WORD_ASK_NOTHING,
            QAKind.SHOW_PHRASE_ASK_NOTHING,
            QAKind.SHOW_SENTENCE_ASK_NOTHING,
            -> listOf()

            QAKind.SHOW_PHRASE_ASK_WORD_KANA -> getKanaAnswersForPhraseOrSentence(
                word,
                phrasePrimaryFormInParts!!,
                includeSuffix
            )
            QAKind.SHOW_PHRASE_ASK_WORD_KANJI -> getKanjiAnswersForPhraseOrSentence(
                word,
                phrasePrimaryFormInParts!!,
                includeSuffix
            )
            QAKind.SHOW_SENTENCE_ASK_WORD_KANA -> getKanaAnswersForPhraseOrSentence(
                word,
                sentencePrimaryFormInParts!!,
                includeSuffix
            )
            QAKind.SHOW_SENTENCE_ASK_WORD_KANJI -> getKanjiAnswersForPhraseOrSentence(
                word,
                sentencePrimaryFormInParts!!,
                includeSuffix
            )
            QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> listOf(phrase?.kana ?: "")
        }

        private fun getKanaAnswersForPhraseOrSentence(
            word: Word,
            parts: SplitPrimaryForm,
            includeSuffix: Boolean,
        ): List<String> =
            when {
                parts.wordStem.kana == word.kana && parts.wordSuffix.isEmpty() -> {
                    mutableListOf(word.kana).apply { addAll(word.synonyms.map { it.kana }) }
                }
                includeSuffix -> {
                    require(parts.wordSuffix.isEmpty() || parts.wordSuffix.isHiragana()) {
                        "Word form suffix is not Hiragana: ${parts.wordSuffix}"
                    }
                    listOf(parts.wordStem.kana + parts.wordSuffix)
                }
                else -> listOf(parts.wordStem.kana)
            }

        private fun getKanjiAnswersForPhraseOrSentence(
            word: Word,
            parts: SplitPrimaryForm,
            includeSuffix: Boolean,
        ): List<String> =
            when {
                word.usuallyInKana && (parts.wordStem.raw == word.kana && parts.wordSuffix.isEmpty()) -> {
                    mutableListOf(word.kana).apply { addAll(word.synonyms.map { it.kana }) }
                }
                !word.usuallyInKana && (parts.wordStem.raw == word.kanji && parts.wordSuffix.isEmpty()) -> {
                    mutableListOf(word.kanji).apply { addAll(word.synonyms.map { it.kanji }) }
                }
                includeSuffix -> listOf(parts.wordStem.kanji + parts.wordSuffix)
                else -> listOf(parts.wordStem.kanji)
            }

        private fun getKanjiOrKanaToReveal(word: Word, kind: QAKind, phrase: PhraseOrSentence?): String =
            when (kind) {
                QAKind.SHOW_ROMAJI_ASK_KANA,
                QAKind.SHOW_TRANSLATION_ASK_KANA,
                QAKind.SHOW_PHRASE_ASK_WORD_KANA,
                QAKind.SHOW_SENTENCE_ASK_WORD_KANA,
                -> when {
                    word.kanji != word.kana && !word.usuallyInKana -> word.kanji
                    else -> ""
                }

                QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
                QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
                QAKind.SHOW_PHRASE_ASK_WORD_KANJI,
                QAKind.SHOW_SENTENCE_ASK_WORD_KANJI,
                -> when {
                    word.kana != word.kanji -> word.kana
                    else -> ""
                }

                QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
                -> when {
                    phrase?.kana != phrase?.kanji -> phrase?.kanji ?: ""
                    else -> ""
                }
                QAKind.SHOW_KANA_ASK_KANJI,
                QAKind.SHOW_KANJI_ASK_KANA,
                QAKind.SHOW_PHRASE_ASK_NOTHING,
                QAKind.SHOW_SENTENCE_ASK_NOTHING,
                QAKind.SHOW_WORD_ASK_NOTHING,
                -> ""
            }

        private fun getTranslationToReveal(
            word: Word,
            kind: QAKind,
            phrase: PhraseOrSentence?,
            sentence: PhraseOrSentence?,
        ): String =
            when (kind) {
                QAKind.SHOW_KANJI_ASK_KANA,
                QAKind.SHOW_KANA_ASK_KANJI,
                QAKind.SHOW_ROMAJI_ASK_KANA,
                QAKind.SHOW_WORD_ASK_NOTHING,
                -> word.translation.withSystemLang

                QAKind.SHOW_PHRASE_ASK_NOTHING -> phrase?.translation?.withSystemLang ?: ""
                QAKind.SHOW_SENTENCE_ASK_NOTHING -> sentence?.translation?.withSystemLang ?: ""

                else -> ""
            }

        private fun getHintToReveal(word: Word, kind: QAKind): String =
            when (kind) {
                QAKind.SHOW_KANJI_ASK_KANA,
                QAKind.SHOW_KANA_ASK_KANJI,
                QAKind.SHOW_ROMAJI_ASK_KANA,
                QAKind.SHOW_WORD_ASK_NOTHING,
                -> word.hintsWithSystemLang

                else -> ""
            }

        private fun getExplanation(
            kind: QAKind,
            phrase: PhraseOrSentence?,
            sentence: PhraseOrSentence?,
        ): FuriganaString? =
            when (kind) {
                QAKind.SHOW_PHRASE_ASK_NOTHING,
                QAKind.SHOW_PHRASE_ASK_WORD_KANA,
                QAKind.SHOW_PHRASE_ASK_WORD_KANJI,
                QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
                -> phrase?.explanation?.withSystemLang?.takeIf { it.isNotEmpty() }?.let { FuriganaString(it) }

                QAKind.SHOW_SENTENCE_ASK_NOTHING,
                QAKind.SHOW_SENTENCE_ASK_WORD_KANJI,
                -> sentence?.explanation?.withSystemLang?.takeIf { it.isNotEmpty() }?.let { FuriganaString(it) }

                else -> null
            }

        private fun getPresentWholeWords(kind: QAKind): Boolean =
            when (kind) {
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
    }
}
