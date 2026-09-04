package io.github.digorydoo.goigoi.core.file

import ch.digorydoo.kutils.file.KDataInputStream.FileMarker
import io.github.digorydoo.goigoi.core.file.GoigoiFileMarker.EOF
import java.io.InputStream

// FIXME get rid of this, use KDataInputStream from kutils
abstract class BinaryFileReader(private val stream: InputStream) {
    private val buf = ByteArray(2)

    fun read() {
        while (true) {
            val key = readUShort16()
            val marker = GoigoiFileMarker.fromUShort(key)
            require(marker != null) { "Illegal value for marker: $key" }

            if (marker == EOF) {
                done()
                break
            } else {
                val value = readUTF8()
                process(marker, value)
            }
        }
    }

    abstract fun process(marker: FileMarker, value: String)
    open fun done() {}

    private fun readUShort16(): UShort =
        readUInt16().toUShort()

    private fun readUInt16(): Int {
        val count = stream.read(buf)
        require(count == 2) { "Unexpected end of file" }
        val u = buf[0].toInt() and 0xff
        val v = buf[1].toInt() and 0xff
        return (u shl 8) or v
    }

    private fun readUTF8(): String {
        val size = readUInt16()
        return if (size == 0) {
            ""
        } else {
            val tmp = ByteArray(size)
            val count = stream.read(tmp)
            require(count == size) { "Unexpected end of file" }
            tmp.toString(Charsets.UTF_8)
        }
    }
}
