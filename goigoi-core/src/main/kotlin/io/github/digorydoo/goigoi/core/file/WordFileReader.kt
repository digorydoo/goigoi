package io.github.digorydoo.goigoi.core.file

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.file.KDataInputStream.FileMarker
import io.github.digorydoo.goigoi.core.db.*
import io.github.digorydoo.goigoi.core.file.GoigoiFileMarker.*
import java.io.InputStream

class WordFileReader(
    stream: InputStream,
    private val word: Word,
): BinaryFileReader(stream) {
    private var phrase: PhraseOrSentence? = null
    private var sentence: PhraseOrSentence? = null
    private var link: WordLink? = null

    override fun process(marker: FileMarker, value: String) {
        when (marker) {
            WORD_ID -> word.id = value
            WORD_PRIMARY_FORM -> word.primaryForm = FuriganaString(value)
            WORD_ROMAJI -> word.romaji = value
            WORD_TRANSLATION_DE -> word.translation.de = value
            WORD_TRANSLATION_EN -> word.translation.en = value
            WORD_TRANSLATION_FR -> word.translation.fr = value
            WORD_TRANSLATION_IT -> word.translation.it = value
            WORD_TRANSLATION_JA -> word.translation.ja = value
            WORD_HINT_DE -> word.hint.de = value
            WORD_HINT_EN -> word.hint.en = value
            WORD_HINT_FR -> word.hint.fr = value
            WORD_HINT_IT -> word.hint.it = value
            WORD_HINT_JA -> word.hint.ja = value
            WORD_KNOWN_HINT -> word.hint2 = value.toIntOrNull()?.let { WordHint.fromInt(it) }
            WORD_DICTIONARY_WORD -> word.dictionaryWord = value
            WORD_LEVEL -> word.level = JLPTLevel.fromStringOrNull(value, nxIsNull = false)
            WORD_USUALLY_IN_KANA -> word.usuallyInKana = value.toBoolean()
            WORD_STUDY_IN_CONTEXT -> word.studyInContext = value.toIntOrNull()
                ?.let { StudyInContextKind.fromInt(it) }
                ?: StudyInContextKind.NOT_REQUIRED
            WORD_CATEGORY -> value.toIntOrNull()
                ?.let { WordCategory.fromInt(it) }
                ?.let { word.cats.add(it) }
            WORD_SYNONYM -> value
                .also { require(it.isNotEmpty()) }
                .let { word.synonyms.add(FuriganaString(value)) }

            PHRASE_ID -> startNewPhrase()
            PHRASE_PRIMARY_FORM -> phrase!!.primaryForm = FuriganaString(value)
            PHRASE_ROMAJI -> phrase!!.romaji = value
            PHRASE_TRANSLATION_DE -> phrase!!.translation.de = value
            PHRASE_TRANSLATION_EN -> phrase!!.translation.en = value
            PHRASE_TRANSLATION_FR -> phrase!!.translation.fr = value
            PHRASE_TRANSLATION_IT -> phrase!!.translation.it = value
            PHRASE_TRANSLATION_JA -> phrase!!.translation.ja = value
            PHRASE_EXPLANATION_DE -> phrase!!.explanation.de = value
            PHRASE_EXPLANATION_EN -> phrase!!.explanation.en = value
            PHRASE_EXPLANATION_FR -> phrase!!.explanation.fr = value
            PHRASE_EXPLANATION_IT -> phrase!!.explanation.it = value
            PHRASE_EXPLANATION_JA -> phrase!!.explanation.ja = value
            PHRASE_WORD_FORM -> phrase!!.wordFormToAsk = FuriganaString(value)
            PHRASE_WORD_FORM_SUFFIX -> phrase!!.wordFormToAskSuffix = value

            SENTENCE_ID -> startNewSentence()
            SENTENCE_PRIMARY_FORM -> sentence!!.primaryForm = FuriganaString(value)
            SENTENCE_ROMAJI -> sentence!!.romaji = value
            SENTENCE_TRANSLATION_DE -> sentence!!.translation.de = value
            SENTENCE_TRANSLATION_EN -> sentence!!.translation.en = value
            SENTENCE_TRANSLATION_FR -> sentence!!.translation.fr = value
            SENTENCE_TRANSLATION_IT -> sentence!!.translation.it = value
            SENTENCE_TRANSLATION_JA -> sentence!!.translation.ja = value
            SENTENCE_EXPLANATION_DE -> sentence!!.explanation.de = value
            SENTENCE_EXPLANATION_EN -> sentence!!.explanation.en = value
            SENTENCE_EXPLANATION_FR -> sentence!!.explanation.fr = value
            SENTENCE_EXPLANATION_IT -> sentence!!.explanation.it = value
            SENTENCE_EXPLANATION_JA -> sentence!!.explanation.ja = value
            SENTENCE_WORD_FORM -> sentence!!.wordFormToAsk = FuriganaString(value)
            SENTENCE_WORD_FORM_SUFFIX -> sentence!!.wordFormToAskSuffix = value

            WORDLINK_ID -> startNewLink(value)
            WORDLINK_PRIMARY_FORM -> link!!.primaryForm = value
            WORDLINK_TRANSLATION_DE -> link!!.translation.de = value
            WORDLINK_TRANSLATION_EN -> link!!.translation.en = value
            WORDLINK_TRANSLATION_FR -> link!!.translation.fr = value
            WORDLINK_TRANSLATION_IT -> link!!.translation.it = value
            WORDLINK_TRANSLATION_JA -> link!!.translation.ja = value
            WORDLINK_KIND -> link!!.kind = value.toIntOrNull()?.let { WordLink.Kind.fromInt(it) }

            else -> throw Exception("Unexpected marker: $marker")
        }
    }

    private fun startNewPhrase() {
        phrase = PhraseOrSentence().also { word.phrases.add(it) }
    }

    private fun startNewSentence() {
        sentence = PhraseOrSentence().also { word.sentences.add(it) }
    }

    private fun startNewLink(wordId: String) {
        link = WordLink(wordId).also { word.links.add(it) }
    }
}
