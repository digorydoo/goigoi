package io.github.digorydoo.goigoi.core.file

import io.github.digorydoo.goigoi.core.db.Unyt
import java.io.InputStream

class MyWordsUnytFileReader(private val unyt: Unyt, stream: InputStream): BinaryFileReader(stream) {
    override fun process(key: Int, value: String) {
        when (key) {
            WORD_FILE_NAME_KEY -> unyt.wordFilenames.add(value)
            else -> throw Exception("Key not understood: $key")
        }
    }
}
