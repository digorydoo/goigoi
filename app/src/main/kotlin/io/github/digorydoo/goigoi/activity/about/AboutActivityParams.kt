package io.github.digorydoo.goigoi.activity.about

import android.app.Activity
import android.content.Intent

// class AboutActivityParams -- currently no params

fun Activity.startAboutActivity() {
    val intent = Intent(this, AboutActivity::class.java)
    // params.putInto(intent)
    startActivity(intent)
}
