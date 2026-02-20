package io.github.digorydoo.goigoi.activity.flipthru

import android.app.Activity
import android.content.Intent
import io.github.digorydoo.goigoi.BuildConfig

class FlipThruActivityParams(val unytId: String) {
    fun putInto(intent: Intent) {
        intent.apply {
            putExtra(UNYTID_EXTRA, unytId)
        }
    }

    companion object {
        private const val PREFIX = BuildConfig.APPLICATION_ID
        private const val UNYTID_EXTRA = "${PREFIX}.unytId"

        fun fromIntent(intent: Intent) = FlipThruActivityParams(
            unytId = intent.getStringExtra(UNYTID_EXTRA) ?: "",
        )
    }
}

fun Activity.startFlipThruActivity(params: FlipThruActivityParams) {
    val intent = Intent(this, FlipThruActivity::class.java)
    params.putInto(intent)
    startActivity(intent)
}
