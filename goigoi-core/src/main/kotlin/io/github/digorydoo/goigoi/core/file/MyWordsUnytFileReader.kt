package io.github.digorydoo.goigoi.core.file

import ch.digorydoo.kutils.file.KDataInputStream.FileMarker
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.file.GoigoiFileMarker.WORD_FILE_NAME
import java.io.InputStream

class MyWordsUnytFileReader(private val unyt: Unyt, stream: InputStream): BinaryFileReader(stream) {
    override fun process(marker: FileMarker, value: String) {
        when (marker) {
            WORD_FILE_NAME -> unyt.wordFilenames.add(value)
            else -> throw Exception("Key not understood: $marker")
        }
    }
}
