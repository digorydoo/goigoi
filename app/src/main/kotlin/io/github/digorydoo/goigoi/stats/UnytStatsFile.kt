package io.github.digorydoo.goigoi.stats

import io.github.digorydoo.goigoi.db.Unyt
import ch.digorydoo.kutils.utils.Moment
import java.io.File
import java.io.OutputStream

class UnytStatsFile(dir: File): Stats.Exportable {
    private val file: RawStatsFile = RawStatsFile(dir, FILE_NAME, FILE_VERSION)

    override fun exportTo(dst: OutputStream) =
        file.exportTo(dst)

    fun notifyUnytActivityLaunched(u: Unyt) {
        // A unyt's progress and rating caches may become stale when the built-in vocab was changed by an update.
        // As a workaround, we throw away the cache when a unyt's WordsOverviewActivity is launched:
        setStudyProgress(u, null)
        setRating(u, null)
    }

    fun setStudyMoment(u: Unyt) {
        setMoment(STUDY_DATE_ID, u, Moment.now())
        setStudyProgress(u, null)
        setRating(u, null)
    }

    fun getStudyMoment(u: Unyt): Moment? {
        return getMoment(STUDY_DATE_ID, u)
    }

    fun getStudyProgress(unyt: Unyt): Float? {
        return getFloat(UNYT_STUDY_PROGRESS_ID, unyt)
    }

    fun setStudyProgress(unyt: Unyt, value: Float?) {
        setFloat(UNYT_STUDY_PROGRESS_ID, unyt, value)
    }

    fun getRating(unyt: Unyt): Float? {
        return getFloat(UNYT_RATING_ID, unyt)
    }

    fun setRating(unyt: Unyt, value: Float?) {
        setFloat(UNYT_RATING_ID, unyt, value)
    }

    private fun getFloat(id: String, unyt: Unyt): Float? {
        val key = getKey(id, unyt)
        return file.getFloat(key)
    }

    private fun setFloat(id: String, unyt: Unyt, value: Float?) {
        val key = getKey(id, unyt)
        file.setFloat(key, value)
    }

    private fun getMoment(id: String, u: Unyt): Moment? {
        val key = getKey(id, u)
        return file.getMoment(key)
    }

    private fun setMoment(id: String, u: Unyt, m: Moment) {
        val key = getKey(id, u)
        file.setMoment(key, m)
    }

    private fun getKey(counterId: String, u: Unyt): String {
        return u.id + "." + counterId
    }

    companion object {
        private const val FILE_NAME = "unit"
        private const val FILE_VERSION = 6
        private const val STUDY_DATE_ID = "stdt"
        private const val UNYT_STUDY_PROGRESS_ID = "progress"
        private const val UNYT_RATING_ID = "rating"
    }
}
