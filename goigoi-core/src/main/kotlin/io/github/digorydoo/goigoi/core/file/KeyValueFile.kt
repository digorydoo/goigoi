package io.github.digorydoo.goigoi.core.file

import ch.digorydoo.kutils.cjk.Unicode
import ch.digorydoo.kutils.logging.Log
import java.io.File
import java.io.OutputStream

class KeyValueFile(private val pathToStringsFile: String, private val pathToBackup: String) {
    private class KeyValue(val key: String, val value: String)
    private class Location(val pos: Long, val length: Long)

    private val stringsFile = StringsFile(pathToStringsFile)
    private val mapKeysToPos: HashMap<String, Location> = HashMap()
    private val invalidPos: ArrayList<Location> = ArrayList()
    private val cache: HashMap<String, String?> = HashMap()

    fun open() {
        var success: Boolean

        try {
            stringsFile.open()
            readEntireFile()
            success = true
        } catch (e: Exception) {
            // If Goigoi crashed while the stats file was written, the file may get corrupted, and we come here.
            Log.error(TAG, "Loading strings file crashed: $e")
            success = false
        }

        if (success && mapKeysToPos.isNotEmpty()) {
            // Write a backup

            try {
                val bak = File(pathToBackup)
                bak.parentFile?.mkdirs()
                File(stringsFile.path).copyTo(bak, overwrite = true)
                Log.debug(TAG, "Backup file written to: $pathToBackup")
            } catch (e: Exception) {
                Log.error(TAG, "Failed to write backup: $e")
                e.printStackTrace()
            }
        } else {
            // Try to recover the backup

            clear()
            val bak = File(pathToBackup)

            if (!bak.exists()) {
                Log.warn(TAG, "Cannot recover backup, because it does not exist: $pathToBackup")
            } else {
                try {
                    stringsFile.close()
                    bak.copyTo(File(stringsFile.path), overwrite = true)
                    stringsFile.open()
                    readEntireFile()
                } catch (e: Exception) {
                    Log.error(TAG, "Failed to recover backup: $e")
                    clear()
                }
            }
        }
    }

    private fun readEntireFile() {
        stringsFile.seek(0)
        mapKeysToPos.clear()
        invalidPos.clear()

        while (stringsFile.pos < stringsFile.length) {
            val start = stringsFile.pos
            val line = stringsFile.readln() ?: throw RuntimeException("Could not parse line!")
            val end = stringsFile.pos
            val kv = parseLine(line)
            val loc = Location(start, end - start)

            if (kv == null) {
                invalidPos.add(loc)
            } else {
                checkKey(kv.key)
                mapKeysToPos[kv.key] = loc
            }
        }

        Log.debug(TAG, "Read ${stringsFile.length / 1024} kB of stats data (${pathToStringsFile.split("/").last()})")
    }

    fun close() {
        stringsFile.close()
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
        stringsFile.clear()
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

        stringsFile.seek(loc.pos)
        val line = stringsFile.readln()

        if (line == null) {
            Log.error(TAG, "Readln failed at pos ${loc.pos}")
            return null
        }

        val pair = parseLine(line)

        if (pair == null) {
            Log.error(TAG, "Parsing failed of line: $line")
            return null
        } else if (pair.key != key) {
            Log.error(TAG, "Line belongs to a different key, pair.key=${pair.key}, key=${key}")
            return null
        }

        return pair.value
    }

    private fun setInFile(key: String, value: String) {
        checkKey(key)
        removeInFile(key)

        val entry = "${key}$KEY_VALUE_SEPARATOR${value}"
        val encodedEntry = stringsFile.encode(entry)
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
            stringsFile.seek(stringsFile.length)
        } else {
            // Replace the area at foundIdx
            val pos = invalidPos[foundIdx].pos
            // Log.d(TAG, "Overwriting slot@$pos with same length: $entry")
            stringsFile.seek(pos)
            invalidPos.removeAt(foundIdx)
        }

        val start = stringsFile.pos
        stringsFile.writeln(encodedEntry)
        val end = stringsFile.pos
        mapKeysToPos[key] = Location(start, end - start)
    }

    private fun removeInFile(key: String) {
        val loc = mapKeysToPos[key] ?: return
        stringsFile.seek(loc.pos)
        stringsFile.overwriteln()
        mapKeysToPos.remove(key)
        invalidPos.add(loc)
    }

    private fun checkKey(key: String) {
        val good = key.isNotEmpty() && key.indexOf(KEY_VALUE_SEPARATOR) < 0

        if (!good) {
            throw RuntimeException("Bad key: $key")
        }
    }

    private fun parseLine(line: String): KeyValue? {
        if (line.isEmpty() || line[0] == 0.toChar()) {
            return null
        }

        // Not using split() here, because the value may contain any character
        val sepAt = line.indexOf(KEY_VALUE_SEPARATOR)

        if (sepAt < 0) {
            Log.warn(TAG, "parseLine: Separator not found on line: $line")
            return null
        } else if (sepAt == 0) {
            Log.warn(TAG, "parseLine: Empty key on line: $line")
            return null
        }

        val key = line.slice(0 ..< sepAt)
        val value = line.substring(sepAt + 1)
        return KeyValue(key, value)
    }

    companion object {
        private val TAG = Log.Tag("KeyValueFile")
        private const val KEY_VALUE_SEPARATOR = Unicode.ESCAPE
    }
}
