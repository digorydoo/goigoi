package io.github.digorydoo.goigoi.file

import android.util.Log
import io.github.digorydoo.goigoi.BuildConfig
import ch.digorydoo.kutils.cjk.Unicode
import java.io.OutputStream

@Suppress("unused")
class KeyValueFile(val path: String) {
    private class KeyValue(val key: String, val value: String)
    private class Location(val pos: Long, val length: Long)

    private val file = StringsFile(path)
    private val mapKeysToPos: HashMap<String, Location> = HashMap()
    private val invalidPos: ArrayList<Location> = ArrayList()
    private val cache: HashMap<String, String?> = HashMap()

    fun open() {
        file.open()

        try {
            readEntireFile()
        } catch (e: Exception) {
            // If Goigoi crashed while the stats file was written, the file may get corrupted, and we
            // come here.
            e.printStackTrace()
            clear()
        }
    }

    fun close() {
        file.close()
    }

    fun exportTo(dst: OutputStream) {
        mapKeysToPos.forEach { (key, _) ->
            var line = "$key:"
            get(key)?.let { line += it }
            line += "\n"
            dst.write(line.toByteArray())
        }
    }

    fun clear() {
        file.clear()
        mapKeysToPos.clear()
        invalidPos.clear()
    }

    fun get(key: String): String? {
        if (cache.containsKey(key)) {
            return cache[key]
        }

        val v = getFromFile(key)
        cache[key] = v
        return v
    }

    fun set(key: String, value: String) {
        cache.remove(key)
        setInFile(key, value)
    }

    fun remove(key: String) {
        cache.remove(key)
        removeInFile(key)
    }

    private fun getFromFile(key: String): String? {
        val loc = mapKeysToPos[key] ?: return null

        file.seek(loc.pos)
        val line = file.readln()

        if (line == null) {
            Log.e(TAG, "Readln failed at pos ${loc.pos}")
            return null
        }

        val pair = parseLine(line)

        if (pair == null) {
            Log.e(TAG, "Parsing failed of line: $line")

            if (BuildConfig.DEBUG) {
                throw RuntimeException("Parsing failed")
            }

            return null
        } else if (pair.key != key) {
            Log.e(TAG, "Line belongs to a different key, pair.key=${pair.key}, key=${key}")

            if (BuildConfig.DEBUG) {
                throw RuntimeException("Line belongs to a different key")
            }

            return null
        }

        return pair.value
    }

    private fun setInFile(key: String, value: String) {
        checkKey(key)
        removeInFile(key)

        val entry = "${key}$KEY_VALUE_SEPARATOR${value}"
        val encodedEntry = file.encode(entry)
        val bytesNeeded = encodedEntry.size.toLong()
        var foundIdx = -1

        invalidPos.forEachIndexed { idx, loc ->
            if (loc.length == bytesNeeded) {
                foundIdx = idx
            }
        }

        if (foundIdx < 0) {
            // Append a new entry at the end of the file
            // Log.d(TAG, "Adding new slot@${file.length}: $entry")
            file.seek(file.length)
        } else {
            // Replace the area at foundIdx
            val pos = invalidPos[foundIdx].pos
            // Log.d(TAG, "Overwriting slot@$pos with same length: $entry")
            file.seek(pos)
            invalidPos.removeAt(foundIdx)
        }

        val start = file.pos
        file.writeln(encodedEntry)
        val end = file.pos
        mapKeysToPos[key] = Location(start, end - start)
    }

    private fun removeInFile(key: String) {
        val loc = mapKeysToPos[key] ?: return
        file.seek(loc.pos)
        file.overwriteln()
        mapKeysToPos.remove(key)
        invalidPos.add(loc)
    }

    private fun checkKey(key: String) {
        val good = key.isNotEmpty() && key.indexOf(KEY_VALUE_SEPARATOR) < 0

        if (!good) {
            throw RuntimeException("Bad key: $key")
        }
    }

    private fun readEntireFile() {
        try {
            file.seek(0)
            mapKeysToPos.clear()
            invalidPos.clear()

            while (file.pos < file.length) {
                val start = file.pos
                val line = file.readln() ?: throw RuntimeException("Could not parse line!")
                val end = file.pos
                val kv = parseLine(line)
                val loc = Location(start, end - start)

                if (kv == null) {
                    invalidPos.add(loc)
                } else {
                    checkKey(kv.key)
                    mapKeysToPos[kv.key] = loc
                }
            }

            Log.d(TAG, "Read ${file.length / 1024} kB of stats data (${path.split("/").last()})")
        } catch (e: Exception) {
            Log.e(TAG, "readEntireFile: Exception: ${e::class.java} ${e.message}")

            if (BuildConfig.DEBUG) {
                throw e
            } else {
                clear()
            }
        }
    }

    private fun parseLine(line: String): KeyValue? {
        if (line.isEmpty() || line[0] == 0.toChar()) {
            return null
        }

        // Not using split() here, because the value may contain any character
        val sepAt = line.indexOf(KEY_VALUE_SEPARATOR)

        if (sepAt < 0) {
            Log.w(TAG, "parseLine: Separator not found on line: $line")

            if (BuildConfig.DEBUG) {
                throw RuntimeException("Separator not found")
            } else {
                return null
            }
        } else if (sepAt == 0) {
            Log.w(TAG, "parseLine: Empty key on line: $line")

            if (BuildConfig.DEBUG) {
                throw RuntimeException("Empty key")
            } else {
                return null
            }
        }

        val key = line.slice(0 ..< sepAt)
        val value = line.substring(sepAt + 1)
        return KeyValue(key, value)
    }

    companion object {
        private const val TAG = "KeyValueFile"
        private const val KEY_VALUE_SEPARATOR = Unicode.ESCAPE
    }
}
