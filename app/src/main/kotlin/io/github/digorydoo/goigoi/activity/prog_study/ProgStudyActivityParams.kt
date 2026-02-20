package io.github.digorydoo.goigoi.activity.prog_study

import android.app.Activity
import android.content.Intent
import io.github.digorydoo.goigoi.BuildConfig

class ProgStudyActivityParams(
    val unytId: String = "", // empty means super-progressive, i.e. across unyts
) {
    fun putInto(intent: Intent) {
        intent.apply {
            putExtra(UNYTID_EXTRA, unytId)
        }
    }

    companion object {
        private const val PREFIX = BuildConfig.APPLICATION_ID
        private const val UNYTID_EXTRA = "${PREFIX}.unytId"

        fun fromIntent(intent: Intent) = ProgStudyActivityParams(
            unytId = intent.getStringExtra(UNYTID_EXTRA) ?: "",
        )
    }
}

fun Activity.startProgStudyActivity(params: ProgStudyActivityParams) {
    val intent = Intent(this, ProgStudyActivity::class.java)
    params.putInto(intent)
    startActivity(intent)
}
