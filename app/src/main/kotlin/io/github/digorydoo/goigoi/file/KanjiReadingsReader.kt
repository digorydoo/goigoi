package io.github.digorydoo.goigoi.file

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class KanjiReadingsReader(private val stream: InputStream) {
    /**
     * Reads the kanji readings file, and calls the given lambda for each reading found
     */
    fun read(lambda: (kana: String, kanjis: List<String>) -> Unit) {
        BufferedReader(InputStreamReader(stream, "UTF-8")).useLines { seq ->
            seq.forEach { line ->
                val colonAt = line.indexOf(':')
                val kana = line.substring(0, colonAt)
                val kanjis = line.substring(colonAt + 1).split(',')
                lambda(kana, kanjis)
            }
        }
    }
}
