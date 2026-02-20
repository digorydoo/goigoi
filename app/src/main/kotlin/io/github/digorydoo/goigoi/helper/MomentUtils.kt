package io.github.digorydoo.goigoi.helper

import android.content.Context
import android.text.format.DateUtils
import io.github.digorydoo.goigoi.R
import ch.digorydoo.kutils.utils.Moment
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * @return a string representing the relative time span in human-readable form
 */
@OptIn(ExperimentalTime::class)
fun Moment.formatRelativeTime(ctx: Context): String {
    val nowMillis = Clock.System.now().toEpochMilliseconds()
    val thisMillis = toInstant().toEpochMilliseconds()
    val deltaMillis = abs(nowMillis - thisMillis)

    return if (deltaMillis < 60000) {
        ctx.getString(R.string.just_now)
    } else {
        DateUtils.getRelativeTimeSpanString(thisMillis, nowMillis, DateUtils.FORMAT_ABBREV_ALL.toLong()).toString()
    }
}
