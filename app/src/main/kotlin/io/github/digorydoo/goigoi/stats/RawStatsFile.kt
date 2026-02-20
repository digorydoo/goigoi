package io.github.digorydoo.goigoi.stats

import android.util.Log
import io.github.digorydoo.goigoi.file.KeyValueFile
import ch.digorydoo.kutils.utils.Moment
import java.io.File
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

class RawStatsFile(dir: File, filename: String, version: Int) {
    private val file: KeyValueFile

    fun exportTo(dst: OutputStream) =
        file.exportTo(dst)

    fun getString(key: String): String? {
        return file.get(key)
    }

    fun setString(key: String, value: String?) {
        if (value == null) {
            file.remove(key)
        } else {
            file.set(key, value)
        }
    }

    fun getInt(key: String): Int? {
        val s = file.get(key) ?: return null
        return s.toInt()
    }

    fun setInt(key: String, value: Int) {
        file.set(key, "$value")
    }

    fun getBoolean(key: String): Boolean? =
        getInt(key)?.let { it > 0 }

    fun setBoolean(key: String, value: Boolean?) {
        if (value == null) {
            file.remove(key)
        } else {
            setInt(key, if (value) 1 else 0)
        }
    }

    fun incInt(key: String, defaultVal: Int, maxVal: Int?) {
        var c = (getInt(key) ?: (defaultVal - 1)) + 1

        if (maxVal != null) {
            c = min(maxVal, c)
        }

        setInt(key, c)
    }

    @Suppress("unused")
    fun decInt(key: String, defaultVal: Int, minVal: Int?) {
        var c = (getInt(key) ?: (defaultVal + 1)) - 1

        if (minVal != null) {
            c = max(minVal, c)
        }

        setInt(key, c)
    }

    @Suppress("unused")
    fun getLong(key: String): Long? {
        val s = file.get(key) ?: return null
        return s.toLong()
    }

    @Suppress("unused")
    fun setLong(key: String, value: Long) {
        file.set(key, "$value")
    }

    fun getFloat(key: String): Float? {
        val s = file.get(key) ?: return null
        return s.toFloat()
    }

    fun setFloat(key: String, value: Float?) {
        if (value == null) {
            file.remove(key)
        } else {
            file.set(key, "$value")
        }
    }

    fun getMoment(key: String): Moment? {
        val s = getString(key)

        return if (s.isNullOrEmpty()) {
            null
        } else {
            Moment.parseZoneAgnosticOrNull(s)
        }
    }

    fun setMoment(key: String, m: Moment?) {
        setString(key, m?.formatAsZoneAgnosticDateTime() ?: "")
    }

    fun remove(key: String) {
        file.remove(key)
    }

    init {
        val path = "${dir.absolutePath}/${filename}.dat"
        file = KeyValueFile(path)
        file.open()

        val v = getInt(VERSION_KEY) ?: 0

        if (version != v) {
            Log.d(TAG, "Versions differ: $version vs. $v, clearing")
            file.clear()
            setInt(VERSION_KEY, version)
        }
    }

    companion object {
        private const val TAG = "RawStatsFile"
        private const val VERSION_KEY = "__RawStatsFile.version"
    }
}
