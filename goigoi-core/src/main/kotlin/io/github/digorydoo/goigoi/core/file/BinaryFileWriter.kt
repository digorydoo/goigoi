package io.github.digorydoo.goigoi.core.file

import java.io.OutputStream

abstract class BinaryFileWriter(private val stream: OutputStream) {
    private val buf = ByteArray(2)

    abstract fun write()

    protected fun beginUnyt(id: String) {
        write(BinaryFileReader.UNYT_ID_KEY, id)
    }

    protected fun beginWord(id: String) {
        write(BinaryFileReader.WORD_ID_KEY, id)
    }

    protected fun beginPhrase() {
        write(BinaryFileReader.PHRASE_ID_KEY, "") // currently without id
    }

    protected fun beginSentence() {
        write(BinaryFileReader.SENTENCE_ID_KEY, "") // currently without id
    }

    protected fun beginSeeAlso(otherWordId: String) {
        write(BinaryFileReader.WORDLINK_ID_KEY, otherWordId)
    }

    protected fun writeEOFMarker() {
        writeUInt16(BinaryFileReader.EOF_KEY)
    }

    protected fun write(key: Int, value: String) {
        writeUInt16(key)
        writeUTF8(value)
    }

    protected fun writeIfNonEmpty(key: Int, value: String) {
        if (value.isNotEmpty()) {
            write(key, value)
        }
    }

    protected fun write(key: Int, value: Boolean) {
        write(key, "$value")
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
