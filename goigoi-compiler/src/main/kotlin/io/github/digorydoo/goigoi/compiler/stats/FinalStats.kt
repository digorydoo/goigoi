package io.github.digorydoo.goigoi.compiler.stats

import ch.digorydoo.kutils.cjk.JLPTLevel
import io.github.digorydoo.goigoi.compiler.KanjiLevels
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import io.github.digorydoo.goigoi.core.WordHint

class FinalStats(
    private val vocab: GoigoiVocab,
    private val kanjiLevels: KanjiLevels,
    private val readings: Map<String, MutableSet<String>>,
) {
    fun print() {
        printKanjiStats()
        printReadingsStats()
        printHintStats()
        printSynonymStats()
        printDontConfuseKanjisStats()
        printMissingCatStats()
        printWordOrdering()
    }

    private fun printKanjiStats() {
        val allKanjis = mutableSetOf<Char>().apply {
            JLPTLevel.entries.forEach { lvl ->
                addAll(kanjiLevels.get(lvl))
            }
        }

        println("Total number of known kanjis: ${allKanjis.size}")

        JLPTLevel.entries.forEach { lvl ->
            println("   $lvl: ${kanjiLevels.get(lvl).size}")
        }

        println("Manual kanji JLPT level corrections:")

        vocab.manualKanjiLevels.forEach { (level, kanjis) ->
            println("   ${level}: ${kanjis.size}")
        }

        println("Kanjis by schoolyear:")

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

        println("Kanjis not in any schoolyear: ${kanjisNotInAnySchoolYear.size}")
        println("   ${kanjisNotInAnySchoolYear.joinToString("")}")

        println("Kanjis by frequency: ${vocab.kanjiByFreq.length} (after removing all kanjis not appearing in Goigoi)")

        val kanjisWithUnknownFreq = mutableSetOf<Char>()
            .apply { addAll(allKanjis) }
            .filter { kanji -> !vocab.kanjiByFreq.contains(kanji) }
            .sorted()

        println("Kanjis with unknown frequency: ${kanjisWithUnknownFreq.size}")
        println("   ${kanjisWithUnknownFreq.joinToString("")}")
    }

    private fun printReadingsStats() {
        println("Total number of readings: ${readings.size}")

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

        println("Hints most often used that do not have an enum yet")

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

    private fun printSynonymStats() {
        var numWordsWithSyn = 0
        var wordWithMostSyn: GoigoiWord? = null
        var numSynInWordWithMostSyn = 0

        vocab.forEachWord { word ->
            if (word.synonyms.isNotEmpty()) {
                numWordsWithSyn++

                if (wordWithMostSyn == null || word.synonyms.size > numSynInWordWithMostSyn) {
                    wordWithMostSyn = word
                    numSynInWordWithMostSyn = word.synonyms.size
                }
            }
        }

        println("\nTotal number of words with synonyms: . . $numWordsWithSyn")
        println(
            "Word with most number of synonyms: . . . " +
                wordWithMostSyn?.toPrettyString(withKanjiKanaSeparated = true, withId = true, withColour = false) +
                " ($numSynInWordWithMostSyn synonyms)"
        )
        println("")
    }

    private fun printDontConfuseKanjisStats() {
        run {
            var count = 0

            vocab.dontConfuseKanjis.forEach { groupOfKanjis ->
                count += groupOfKanjis.length
            }

            println("\"Don't confuse\" groups:")
            println("   - Total number of kanjis covered: $count")
        }

        run {
            // we cannot provide good "don't confuse" siblings for these
            val excludedKanjis = arrayOf(
                '一', '二', '三', '七', '八', '十', '口', '中', '川', '山', '出', '多', '母', '気', '雨'
            )

            var kanjiNotCovered = Char(0)
            var rankOfKanjiNotCovered = Int.MAX_VALUE

            vocab.kanjiByFreq.forEachIndexed { rank, kanji ->
                if (rankOfKanjiNotCovered == Int.MAX_VALUE && !excludedKanjis.contains(kanji)) {
                    val found = vocab.dontConfuseKanjis.any { groupOfKanjis ->
                        groupOfKanjis.contains(kanji)
                    }

                    if (!found) {
                        kanjiNotCovered = kanji
                        rankOfKanjiNotCovered = rank
                    }
                }
            }

            print("   - Kanji that is not excluded with least rank not covered: $kanjiNotCovered")
            println(", rank $rankOfKanjiNotCovered")
        }

        arrayOf(JLPTLevel.N5, JLPTLevel.N4, JLPTLevel.N3, JLPTLevel.N2, JLPTLevel.N1).forEach { lvl ->
            vocab.manualKanjiLevels[lvl]
                ?.filter { kanji ->
                    vocab.dontConfuseKanjis.all { groupOfKanjis ->
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
    }

    private fun printWordOrdering() {
        // The word ordering is relevant for super progressive mode and is based on the filenames.
        println("\nWord ordering:")
        val words = mutableListOf<GoigoiWord>()
        vocab.forEachVisibleWord { words.add(it) }

        words.sortWith { w1, w2 ->
            w1.fileName.compareTo(w2.fileName)
        }

        var firstWordWithNoPhrase: GoigoiWord? = null
        var firstWordWithNoSentence: GoigoiWord? = null

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
    }
}
