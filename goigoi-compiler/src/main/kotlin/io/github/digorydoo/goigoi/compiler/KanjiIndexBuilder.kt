package io.github.digorydoo.goigoi.compiler

import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import ch.digorydoo.kutils.cjk.*

class KanjiIndexBuilder(
    private val vocab: GoigoiVocab,
    private val kanjiLevels: KanjiLevels,
    private val readings: MutableMap<String, MutableSet<String>>,
    private val options: Options,
) {
    fun build() {
        if (!options.quiet) {
            println("Building kanji index...")
        }

        vocab.topics.forEach { topic ->
            topic.unyts.forEach { unyt ->
                unyt.sections.forEach { section ->
                    section.words.forEach { word ->
                        word.primaryForm.raw.forEach { c ->
                            if (
                                c.isCJK() &&
                                !c.isHiragana() &&
                                !c.isKatakana() &&
                                !c.isPunctuation() &&
                                !c.isBracket() &&
                                c != '〜' &&
                                c != '々'
                            ) {
                                if (word.usuallyInKana) {
                                    // The kanji is not usually used with this word, so word.level does not necessarily
                                    // indicate the level of the kanji. We add it to level nx. It may be moved further
                                    // up if we find another word using that kanji, or if it is manually adjusted via
                                    // vocab.manualKanjiLevels.
                                    kanjiLevels.other.add(c)
                                } else {
                                    kanjiLevels.get(word.level).add(c)
                                }
                            }
                        }

                        FuriganaIterator(word.primaryForm.raw).forEach { range ->
                            val errorCtx = "Word ${word.romaji}\n   ${word.primaryForm}"

                            val kanji = range.primaryText.toString()
                            val kana = range.secondaryText.toString()

                            if (kanji.isEmpty()) {
                                throw Exception("${errorCtx}: First part of furigana is empty")
                            }

                            if (kana.isEmpty()) {
                                throw Exception("${errorCtx}: Second part of furigana is empty")
                            }

                            if (kanji != "々") {
                                val entry = readings[kana]

                                if (entry != null) {
                                    entry.add(kanji)
                                } else {
                                    readings[kana] = mutableSetOf(kanji)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Since kanjis are part of words and sentences, a kanji found in n5 may reappear in n4, n3, etc.
        // We want the list grouped by first occurrence.

        kanjiLevels.n5.forEach { kanji ->
            kanjiLevels.n4.remove(kanji)
            kanjiLevels.n3.remove(kanji)
            kanjiLevels.n2.remove(kanji)
            kanjiLevels.n1.remove(kanji)
            kanjiLevels.other.remove(kanji)
        }

        kanjiLevels.n4.forEach { kanji ->
            kanjiLevels.n3.remove(kanji)
            kanjiLevels.n2.remove(kanji)
            kanjiLevels.n1.remove(kanji)
            kanjiLevels.other.remove(kanji)
        }

        kanjiLevels.n3.forEach { kanji ->
            kanjiLevels.n2.remove(kanji)
            kanjiLevels.n1.remove(kanji)
            kanjiLevels.other.remove(kanji)
        }

        kanjiLevels.n2.forEach { kanji ->
            kanjiLevels.n1.remove(kanji)
            kanjiLevels.other.remove(kanji)
        }

        kanjiLevels.n1.forEach { kanji ->
            kanjiLevels.other.remove(kanji)
        }

        // Apply manual kanji level corrections from Goigoi XML.

        applyManualKanjiLevels()
        removeUnusedKanjiByFreq()

        // Readings appear in Goigoi XML as either hiragana or katakana. To make it easier to look them up by Goigoi,
        // we convert all of them to hiragana.

        convertReadings()
    }

    private fun applyManualKanjiLevels() {
        if (!options.quiet) {
            println("Manual kanji level corrections")
        }

        class Change(val from: JLPTLevel, val to: JLPTLevel, val kanji: Char)

        val kanjisMentioned = mutableSetOf<Char>()
        val changes = mutableListOf<Change>()

        vocab.manualKanjiLevels.forEach { (setToLvl, kanjis) ->
            kanjis.forEach { kanji ->
                if (kanjisMentioned.contains(kanji)) {
                    throw Exception("Manual kanji correction mentions kanji more than once: $kanji")
                }

                kanjisMentioned.add(kanji)
                var found = false

                JLPTLevel.entries.forEach { lvl ->
                    val before = kanjiLevels.get(lvl)

                    if (before.contains(kanji)) {
                        found = true
                        if (lvl == setToLvl) {
                            if (!options.quiet) {
                                println("   $kanji: $lvl is already $setToLvl")
                            }
                        } else {
                            changes.add(Change(from = lvl, to = setToLvl, kanji = kanji))
                        }
                    }
                }

                if (!found) {
                    // This happens if ignored characters such as 々 appear in manual kanji-index.
                    changes.add(Change(from = JLPTLevel.Nx, to = setToLvl, kanji = kanji))
                }
            }
        }

        changes.forEach {
            if (!options.quiet) {
                println("   ${it.kanji}: ${it.from} -> ${it.to}")
            }

            kanjiLevels.get(it.from).remove(it.kanji)
            kanjiLevels.get(it.to).add(it.kanji)
        }

        if (!options.quiet) {
            println("")
        }
    }

    private fun removeUnusedKanjiByFreq() {
        // We remove all kanjis in kanjiByFreq list that are not otherwise used by Goigoi, in order to reduce the size.

        val allKanjis = mutableSetOf<Char>().apply {
            JLPTLevel.entries.forEach { lvl ->
                addAll(kanjiLevels.get(lvl))
            }
        }

        vocab.kanjiByFreq = vocab.kanjiByFreq.filter { allKanjis.contains(it) }
    }

    private fun convertReadings() {
        if (!options.quiet) {
            println("Converting readings")
        }

        val newReadings = mutableMapOf<String, MutableSet<String>>()

        readings.forEach { (kana, kanjis) ->
            if (kana.isKatakana()) {
                val hiragana = kana.toHiragana()

                if (!options.quiet) {
                    println("   $kana -> $hiragana")
                }

                val otherKanjis = readings[hiragana]
                    ?: mutableSetOf<String>().also { newReadings[hiragana] = it }

                otherKanjis.addAll(kanjis)
                kanjis.clear()
            } else if (!kana.isHiragana()) {
                throw Exception("Readings must be either pure katakana or pure hiragana, but found: $kana")
            }
        }

        readings.entries.removeAll { (_, kanjis) ->
            kanjis.isEmpty()
        }

        readings.putAll(newReadings)

        if (!options.quiet) {
            println("")
        }
    }
}
