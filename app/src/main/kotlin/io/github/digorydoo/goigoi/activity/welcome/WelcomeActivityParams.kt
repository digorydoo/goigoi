package io.github.digorydoo.goigoi.activity.welcome

import android.app.Activity
import android.content.Intent

// class WelcomeActivityParams -- currently no params

fun Activity.startWelcomeActivity() {
    val intent = Intent(this, WelcomeActivity::class.java)
    // params.putInto(intent)
    startActivity(intent)
}
