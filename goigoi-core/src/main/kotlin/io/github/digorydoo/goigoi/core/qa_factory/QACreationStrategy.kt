package io.github.digorydoo.goigoi.core.qa_factory

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.isHiragana
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.db.AcceptableAnswer
import io.github.digorydoo.goigoi.core.db.PhraseOrSentence
import io.github.digorydoo.goigoi.core.db.PhraseOrSentence.SplitPrimaryForm
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer.Hint
import io.github.digorydoo.goigoi.core.qa_factory.QAFactory.QuestionData

abstract class QACreationStrategy {
    abstract fun getQuestion(data: QuestionData): OneOf<String, FuriganaString>
    abstract fun getQuestionAfterReveal(data: QuestionData): OneOf<String, FuriganaString>?
    abstract fun getQuestionHint(data: QuestionData): OneOf<String, Hint>
    abstract fun getExpectedAnswer(data: QuestionData): String
    abstract fun getAcceptableAnswers(data: QuestionData): List<AcceptableAnswer>
    abstract fun getKanjiOrKanaToReveal(data: QuestionData): String
    abstract fun getTranslationToReveal(data: QuestionData): String
    abstract fun getHintToReveal(data: QuestionData): String
    abstract fun getExplanation(data: QuestionData): FuriganaString?

    protected fun getQuestionWithGap(parts: SplitPrimaryForm, gapIncludesSuffix: Boolean): FuriganaString {
        val gap = if (parts.wordStem.kanji != parts.wordStem.kana) GAP_WITH_EMPTY_FURIGANA else GAP
        val suffix = if (gapIncludesSuffix) "" else parts.wordSuffix
        return FuriganaString("${parts.begin.raw}$gap$suffix${parts.end.raw}")
    }

    protected fun getExpectedKanaAnswerForPhraseOrSentence(
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

    protected fun getExpectedKanjiAnswerForPhraseOrSentence(
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

    protected fun getAcceptableKanaAnswersForPhraseOrSentence(
        word: Word,
        phraseOrSentence: PhraseOrSentence,
        primaryFormInParts: SplitPrimaryForm,
        includeSuffix: Boolean,
    ): List<AcceptableAnswer> {
        val result = mutableListOf<AcceptableAnswer>()
        val wordFormToAskSuffix = phraseOrSentence.wordFormToAskSuffix

        var expectedAnswer = primaryFormInParts.wordStem.kana
        if (includeSuffix) expectedAnswer += primaryFormInParts.wordSuffix

        // Check acceptable answers from the phrase or sentence itself

        for (acceptable in phraseOrSentence.acceptableAnswers) {
            if (acceptable.isKana) {
                var answerWithSuffix = acceptable.answer // if phrase defines a suffix, this is only the stem
                if (includeSuffix) answerWithSuffix += wordFormToAskSuffix

                if (answerWithSuffix != expectedAnswer) {
                    result.add(AcceptableAnswer(answerWithSuffix, accuracy = acceptable.accuracy))
                }
            }
        }

        // If the word was used in unchanged dictionary form, also check acceptable answers from the word

        if (primaryFormInParts.wordStem.raw == word.primaryForm.raw && primaryFormInParts.wordSuffix.isEmpty()) {
            for (acceptable in word.acceptableAnswers) {
                if (acceptable.accuracy.canPropagateToPhrasesAndSentences && acceptable.isKana) {
                    val answerWithSuffix = acceptable.answer // word answers aren't split into stem and suffix

                    if (answerWithSuffix != expectedAnswer) {
                        result.add(AcceptableAnswer(answerWithSuffix, accuracy = acceptable.accuracy))
                    }
                }
            }
        }

        return result
    }

    protected fun getAcceptableKanjiAnswersForPhraseOrSentence(
        word: Word,
        phraseOrSentence: PhraseOrSentence,
        primaryFormInParts: SplitPrimaryForm,
        includeSuffix: Boolean,
    ): List<AcceptableAnswer> {
        val result = mutableListOf<AcceptableAnswer>()
        val wordFormToAskSuffix = phraseOrSentence.wordFormToAskSuffix

        var expectedAnswer = primaryFormInParts.wordStem.kanji
        if (includeSuffix) expectedAnswer += primaryFormInParts.wordSuffix

        // Check acceptable answers from the phrase or sentence itself

        for (acceptable in phraseOrSentence.acceptableAnswers) {
            if (!acceptable.isKana) {
                var answerWithSuffix = acceptable.answer // if phrase defines a suffix, this is only the stem
                if (includeSuffix) answerWithSuffix += wordFormToAskSuffix

                if (answerWithSuffix != expectedAnswer) {
                    result.add(AcceptableAnswer(answerWithSuffix, accuracy = acceptable.accuracy))
                }
            }
        }

        // If the word was used in unchanged dictionary form, also check acceptable answers from the word

        if (primaryFormInParts.wordStem.raw == word.primaryForm.raw && primaryFormInParts.wordSuffix.isEmpty()) {
            for (acceptable in word.acceptableAnswers) {
                if (acceptable.accuracy.canPropagateToPhrasesAndSentences && !acceptable.isKana) {
                    val answerWithSuffix = acceptable.answer // word answers aren't split into stem and suffix

                    if (answerWithSuffix != expectedAnswer) {
                        result.add(AcceptableAnswer(answerWithSuffix, accuracy = acceptable.accuracy))
                    }
                }
            }
        }

        return result
    }

    protected fun getAcceptableKanaAnswersForFullPhrase(
        word: Word,
        phrase: PhraseOrSentence,
        phraseParts: SplitPrimaryForm,
    ): List<AcceptableAnswer> {
        val result = mutableListOf<AcceptableAnswer>()
        val expectedAnswer = phrase.kana // we expect the full phrase as the answer

        // Check acceptable answers from the phrase or sentence itself

        for (acceptable in phrase.acceptableAnswers) {
            if (acceptable.isKana) {
                val phraseWithWordExchanged = phraseParts.begin.kana +
                    acceptable.answer + // if phrase defines a suffix, this is only the stem
                    phraseParts.wordSuffix +
                    phraseParts.end.kana

                if (phraseWithWordExchanged != expectedAnswer) {
                    result.add(AcceptableAnswer(phraseWithWordExchanged, accuracy = acceptable.accuracy))
                }
            }
        }

        // If the word was used in unchanged dictionary form, also check acceptable answers from the word

        if (phraseParts.wordStem.raw == word.primaryForm.raw && phraseParts.wordSuffix.isEmpty()) {
            for (acceptable in word.acceptableAnswers) {
                if (acceptable.accuracy.canPropagateToPhrasesAndSentences && acceptable.isKana) {
                    val phraseWithWordExchanged = phraseParts.begin.kana +
                        acceptable.answer + // word answers aren't split into stem and suffix
                        phraseParts.end.kana

                    if (phraseWithWordExchanged != expectedAnswer) {
                        result.add(AcceptableAnswer(phraseWithWordExchanged, accuracy = acceptable.accuracy))
                    }
                }
            }
        }

        return result
    }

    companion object {
        private const val GAP = "___"
        private const val GAP_WITH_EMPTY_FURIGANA = "$GAP【：】" // to preserve the height of the furigana
    }
}
