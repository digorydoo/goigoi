package io.github.digorydoo.goigoi.file

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class DontConfuseFileReader(private val stream: InputStream) {
    /**
     * Reads the "don't confuse" kanji list, and calls the given lambda for each line
     */
    fun read(lambda: (kanji: Char, similarKanjis: List<Char>) -> Unit) {
        BufferedReader(InputStreamReader(stream, "UTF-8")).useLines { seq ->
            seq.forEach { line ->
                val colonAt = line.indexOf(':')

                val kanji = line.substring(0, colonAt)
                    .also { require(it.length == 1) { "Kanji length is ${it.length}: $it" } }
                    .let { it[0] }

                val similarKanjis = line.substring(colonAt + 1).split(',')
                    .also { kanjis ->
                        require(kanjis.isNotEmpty()) { "similarKanjis is empty" }

                        kanjis.forEach {
                            require(it.length == 1) { "Length of entry in similarKanjis is ${it.length}: $it" }
                        }
                    }
                    .map { it[0] }

                lambda(kanji, similarKanjis)
            }
        }
    }
}
