package io.github.digorydoo.goigoi.compiler.writer

import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import java.io.OutputStream

class WordFileWriter(private val word: GoigoiWord, stream: OutputStream): AbstrWriter(stream) {
    override fun write() {
        require(!word.hidden) { "WordFileWriter invoked on a hidden word: $word" }

        write(WORD_ID_KEY, word.id)
        write(WORD_PRIMARY_FORM_KEY, word.primaryForm.raw)
        writeIfNonEmpty(WORD_ROMAJI_KEY, word.romaji)

        writeIfNonEmpty(WORD_TRANSLATION_DE_KEY, word.translation.de)
        writeIfNonEmpty(WORD_TRANSLATION_EN_KEY, word.translation.en)
        writeIfNonEmpty(WORD_TRANSLATION_FR_KEY, word.translation.fr)
        writeIfNonEmpty(WORD_TRANSLATION_IT_KEY, word.translation.it)
        writeIfNonEmpty(WORD_TRANSLATION_JA_KEY, word.translation.ja)

        writeIfNonEmpty(WORD_HINT_DE_KEY, word.hint.de)
        writeIfNonEmpty(WORD_HINT_EN_KEY, word.hint.en)
        writeIfNonEmpty(WORD_HINT_FR_KEY, word.hint.fr)
        writeIfNonEmpty(WORD_HINT_IT_KEY, word.hint.it)
        writeIfNonEmpty(WORD_HINT_JA_KEY, word.hint.ja)

        word.hint2?.let { write(WORD_KNOWN_HINT_KEY, it.id) }

        writeIfNonEmpty(WORD_DICTIONARY_WORD_KEY, word.dictionaryWord)
        writeIfNonEmpty(WORD_LEVEL_KEY, word.level?.toString() ?: "")
        write(WORD_USUALLY_IN_KANA_KEY, word.usuallyInKana)
        write(WORD_STUDY_IN_CONTEXT_KEY, word.studyInContext.id)

        word.cats.forEach { write(WORD_CATEGORY_KEY, it.id) }
        word.synonyms.forEach { write(WORD_SYNONYM_KEY, it.raw) }

        for (phrase in word.phrases) {
            beginPhrase()
            write(PHRASE_PRIMARY_FORM_KEY, phrase.primaryForm.raw)
            writeIfNonEmpty(PHRASE_ROMAJI_KEY, phrase.romaji)

            writeIfNonEmpty(PHRASE_TRANSLATION_DE_KEY, phrase.translation.de)
            writeIfNonEmpty(PHRASE_TRANSLATION_EN_KEY, phrase.translation.en)
            writeIfNonEmpty(PHRASE_TRANSLATION_FR_KEY, phrase.translation.fr)
            writeIfNonEmpty(PHRASE_TRANSLATION_IT_KEY, phrase.translation.it)
            writeIfNonEmpty(PHRASE_TRANSLATION_JA_KEY, phrase.translation.ja)

            writeIfNonEmpty(PHRASE_EXPLANATION_DE_KEY, phrase.explanation.de)
            writeIfNonEmpty(PHRASE_EXPLANATION_EN_KEY, phrase.explanation.en)
            writeIfNonEmpty(PHRASE_EXPLANATION_FR_KEY, phrase.explanation.fr)
            writeIfNonEmpty(PHRASE_EXPLANATION_IT_KEY, phrase.explanation.it)
            writeIfNonEmpty(PHRASE_EXPLANATION_JA_KEY, phrase.explanation.ja)
        }

        for (sentence in word.sentences) {
            beginSentence()
            write(SENTENCE_PRIMARY_FORM_KEY, sentence.primaryForm.raw)
            writeIfNonEmpty(SENTENCE_ROMAJI_KEY, sentence.romaji)

            writeIfNonEmpty(SENTENCE_TRANSLATION_DE_KEY, sentence.translation.de)
            writeIfNonEmpty(SENTENCE_TRANSLATION_EN_KEY, sentence.translation.en)
            writeIfNonEmpty(SENTENCE_TRANSLATION_FR_KEY, sentence.translation.fr)
            writeIfNonEmpty(SENTENCE_TRANSLATION_IT_KEY, sentence.translation.it)
            writeIfNonEmpty(SENTENCE_TRANSLATION_JA_KEY, sentence.translation.ja)

            writeIfNonEmpty(SENTENCE_EXPLANATION_DE_KEY, sentence.explanation.de)
            writeIfNonEmpty(SENTENCE_EXPLANATION_EN_KEY, sentence.explanation.en)
            writeIfNonEmpty(SENTENCE_EXPLANATION_FR_KEY, sentence.explanation.fr)
            writeIfNonEmpty(SENTENCE_EXPLANATION_IT_KEY, sentence.explanation.it)
            writeIfNonEmpty(SENTENCE_EXPLANATION_JA_KEY, sentence.explanation.ja)
        }

        for (link in word.links) {
            // When link.word is null, it has been skipped by PrepWordLinks, e.g. when the linked word is hidden.
            // When link.extendedKind is null, the link is not meant for Goigoi.
            val otherWord = link.word
            val extKind = link.extendedKind

            if (otherWord != null && extKind != null) {
                require(!otherWord.hidden) { "Internal error: Link to hidden word was not cleared" }

                beginSeeAlso(otherWord.id)
                write(WORDLINK_KIND_KEY, extKind.value)
                write(WORDLINK_PRIMARY_FORM_KEY, otherWord.primaryForm.raw)

                writeIfNonEmpty(WORDLINK_TRANSLATION_DE_KEY, otherWord.translation.de)
                writeIfNonEmpty(WORDLINK_TRANSLATION_EN_KEY, otherWord.translation.en)
                writeIfNonEmpty(WORDLINK_TRANSLATION_FR_KEY, otherWord.translation.fr)
                writeIfNonEmpty(WORDLINK_TRANSLATION_IT_KEY, otherWord.translation.it)
                writeIfNonEmpty(WORDLINK_TRANSLATION_JA_KEY, otherWord.translation.ja)
            }
        }

        writeEOFMarker()
    }
}
