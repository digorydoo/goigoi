package io.github.digorydoo.goigoi.compiler

import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.string.*
import io.github.digorydoo.goigoi.compiler.vocab.CompilerPhraseOrSentence
import io.github.digorydoo.goigoi.compiler.vocab.CompilerUnyt
import io.github.digorydoo.goigoi.compiler.vocab.CompilerVocab
import io.github.digorydoo.goigoi.compiler.vocab.CompilerWord
import io.github.digorydoo.goigoi.compiler.vocab.CompilerWordLink.Kind
import io.github.digorydoo.goigoi.core.db.AnswerAccuracy
import io.github.digorydoo.goigoi.core.db.WordCategory
import io.github.digorydoo.goigoi.core.db.WordHint
import kotlin.math.roundToInt

class Stats(
    private val vocab: CompilerVocab,
    private val prep: PrepWordLinks,
    private val kanjiLevels: KanjiLevels,
    private val readings: Map<String, Set<String>>,
) {
    private data class VisibleHidden(var visible: Int, var hidden: Int)

    private class UnytAndCount(val unyt: CompilerUnyt, val count: Int) {
        fun print(postfix: String) {
            val total = unyt.countWords { !it.hidden }
            val percent = (100.0f * count / total).roundToInt()
            println("${dots("   " + unyt.nameForStdout)}${lpad(count)} of $total words (${percent}%) $postfix")
        }
    }

    fun print() {
        printWordsStats()
        printGeneralSentencePhraseStats()
        printTopicsAndUnytsStats()
        printRequirePhrasesStats()
        printRequireSentencesStats()
        printPhraseOriginStats()
        printSentenceOriginStats()
        printUnytsWithHiddenSentences()
        printPhraseSentenceAskStats()
        printSurnameFirstnameStats()
        printHintStats()
        printLengthyWordsStats()
        printAcceptableAnswersStats()
        printCategories()
        printMissingCatStats()
        printManualKanjiLevelStats()
        printKanjiStats()
        printVisuallySimilarKanjisStats()
        printReadingsStats()
        printWordLinkStats()
        printWordOrdering()
    }

    private fun printWordsStats() {
        var totalNumUnyts = 0
        var totalNumWords = 0
        var numVisibleWords = 0
        var numVisibleWordsWithSentences = 0
        var numVisibleWordsWithPhrases = 0
        var numWordsWithCustomIds = 0
        var numGermanUnyts = 0
        var numGermanWords = 0
        var numHiddenUnyts = 0
        var numHiddenWords = 0

        var totalN5Count = 0
        var totalN4Count = 0
        var totalN3Count = 0
        var totalN2Count = 0
        var totalN1Count = 0
        var totalNxCount = 0

        val n5PrimaryForms = mutableMapOf<String, VisibleHidden>()
        val n4PrimaryForms = mutableMapOf<String, VisibleHidden>()
        val n3PrimaryForms = mutableMapOf<String, VisibleHidden>()
        val n2PrimaryForms = mutableMapOf<String, VisibleHidden>()
        val n1PrimaryForms = mutableMapOf<String, VisibleHidden>()
        val nxPrimaryForms = mutableMapOf<String, VisibleHidden>()

        vocab.forEachUnyt { unyt, topic ->
            totalNumUnyts++

            if (unyt.requiredTranslations.contains("de")) {
                numGermanUnyts++
            }

            if (topic.hidden || unyt.hidden) {
                numHiddenUnyts++
            }

            unyt.forEachWord { word, _ ->
                totalNumWords++

                when (word.level) {
                    JLPTLevel.N5 -> totalN5Count++
                    JLPTLevel.N4 -> totalN4Count++
                    JLPTLevel.N3 -> totalN3Count++
                    JLPTLevel.N2 -> totalN2Count++
                    JLPTLevel.N1 -> totalN1Count++
                    else -> totalNxCount++
                }

                val pfMap = when (word.level) {
                    JLPTLevel.N5 -> n5PrimaryForms
                    JLPTLevel.N4 -> n4PrimaryForms
                    JLPTLevel.N3 -> n3PrimaryForms
                    JLPTLevel.N2 -> n2PrimaryForms
                    JLPTLevel.N1 -> n1PrimaryForms
                    else -> nxPrimaryForms
                }

                if (!pfMap.contains(word.primaryForm.raw)) {
                    pfMap[word.primaryForm.raw] = VisibleHidden(0, 0)
                }

                pfMap[word.primaryForm.raw]?.apply {
                    if (topic.hidden || unyt.hidden || word.hidden) {
                        hidden++
                    } else {
                        visible++
                    }
                }

                if (word.hasCustomId) {
                    numWordsWithCustomIds++
                }

                if (word.translation.de.isNotEmpty()) {
                    numGermanWords++
                }

                if (topic.hidden || unyt.hidden || word.hidden) {
                    numHiddenWords++
                } else {
                    numVisibleWords++
                }

                if (word.sentences.isNotEmpty()) {
                    if (!topic.hidden && !unyt.hidden && !word.hidden) {
                        numVisibleWordsWithSentences++
                    }
                }

                if (word.phrases.isNotEmpty()) {
                    if (!topic.hidden && !unyt.hidden && !word.hidden) {
                        numVisibleWordsWithPhrases++
                    }
                }
            }
        }

        val n5DistinctVisible = countVisibleHidden(n5PrimaryForms, true)
        val n4DistinctVisible = countVisibleHidden(n4PrimaryForms, true)
        val n3DistinctVisible = countVisibleHidden(n3PrimaryForms, true)
        val n2DistinctVisible = countVisibleHidden(n2PrimaryForms, true)
        val n1DistinctVisible = countVisibleHidden(n1PrimaryForms, true)
        val nxDistinctVisible = countVisibleHidden(nxPrimaryForms, true)

        val totalDistinctVisible = n5DistinctVisible +
            n4DistinctVisible +
            n3DistinctVisible +
            n2DistinctVisible +
            n1DistinctVisible +
            nxDistinctVisible

        val n5DistinctHidden = countVisibleHidden(n5PrimaryForms, false)
        val n4DistinctHidden = countVisibleHidden(n4PrimaryForms, false)
        val n3DistinctHidden = countVisibleHidden(n3PrimaryForms, false)
        val n2DistinctHidden = countVisibleHidden(n2PrimaryForms, false)
        val n1DistinctHidden = countVisibleHidden(n1PrimaryForms, false)
        val nxDistinctHidden = countVisibleHidden(nxPrimaryForms, false)

        val totalDistinctHidden = n5DistinctHidden +
            n4DistinctHidden +
            n3DistinctHidden +
            n2DistinctHidden +
            n1DistinctHidden +
            nxDistinctHidden

        println("")
        println("Total #topics: . . . . . . . . . . . ${lpad(vocab.topics.size)}\n")

        println("Total #unyts:  . . . . . . . . . . . ${lpad(totalNumUnyts)}")
        println("   - fully translated to German: . . ${withPercent(numGermanUnyts, totalNumUnyts)}")
        println("   - hidden: . . . . . . . . . . . . ${withPercent(numHiddenUnyts, totalNumUnyts)}\n")

        println("Words")
        println("   Total #words  . . . . . . . . . . ${lpad(totalNumWords)}")
        println("      - total N5 . . . . . . . . . . ${withPercent(totalN5Count, totalNumWords)}")
        println("      - total N4 . . . . . . . . . . ${withPercent(totalN4Count, totalNumWords)}")
        println("      - total N3 . . . . . . . . . . ${withPercent(totalN3Count, totalNumWords)}")
        println("      - total N2 . . . . . . . . . . ${withPercent(totalN2Count, totalNumWords)}")
        println("      - total N1 . . . . . . . . . . ${withPercent(totalN1Count, totalNumWords)}")
        println("      - total non-JLPT . . . . . . . ${withPercent(totalNxCount, totalNumWords)}")
        println("")
        println("   Distinct visible  . . . . . . . . ${lpad(totalDistinctVisible)}")
        println("      - distinct visible N5  . . . . ${withPercent(n5DistinctVisible, totalDistinctVisible)}")
        println("      - distinct visible N4  . . . . ${withPercent(n4DistinctVisible, totalDistinctVisible)}")
        println("      - distinct visible N3  . . . . ${withPercent(n3DistinctVisible, totalDistinctVisible)}")
        println("      - distinct visible N2  . . . . ${withPercent(n2DistinctVisible, totalDistinctVisible)}")
        println("      - distinct visible N1  . . . . ${withPercent(n1DistinctVisible, totalDistinctVisible)}")
        println("      - distinct visible non-JLPT  . ${withPercent(nxDistinctVisible, totalDistinctVisible)}")
        println("")
        println("   Distinct hidden . . . . . . . . . ${lpad(totalDistinctHidden)}")
        println("      - distinct hidden N5 . . . . . ${withPercent(n5DistinctHidden, totalDistinctHidden)}")
        println("      - distinct hidden N4 . . . . . ${withPercent(n4DistinctHidden, totalDistinctHidden)}")
        println("      - distinct hidden N3 . . . . . ${withPercent(n3DistinctHidden, totalDistinctHidden)}")
        println("      - distinct hidden N2 . . . . . ${withPercent(n2DistinctHidden, totalDistinctHidden)}")
        println("      - distinct hidden N1 . . . . . ${withPercent(n1DistinctHidden, totalDistinctHidden)}")
        println("      - distinct hidden non-JLPT . . ${withPercent(nxDistinctHidden, totalDistinctHidden)}")
        println("")
        println("   Total hidden  . . . . . . . . . . ${withPercent(numHiddenWords, totalNumWords)}")
        println("   Words with German translations  . ${withPercent(numGermanWords, totalNumWords)}")
        println("   Words with custom id  . . . . . . ${withPercent(numWordsWithCustomIds, totalNumWords)}")
        println("   Visible words with sentences. . . ${withPercent(numVisibleWordsWithSentences, numVisibleWords)}")
        println("   Visible words with phrases  . . . ${withPercent(numVisibleWordsWithPhrases, numVisibleWords)}\n")
    }

    private fun printTopicsAndUnytsStats() {
        for (topic in vocab.topics) {
            var text = topic.name.en

            if (topic.hidden) {
                text = "[hidden] $text"
            }

            println("\n" + dots(trunc(text)) + lpad(topic.unyts.size) + " unyts")

            for (unyt in topic.unyts) {
                var numVisible = 0
                var numHidden = 0
                var numSentences = 0
                var numPhrases = 0

                for (section in unyt.sections) {
                    for (word in section.words) {
                        if (word.hidden) {
                            numHidden++
                        } else {
                            numVisible++
                        }

                        numSentences += word.sentences.size
                        numPhrases += word.phrases.size
                    }
                }

                println(
                    dots("   ${unyt.nameForStdout}") +
                        lpad(numVisible) +
                        ", h(${lpadOrEmpty(numHidden)})" +
                        ", s(${lpadOrEmpty(numSentences)})" +
                        ", ph(${lpadOrEmpty(numPhrases)})"
                )
            }
        }
    }

    private fun printLengthyWordsStats() {
        var foundLengthyWords = false

        vocab.forEachUnyt { unyt, _ ->
            var foundLengthyWordsInThisUnyt = false

            unyt.forEachWord { w, _ ->
                val text = when {
                    w.romaji.isNotEmpty() -> w.romaji
                    else -> w.primaryForm.raw
                }

                if (text.length > 20) {
                    if (!foundLengthyWords) {
                        println("Lengthy words")
                        foundLengthyWords = true
                    }

                    if (!foundLengthyWordsInThisUnyt) {
                        println("   " + unyt.nameForStdout)
                        foundLengthyWordsInThisUnyt = true
                    }

                    println("      $text")
                }
            }
        }

        println("")
    }

    private fun printGeneralSentencePhraseStats() {
        var totalNumSentences = 0
        var totalNumPhrases = 0
        var numSentencesWithFormUnchanged = 0
        var numSentencesWithFormSpecified = 0
        var numSentencesThatShouldSpecifyForm = 0
        var numPhrasesWithFormUnchanged = 0
        var numPhrasesWithFormSpecified = 0
        var numPhrasesThatShouldSpecifyForm = 0

        vocab.forEachWord { word ->
            totalNumSentences += word.sentences.size
            totalNumPhrases += word.phrases.size

            for (sentence in word.sentences) {
                if (sentence.wordFormToAsk.isNotEmpty()) {
                    numSentencesWithFormSpecified++
                } else if (
                    (!word.usuallyInKana && sentence.primaryForm.raw.contains(word.primaryForm.raw)) ||
                    (word.usuallyInKana && sentence.kana.contains(word.kana))
                ) {
                    numSentencesWithFormUnchanged++
                } else {
                    numSentencesThatShouldSpecifyForm++
                }
            }

            for (phrase in word.phrases) {
                if (phrase.wordFormToAsk.isNotEmpty()) {
                    numPhrasesWithFormSpecified++
                } else if (
                    (!word.usuallyInKana && phrase.primaryForm.raw.contains(word.primaryForm.raw)) ||
                    (word.usuallyInKana && phrase.kana.contains(word.kana))
                ) {
                    numPhrasesWithFormUnchanged++
                } else {
                    numPhrasesThatShouldSpecifyForm++
                }
            }
        }

        val strSentencesWithFormUnchanged = withPercent(numSentencesWithFormUnchanged, totalNumSentences)
        val strSentencesWithFormSpecified = withPercent(numSentencesWithFormSpecified, totalNumSentences)
        val strSentencesThatShouldSpecifyForm = withPercent(numSentencesThatShouldSpecifyForm, totalNumSentences)

        val strPhrasesWithFormUnchanged = withPercent(numPhrasesWithFormUnchanged, totalNumPhrases)
        val strPhrasesWithFormSpecified = withPercent(numPhrasesWithFormSpecified, totalNumPhrases)
        val strPhrasesThatShouldSpecifyForm = withPercent(numPhrasesThatShouldSpecifyForm, totalNumPhrases)

        println("Total #nested sentences  . . . . . . ${lpad(totalNumSentences)}")
        println("   with form unchanged . . . . . . . $strSentencesWithFormUnchanged")
        println("   with form specified . . . . . . . $strSentencesWithFormSpecified")
        println("   should specify form . . . . . . . $strSentencesThatShouldSpecifyForm")
        println("Total #nested phrases  . . . . . . . ${lpad(totalNumPhrases)}")
        println("   with form unchanged . . . . . . . $strPhrasesWithFormUnchanged")
        println("   with form specified . . . . . . . $strPhrasesWithFormSpecified")
        println("   should specify form . . . . . . . $strPhrasesThatShouldSpecifyForm")
    }

    private fun printUnytsWithHiddenSentences() {
        println("\nUnyts with hidden sentences")

        mutableMapOf<CompilerUnyt, Int>()
            .also { map ->
                vocab.forEachWord { word, _, unyt, topic ->
                    if (word.sentences.isNotEmpty() && (topic.hidden || unyt.hidden || word.hidden)) {
                        val count = map[unyt] ?: 0
                        map[unyt] = count + 1
                    }
                }
            }
            .toList()
            .sortedBy { (_, count) -> -count }
            .onEach { (unyt, count) -> println(dots("   " + unyt.nameForStdout) + lpad(count)) }
            .also { println("   total: ${it.size}") }
    }

    private fun printSurnameFirstnameStats() {
        println("\nSurnames used in visible sentences")

        knownSurnames.forEach { (surname, _) ->
            var count = 0
            vocab.forEachVisibleWord { word ->
                word.sentences.forEach { sentence ->
                    if (sentence.romaji.contains(surname)) {
                        count++
                    }
                }
            }
            println(dots("   $surname") + lpad(count))
        }

        println("\nFirstnames used in visible sentences")

        knownFirstnames.forEach { (firstname, _) ->
            var count = 0
            vocab.forEachVisibleWord { word ->
                word.sentences.forEach { sentence ->
                    if (sentence.romaji.contains(firstname)) {
                        count++
                    }
                }
            }
            println(dots("   $firstname") + lpad(count))
        }
    }

    private fun printRequireSentencesStats() {
        println("\nUnyts that require sentences for each word")

        mutableListOf<UnytAndCount>()
            .also { list ->
                vocab.forEachUnyt { unyt, _ ->
                    if (unyt.requiresSentences) {
                        val count = unyt.countVisibleWordsWithSentences()
                        list.add(UnytAndCount(unyt, count))
                    }
                }
            }
            .sortedBy { -it.count }
            .onEach { it.print("have sentences") }
            .also { println("   total: ${it.size}") }

        println("\nUnyts that do not require sentences for each word")

        mutableListOf<UnytAndCount>()
            .also { list ->
                vocab.forEachUnyt { unyt, _ ->
                    if (!unyt.requiresSentences) {
                        val count = unyt.countVisibleWordsWithSentences()
                        list.add(UnytAndCount(unyt, count))
                    }
                }
            }
            .sortedBy { -it.count }
            .onEach { it.print("have sentences") }
            .also { println("   total: ${it.size}") }
    }

    private fun printRequirePhrasesStats() {
        println("\nUnyts that require phrases for each word")

        mutableListOf<UnytAndCount>()
            .also { list ->
                vocab.forEachUnyt { unyt, _ ->
                    if (unyt.requiresPhrases) {
                        val count = unyt.countVisibleWordsWithPhrases()
                        list.add(UnytAndCount(unyt, count))
                    }
                }
            }
            .sortedBy { -it.count }
            .onEach { it.print("have phrases") }
            .also { println("   total: ${it.size}") }

        println("\nUnyts that do not require phrases for each word")

        mutableListOf<UnytAndCount>()
            .also { list ->
                vocab.forEachUnyt { unyt, _ ->
                    if (!unyt.requiresPhrases) {
                        val count = unyt.countVisibleWordsWithPhrases()
                        list.add(UnytAndCount(unyt, count))
                    }
                }
            }
            .sortedBy { -it.count }
            .onEach { it.print("have phrases") }
            .also { println("   total: ${it.size}") }
    }

    private fun printSentenceOriginStats() {
        println("\nOrigins of sentences")

        val originsMap = mutableMapOf<String, Int>()

        vocab.forEachVisibleWord { word ->
            word.sentences.forEach { sentence ->
                val key = sentence.origin.let { o ->
                    when (o.isEmpty()) {
                        true -> "empty"
                        false -> originPrefixes.find { o.startsWith(it) }
                            ?: throw CheckFailed(
                                "Word ${word.id}: No prefix matches origin of sentence: ${sentence.origin}"
                            )
                    }
                }
                val count = originsMap[key] ?: 0
                originsMap[key] = count + 1
            }
        }

        originPrefixes.forEach { prefix ->
            println(dots("   $prefix") + lpad(originsMap[prefix] ?: 0))
        }

        println(dots("   empty") + lpad(originsMap["empty"] ?: 0))
    }

    private fun printPhraseOriginStats() {
        println("\nOrigins of phrases")

        val originsMap = mutableMapOf<String, Int>()

        vocab.forEachVisibleWord { word ->
            word.phrases.forEach { phrase ->
                val key = phrase.origin.let { o ->
                    when (o.isEmpty()) {
                        true -> "empty"
                        false -> originPrefixes.find { o.startsWith(it) }
                            ?: throw CheckFailed(
                                "Word ${word.id}: No prefix matches origin of phrase: ${phrase.origin}"
                            )
                    }
                }
                val count = originsMap[key] ?: 0
                originsMap[key] = count + 1
            }
        }

        originPrefixes.forEach { prefix ->
            println(dots("   $prefix") + lpad(originsMap[prefix] ?: 0))
        }

        println(dots("   empty") + lpad(originsMap["empty"] ?: 0))
    }

    private fun printManualKanjiLevelStats() {
        println("Manual kanji levels")

        JLPTLevel.entries.forEach { lvl ->
            val count = vocab.manualKanjiLevels[lvl]?.size ?: 0
            println(dots("   $lvl") + (lpad(count)))
        }
    }

    private fun printCategories() {
        println("Categories")

        val categoriesUsedByVisibleWordsSolo = mutableMapOf<WordCategory, MutableSet<CompilerWord>>()
        val categoriesUsedByVisibleWordsInCombination = mutableMapOf<WordCategory, MutableSet<CompilerWord>>()
        val categoriesUsedByHiddenWords = mutableMapOf<WordCategory, MutableSet<CompilerWord>>()

        vocab.forEachUnyt { unyt, topic ->
            unyt.forEachWord { word, _ ->
                val catsMap = when {
                    topic.hidden || unyt.hidden || word.hidden -> categoriesUsedByHiddenWords
                    word.cats.size == 1 -> categoriesUsedByVisibleWordsSolo
                    else -> categoriesUsedByVisibleWordsInCombination
                }

                word.cats.forEach { cat ->
                    catsMap[cat] = (catsMap[cat] ?: mutableSetOf()).apply { add(word) }
                }
            }
        }

        WordCategory.entries.forEach { cat ->
            val numHidden = categoriesUsedByHiddenWords[cat]?.size ?: 0
            val numSolo = categoriesUsedByVisibleWordsSolo[cat]?.size ?: 0
            val numInCombi = categoriesUsedByVisibleWordsInCombination[cat]?.size ?: 0
            println(
                dots("   ${cat.text}")
                    + "${lpad(numSolo)} solo"
                    + (if (numInCombi == 0) "" else " + ${lpad(numInCombi)} combined")
                    + (if (numHidden == 0) "" else " + ${lpad(numHidden)} hidden")
            )
        }
    }

    private fun printWordLinkStats() {
        println("Hiragana reading with most #words  . ${lpad(prep.hiraganaWithMostWords)}")
        println("# of vis. words using that reading . ${lpad(prep.countOfWordsForHiraganaWithMostWords)}")

        prep.hiraganaToVisibleWords[prep.hiraganaWithMostWords]?.forEach { (w, u, _) ->
            print("   ${lpad(trunc(u.name.en, 20), 20)}: ")
            println(
                w.toPrettyString(withRomaji = true, withLvl = true, withKanjiKanaSeparated = true, withColour = false)
            )
        }

        println("")
        println("Total # word links . . . . . . . . . ${lpad(prep.totalNumWordLinks)}")
        println("   manual see-also links . . . . . . ${lpad(prep.numSeeAlsoLinksFromXML)}")
        println("   manual keep-apart links . . . . . ${lpad(prep.numKeepApartLinksFromXML)}")
        println("   manual keep-together links  . . . ${lpad(prep.numKeepTogetherLinksFromXML)}")
        println("   generated (same reading)  . . . . ${lpad(prep.numAutoSameReadingLinks)}")
        println("   generated (same kanji)  . . . . . ${lpad(prep.numAutoSameKanjiLinks)}")
        println("   generated (same translation)  . . ${lpad(prep.numAutoSameTranslationLinks)}")
        println("")
        println("# of vis. words with active links  . ${lpad(prep.numVisibleWordsWithActiveLinks)}")
        println("# of hidden words with links . . . . ${lpad(prep.numHiddenWordsWithLinks)}")

        print("\nWord with the most of active links:")

        val mostActiveLinksWord = prep.mostActiveLinksWord

        if (mostActiveLinksWord == null) {
            println(" none")
        } else {
            print("\n   ")

            println(
                mostActiveLinksWord.toPrettyString(
                    withRomaji = true,
                    withLvl = true,
                    withKanjiKanaSeparated = true,
                    withColour = false
                )
            )

            println("   Unyt ${prep.mostActiveLinksUnyt?.nameForStdout}")
            println("   # of links: ${prep.mostActiveLinksCount}")

            mostActiveLinksWord.links.forEach { see ->
                println("   - id=${see.wordId}")
            }
        }

        println("\nAll see-also links of visible words:")

        val words = mutableListOf<CompilerWord>()
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
                                Kind.XML_SEE_ALSO -> "<see> ${see.type}"
                                Kind.XML_KEEP_APART -> "<keep_apart_from>"
                                Kind.XML_KEEP_TOGETHER -> "<keep_together>"
                                Kind.AUTO_SAME_READING -> "auto, same reading"
                                Kind.AUTO_SAME_KANJI -> "auto, same kanji"
                                Kind.AUTO_SAME_EN_TRANSLATION -> "auto, same EN translation '${see.type}'"
                                Kind.AUTO_SAME_DE_TRANSLATION -> "auto, same DE translation '${see.type}'"
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

    private fun wordToString(word: CompilerWord, rem: String) =
        arrayOf(
            lpad(word.romaji, 20),
            lpad(word.level?.toString() ?: "", 2),
            lpad(word.kanji, 4, '　'),
            lpad(word.kana, 6, '　'),
            rem,
        )
            .joinToString(" ・ ")

    private fun countVisibleHidden(map: Map<String, VisibleHidden>, visible: Boolean): Int {
        var result = 0

        map.forEach { pair ->
            if (visible && pair.value.visible > 0) {
                result++
            } else if (!visible && pair.value.hidden > 0) {
                result++
            }
        }

        return result
    }

    private fun printKanjiStats() {
        val allKanjis = mutableSetOf<Char>().apply {
            JLPTLevel.entries.forEach { lvl ->
                addAll(kanjiLevels.get(lvl))
            }
        }

        println("\nTotal number of known kanjis: ${allKanjis.size}")

        JLPTLevel.entries.forEach { lvl ->
            println("   $lvl: ${kanjiLevels.get(lvl).size}")
        }

        println("\nManual kanji JLPT level corrections:")

        vocab.manualKanjiLevels.forEach { (level, kanjis) ->
            println("   ${level}: ${kanjis.size}")
        }

        println("\nKanjis by schoolyear:")

        vocab.kanjiBySchoolYear.forEach { (year, kanjis) ->
            println("   ${year}: ${kanjis.size}")
        }

        val kanjisNotInAnySchoolYear = mutableSetOf<Char>()
            .apply { addAll(allKanjis) }
            .filter { kanji ->
                !vocab.kanjiBySchoolYear.any { (_, kanjis) ->
                    kanjis.contains(kanji)
                }
            }
            .sorted()

        println("\nKanjis not in any schoolyear: ${kanjisNotInAnySchoolYear.size}")
        println("   ${kanjisNotInAnySchoolYear.joinToString("")}")

        println(
            "\nKanjis by frequency: ${vocab.kanjiByFreq.length}"
        )

        val kanjisWithUnknownFreq = mutableSetOf<Char>()
            .apply { addAll(allKanjis) }
            .filter { kanji -> !vocab.kanjiByFreq.contains(kanji) }
            .sorted()

        println("\nKanjis with unknown frequency: ${kanjisWithUnknownFreq.size}")
        println("   ${kanjisWithUnknownFreq.joinToString("")}")
    }

    private fun printReadingsStats() {
        println("\nTotal number of readings: ${readings.size}")

        val kanjisMap = mutableMapOf<String, MutableSet<String>>()
        var mostReadingsCount = 0
        var mostReadingsKanji = ""
        var readingWithMostKanji: Map.Entry<String, Set<String>>? = null

        readings.forEach { reading ->
            val kana = reading.component1()
            val kanjis = reading.component2()

            if (kanjis.size > (readingWithMostKanji?.component2()?.size ?: 0)) {
                readingWithMostKanji = reading
            }

            kanjis.forEach { kanji ->
                val set = kanjisMap[kanji]
                    ?: mutableSetOf<String>().also { kanjisMap[kanji] = it }

                set.add(kana)

                if (set.size > mostReadingsCount) {
                    mostReadingsCount = set.size
                    mostReadingsKanji = kanji
                }
            }
        }

        println("")
        println("Kanji with the most readings: . $mostReadingsKanji")
        println("   Count: . . . . . . . . . . . $mostReadingsCount")
        println("   Readings: " + (kanjisMap[mostReadingsKanji]?.sorted()?.joinToString("、") ?: ""))
        println("")

        println("Reading with the most kanjis: . ${readingWithMostKanji?.component1()}")
        println("   Count: . . . . . . . . . . . ${readingWithMostKanji?.component2()?.size ?: 0}")
        println("   Kanjis: " + (readingWithMostKanji?.component2()?.sorted()?.joinToString("、") ?: ""))
        println("")

        println("All kanjis with their readings:")

        kanjisMap
            .toSortedMap()
            .forEach { (kanji, readings) ->
                println("   $kanji: " + readings.sorted().joinToString("、"))
            }

        println("")
        println("All readings with their kanjis:")

        readings
            .toSortedMap()
            .forEach { (kana, kanjis) ->
                println("   $kana: " + kanjis.sorted().joinToString("、"))
            }

        println("")
    }

    private fun printHintStats() {
        val enHintCounts = mutableMapOf<String, Int>()
        val hint2Counts = mutableMapOf<WordHint, Int>()

        vocab.forEachVisibleWord { word ->
            if (word.hint.en != "") {
                enHintCounts[word.hint.en] = (enHintCounts[word.hint.en] ?: 0) + 1
            }

            if (word.hint2 != null) {
                hint2Counts[word.hint2!!] = (hint2Counts[word.hint2] ?: 0) + 1
            }
        }

        println("\nHints most often used that do not have an enum yet")

        enHintCounts.entries
            .sortedBy { (_, value) -> -value } // minus reverses the list
            .take(10)
            .forEach { (hint, count) ->
                println("   ${count}x $hint")
            }

        println("\nHints that have an enum used the least number of times")

        hint2Counts.entries
            .sortedBy { (_, value) -> value }
            .take(10)
            .forEach { (hint, count) ->
                println("   ${count}x ${hint.en}")
            }

        println("")
    }

    private fun printAcceptableAnswersStats() {
        class Values {
            var numWords = 0
            var topWord: CompilerWord? = null
            var countOfTopWord = 0
            var prevWord: CompilerWord? = null

            fun add(word: CompilerWord, count: Int) {
                if (count > 0) {
                    if (word != prevWord) numWords++

                    if (topWord == null || count > countOfTopWord) {
                        topWord = word
                        countOfTopWord = count
                    }
                }
                prevWord = word
            }

            fun printTopWord() {
                print("   Top word: . . . . . . . . . . . . . . . . ")
                print(
                    topWord?.toPrettyString(
                        withKanjiKanaSeparated = true,
                        withId = true,
                        withColour = false
                    )
                )
                println(" ($countOfTopWord entries)")
            }
        }

        val map = mutableMapOf<AnswerAccuracy, Values>()

        vocab.forEachWord { word ->
            for (acceptable in word.acceptableAnswers) {
                val values = map[acceptable.accuracy] ?: Values().also { map[acceptable.accuracy] = it }
                values.add(word, 1)
            }
        }

        AnswerAccuracy.entries.forEach { reason ->
            if (reason.intendedForWord) {
                val values = map[reason]
                println("Total # words with ${reason.xmlKey}: ${values?.numWords ?: 0}")
                values?.printTopWord()
            }
        }

        println("")
    }

    private fun printPhraseSentenceAskStats() {
        class Values {
            var numAsk = 0
            var numAskWithSuffix = 0
            var numAskWithAccept = 0

            var curWord: CompilerWord? = null
            var countOfCurWord = 0

            var wordWithMostAsk: CompilerWord? = null
            var countOfWordWithMostAsk = 0

            fun add(word: CompilerWord, phraseOrSentence: CompilerPhraseOrSentence) {
                if (phraseOrSentence.wordFormToAsk.isNotEmpty()) {
                    numAsk++

                    if (word == curWord) {
                        countOfCurWord++
                    } else {
                        curWord = word
                        countOfCurWord = 1
                    }

                    if (wordWithMostAsk == null || countOfWordWithMostAsk < countOfCurWord) {
                        wordWithMostAsk = word
                        countOfWordWithMostAsk = countOfCurWord
                    }
                }

                if (phraseOrSentence.wordFormToAskSuffix.isNotEmpty()) {
                    numAskWithSuffix++
                }

                if (phraseOrSentence.acceptableAnswers.isNotEmpty()) {
                    numAskWithAccept++
                }
            }

            fun printDetails() {
                println("   with stem/suffix:  . . . . . . . . . $numAskWithSuffix")
                println("   with <accept>: . . . . . . . . . . . $numAskWithAccept")
                print("   Word with most <ask>:  . . . . . . . ")

                print(
                    wordWithMostAsk?.toPrettyString(
                        withKanjiKanaSeparated = true,
                        withId = true,
                        withColour = false
                    )
                )

                println(" ($countOfWordWithMostAsk)")
            }
        }

        val phraseValues = Values()
        val sentenceValues = Values()

        vocab.forEachWord { word ->
            for (phrase in word.phrases) {
                phraseValues.add(word, phrase)
            }

            for (sentence in word.sentences) {
                sentenceValues.add(word, sentence)
            }
        }

        println("\nTotal number of phrases with <ask>: . . ${phraseValues.numAsk}")
        phraseValues.printDetails()

        println("\nTotal number of sentences with <ask>: . ${sentenceValues.numAsk}")
        sentenceValues.printDetails()

        println("")
    }

    private fun printVisuallySimilarKanjisStats() {
        run {
            var count = 0

            vocab.visuallySimilar.forEach { groupOfKanjis ->
                count += groupOfKanjis.length
            }

            println("\"Visually similar kanjis:")
            println("   - Total number of kanjis covered: $count")
        }

        arrayOf(JLPTLevel.N5, JLPTLevel.N4, JLPTLevel.N3, JLPTLevel.N2, JLPTLevel.N1).forEach { lvl ->
            vocab.manualKanjiLevels[lvl]
                ?.filter { kanji ->
                    vocab.visuallySimilar.all { groupOfKanjis ->
                        groupOfKanjis.none { it == kanji }
                    }
                }
                .let { kanjisOrNull ->
                    print("   - $lvl kanjis not covered: ")
                    println(if (kanjisOrNull?.isEmpty() != false) "none" else kanjisOrNull.joinToString(""))
                }
        }
    }

    private fun printMissingCatStats() {
        val missingCatsMap = mutableMapOf<JLPTLevel, Int>()

        vocab.forEachVisibleWord { w ->
            if (w.cats.isEmpty()) {
                missingCatsMap[w.level ?: JLPTLevel.Nx] = 1 + (missingCatsMap[w.level ?: JLPTLevel.Nx] ?: 0)
            }
        }

        println("\nUncategorised words:")

        arrayOf(JLPTLevel.N5, JLPTLevel.N4, JLPTLevel.N3, JLPTLevel.N2, JLPTLevel.N1).forEach { level ->
            val count = missingCatsMap[level] ?: 0
            println("   $level: . . . $count")
        }

        println("")
    }

    private fun printWordOrdering() {
        // The word ordering is relevant for super progressive mode and is based on the filenames.
        println("\nWord ordering:")
        val words = mutableListOf<CompilerWord>()
        vocab.forEachVisibleWord { words.add(it) }

        words.sortWith { w1, w2 ->
            w1.fileName.compareTo(w2.fileName)
        }

        var firstWordWithNoPhrase: CompilerWord? = null
        var firstWordWithNoSentence: CompilerWord? = null

        words.forEach { word ->
            if (firstWordWithNoPhrase == null && word.phrases.isEmpty()) {
                firstWordWithNoPhrase = word
            }
            if (firstWordWithNoSentence == null && word.sentences.isEmpty()) {
                firstWordWithNoSentence = word
            }
            println(
                "   " + word.toPrettyString(
                    withKanjiKanaSeparated = true,
                    withLvl = true,
                    withId = true,
                    withCats = true,
                    withColour = false,
                )
            )
        }

        firstWordWithNoPhrase
            ?.toPrettyString(withKanjiKanaSeparated = true, withLvl = true, withId = true, withColour = false)
            .let { print("\nFirst word with no phrase: $it") }

        firstWordWithNoSentence
            ?.toPrettyString(withKanjiKanaSeparated = true, withLvl = true, withId = true, withColour = false)
            .let { print("\nFirst word with no sentence: $it") }

        println()
    }

    companion object {
        private val originPrefixes = arrayOf(
            "self",
            "500mon N3", // must appear before 500mon in this list
            "500mon N2", // dito
            "500mon N1", // dito
            "500mon", // denotes 500mon N5/n4
            "BondLingo",
            "ChatGPT",
            "DeepL",
            "Dragon Ball",
            "Doraemon",
            "GENKI",
            "Kaname Naito",
            "Langenscheidt",
            "Miku",
            "Oxford",
            "Yuko",
            "duolingo",
            "hinative",
            "jisho",
            "jpod",
            "lib0",
            "tofugu",
        )
    }
}
