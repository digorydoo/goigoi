package io.github.digorydoo.goigoi.core.file

import io.github.digorydoo.goigoi.core.db.Unyt
import java.io.OutputStream

class MyWordsUnytFileWriter(private val unyt: Unyt, stream: OutputStream): BinaryFileWriter(stream) {
    override fun write() {
        unyt.wordFilenames.forEach { filename ->
            write(BinaryFileReader.WORD_FILE_NAME_KEY, filename)
        }

        writeEOFMarker()
    }
}
