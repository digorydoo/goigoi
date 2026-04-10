package io.github.digorydoo.goigoi.activity.unyt

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.utils.SingletonHolder

class UnytActivityParams(val unytId: String) {
    fun putInto(intent: Intent) {
        intent.putExtra(UNYTID_EXTRA, unytId)
    }

    companion object {
        private const val PREFIX = BuildConfig.APPLICATION_ID
        private const val UNYTID_EXTRA = "${PREFIX}.unytId"

        fun fromIntent(intent: Intent) = UnytActivityParams(
            unytId = intent.getStringExtra(UNYTID_EXTRA) ?: "",
        )
    }
}

/**
 * Starts the UnytActivity. The unyt is loaded before navigating. The UnytActivity does not strictly require this, but
 * it improves the transition. Important: Make sure to disallow selecting the item again until done is called!
 */
fun Activity.startUnytActivityAsync(unyt: Unyt, done: () -> Unit) {
    val vocab = SingletonHolder.vocab

    Thread {
        vocab.loadUnytIfNecessary(unyt)

        Handler(Looper.getMainLooper()).post {
            // Back on the main thread
            val params = UnytActivityParams(unytId = unyt.id)
            val intent = Intent(this, UnytActivity::class.java)
            params.putInto(intent)
            startActivity(intent)
            done() // called while the new activity is now active
        }
    }.start()
}
