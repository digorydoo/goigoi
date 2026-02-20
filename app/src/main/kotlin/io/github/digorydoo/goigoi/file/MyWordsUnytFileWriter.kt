package io.github.digorydoo.goigoi.file

import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.file.BinaryFileReader.Companion.WORD_FILE_NAME_KEY
import java.io.OutputStream

class MyWordsUnytFileWriter(private val unyt: Unyt, stream: OutputStream): BinaryFileWriter(stream) {
    override fun write() {
        unyt.wordFilenames.forEach { filename ->
            write(WORD_FILE_NAME_KEY, filename)
        }

        writeEOFMarker()
    }
}
