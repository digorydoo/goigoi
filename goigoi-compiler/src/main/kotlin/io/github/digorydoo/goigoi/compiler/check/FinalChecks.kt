package io.github.digorydoo.goigoi.compiler.check

import io.github.digorydoo.goigoi.compiler.CheckFailed
import io.github.digorydoo.goigoi.compiler.KanjiLevels
import io.github.digorydoo.goigoi.compiler.Options
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiPhrase
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import ch.digorydoo.kutils.cjk.*
import ch.digorydoo.kutils.cjk.JLPTLevel.Companion.toJLPTLevelOrNull
import kotlin.math.min

/**
 * Implements the checks that need to access both the vocab and the kanji index. Note that the checks that do not need
 * access to the kanji index should be implemented in CheckGoigoiVocab.
 */
class FinalChecks(
    private val vocab: GoigoiVocab,
    private val kanjiLevels: KanjiLevels,
    private val readings: Map<String, MutableSet<String>>,
    private val options: Options,
) {
    fun check() {
        if (!options.quiet) {
            println("Final integrity checks...")
        }

        checkRequiredManualKanjiLevels()
        checkKanjisInPhrasesAndSentences()
        checkUsuallyInKana()
        checkConsistentKanjiLevels()
        checkDontConfuseKanjis()

        if (!options.quiet) {
            vocab.warnings.forEach { println(it) }
        }
    }

    private fun checkRequiredManualKanjiLevels() {
        val n5KanjisMissing = mutableSetOf<Char>()
        val n4KanjisMissing = mutableSetOf<Char>()

        vocab.forEachWord { word ->
            if (word.level == JLPTLevel.N5 || word.level == JLPTLevel.N4) {
                val withoutManualLevel = word.kanji
                    .filter { c ->
                        if (!c.isCJK()) {
                            false
                        } else {
                            !vocab.manualKanjiLevels.any { (_, kanjis) ->
                                kanjis.contains(c)
                            }
                        }
                    }

                if (withoutManualLevel.isNotEmpty()) {
                    word.prettyPrint(withRomaji = true, withLvl = true, withKanjiKanaSeparated = true)

                    when (word.level) {
                        JLPTLevel.N5 -> n5KanjisMissing.addAll(withoutManualLevel.toList())
                        JLPTLevel.N4 -> n4KanjisMissing.addAll(withoutManualLevel.toList())
                        else -> throw CheckFailed("Internal error: Unexpected level ${word.level}")
                    }
                }
            }
        }

        if (n5KanjisMissing.isNotEmpty()) {
            throw CheckFailed("Kanjis of N5-rated words missing from manual index: ${n5KanjisMissing.joinToString("")}")
        }

        if (n4KanjisMissing.isNotEmpty()) {
            throw CheckFailed("Kanjis of N4-rated words missing from manual index: ${n4KanjisMissing.joinToString("")}")
        }
    }

    private fun checkKanjisInPhrasesAndSentences() {
        vocab.forEachWord { word ->
            word.phrases.forEach { checkKanjis(it, "Phrase") }
            word.sentences.forEach { checkKanjis(it, "Sentence") }
        }
    }

    private fun checkKanjis(s: GoigoiPhrase, what: String) {
        FuriganaIterator(s.primaryForm.raw).forEach { range ->
            val errorCtx = "${what}\n   ${s.primaryForm.kanji}\n   ${s.romaji}"
            val kanji = range.primaryText.toString()
            val kana = range.secondaryText.toString()

            if (kanji.isEmpty()) {
                throw CheckFailed("${errorCtx}\nFirst part of furigana is empty!")
            }

            if (kana.isEmpty()) {
                throw CheckFailed("${errorCtx}\nSecond part of furigana is empty!")
            }

            if (kanji != "々") {
                val entry = readings[kana]
                    ?: readings[kana.toHiragana()]
                    ?: readings[kana.toKatakana()]
                    ?: throw CheckFailed(
                        "${errorCtx}\n${what} uses a reading ($kana) that does not match any known kanji"
                    )

                if (!entry.contains(kanji)) {
                    throw CheckFailed(
                        "${errorCtx}\n${what} uses a reading ($kana) that does not match the kanji $kanji"
                    )
                }
            }
        }
    }

    private fun checkUsuallyInKana() {
        // data classes implicitly declare component1() and component2()
        data class WordAndKanjis(val word: GoigoiWord, val kanjis: Set<Char>)

        fun GoigoiWord.isAtLeastN1() =
            level?.isEasierThan(JLPTLevel.Nx) ?: true

        fun Char.isAtLeastN1() =
            kanjiLevels.findLevelOf(this)?.isEasierThan(JLPTLevel.Nx) ?: false

        val maybeUsuallyInKana = mutableListOf<WordAndKanjis>()

        vocab.forEachVisibleWord { word ->
            if (!word.usuallyInKana && word.isAtLeastN1()) {
                val difficultKanjis = word.primaryForm.kanji
                    .filter { it.isCJK() && it != '〜' && !it.isAtLeastN1() }
                    .toSet()

                if (difficultKanjis.isNotEmpty()) {
                    maybeUsuallyInKana.add(WordAndKanjis(word, difficultKanjis))
                }
            }
        }

        if (maybeUsuallyInKana.isNotEmpty()) {
            maybeUsuallyInKana.forEach { (word, kanjis) ->
                val w = word.toPrettyString(withRomaji = true, withLvl = true, withKanjiKanaSeparated = true)
                val k = kanjis.joinToString()
                println("$w has difficult kanji $k")
            }
            throw CheckFailed("${maybeUsuallyInKana.size} words use difficult kanjis, but not marked as usuallyInKana")
        }
    }

    private fun checkConsistentKanjiLevels() {
        class WordAndKanjiLevel(val word: GoigoiWord) {
            val kanjiLevel = word.primaryForm.kanji
                .filter { it.isCJK() && it != '〜' }
                .map { (kanjiLevels.findLevelOf(it) ?: JLPTLevel.Nx).toInt() }
                .maxOrNull()
                ?.toJLPTLevelOrNull()

            override fun toString() =
                word.toPrettyString(withId = true, withKanjiKanaSeparated = true, withLvl = true) +
                    " word using $kanjiLevel kanji"
        }

        class Problem(val wordMinLevel: JLPTLevel, val w1: WordAndKanjiLevel, val w2: WordAndKanjiLevel)

        val wordsByReadings = mutableMapOf<String, MutableList<GoigoiWord>>()

        vocab.forEachVisibleWord { word ->
            if (!word.usuallyInKana) {
                wordsByReadings
                    .getOrPut(word.kana) { mutableListOf() }
                    .add(word)
            }
        }

        val problems = mutableMapOf<String, Problem>()

        wordsByReadings.forEach { (_, wordsWithSameReading) ->
            if (wordsWithSameReading.size > 1) {
                val list = wordsWithSameReading
                    .map { WordAndKanjiLevel(it) }
                    .sortedByDescending { it.kanjiLevel?.toInt() ?: 0 }

                for (i in list.indices) {
                    val wi = list[i]

                    for (j in i - 1 downTo 0) {
                        val wj = list[j]

                        if (wi.word.id != wj.word.id) {
                            val wordMinLevel = min(wi.word.level?.toInt() ?: 0, wj.word.level?.toInt() ?: 0)
                                .toJLPTLevelOrNull() ?: JLPTLevel.Nx

                            // If kanjiLevel is null, the word is just given by kana.
                            val iKanjiDifficult = wi.kanjiLevel?.isMoreDifficultThan(wordMinLevel) == true
                            val jKanjiDifficult = wj.kanjiLevel?.isMoreDifficultThan(wordMinLevel) == true

                            if (iKanjiDifficult && jKanjiDifficult) {
                                val id = "${wi.word.id}/${wj.word.id}"
                                problems[id] = Problem(wordMinLevel, wi, wj) // using a map to get rid of duplicates
                            }
                        }
                    }
                }
            }
        }

        if (problems.isNotEmpty()) {
            problems.forEach { (_, problem) ->
                println("Confusing to learners of ${problem.wordMinLevel}, because they do not know all the kanjis:")
                println("   ${problem.w1}")
                println("   ${problem.w2}")
            }
            throw CheckFailed("Kanji level mismatch, see above")
        }
    }

    private fun checkDontConfuseKanjis() {
        // these are valid Japanese characters, but not currently used by any word,
        // so they were removed from vocab.kanjiByFreq
        val allowedChars = arrayOf('龍', '々')

        val illegalChars = mutableSetOf<Char>()

        vocab.dontConfuseKanjis.forEach { similarKanjis ->
            similarKanjis.forEach { kanji ->
                if (
                    !vocab.kanjiByFreq.contains(kanji) &&
                    !allowedChars.contains(kanji) &&
                    !kanji.isHiragana() &&
                    !kanji.isKatakana()
                ) {
                    illegalChars.add(kanji)
                }
            }
        }

        if (illegalChars.isNotEmpty()) {
            throw CheckFailed(
                "dont_confuse mentions character(s) that are not part of the list of kanji by " +
                    "frequency. Using non-Japanese characters may lead to missing glyph problems in Goigoi.\n" +
                    "Offending characters: ${illegalChars.joinToString(", ")}"
            )
        }
    }
}
