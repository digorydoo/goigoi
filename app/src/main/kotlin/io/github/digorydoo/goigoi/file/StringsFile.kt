package io.github.digorydoo.goigoi.file

import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.nio.charset.Charset

@Suppress("unused")
class StringsFile(val path: String) {
    private val utf8 = Charset.forName("UTF-8")
    private var access: RandomAccessFile? = null

    val pos: Long
        get() = access?.filePointer ?: 0

    val length: Long
        get() = access?.length() ?: 0

    fun open() {
        if (access != null) {
            Log.d(TAG, "File already open")
            return
        }

        try {
            val f = File(path)

            if (!f.exists()) {
                Log.d(TAG, "Creating new file: $path")
                f.createNewFile()
            }

            access = RandomAccessFile(path, "rw")
        } catch (e: FileNotFoundException) {
            Log.d(TAG, "File not found: $path")
        }
    }

    fun close() {
        access?.close()
        access = null
    }

    fun clear() {
        access?.seek(0)
        access?.setLength(0)
    }

    fun seek(pos: Long) {
        access?.seek(pos)
    }

    fun readln(): String? {
        val access = access ?: return null

        try {
            if (access.filePointer >= access.length()) {
                Log.e(TAG, "readln: At end of file!")
                return null
            }

            val start = access.readInt()

            if (start != START_MARK) {
                Log.e(
                    TAG,
                    "readln: Missing start mark, read instead: 0x${Integer.toHexString(start)}"
                )
                return null
            }

            val len = access.readInt()

            if (len < 0) {
                Log.e(TAG, "Bad length: $len")
                return null
            }

            if (len == 0) {
                return ""
            }

            val buf = ByteArray(len)
            access.read(buf)

            val end = access.readInt()

            if (end != END_MARK) {
                Log.e(TAG, "readln: Missing end mark!")
                return null
            }

            return String(buf, utf8)
        } catch (e: Exception) {
            Log.d(TAG, "readln: Exception: ${e::class.java.name} ${e.message}")
            return null
        }
    }

    fun writeln(s: String) {
        val bytes = encode(s)
        writeln(bytes)
    }

    fun writeln(bytes: ByteArray) {
        val access = access ?: return

        try {
            access.write(bytes)
        } catch (e: Exception) {
            Log.d(TAG, "writeln: Exception: ${e.message}")
            return
        }
    }

    fun overwriteln() {
        val access = access ?: return

        try {
            val startMark = access.readInt()

            if (startMark != START_MARK) {
                Log.e(
                    TAG,
                    "overwriteln: Missing start mark, " +
                        "read instead: 0x${Integer.toHexString(startMark)}"
                )
                return
            }

            val len = access.readInt()
            val dataPos = access.filePointer

            access.seek(dataPos + len)
            var end = access.readInt()

            if (end != END_MARK) {
                Log.e(TAG, "overwriteln: Missing end mark!")
                return
            }

            access.seek(dataPos)
            val buf = ByteArray(len)
            access.read(buf)

            buf.forEachIndexed { idx, _ -> buf[idx] = 0 }

            access.seek(dataPos)
            access.write(buf)

            access.seek(dataPos + len)
            end = access.readInt()

            if (end != END_MARK) {
                Log.e(TAG, "overwriteln: End mark was overwritten!")
                return
            }
        } catch (e: Exception) {
            Log.d(TAG, "writeln: Exception: ${e.message}")
            return
        }
    }

    fun encode(string: String): ByteArray {
        val bytes = ByteArrayOutputStream()
        val data = DataOutputStream(bytes)

        val buf = string.toByteArray(utf8)
        data.writeInt(START_MARK)
        data.writeInt(buf.size)
        data.write(buf)
        data.writeInt(END_MARK)

        return bytes.toByteArray()
    }

    companion object {
        private const val TAG = "StringsFile"
        private const val START_MARK = 0x56
        private const val END_MARK = 0x42
    }
}
