package io.github.digorydoo.goigoi.compiler

import io.github.digorydoo.goigoi.compiler.vocab.GoigoiTopic
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiUnyt
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWordLink
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWordLink.Kind
import ch.digorydoo.kutils.cjk.toHiragana
import ch.digorydoo.kutils.string.lpad
import ch.digorydoo.kutils.string.rpad
import ch.digorydoo.kutils.string.trunc

class PrepWordLinks(val vocab: GoigoiVocab, val options: Options) {
    private data class WordUnytTopic(val word: GoigoiWord, val unyt: GoigoiUnyt, val topic: GoigoiTopic) {
        override fun toString() =
            "WordUnytTopic(${word}, ${unyt}, ${topic})"
    }

    private var hiraganaWithMostWords = ""
    private var countOfWordsForHiraganaWithMostWords = 0
    private var totalNumWordLinks = 0
    private var numSeeAlsoLinksFromXML = 0
    private var numKeepApartLinksFromXML = 0
    private var numKeepTogetherLinksFromXML = 0
    private var numAutoSameReadingLinks = 0
    private var numAutoSameKanjiLinks = 0
    private var numAutoSameTranslationLinks = 0
    private var numHiddenWordsWithLinks = 0
    private var numVisibleWordsWithActiveLinks = 0
    private var mostActiveLinksWord: GoigoiWord? = null
    private var mostActiveLinksUnyt: GoigoiUnyt? = null
    private var mostActiveLinksCount = 0

    private val hiraganaToVisibleWords = mutableMapOf<String, MutableList<WordUnytTopic>>()
    private val kanjiToVisibleWords = mutableMapOf<String, MutableList<WordUnytTopic>>()
    private val enTranslationToVisibleWords = mutableMapOf<String, MutableList<WordUnytTopic>>()
    private val deTranslationToVisibleWords = mutableMapOf<String, MutableList<WordUnytTopic>>()

    fun prepare() {
        buildHiraganaToVisibleWordsMap() // builds a map from reading in kana to a list of WordAndUnytTopic
        buildKanjiToVisibleWordsMap() // builds a map from kanji to a list of WordUnytTopic
        buildTranslationToVisibleWordsMap() // builds a map from partial translation to a list of WordUnytTopic

        connectSeeAlsoLinks() // finds the other word of see-also links and sets see.word
        generateSameReadingLinks() // adds AUTO_SAME_READING between visible words with identical kana
        generateSameKanjiLinks() // adds AUTO_SAME_KANJI between visible words with identical kanji
        generateSameTranslationLinks() // adds AUTO_SAME_TRANSLATION when at least one translation is identical

        buildSeeAlsoLinkStats() // do this even in quiet mode, because it also does some integrity checks

        if (!options.quiet) {
            printStats()
        }

        // Free some memory
        hiraganaToVisibleWords.clear()
        kanjiToVisibleWords.clear()
        enTranslationToVisibleWords.clear()
        deTranslationToVisibleWords.clear()
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
                    throw CheckFailed("Word has empty translation", wut.unyt, wut.word)
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
                        word.links.add(GoigoiWordLink(Kind.AUTO_SAME_READING, otherWord.id, "", otherWord))
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
                        word.links.add(GoigoiWordLink(Kind.AUTO_SAME_KANJI, otherWord.id, "", otherWord))
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
                            word.links.add(GoigoiWordLink(kind, other.word.id, tr, other.word))
                        }

                        if (other.word.links.none { it.kind == kind && it.wordId == word.id }) {
                            other.word.links.add(GoigoiWordLink(kind, word.id, tr, word))
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

    private fun printStats() {
        println("")
        println("Hiragana reading with most #words  . ${lpad(hiraganaWithMostWords)}")
        println("# of vis. words using that reading . ${lpad(countOfWordsForHiraganaWithMostWords)}")

        hiraganaToVisibleWords[hiraganaWithMostWords]?.forEach { (w, u, _) ->
            print("   ${lpad(trunc(u.name.en, 20), 20)}: ")
            w.prettyPrint(withRomaji = true, withLvl = true, withKanjiKanaSeparated = true, withColour = false)
        }

        println("")
        println("Total # word links . . . . . . . . . ${lpad(totalNumWordLinks)}")
        println("   manual see-also links . . . . . . ${lpad(numSeeAlsoLinksFromXML)}")
        println("   manual keep-apart links . . . . . ${lpad(numKeepApartLinksFromXML)}")
        println("   manual keep-together links  . . . ${lpad(numKeepTogetherLinksFromXML)}")
        println("   generated (same reading)  . . . . ${lpad(numAutoSameReadingLinks)}")
        println("   generated (same kanji)  . . . . . ${lpad(numAutoSameKanjiLinks)}")
        println("   generated (same translation)  . . ${lpad(numAutoSameTranslationLinks)}")
        println("")
        println("# of vis. words with active links  . ${lpad(numVisibleWordsWithActiveLinks)}")
        println("# of hidden words with links . . . . ${lpad(numHiddenWordsWithLinks)}")

        print("\nWord with the most of active links:")

        val mostActiveLinksWord = mostActiveLinksWord

        if (mostActiveLinksWord == null) {
            println(" none")
        } else {
            print("\n   ")

            mostActiveLinksWord.prettyPrint(
                withRomaji = true,
                withLvl = true,
                withKanjiKanaSeparated = true,
                withColour = false
            )

            println("   Unyt ${mostActiveLinksUnyt?.nameForStdout}")
            println("   # of links: $mostActiveLinksCount")

            mostActiveLinksWord.links.forEach { see ->
                println("   - id=${see.wordId}")
            }
        }

        println("\nAll see-also links of visible words:")

        val words = mutableListOf<GoigoiWord>()
        vocab.forEachVisibleWord { words.add(it) }

        words
            .sortedBy { it.id }
            .forEach { word ->
                if (word.links.isNotEmpty()) {
                    println("")
                    println("   from ${wordToString(word, word.id)}")

                    word.links
                        .sortedBy { "${it.word?.id}" }
                        .forEach { see ->
                            val rem = when (see.kind) {
                                Kind.XML_SEE_ALSO -> "<see> ${see.remark}"
                                Kind.XML_KEEP_APART -> "<keep_apart_from>"
                                Kind.XML_KEEP_TOGETHER -> "<keep_together>"
                                Kind.AUTO_SAME_READING -> "auto, same reading"
                                Kind.AUTO_SAME_KANJI -> "auto, same kanji"
                                Kind.AUTO_SAME_EN_TRANSLATION -> "auto, same EN translation '${see.remark}'"
                                Kind.AUTO_SAME_DE_TRANSLATION -> "auto, same DE translation '${see.remark}'"
                            }

                            val to = see.word

                            if (to == null) {
                                // Happens with explicit see-also links when the target word is hidden
                                println("     to HIDDEN word id=${see.wordId} ($rem)")
                            } else {
                                println("     to ${wordToString(to, "${rpad(to.id, 20)} ($rem)")}")
                            }
                        }
                }
            }
    }

    private fun wordToString(word: GoigoiWord, rem: String) =
        arrayOf(
            lpad(word.romaji, 20),
            lpad(word.level?.toString() ?: "", 2),
            lpad(word.kanji, 4, '　'),
            lpad(word.kana, 6, '　'),
            rem,
        )
            .joinToString(" ・ ")
}
