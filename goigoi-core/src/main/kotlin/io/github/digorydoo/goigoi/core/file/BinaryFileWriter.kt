package io.github.digorydoo.goigoi.core.file

import ch.digorydoo.kutils.file.KDataInputStream.FileMarker
import io.github.digorydoo.goigoi.core.file.GoigoiFileMarker.*
import java.io.OutputStream

// FIXME get rid of this, use KDataOutputStream from kutils
abstract class BinaryFileWriter(private val stream: OutputStream) {
    private val buf = ByteArray(2)

    abstract fun write()

    protected fun beginUnyt(id: String) {
        write(UNYT_ID, id)
    }

    protected fun beginWord(id: String) {
        write(WORD_ID, id)
    }

    protected fun beginPhrase() {
        write(PHRASE_ID, "") // currently without id
    }

    protected fun beginSentence() {
        write(SENTENCE_ID, "") // currently without id
    }

    protected fun beginSeeAlso(otherWordId: String) {
        write(WORDLINK_ID, otherWordId)
    }

    protected fun writeEOFMarker() {
        writeUShort16(EOF.value)
    }

    protected fun write(marker: FileMarker, value: String) {
        writeUShort16(marker.value)
        writeUTF8(value)
    }

    protected fun writeIfNonEmpty(marker: FileMarker, value: String) {
        if (value.isNotEmpty()) {
            write(marker, value)
        }
    }

    protected fun write(marker: FileMarker, value: Boolean) {
        write(marker, "$value")
    }

    private fun writeUShort16(us: UShort) {
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
