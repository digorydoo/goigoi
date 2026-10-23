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
    private class IntermediateAcceptableAnswer {
        var answer = ""
        var accuracy: AnswerAccuracy? = null

        fun setAccuracy(value: String) {
            require(accuracy == null) { "Accuracy already set" }
            val n = value.toIntOrNull() ?: throw Exception("Value for accuracy is not numeric")
            accuracy = AnswerAccuracy.fromInt(n) ?: throw Exception("Not a valid value for accuracy: $n")
        }
    }

    private var phrase: PhraseOrSentence? = null
    private var sentence: PhraseOrSentence? = null
    private var link: WordLink? = null
    private var wordAccept: IntermediateAcceptableAnswer? = null
    private var phraseAccept: IntermediateAcceptableAnswer? = null
    private var sentenceAccept: IntermediateAcceptableAnswer? = null

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

            WORD_BEGIN_ACCEPT -> beginWordAccept(value)
            WORD_ANSWER_ACCURACY -> withWordAccept { it.setAccuracy(value) }
            WORD_END_ACCEPT -> endWordAccept()

            PHRASE_ID -> startNewPhrase()
            PHRASE_PRIMARY_FORM -> withPhrase { it.primaryForm = FuriganaString(value) }
            PHRASE_ROMAJI -> withPhrase { it.romaji = value }
            PHRASE_TRANSLATION_DE -> withPhrase { it.translation.de = value }
            PHRASE_TRANSLATION_EN -> withPhrase { it.translation.en = value }
            PHRASE_TRANSLATION_FR -> withPhrase { it.translation.fr = value }
            PHRASE_TRANSLATION_IT -> withPhrase { it.translation.it = value }
            PHRASE_TRANSLATION_JA -> withPhrase { it.translation.ja = value }
            PHRASE_EXPLANATION_DE -> withPhrase { it.explanation.de = value }
            PHRASE_EXPLANATION_EN -> withPhrase { it.explanation.en = value }
            PHRASE_EXPLANATION_FR -> withPhrase { it.explanation.fr = value }
            PHRASE_EXPLANATION_IT -> withPhrase { it.explanation.it = value }
            PHRASE_EXPLANATION_JA -> withPhrase { it.explanation.ja = value }
            PHRASE_WORD_FORM -> withPhrase { it.wordFormToAsk = FuriganaString(value) }
            PHRASE_WORD_FORM_SUFFIX -> withPhrase { it.wordFormToAskSuffix = value }

            PHRASE_BEGIN_ACCEPT -> beginPhraseAccept(value)
            PHRASE_ANSWER_ACCURACY -> withPhraseAccept { it.setAccuracy(value) }
            PHRASE_END_ACCEPT -> endPhraseAccept()

            SENTENCE_ID -> startNewSentence()
            SENTENCE_PRIMARY_FORM -> withSentence { it.primaryForm = FuriganaString(value) }
            SENTENCE_ROMAJI -> withSentence { it.romaji = value }
            SENTENCE_TRANSLATION_DE -> withSentence { it.translation.de = value }
            SENTENCE_TRANSLATION_EN -> withSentence { it.translation.en = value }
            SENTENCE_TRANSLATION_FR -> withSentence { it.translation.fr = value }
            SENTENCE_TRANSLATION_IT -> withSentence { it.translation.it = value }
            SENTENCE_TRANSLATION_JA -> withSentence { it.translation.ja = value }
            SENTENCE_EXPLANATION_DE -> withSentence { it.explanation.de = value }
            SENTENCE_EXPLANATION_EN -> withSentence { it.explanation.en = value }
            SENTENCE_EXPLANATION_FR -> withSentence { it.explanation.fr = value }
            SENTENCE_EXPLANATION_IT -> withSentence { it.explanation.it = value }
            SENTENCE_EXPLANATION_JA -> withSentence { it.explanation.ja = value }
            SENTENCE_WORD_FORM -> withSentence { it.wordFormToAsk = FuriganaString(value) }
            SENTENCE_WORD_FORM_SUFFIX -> withSentence { it.wordFormToAskSuffix = value }

            SENTENCE_BEGIN_ACCEPT -> beginSentenceAccept(value)
            SENTENCE_ANSWER_ACCURACY -> withSentenceAccept { it.setAccuracy(value) }
            SENTENCE_END_ACCEPT -> endSentenceAccept()

            WORDLINK_ID -> startNewLink(value)
            WORDLINK_PRIMARY_FORM -> withLink { it.primaryForm = value }
            WORDLINK_TRANSLATION_DE -> withLink { it.translation.de = value }
            WORDLINK_TRANSLATION_EN -> withLink { it.translation.en = value }
            WORDLINK_TRANSLATION_FR -> withLink { it.translation.fr = value }
            WORDLINK_TRANSLATION_IT -> withLink { it.translation.it = value }
            WORDLINK_TRANSLATION_JA -> withLink { it.translation.ja = value }
            WORDLINK_KIND -> withLink { link ->
                link.kind = value.toIntOrNull()?.let { WordLink.Kind.fromInt(it) }
            }

            else -> throw Exception("Unexpected marker: $marker")
        }
    }

    private fun beginWordAccept(answer: String) {
        wordAccept = IntermediateAcceptableAnswer().also {
            it.answer = answer
        }
    }

    private fun withWordAccept(lambda: (IntermediateAcceptableAnswer) -> Unit) {
        val wordAccept = wordAccept ?: throw Exception("wordAccept is null")
        lambda(wordAccept)
    }

    private fun endWordAccept() {
        val wordAccept = wordAccept ?: throw Exception("wordAccept is null")
        val answer = wordAccept.answer
        val reason = wordAccept.accuracy ?: throw Exception("reason is null")
        word.acceptableAnswers.add(AcceptableAnswer(answer, reason))
        this.wordAccept = null
    }

    private fun startNewPhrase() {
        phrase = PhraseOrSentence().also { word.phrases.add(it) }
    }

    private fun withPhrase(lambda: (PhraseOrSentence) -> Unit) {
        val phrase = phrase ?: throw Exception("phrase is null")
        lambda(phrase)
    }

    private fun beginPhraseAccept(answer: String) {
        phraseAccept = IntermediateAcceptableAnswer().also {
            it.answer = answer
        }
    }

    private fun withPhraseAccept(lambda: (IntermediateAcceptableAnswer) -> Unit) {
        val phraseAccept = phraseAccept ?: throw Exception("phraseAccept is null")
        lambda(phraseAccept)
    }

    private fun endPhraseAccept() {
        val phraseAccept = phraseAccept ?: throw Exception("phraseAccept is null")
        val answer = phraseAccept.answer
        val reason = phraseAccept.accuracy ?: throw Exception("reason is null")
        withPhrase { it.acceptableAnswers.add(AcceptableAnswer(answer, reason)) }
        this.phraseAccept = null
    }

    private fun startNewSentence() {
        sentence = PhraseOrSentence().also { word.sentences.add(it) }
    }

    private fun withSentence(lambda: (PhraseOrSentence) -> Unit) {
        val sentence = sentence ?: throw Exception("sentence is null")
        lambda(sentence)
    }

    private fun beginSentenceAccept(answer: String) {
        sentenceAccept = IntermediateAcceptableAnswer().also {
            it.answer = answer
        }
    }

    private fun withSentenceAccept(lambda: (IntermediateAcceptableAnswer) -> Unit) {
        val sentenceAccept = sentenceAccept ?: throw Exception("sentenceAccept is null")
        lambda(sentenceAccept)
    }

    private fun endSentenceAccept() {
        val sentenceAccept = sentenceAccept ?: throw Exception("sentenceAccept is null")
        val answer = sentenceAccept.answer
        val reason = sentenceAccept.accuracy ?: throw Exception("reason is null")
        withSentence { it.acceptableAnswers.add(AcceptableAnswer(answer, reason)) }
        this.sentenceAccept = null
    }

    private fun startNewLink(wordId: String) {
        link = WordLink(wordId).also { word.links.add(it) }
    }

    private fun withLink(lambda: (WordLink) -> Unit) {
        val link = link ?: throw Exception("link is null")
        lambda(link)
    }
}
