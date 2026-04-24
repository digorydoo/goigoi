package io.github.digorydoo.goigoi.compiler

import io.github.digorydoo.goigoi.compiler.vocab.GoigoiPhrase
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import java.io.File
import java.io.FileWriter

class WordIndexWriter(private val vocab: GoigoiVocab, private val quiet: Boolean) {
    /**
     * The word index is used by jwotd/goigoi-find-missing.sh to iterate over Goigoi words.
     */
    fun writeWordIndex(path: String) {
        val file = File(path)
        if (!quiet) println("Writing ${file.name}...")

        FileWriter(file).use { writer ->
            writer.write("[\n")
            // goigoi-find-missing must find the word even if it's hidden in Goigoi
            vocab.forEachWord { w ->
                writeWord(w, writer)
            }
            writer.write("null]\n")
        }
    }

    /**
     * The phrase index is used by Sites/api/search.php to include Goigoi phrases in the search results.
     */
    fun writePhraseIndex(path: String) {
        val file = File(path)
        if (!quiet) println("Writing ${file.name}...")

        FileWriter(file).use { writer ->
            writer.write("[\n")
            // Only phrases from visible words should appear in the index
            vocab.forEachVisibleWord { w ->
                w.phrases.forEach { phrase ->
                    writePhraseOrSentence(phrase, writer)
                }
            }
            writer.write("null]\n")
        }
    }

    /**
     * The sentence index is used by Sites/api/search.php to include Goigoi sentences in the search results.
     */
    fun writeSentenceIndex(path: String) {
        val file = File(path)
        if (!quiet) println("Writing ${file.name}...")

        FileWriter(file).use { writer ->
            writer.write("[\n")
            // Only sentences from visible words should appear in the index
            vocab.forEachVisibleWord { w ->
                w.sentences.forEach { sentence ->
                    writePhraseOrSentence(sentence, writer)
                }
            }
            writer.write("null]\n")
        }
    }

    private fun writeWord(word: GoigoiWord, writer: FileWriter) {
        val body = arrayOf(
            "\"k\":${encodeJSONValue(if (word.usuallyInKana || word.kanji == word.kana) "" else word.kanji)}",
            "\"n\":${encodeJSONValue(word.kana)}",
            "\"r\":${encodeJSONValue(word.romaji)}",
            "\"t\":${encodeJSONValue(word.translation.en)}",
            "\"l\":${encodeJSONValue(word.level?.toString() ?: "-")}",
        ).joinToString(",")

        writer.write("{$body},\n")
    }

    private fun writePhraseOrSentence(phrase: GoigoiPhrase, writer: FileWriter) {
        val body = arrayOf(
            "\"jp\":${encodeJSONValue(phrase.kanji)}",
            "\"en\":${encodeJSONValue(phrase.translation.en)}",
            "\"o\":\"goigoi\"",
        ).joinToString(",")

        writer.write("{$body},\n")
    }
}
