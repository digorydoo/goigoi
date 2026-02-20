package io.github.digorydoo.goigoi.stats

import android.util.Log
import ch.digorydoo.kutils.utils.Moment
import java.io.File
import java.io.OutputStream
import java.util.UUID
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class GeneralStatsFile(dir: File): Stats.Exportable {
    private val file: RawStatsFile = RawStatsFile(dir, FILE_NAME, FILE_VERSION)

    override fun exportTo(dst: OutputStream) =
        file.exportTo(dst)

    val launchCount: Int get() = file.getInt(APP_LAUNCH_COUNT_ID) ?: 1

    fun notifyAppLaunch() {
        var id = file.getString(APP_INSTALL_ID_ID)

        if (id == null) {
            // We can't retrieve the installation id.
            // We interpret this as the first launch after installation.

            id = UUID.randomUUID().toString()
            file.setString(APP_INSTALL_ID_ID, id)

            val now = Moment.now().formatAsZoneAgnosticDateTime()
            file.setString(APP_INSTALL_DATE_ID, now)
        }

        file.incInt(APP_LAUNCH_COUNT_ID, 1, null)

        Log.d(TAG, "App install id: " + file.getString(APP_INSTALL_ID_ID))
        Log.d(TAG, "App install date: " + file.getString(APP_INSTALL_DATE_ID))
        Log.d(TAG, "App launch count: $launchCount")
    }

    fun notifyMainActivityResume() {
        // Since KeyValueFile loads in the entire file at launch, it's not too efficient to keep all
        // past stats. Throw away what we don't need any more!

        val limit = pastStatsLimit // there aren't any stats older than this
        val keepUntil = Moment.now().atMidnight() - 10.toDuration(DurationUnit.DAYS)
        var toGo = 7 // don't wipe everything at once (performance)
        var m = limit

        while (toGo > 0 && m < keepUntil) {
            Log.d(TAG, "Deleting ${m.formatAsZoneAgnosticDateTime()} (was: ${getUserStudyCountOfDay(m)})")
            removeUserStudyCountOfDay(m)
            m += 1.toDuration(DurationUnit.DAYS)
            toGo--
        }

        if (m != limit) {
            pastStatsLimit = m
        }
    }

    private var pastStatsLimit: Moment
        get() = file.getMoment(PAST_STATS_LIMIT_ID) ?: Moment.now().atMidnight()
        set(moment) = file.setMoment(PAST_STATS_LIMIT_ID, moment)

    private fun keyForUserStudyCount(m: Moment): String {
        val d = m.formatAsZoneAgnosticDate() // date without time
        return "$USER_STUDY_COUNT_ID.$d"
    }

    fun incUserStudyCountOfToday(reason: StatsKey) {
        val now = Moment.now()
        // file.setString(APP_USED_DATE_ID, now.formatRevDateTime())

        val key = keyForUserStudyCount(now)
        val count = file.getFloat(key) ?: 0.0f

        val increment = when (reason) {
            StatsKey.BOTTOM_SHEET -> 0.5f
            else -> 1.0f
        }

        file.setFloat(key, count + increment)
    }

    fun getUserStudyCountOfDay(m: Moment): Float {
        val key = keyForUserStudyCount(m)
        return file.getFloat(key) ?: 0.0f
    }

    private fun removeUserStudyCountOfDay(m: Moment) {
        val key = keyForUserStudyCount(m)
        file.remove(key)
    }

    fun hasHintBeenShown(hintDlgKey: HintDlgKey): Boolean {
        val key = "$HINT_SHOWN_ID.$hintDlgKey"
        return file.getBoolean(key) ?: false
    }

    fun didShowHint(hintDlgKey: HintDlgKey) {
        val key = "$HINT_SHOWN_ID.$hintDlgKey"
        file.setBoolean(key, true)
    }

    val superProgressiveIdx: Int get() = file.getInt(SUPER_PROGRESSIVE_IDX_ID) ?: 0

    fun setSuperProgressiveIdx(idx: Int) {
        file.setInt(SUPER_PROGRESSIVE_IDX_ID, idx)
    }

    companion object {
        private const val TAG = "GeneralStats"

        private const val FILE_NAME = "app"
        private const val FILE_VERSION = 5

        private const val APP_INSTALL_ID_ID = "appInstallId"
        private const val APP_INSTALL_DATE_ID = "appInstallDate"
        private const val APP_LAUNCH_COUNT_ID = "appLaunchCount"
        private const val USER_STUDY_COUNT_ID = "userActivity"
        private const val PAST_STATS_LIMIT_ID = "pastStatsLimit"
        private const val HINT_SHOWN_ID = "hintShown"
        private const val SUPER_PROGRESSIVE_IDX_ID = "superProgIdx"
    }
}
