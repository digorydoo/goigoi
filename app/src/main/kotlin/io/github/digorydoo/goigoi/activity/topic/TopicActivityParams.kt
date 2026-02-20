package io.github.digorydoo.goigoi.activity.topic

import android.app.Activity
import android.content.Intent
import io.github.digorydoo.goigoi.BuildConfig

class TopicActivityParams(val topicId: String) {
    fun putInto(intent: Intent) {
        intent.putExtra(TOPICID_EXTRA, topicId)
    }

    companion object {
        private const val PREFIX = BuildConfig.APPLICATION_ID
        private const val TOPICID_EXTRA = "${PREFIX}.topicId"

        fun fromIntent(intent: Intent) =
            TopicActivityParams(topicId = intent.getStringExtra(TOPICID_EXTRA) ?: "")
    }
}

fun Activity.startTopicActivity(params: TopicActivityParams) {
    val intent = Intent(this, TopicActivity::class.java)
    params.putInto(intent)
    startActivity(intent)
}
