package io.github.digorydoo.goigoi.compiler.stats

import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.string.dots
import ch.digorydoo.kutils.string.lpad
import ch.digorydoo.kutils.string.lpadOrEmpty
import ch.digorydoo.kutils.string.trunc
import ch.digorydoo.kutils.string.withPercent
import io.github.digorydoo.goigoi.compiler.knownFirstnames
import io.github.digorydoo.goigoi.compiler.knownSurnames
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiUnyt
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import io.github.digorydoo.goigoi.core.WordCategory
import kotlin.math.roundToInt

object Stats {
    private data class VisibleHidden(var visible: Int, var hidden: Int)

    fun printStats(vocab: GoigoiVocab) {
        var totalNumUnyts = 0
        var totalNumWords = 0
        var totalNumSentences = 0
        var totalNumPhrases = 0
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

        val categoriesUsedByVisibleWordsSolo = mutableMapOf<WordCategory, MutableSet<GoigoiWord>>()
        val categoriesUsedByVisibleWordsInCombination = mutableMapOf<WordCategory, MutableSet<GoigoiWord>>()
        val categoriesUsedByHiddenWords = mutableMapOf<WordCategory, MutableSet<GoigoiWord>>()

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
                    totalNumSentences += word.sentences.size

                    if (!topic.hidden && !unyt.hidden && !word.hidden) {
                        numVisibleWordsWithSentences++
                    }
                }

                if (word.phrases.isNotEmpty()) {
                    totalNumPhrases += word.phrases.size

                    if (!topic.hidden && !unyt.hidden && !word.hidden) {
                        numVisibleWordsWithPhrases++
                    }
                }

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

        println("Total #nested sentences  . . . . . . ${lpad(totalNumSentences)}")
        println("Total #nested phrases  . . . . . . . ${lpad(totalNumPhrases)}")

        // Print the name of each unyt along with the number of words in it

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

        // Print the name of unyts that have lengthy words

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
                        println("\nLengthy words")
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

        // Print the name of unyts that require sentences for each word

        println("\nUnyts that require sentences for each word")

        class UnytAndCount(val unyt: GoigoiUnyt, val count: Int) {
            fun print(postfix: String) {
                val total = unyt.countWords { !it.hidden }
                val percent = (100.0f * count / total).roundToInt()
                println("${dots("   " + unyt.nameForStdout)}${lpad(count)} of $total words (${percent}%) $postfix")
            }
        }

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

        // Print the name of unyts that do not require sentences for each word

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

        // Print the name of unyts that contain hidden sentences

        println("\nUnyts with hidden sentences")

        mutableMapOf<GoigoiUnyt, Int>()
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

        // Count the number of sentence per known surname

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

        // Count the number of sentence per known firstname

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

        // Count the number of sentence origins

        println("\nOrigins of sentences")

        val originPrefixes = arrayOf(
            "self",
            "500mon",
            "GENKI",
            "lib0",
            "jpod",
            "Oxford",
            "Kaname Naito",
            "Langenscheidt",
            "Miku",
            "Yuko",
            "BondLingo",
            "duolingo",
            "hinative",
            "tofugu",
            "ChatGPT",
            "Doraemon",
        )
        val originsMap = mutableMapOf<String, Int>()

        vocab.forEachVisibleWord { word ->
            word.sentences.forEach { sentence ->
                val key = sentence.origin.let { o ->
                    when (o.isEmpty()) {
                        true -> "empty"
                        false -> originPrefixes.find { o.startsWith(it) } ?: "other"
                    }
                }
                val count = originsMap[key] ?: 0
                originsMap[key] = count + 1
            }
        }

        originPrefixes.forEach { prefix ->
            println(dots("   $prefix") + lpad(originsMap[prefix] ?: 0))
        }

        println(dots("   other") + lpad(originsMap["other"] ?: 0))
        println(dots("   empty") + lpad(originsMap["empty"] ?: 0))

        // Print the name of the unyts that require phrases for each word

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

        // Print the name of the unyts that do not require phrases for each word

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

        // Count the number of phrase origins

        println("\nOrigins of phrases")
        originsMap.clear()

        vocab.forEachVisibleWord { word ->
            word.phrases.forEach { phrase ->
                val key = phrase.origin.let { o ->
                    when (o.isEmpty()) {
                        true -> "empty"
                        false -> originPrefixes.find { o.startsWith(it) } ?: "other"
                    }
                }
                val count = originsMap[key] ?: 0
                originsMap[key] = count + 1
            }
        }

        originPrefixes.forEach { prefix ->
            println(dots("   $prefix") + lpad(originsMap[prefix] ?: 0))
        }

        println(dots("   other") + lpad(originsMap["other"] ?: 0))
        println(dots("   empty") + lpad(originsMap["empty"] ?: 0))

        // Emit status about manualKanjiLevel

        println("\nManual kanji levels")

        JLPTLevel.entries.forEach { lvl ->
            val count = vocab.manualKanjiLevels[lvl]?.size ?: 0
            println(dots("   $lvl") + (lpad(count)))
        }

        // Emit categories

        println("\nCategories")

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
}
