package io.github.digorydoo.goigoi.compiler.writer

import ch.digorydoo.kutils.file.KDataInputStream.FileMarker
import io.github.digorydoo.goigoi.core.file.GoigoiFileMarker.EOF
import java.io.OutputStream

// FIXME get rid of this, use KDataOutputStream from kutils
abstract class AbstrWriter(private val stream: OutputStream) {
    private val buf = ByteArray(2)

    abstract fun write()

    protected fun writeEOFMarker() {
        writeUInt16(EOF.value)
    }

    protected fun write(marker: FileMarker, value: String) {
        writeUInt16(marker.value)
        writeUTF8(value)
    }

    protected fun writeIfNonEmpty(marker: FileMarker, value: String) {
        if (value.isNotEmpty()) {
            write(marker, value)
        }
    }

    protected fun write(marker: FileMarker, value: Int) {
        write(marker, "$value")
    }

    protected fun write(marker: FileMarker, value: Boolean) {
        write(marker, "$value")
    }

    private fun writeUInt16(us: UShort) {
        writeUInt16(us.toInt())
    }

    private fun writeUInt16(i: Int) {
        require(i in 0 .. 65535) { "Parameter out of range: $i" }
        buf[0] = ((i shr 8) and 0xff).toByte()
        buf[1] = (i and 0xff).toByte()
        stream.write(buf)
    }

    private fun writeUTF8(s: String) {
        val ba = s.toByteArray(Charsets.UTF_8)
        writeUInt16(ba.size)
        stream.write(ba)
    }
}
