package io.github.digorydoo.goigoi.compiler.writer

import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import io.github.digorydoo.goigoi.core.file.GoigoiFileMarker.*
import java.io.OutputStream

class WordFileWriter(private val word: GoigoiWord, stream: OutputStream): AbstrWriter(stream) {
    override fun write() {
        require(!word.hidden) { "WordFileWriter invoked on a hidden word: $word" }

        write(WORD_ID, word.id)
        write(WORD_PRIMARY_FORM, word.primaryForm.raw)
        writeIfNonEmpty(WORD_ROMAJI, word.romaji)

        writeIfNonEmpty(WORD_TRANSLATION_DE, word.translation.de)
        writeIfNonEmpty(WORD_TRANSLATION_EN, word.translation.en)
        writeIfNonEmpty(WORD_TRANSLATION_FR, word.translation.fr)
        writeIfNonEmpty(WORD_TRANSLATION_IT, word.translation.it)
        writeIfNonEmpty(WORD_TRANSLATION_JA, word.translation.ja)

        writeIfNonEmpty(WORD_HINT_DE, word.hint.de)
        writeIfNonEmpty(WORD_HINT_EN, word.hint.en)
        writeIfNonEmpty(WORD_HINT_FR, word.hint.fr)
        writeIfNonEmpty(WORD_HINT_IT, word.hint.it)
        writeIfNonEmpty(WORD_HINT_JA, word.hint.ja)

        word.hint2?.let { write(WORD_KNOWN_HINT, it.id) }

        writeIfNonEmpty(WORD_DICTIONARY_WORD, word.dictionaryWord)
        writeIfNonEmpty(WORD_LEVEL, word.level?.toString() ?: "")
        write(WORD_USUALLY_IN_KANA, word.usuallyInKana)
        write(WORD_STUDY_IN_CONTEXT, word.studyInContext.id)

        word.cats.forEach { write(WORD_CATEGORY, it.id) }
        word.synonyms.forEach { write(WORD_SYNONYM, it.raw) }

        for (phrase in word.phrases) {
            beginPhrase()
            write(PHRASE_PRIMARY_FORM, phrase.primaryForm.raw)
            writeIfNonEmpty(PHRASE_ROMAJI, phrase.romaji)

            writeIfNonEmpty(PHRASE_TRANSLATION_DE, phrase.translation.de)
            writeIfNonEmpty(PHRASE_TRANSLATION_EN, phrase.translation.en)
            writeIfNonEmpty(PHRASE_TRANSLATION_FR, phrase.translation.fr)
            writeIfNonEmpty(PHRASE_TRANSLATION_IT, phrase.translation.it)
            writeIfNonEmpty(PHRASE_TRANSLATION_JA, phrase.translation.ja)

            writeIfNonEmpty(PHRASE_EXPLANATION_DE, phrase.explanation.de)
            writeIfNonEmpty(PHRASE_EXPLANATION_EN, phrase.explanation.en)
            writeIfNonEmpty(PHRASE_EXPLANATION_FR, phrase.explanation.fr)
            writeIfNonEmpty(PHRASE_EXPLANATION_IT, phrase.explanation.it)
            writeIfNonEmpty(PHRASE_EXPLANATION_JA, phrase.explanation.ja)

            if (phrase.wordFormToAsk.raw.isNotEmpty()) {
                write(PHRASE_WORD_FORM, phrase.wordFormToAsk.raw)
                writeIfNonEmpty(PHRASE_WORD_FORM_SUFFIX, phrase.wordFormToAskSuffix)
            }
        }

        for (sentence in word.sentences) {
            beginSentence()
            write(SENTENCE_PRIMARY_FORM, sentence.primaryForm.raw)
            writeIfNonEmpty(SENTENCE_ROMAJI, sentence.romaji)

            writeIfNonEmpty(SENTENCE_TRANSLATION_DE, sentence.translation.de)
            writeIfNonEmpty(SENTENCE_TRANSLATION_EN, sentence.translation.en)
            writeIfNonEmpty(SENTENCE_TRANSLATION_FR, sentence.translation.fr)
            writeIfNonEmpty(SENTENCE_TRANSLATION_IT, sentence.translation.it)
            writeIfNonEmpty(SENTENCE_TRANSLATION_JA, sentence.translation.ja)

            writeIfNonEmpty(SENTENCE_EXPLANATION_DE, sentence.explanation.de)
            writeIfNonEmpty(SENTENCE_EXPLANATION_EN, sentence.explanation.en)
            writeIfNonEmpty(SENTENCE_EXPLANATION_FR, sentence.explanation.fr)
            writeIfNonEmpty(SENTENCE_EXPLANATION_IT, sentence.explanation.it)
            writeIfNonEmpty(SENTENCE_EXPLANATION_JA, sentence.explanation.ja)

            if (sentence.wordFormToAsk.raw.isNotEmpty()) {
                write(SENTENCE_WORD_FORM, sentence.wordFormToAsk.raw)
                writeIfNonEmpty(SENTENCE_WORD_FORM_SUFFIX, sentence.wordFormToAskSuffix)
            }
        }

        for (link in word.links) {
            // When link.word is null, it has been skipped by PrepWordLinks, e.g. when the linked word is hidden.
            // When link.extendedKind is null, the link is not meant for Goigoi.
            val otherWord = link.word
            val extKind = link.extendedKind

            if (otherWord != null && extKind != null) {
                require(!otherWord.hidden) { "Internal error: Link to hidden word was not cleared" }

                beginSeeAlso(otherWord.id)
                write(WORDLINK_KIND, extKind.value)
                write(WORDLINK_PRIMARY_FORM, otherWord.primaryForm.raw)

                writeIfNonEmpty(WORDLINK_TRANSLATION_DE, otherWord.translation.de)
                writeIfNonEmpty(WORDLINK_TRANSLATION_EN, otherWord.translation.en)
                writeIfNonEmpty(WORDLINK_TRANSLATION_FR, otherWord.translation.fr)
                writeIfNonEmpty(WORDLINK_TRANSLATION_IT, otherWord.translation.it)
                writeIfNonEmpty(WORDLINK_TRANSLATION_JA, otherWord.translation.ja)
            }
        }

        writeEOFMarker()
    }
}
