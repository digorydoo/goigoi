package io.github.digorydoo.goigoi.file

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.JLPTLevel
import io.github.digorydoo.goigoi.core.StudyInContextKind
import io.github.digorydoo.goigoi.core.WordCategory
import io.github.digorydoo.goigoi.core.WordHint
import io.github.digorydoo.goigoi.db.Phrase
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.db.WordLink
import java.io.InputStream

class WordFileReader(
    stream: InputStream,
    private val word: Word,
): BinaryFileReader(stream) {
    private var phrase: Phrase? = null
    private var sentence: Phrase? = null
    private var link: WordLink? = null

    override fun process(key: Int, value: String) {
        when (key) {
            WORD_ID_KEY -> word.id = value
            WORD_PRIMARY_FORM_KEY -> word.primaryForm = FuriganaString(value)
            WORD_ROMAJI_KEY -> word.romaji = value
            WORD_TRANSLATION_DE_KEY -> word.translation.de = value
            WORD_TRANSLATION_EN_KEY -> word.translation.en = value
            WORD_TRANSLATION_FR_KEY -> word.translation.fr = value
            WORD_TRANSLATION_IT_KEY -> word.translation.it = value
            WORD_TRANSLATION_JA_KEY -> word.translation.ja = value
            WORD_HINT_DE_KEY -> word.hint.de = value
            WORD_HINT_EN_KEY -> word.hint.en = value
            WORD_HINT_FR_KEY -> word.hint.fr = value
            WORD_HINT_IT_KEY -> word.hint.it = value
            WORD_HINT_JA_KEY -> word.hint.ja = value
            WORD_KNOWN_HINT_KEY -> word.hint2 = value.toIntOrNull()?.let { WordHint.fromInt(it) }
            WORD_DICTIONARY_WORD_KEY -> word.dictionaryWord = value
            WORD_LEVEL_KEY -> word.level = JLPTLevel.fromString(value)
            WORD_USUALLY_IN_KANA_KEY -> word.usuallyInKana = value.toBoolean()
            WORD_STUDY_IN_CONTEXT_KEY -> word.studyInContext = value.toIntOrNull()
                ?.let { StudyInContextKind.fromInt(it) }
                ?: StudyInContextKind.NOT_REQUIRED
            WORD_CATEGORY_KEY -> value.toIntOrNull()
                ?.let { WordCategory.fromInt(it) }
                ?.let { word.cats.add(it) }
            WORD_SYNONYM_KEY -> value
                .also { require(it.isNotEmpty()) }
                .let { word.synonyms.add(FuriganaString(value)) }

            PHRASE_ID_KEY -> startNewPhrase()
            PHRASE_PRIMARY_FORM_KEY -> phrase!!.primaryForm = FuriganaString(value)
            PHRASE_ROMAJI_KEY -> phrase!!.romaji = value
            PHRASE_TRANSLATION_DE_KEY -> phrase!!.translation.de = value
            PHRASE_TRANSLATION_EN_KEY -> phrase!!.translation.en = value
            PHRASE_TRANSLATION_FR_KEY -> phrase!!.translation.fr = value
            PHRASE_TRANSLATION_IT_KEY -> phrase!!.translation.it = value
            PHRASE_TRANSLATION_JA_KEY -> phrase!!.translation.ja = value
            PHRASE_EXPLANATION_DE_KEY -> phrase!!.explanation.de = value
            PHRASE_EXPLANATION_EN_KEY -> phrase!!.explanation.en = value
            PHRASE_EXPLANATION_FR_KEY -> phrase!!.explanation.fr = value
            PHRASE_EXPLANATION_IT_KEY -> phrase!!.explanation.it = value
            PHRASE_EXPLANATION_JA_KEY -> phrase!!.explanation.ja = value

            SENTENCE_ID_KEY -> startNewSentence()
            SENTENCE_PRIMARY_FORM_KEY -> sentence!!.primaryForm = FuriganaString(value)
            SENTENCE_ROMAJI_KEY -> sentence!!.romaji = value
            SENTENCE_TRANSLATION_DE_KEY -> sentence!!.translation.de = value
            SENTENCE_TRANSLATION_EN_KEY -> sentence!!.translation.en = value
            SENTENCE_TRANSLATION_FR_KEY -> sentence!!.translation.fr = value
            SENTENCE_TRANSLATION_IT_KEY -> sentence!!.translation.it = value
            SENTENCE_TRANSLATION_JA_KEY -> sentence!!.translation.ja = value
            SENTENCE_EXPLANATION_DE_KEY -> sentence!!.explanation.de = value
            SENTENCE_EXPLANATION_EN_KEY -> sentence!!.explanation.en = value
            SENTENCE_EXPLANATION_FR_KEY -> sentence!!.explanation.fr = value
            SENTENCE_EXPLANATION_IT_KEY -> sentence!!.explanation.it = value
            SENTENCE_EXPLANATION_JA_KEY -> sentence!!.explanation.ja = value

            WORDLINK_ID_KEY -> startNewLink(value)
            WORDLINK_PRIMARY_FORM_KEY -> link!!.primaryForm = value
            WORDLINK_TRANSLATION_DE_KEY -> link!!.translation.de = value
            WORDLINK_TRANSLATION_EN_KEY -> link!!.translation.en = value
            WORDLINK_TRANSLATION_FR_KEY -> link!!.translation.fr = value
            WORDLINK_TRANSLATION_IT_KEY -> link!!.translation.it = value
            WORDLINK_TRANSLATION_JA_KEY -> link!!.translation.ja = value
            WORDLINK_KIND_KEY -> link!!.kind = value.toIntOrNull()?.let { WordLink.Kind.fromInt(it) }

            else -> throw Exception("Key not understood: $key")
        }
    }

    private fun startNewPhrase() {
        phrase = Phrase().also { word.phrases.add(it) }
    }

    private fun startNewSentence() {
        sentence = Phrase().also { word.sentences.add(it) }
    }

    private fun startNewLink(wordId: String) {
        link = WordLink(wordId).also { word.links.add(it) }
    }
}
