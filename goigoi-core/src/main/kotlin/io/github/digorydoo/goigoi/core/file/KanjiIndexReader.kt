package io.github.digorydoo.goigoi.core.file

import ch.digorydoo.kutils.cjk.JLPTLevel
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class KanjiIndexReader(private val stream: InputStream) {
    /**
     * Reads the kanjis index, and calls the given lambda for each kanji found
     */
    fun read(lambda: (kanji: Char, level: JLPTLevel) -> Unit) {
        BufferedReader(InputStreamReader(stream, "UTF-8")).useLines { seq ->
            seq.forEach { line ->
                val colonAt = line.indexOf(':')
                val rawLevel = line.substring(0, colonAt)

                val level = JLPTLevel.fromStringOrNull(rawLevel, nxIsNull = false)
                    ?: throw Exception("Not a valid JLPT level: $rawLevel")

                line.substring(colonAt + 1).forEach { kanji ->
                    lambda(kanji, level)
                }
            }
        }
    }
}
