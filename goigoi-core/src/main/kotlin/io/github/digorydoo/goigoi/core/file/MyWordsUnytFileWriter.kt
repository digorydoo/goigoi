package io.github.digorydoo.goigoi.core.file

import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.file.GoigoiFileMarker.WORD_FILE_NAME
import java.io.OutputStream

class MyWordsUnytFileWriter(private val unyt: Unyt, stream: OutputStream): BinaryFileWriter(stream) {
    override fun write() {
        unyt.wordFilenames.forEach { filename ->
            write(WORD_FILE_NAME, filename)
        }

        writeEOFMarker()
    }
}
