package io.github.digorydoo.goigoi.compiler.writer

import io.github.digorydoo.goigoi.compiler.KanjiLevels
import io.github.digorydoo.goigoi.compiler.Options
import java.io.File
import java.io.FileWriter

class KanjiIndexWriter(private val options: Options) {
    fun writeFiles(
        kanjiLevels: KanjiLevels,
        readings: Map<String, Set<String>>,
        kanjiBySchoolYear: Map<Int, Set<Char>>,
        kanjiByFreq: String,
        dontConfuse: List<String>,
    ) {
        if (!options.quiet) {
            println("Writing kanji index...")
        }

        writeKanjiIndex(kanjiLevels) // JLPT level followed by all kanjis of that level
        writeReadings(readings) // hiragana followed by all kanjis with that reading
        writeKanjiBySchoolYear(kanjiBySchoolYear) // grade number followed by all kanjis of that school year
        writeKanjiByFrequency(kanjiByFreq) // kanjis ordered by descending frequency
        writeDontConfuse(dontConfuse) // kanji followed by visually similar kanjis
    }

    private fun writeKanjiIndex(kanjiLevels: KanjiLevels) {
        val file = File("${options.dstDir}/all-kanjis.txt")
        FileWriter(file).use { writer ->
            writeKanjiIndex(kanjiLevels.n5, "n5", writer)
            writeKanjiIndex(kanjiLevels.n4, "n4", writer)
            writeKanjiIndex(kanjiLevels.n3, "n3", writer)
            writeKanjiIndex(kanjiLevels.n2, "n2", writer)
            writeKanjiIndex(kanjiLevels.n1, "n1", writer)
            writeKanjiIndex(kanjiLevels.other, "-", writer)
        }
    }

    private fun writeKanjiIndex(kanjis: Set<Char>, level: String, writer: FileWriter) {
        writer.write("${level}:")
        kanjis.forEach { writer.write(it.toString()) }
        writer.write("\n")
    }

    private fun writeReadings(readings: Map<String, Set<String>>) {
        val file = File("${options.dstDir}/readings.txt")
        FileWriter(file).use { writer ->
            readings.forEach { (kana, set) ->
                writer.write("$kana:")
                writer.write(set.joinToString(","))
                writer.write("\n")
            }
        }
    }

    private fun writeKanjiBySchoolYear(kanjiBySchoolYear: Map<Int, Set<Char>>) {
        val file = File("${options.dstDir}/schoolyears.txt")
        FileWriter(file).use { writer ->
            kanjiBySchoolYear.forEach { (year, kanjis) ->
                writer.apply {
                    write("$year:")
                    write(kanjis.joinToString(","))
                    write("\n")
                }
            }
        }
    }

    private fun writeKanjiByFrequency(kanjiByFreq: String) {
        val file = File("${options.dstDir}/kanji-freq.txt")
        FileWriter(file).use { writer ->
            writer.write("$kanjiByFreq\n")
        }
    }

    private fun writeDontConfuse(dontConfuse: List<String>) {
        val file = File("${options.dstDir}/dont-confuse.txt")
        FileWriter(file).use { writer ->
            dontConfuse.forEach { similarKanjis ->
                similarKanjis.forEach { kanji ->
                    writer.apply {
                        write("$kanji:")
                        write(similarKanjis.filter { it != kanji }.toList().joinToString(","))
                        write("\n")
                    }
                }
            }
        }
    }
}
