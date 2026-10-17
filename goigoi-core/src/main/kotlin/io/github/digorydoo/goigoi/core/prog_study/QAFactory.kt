package io.github.digorydoo.goigoi.core.prog_study

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.cjk.isHiragana
import ch.digorydoo.kutils.cjk.isKana
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.db.AcceptableAnswer
import io.github.digorydoo.goigoi.core.db.PhraseOrSentence
import io.github.digorydoo.goigoi.core.db.PhraseOrSentence.SplitPrimaryForm
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer.Hint
import io.github.digorydoo.goigoi.core.stats.IWordStats

class QAFactory(private val stats: IWordStats) {
    fun createQA(
        word: Word,
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
            // When we're not presenting whole words, allowing the user to type any kana, we do not ask for the
            // word form suffix (e.g. 〜ます, 〜ました, etc.) in order to make the answer less ambiguous.
            gapIncludesSuffix = presentWholeWords,
        )

        val allowExtendedKeyboard = when {
            presentWholeWords -> false // we're going to use FIXED_KEYS
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
            questionAfterReveal = getQuestionAfterReveal(word, kind, phrase, sentence),
            questionHint = getQuestionHint(word, kind, phrase, sentence),
            expectedAnswer = getExpectedAnswer(
                word,
                kind,
                phrase,
                phrasePrimaryFormInParts,
                sentencePrimaryFormInParts,
                includeSuffix = presentWholeWords,
            ),
            acceptableAnswers = getAcceptableAnswers(
                word,
                kind,
                phrase,
                sentence,
                phrasePrimaryFormInParts,
                sentencePrimaryFormInParts,
                includeSuffix = presentWholeWords,
            ),
            kanjiOrKanaToReveal = getKanjiOrKanaToReveal(word, kind),
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

        QAKind.SHOW_PHRASE_ASK_NOTHING -> {
            require(phrase != null) { "$kind requires valid phrase" }
            OneOf.Second(phrase.primaryForm)
        }
        QAKind.SHOW_SENTENCE_ASK_NOTHING -> {
            require(sentence != null) { "$kind requires valid sentence" }
            OneOf.Second(sentence.primaryForm)
        }

        QAKind.SHOW_PHRASE_ASK_WORD_KANA,
        QAKind.SHOW_PHRASE_ASK_WORD_KANJI,
        -> {
            require(phrasePrimaryFormInParts != null) { "$kind requires valid phrase" }
            OneOf.Second(getQuestionWithGap(phrasePrimaryFormInParts, gapIncludesSuffix))
        }

        QAKind.SHOW_SENTENCE_ASK_WORD_KANA,
        QAKind.SHOW_SENTENCE_ASK_WORD_KANJI,
        -> {
            require(sentencePrimaryFormInParts != null) { "$kind requires valid sentence" }
            OneOf.Second(getQuestionWithGap(sentencePrimaryFormInParts, gapIncludesSuffix))
        }

        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> {
            require(phrase != null) { "$kind requires valid phrase" }
            OneOf.First(phrase.translation.withSystemLang)
        }
    }

    private fun getQuestionWithGap(parts: SplitPrimaryForm, gapIncludesSuffix: Boolean): FuriganaString {
        val gap = if (parts.wordStem.kanji != parts.wordStem.kana) GAP_WITH_EMPTY_FURIGANA else GAP
        val suffix = if (gapIncludesSuffix) "" else parts.wordSuffix
        return FuriganaString("${parts.begin.raw}$gap$suffix${parts.end.raw}")
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
        -> {
            require(phrase != null) { "$kind requires valid phrase" }
            OneOf.Second(phrase.primaryForm)
        }

        QAKind.SHOW_SENTENCE_ASK_NOTHING,
        QAKind.SHOW_SENTENCE_ASK_WORD_KANA,
        QAKind.SHOW_SENTENCE_ASK_WORD_KANJI,
        -> {
            require(sentence != null) { "$kind requires valid sentence" }
            OneOf.Second(sentence.primaryForm)
        }

        QAKind.SHOW_KANJI_ASK_KANA,
        QAKind.SHOW_KANA_ASK_KANJI,
        QAKind.SHOW_ROMAJI_ASK_KANA,
        QAKind.SHOW_TRANSLATION_ASK_KANA,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
        -> null
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

    private fun getExpectedAnswer(
        word: Word,
        kind: QAKind,
        phrase: PhraseOrSentence?,
        phrasePrimaryFormInParts: SplitPrimaryForm?,
        sentencePrimaryFormInParts: SplitPrimaryForm?,
        includeSuffix: Boolean,
    ): String = when (kind) {
        QAKind.SHOW_KANJI_ASK_KANA,
        QAKind.SHOW_ROMAJI_ASK_KANA,
        QAKind.SHOW_TRANSLATION_ASK_KANA,
        -> word.kana

        QAKind.SHOW_KANA_ASK_KANJI,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
        -> word.kanji

        QAKind.SHOW_WORD_ASK_NOTHING,
        QAKind.SHOW_PHRASE_ASK_NOTHING,
        QAKind.SHOW_SENTENCE_ASK_NOTHING,
        -> ""

        QAKind.SHOW_PHRASE_ASK_WORD_KANA -> {
            require(phrasePrimaryFormInParts != null) { "$kind requires valid phrase" }
            getExpectedKanaAnswerForPhraseOrSentence(word, phrasePrimaryFormInParts, includeSuffix)
        }
        QAKind.SHOW_PHRASE_ASK_WORD_KANJI -> {
            require(phrasePrimaryFormInParts != null) { "$kind requires valid phrase" }
            getExpectedKanjiAnswerForPhraseOrSentence(word, phrasePrimaryFormInParts, includeSuffix)
        }
        QAKind.SHOW_SENTENCE_ASK_WORD_KANA -> {
            require(sentencePrimaryFormInParts != null) { "$kind requires valid sentence" }
            getExpectedKanaAnswerForPhraseOrSentence(word, sentencePrimaryFormInParts, includeSuffix)
        }
        QAKind.SHOW_SENTENCE_ASK_WORD_KANJI -> {
            require(sentencePrimaryFormInParts != null) { "$kind requires valid sentence" }
            getExpectedKanjiAnswerForPhraseOrSentence(word, sentencePrimaryFormInParts, includeSuffix)
        }
        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> phrase?.kana ?: ""
    }

    private fun getExpectedKanaAnswerForPhraseOrSentence(
        word: Word,
        parts: SplitPrimaryForm,
        includeSuffix: Boolean,
    ): String = when {
        parts.wordStem.kana == word.kana && parts.wordSuffix.isEmpty() -> {
            word.kana
        }
        includeSuffix -> {
            require(parts.wordSuffix.isEmpty() || parts.wordSuffix.isHiragana()) {
                "Word form suffix is not Hiragana: ${parts.wordSuffix}"
            }
            parts.wordStem.kana + parts.wordSuffix
        }
        else -> parts.wordStem.kana
    }

    private fun getExpectedKanjiAnswerForPhraseOrSentence(
        word: Word,
        parts: SplitPrimaryForm,
        includeSuffix: Boolean,
    ): String = when {
        word.usuallyInKana && (parts.wordStem.raw == word.kana && parts.wordSuffix.isEmpty()) -> {
            word.kana
        }
        !word.usuallyInKana && (parts.wordStem.raw == word.kanji && parts.wordSuffix.isEmpty()) -> {
            word.kanji
        }
        includeSuffix -> parts.wordStem.kanji + parts.wordSuffix
        else -> parts.wordStem.kanji
    }

    private fun getAcceptableAnswers(
        word: Word,
        kind: QAKind,
        phrase: PhraseOrSentence?,
        sentence: PhraseOrSentence?,
        phrasePrimaryFormInParts: SplitPrimaryForm?,
        sentencePrimaryFormInParts: SplitPrimaryForm?,
        includeSuffix: Boolean,
    ): List<AcceptableAnswer> = when (kind) {
        QAKind.SHOW_TRANSLATION_ASK_KANA,
        -> {
            // The kana answers of acceptForTranslation is sufficient,
            // because alternativeReadings were derived from them
            word.acceptForTranslation.filter { it.answer.isKana() }
        }

        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
        -> {
            // The kanji answers of acceptForTranslation is sufficient,
            // because alternativeSpellings were derived from them
            word.acceptForTranslation.filter { !it.answer.isKana() }
        }

        QAKind.SHOW_KANJI_ASK_KANA -> word.alternativeReadings.map {
            AcceptableAnswer(it, inaccurate = false, interchangeable = false)
        }
        QAKind.SHOW_KANA_ASK_KANJI -> word.alternativeSpellings.map {
            AcceptableAnswer(it, inaccurate = false, interchangeable = false)
        }
        QAKind.SHOW_PHRASE_ASK_WORD_KANA -> {
            require(phrase != null && phrasePrimaryFormInParts != null) { "$kind requires valid phrase" }
            getAcceptableKanaAnswersForPhraseOrSentence(word, phrase, phrasePrimaryFormInParts, includeSuffix)
        }
        QAKind.SHOW_PHRASE_ASK_WORD_KANJI -> {
            require(phrase != null && phrasePrimaryFormInParts != null) { "$kind requires valid phrase" }
            getAcceptableKanjiAnswersForPhraseOrSentence(
                word,
                phrase,
                phrasePrimaryFormInParts,
                includeSuffix
            )
        }
        QAKind.SHOW_SENTENCE_ASK_WORD_KANA -> {
            require(sentence != null && sentencePrimaryFormInParts != null) { "$kind requires valid sentence" }
            getAcceptableKanaAnswersForPhraseOrSentence(
                word,
                sentence,
                sentencePrimaryFormInParts,
                includeSuffix
            )
        }
        QAKind.SHOW_SENTENCE_ASK_WORD_KANJI -> {
            require(sentence != null && sentencePrimaryFormInParts != null) { "$kind requires valid sentence" }
            getAcceptableKanjiAnswersForPhraseOrSentence(
                word,
                sentence,
                sentencePrimaryFormInParts,
                includeSuffix
            )
        }
        QAKind.SHOW_ROMAJI_ASK_KANA,
        QAKind.SHOW_WORD_ASK_NOTHING,
        QAKind.SHOW_PHRASE_ASK_NOTHING,
        QAKind.SHOW_SENTENCE_ASK_NOTHING,
        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
        -> listOf()
    }

    private fun getAcceptableKanaAnswersForPhraseOrSentence(
        word: Word,
        phraseOrSentence: PhraseOrSentence,
        primaryFormInParts: SplitPrimaryForm,
        includeSuffix: Boolean,
    ): List<AcceptableAnswer> {
        val result = mutableListOf<AcceptableAnswer>()
        val wordFormToAskSuffix = phraseOrSentence.wordFormToAskSuffix

        for (acceptable in phraseOrSentence.acceptableAnswers) {
            val answer = when {
                !includeSuffix || wordFormToAskSuffix.isEmpty() -> acceptable.kana
                else -> acceptable.kana + wordFormToAskSuffix
            }

            if (answer != phraseOrSentence.wordFormToAsk.kana) {
                result.add(
                    AcceptableAnswer(answer, inaccurate = false, interchangeable = false)
                )
            }
        }

        if (primaryFormInParts.wordStem.raw == word.primaryForm.raw && primaryFormInParts.wordSuffix.isEmpty()) {
            // The word is being used in unchanged dictionary form inside this phrase or sentence.
            // Fully interchangeable variants of this word may be used if they're kana-only.
            result.addAll(
                word.acceptForTranslation.filter { it.interchangeable && !it.inaccurate && it.answer.isKana() }
            )
        }

        return result
    }

    private fun getAcceptableKanjiAnswersForPhraseOrSentence(
        word: Word,
        phraseOrSentence: PhraseOrSentence,
        primaryFormInParts: SplitPrimaryForm,
        includeSuffix: Boolean,
    ): List<AcceptableAnswer> {
        val result = mutableListOf<AcceptableAnswer>()
        val wordFormToAskSuffix = phraseOrSentence.wordFormToAskSuffix

        for (acceptable in phraseOrSentence.acceptableAnswers) {
            val answer = when {
                !includeSuffix || wordFormToAskSuffix.isEmpty() -> acceptable.kanji
                else -> acceptable.kanji + wordFormToAskSuffix
            }

            if (answer != phraseOrSentence.wordFormToAsk.kanji) {
                result.add(
                    AcceptableAnswer(answer, inaccurate = false, interchangeable = false)
                )
            }
        }

        if (primaryFormInParts.wordStem.raw == word.primaryForm.raw && primaryFormInParts.wordSuffix.isEmpty()) {
            // The word is being used in unchanged dictionary form inside this phrase or sentence.
            // Fully interchangeable variants of this word may be used unless they're kana-only.
            result.addAll(
                word.acceptForTranslation.filter { it.interchangeable && !it.inaccurate && !it.answer.isKana() }
            )
        }

        return result
    }

    private fun getKanjiOrKanaToReveal(word: Word, kind: QAKind): String =
        when (kind) {
            QAKind.SHOW_ROMAJI_ASK_KANA,
            QAKind.SHOW_TRANSLATION_ASK_KANA,
            -> when {
                word.kanji != word.kana && !word.usuallyInKana -> word.kanji
                else -> ""
            }

            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
            -> when {
                word.kana != word.kanji -> word.kana
                else -> ""
            }

            QAKind.SHOW_WORD_ASK_NOTHING,
            QAKind.SHOW_PHRASE_ASK_NOTHING,
            QAKind.SHOW_SENTENCE_ASK_NOTHING,
            QAKind.SHOW_KANA_ASK_KANJI,
            QAKind.SHOW_KANJI_ASK_KANA,
            QAKind.SHOW_PHRASE_ASK_WORD_KANA,
            QAKind.SHOW_PHRASE_ASK_WORD_KANJI,
            QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
            QAKind.SHOW_SENTENCE_ASK_WORD_KANA,
            QAKind.SHOW_SENTENCE_ASK_WORD_KANJI,
            -> ""
        }

    private fun getTranslationToReveal(
        word: Word,
        kind: QAKind,
        phrase: PhraseOrSentence?,
        sentence: PhraseOrSentence?,
    ): String = when (kind) {
        QAKind.SHOW_KANJI_ASK_KANA,
        QAKind.SHOW_KANA_ASK_KANJI,
        QAKind.SHOW_ROMAJI_ASK_KANA,
        QAKind.SHOW_WORD_ASK_NOTHING,
        -> word.translation.withSystemLang

        QAKind.SHOW_PHRASE_ASK_NOTHING -> phrase?.translation?.withSystemLang ?: ""
        QAKind.SHOW_SENTENCE_ASK_NOTHING -> sentence?.translation?.withSystemLang ?: ""

        QAKind.SHOW_PHRASE_ASK_WORD_KANA,
        QAKind.SHOW_PHRASE_ASK_WORD_KANJI,
        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
        QAKind.SHOW_SENTENCE_ASK_WORD_KANA,
        QAKind.SHOW_SENTENCE_ASK_WORD_KANJI,
        QAKind.SHOW_TRANSLATION_ASK_KANA,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
        -> ""
    }

    private fun getHintToReveal(word: Word, kind: QAKind): String =
        when (kind) {
            QAKind.SHOW_KANJI_ASK_KANA,
            QAKind.SHOW_KANA_ASK_KANJI,
            QAKind.SHOW_ROMAJI_ASK_KANA,
            -> word.hintsWithSystemLang

            QAKind.SHOW_WORD_ASK_NOTHING,
            QAKind.SHOW_PHRASE_ASK_NOTHING,
            QAKind.SHOW_SENTENCE_ASK_NOTHING,
            QAKind.SHOW_PHRASE_ASK_WORD_KANA,
            QAKind.SHOW_PHRASE_ASK_WORD_KANJI,
            QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
            QAKind.SHOW_SENTENCE_ASK_WORD_KANA,
            QAKind.SHOW_SENTENCE_ASK_WORD_KANJI,
            QAKind.SHOW_TRANSLATION_ASK_KANA,
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
            -> ""
        }

    private fun getExplanation(
        kind: QAKind,
        phrase: PhraseOrSentence?,
        sentence: PhraseOrSentence?,
    ): FuriganaString? = when (kind) {
        QAKind.SHOW_PHRASE_ASK_NOTHING,
        QAKind.SHOW_PHRASE_ASK_WORD_KANA,
        QAKind.SHOW_PHRASE_ASK_WORD_KANJI,
        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
        -> phrase?.explanation?.withSystemLang?.takeIf { it.isNotEmpty() }?.let { FuriganaString(it) }

        QAKind.SHOW_SENTENCE_ASK_NOTHING,
        QAKind.SHOW_SENTENCE_ASK_WORD_KANA,
        QAKind.SHOW_SENTENCE_ASK_WORD_KANJI,
        -> sentence?.explanation?.withSystemLang?.takeIf { it.isNotEmpty() }?.let { FuriganaString(it) }

        QAKind.SHOW_KANJI_ASK_KANA,
        QAKind.SHOW_KANA_ASK_KANJI,
        QAKind.SHOW_ROMAJI_ASK_KANA,
        QAKind.SHOW_TRANSLATION_ASK_KANA,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
        QAKind.SHOW_WORD_ASK_NOTHING,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
        -> null
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

    companion object {
        private const val GAP = "___"
        private const val GAP_WITH_EMPTY_FURIGANA = "$GAP【：】" // to preserve the height of the furigana
        private const val MIN_SEEN_COUNT_FOR_EXTENDED_KEYBOARD = 5
        private const val MIN_RATING_FOR_EXTENDED_KEYBOARD = 0.77f
    }
}
