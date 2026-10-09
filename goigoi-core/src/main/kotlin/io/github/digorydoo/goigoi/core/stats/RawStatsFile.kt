package io.github.digorydoo.goigoi.core.stats

import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.core.file.KeyValueFile
import java.io.File
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

class RawStatsFile(dir: File, filename: String) {
    private val file = KeyValueFile(
        pathToStringsFile = "${dir.absolutePath}/${filename}.dat",
        pathToBackup = "${dir.absolutePath}/${filename}.bak",
    )

    init {
        file.open()
    }

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
}
