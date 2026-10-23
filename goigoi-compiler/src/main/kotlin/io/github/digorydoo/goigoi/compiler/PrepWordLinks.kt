package io.github.digorydoo.goigoi.compiler

import ch.digorydoo.kutils.cjk.toHiragana
import io.github.digorydoo.goigoi.compiler.vocab.CompilerTopic
import io.github.digorydoo.goigoi.compiler.vocab.CompilerUnyt
import io.github.digorydoo.goigoi.compiler.vocab.CompilerVocab
import io.github.digorydoo.goigoi.compiler.vocab.CompilerWord
import io.github.digorydoo.goigoi.compiler.vocab.CompilerWordLink
import io.github.digorydoo.goigoi.compiler.vocab.CompilerWordLink.Kind

class PrepWordLinks(val vocab: CompilerVocab, val options: Options) {
    data class WordUnytTopic(val word: CompilerWord, val unyt: CompilerUnyt, val topic: CompilerTopic) {
        override fun toString() =
            "WordUnytTopic(${word}, ${unyt}, ${topic})"
    }

    var hiraganaWithMostWords = ""; private set
    var countOfWordsForHiraganaWithMostWords = 0; private set
    var totalNumWordLinks = 0; private set
    var numSeeAlsoLinksFromXML = 0; private set
    var numKeepApartLinksFromXML = 0; private set
    var numKeepTogetherLinksFromXML = 0; private set
    var numAutoSameReadingLinks = 0; private set
    var numAutoSameKanjiLinks = 0; private set
    var numAutoSameTranslationLinks = 0; private set
    var numHiddenWordsWithLinks = 0; private set
    var numVisibleWordsWithActiveLinks = 0; private set
    var mostActiveLinksWord: CompilerWord? = null; private set
    var mostActiveLinksUnyt: CompilerUnyt? = null; private set
    var mostActiveLinksCount = 0; private set

    val hiraganaToVisibleWords: Map<String, List<WordUnytTopic>>
        field = mutableMapOf<String, MutableList<WordUnytTopic>>()

    private val kanjiToVisibleWords = mutableMapOf<String, MutableList<WordUnytTopic>>()
    private val enTranslationToVisibleWords = mutableMapOf<String, MutableList<WordUnytTopic>>()
    private val deTranslationToVisibleWords = mutableMapOf<String, MutableList<WordUnytTopic>>()

    fun prepare() {
        buildHiraganaToVisibleWordsMap() // builds a map from reading in kana to a list of WordAndUnytTopic
        buildKanjiToVisibleWordsMap() // builds a map from kanji to a list of WordUnytTopic
        buildTranslationToVisibleWordsMap() // builds a map from partial translation to a list of WordUnytTopic
        connectSeeAlsoLinks() // finds the other word of see-also links and sets see.word
        if (options.quiet) print(".")

        generateSameReadingLinks() // adds AUTO_SAME_READING between visible words with identical kana
        generateSameKanjiLinks() // adds AUTO_SAME_KANJI between visible words with identical kanji
        generateSameTranslationLinks() // adds AUTO_SAME_TRANSLATION when at least one translation is identical
        buildSeeAlsoLinkStats() // do this even in quiet mode, because it also does some integrity checks

        // Free some memory
        hiraganaToVisibleWords.clear()
        kanjiToVisibleWords.clear()
        enTranslationToVisibleWords.clear()
        deTranslationToVisibleWords.clear()
        if (options.quiet) print(".")
    }

    private fun buildHiraganaToVisibleWordsMap() {
        vocab.forEachVisibleWord { word, _, unyt, topic ->
            val hiragana = word.kana.toHiragana()

            if (!hiraganaToVisibleWords.containsKey(hiragana)) {
                hiraganaToVisibleWords[hiragana] = mutableListOf(WordUnytTopic(word, unyt, topic))
            } else {
                hiraganaToVisibleWords[hiragana]?.add(WordUnytTopic(word, unyt, topic))
            }

            val numWordsWithThisReading = hiraganaToVisibleWords[hiragana]?.size ?: 0

            if (numWordsWithThisReading > countOfWordsForHiraganaWithMostWords) {
                hiraganaWithMostWords = hiragana
                countOfWordsForHiraganaWithMostWords = numWordsWithThisReading
            }
        }
    }

    private fun buildKanjiToVisibleWordsMap() {
        vocab.forEachVisibleWord { word, _, unyt, topic ->
            if (word.kanji != word.kana) {
                if (!kanjiToVisibleWords.containsKey(word.kanji)) {
                    kanjiToVisibleWords[word.kanji] = mutableListOf(WordUnytTopic(word, unyt, topic))
                } else {
                    kanjiToVisibleWords[word.kanji]?.add(WordUnytTopic(word, unyt, topic))
                }
            }
        }
    }

    private fun buildTranslationToVisibleWordsMap() {
        fun addToMap(map: MutableMap<String, MutableList<WordUnytTopic>>, translation: String, wut: WordUnytTopic) {
            if (translation.isEmpty()) return // de is often empty

            translation.split(";").forEach { partialTranslation ->
                val tr = partialTranslation.trim().lowercase()

                if (tr.isEmpty()) {
                    throw CheckFailed("Word ${wut.word} has empty translation")
                }

                if (!map.containsKey(tr)) {
                    map[tr] = mutableListOf(wut)
                } else {
                    val list = map[tr]!!
                    val found = list.find { it.word.id == wut.word.id }
                    if (found == null) list.add(wut)
                }
            }
        }

        vocab.forEachVisibleWord { word, _, unyt, topic ->
            val wut = WordUnytTopic(word, unyt, topic)
            addToMap(enTranslationToVisibleWords, word.translation.en, wut)
            addToMap(deTranslationToVisibleWords, word.translation.de, wut)
        }
    }

    private fun connectSeeAlsoLinks() {
        vocab.forEachWord { word, _, unyt, topic ->
            if (word.links.isNotEmpty()) {
                if (!topic.hidden && !unyt.hidden && !word.hidden) {
                    // For each link, find the first non-hidden word with the linked id (should be unique since
                    // sharedId is no longer supported).

                    for (see in word.links) {
                        vocab.forEachWordWithId(see.wordId) { otherWord, otherUnyt, otherTopic ->
                            if (!otherWord.hidden && !otherUnyt.hidden && !otherTopic.hidden && see.word == null) {
                                see.word = otherWord
                            }
                        }
                    }
                }
            }
        }
    }

    private fun generateSameReadingLinks() {
        vocab.forEachVisibleWord { word ->
            val hiragana = word.kana.toHiragana()

            hiraganaToVisibleWords[hiragana]?.forEach { (otherWord, otherUnyt, otherTopic) ->
                when {
                    otherWord.id == word.id -> Unit
                    word.links.any { it.wordId == otherWord.id && it.kind == Kind.AUTO_SAME_READING } -> Unit
                    otherWord.hidden || otherUnyt.hidden || otherTopic.hidden -> {
                        throw Exception("hiraganaToVisibleWords should not contain hidden word $otherWord")
                    }
                    else -> {
                        // Add a link from word to otherWord.
                        // No need to do the other way round, because the iteration will see that one, too.
                        word.links.add(CompilerWordLink(Kind.AUTO_SAME_READING, otherWord.id, "", otherWord))
                    }
                }
            }
        }
    }

    private fun generateSameKanjiLinks() {
        vocab.forEachVisibleWord { word ->
            kanjiToVisibleWords[word.kanji]?.forEach { (otherWord, otherUnyt, otherTopic) ->
                when {
                    otherWord.id == word.id -> Unit
                    word.links.any { it.wordId == otherWord.id && it.kind == Kind.AUTO_SAME_KANJI } -> Unit
                    otherWord.hidden || otherUnyt.hidden || otherTopic.hidden -> {
                        throw Exception("kanjiToVisibleWords should not contain hidden word $otherWord")
                    }
                    else -> {
                        // Add a link from word to otherWord.
                        // No need to do the other way round, because the iteration will see that one, too.
                        word.links.add(CompilerWordLink(Kind.AUTO_SAME_KANJI, otherWord.id, "", otherWord))
                    }
                }
            }
        }
    }

    private fun generateSameTranslationLinks() {
        fun generateLinks(map: Map<String, MutableList<WordUnytTopic>>, kind: Kind) {
            map.forEach { (tr, list) ->
                list.forEachIndexed { i, wut ->
                    val word = wut.word

                    (i + 1 ..< list.size).forEach { j ->
                        val other = list[j]
                        require(wut.word.id != other.word.id) { "Word added multiple times: ${word.id}" }

                        if (other.word.hidden || other.unyt.hidden || other.topic.hidden) {
                            throw Exception("translationToVisibleWords should not contain hidden word ${other.word}")
                        }

                        if (word.links.none { it.kind == kind && it.wordId == other.word.id }) {
                            word.links.add(CompilerWordLink(kind, other.word.id, tr, other.word))
                        }

                        if (other.word.links.none { it.kind == kind && it.wordId == word.id }) {
                            other.word.links.add(CompilerWordLink(kind, word.id, tr, word))
                        }
                    }
                }
            }
        }

        generateLinks(enTranslationToVisibleWords, Kind.AUTO_SAME_EN_TRANSLATION)
        generateLinks(deTranslationToVisibleWords, Kind.AUTO_SAME_DE_TRANSLATION)
    }

    private fun buildSeeAlsoLinkStats() {
        vocab.forEachWord { word, _, unyt, topic ->
            if (word.links.isNotEmpty()) {
                totalNumWordLinks += word.links.size

                if (topic.hidden || unyt.hidden || word.hidden) {
                    numHiddenWordsWithLinks++

                    for (see in word.links) {
                        when (see.kind) {
                            Kind.XML_SEE_ALSO -> numSeeAlsoLinksFromXML++
                            Kind.XML_KEEP_APART -> numKeepApartLinksFromXML++
                            Kind.XML_KEEP_TOGETHER -> numKeepTogetherLinksFromXML++
                            else -> throw Exception("Should not have generated see-also link for hidden word $word")
                        }
                    }
                } else {
                    var numActiveSeeAlsoOfThisWord = 0

                    for (see in word.links) {
                        when (see.kind) {
                            Kind.XML_SEE_ALSO -> numSeeAlsoLinksFromXML++
                            Kind.XML_KEEP_APART -> numKeepApartLinksFromXML++
                            Kind.XML_KEEP_TOGETHER -> numKeepTogetherLinksFromXML++
                            Kind.AUTO_SAME_READING -> numAutoSameReadingLinks++
                            Kind.AUTO_SAME_KANJI -> numAutoSameKanjiLinks++
                            Kind.AUTO_SAME_EN_TRANSLATION -> numAutoSameTranslationLinks++
                            Kind.AUTO_SAME_DE_TRANSLATION -> numAutoSameTranslationLinks++
                        }

                        val otherWord = see.word

                        if (otherWord != null) {
                            // Just a check to see that the preparation for this link is correct.
                            require(!otherWord.hidden) { "See-also link to a hidden word should not be picked!" }
                            require(see.wordId == otherWord.id) { "See-also link doesn't match the id of the linked word!" }

                            numActiveSeeAlsoOfThisWord++
                        }
                    }

                    if (numActiveSeeAlsoOfThisWord > 0) {
                        numVisibleWordsWithActiveLinks++
                    }

                    if (numActiveSeeAlsoOfThisWord > mostActiveLinksCount) {
                        mostActiveLinksWord = word
                        mostActiveLinksUnyt = unyt
                        mostActiveLinksCount = numActiveSeeAlsoOfThisWord
                    }
                }
            }
        }
    }
}
