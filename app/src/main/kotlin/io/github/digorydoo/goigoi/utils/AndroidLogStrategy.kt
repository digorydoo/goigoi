package io.github.digorydoo.goigoi.utils

import android.util.Log
import ch.digorydoo.kutils.logging.Log.LogStrategy
import ch.digorydoo.kutils.logging.Log.Severity
import ch.digorydoo.kutils.logging.Log.Tag

class AndroidLogStrategy: LogStrategy {
    override fun log(tag: Tag, severity: Severity, msg: String) {
        when (severity) {
            Severity.DEBUG -> Log.d(tag.name, msg)
            Severity.INFO -> Log.i(tag.name, msg)
            Severity.WARNING -> Log.w(tag.name, msg)
            Severity.ERROR -> Log.e(tag.name, msg)
        }
    }
}
