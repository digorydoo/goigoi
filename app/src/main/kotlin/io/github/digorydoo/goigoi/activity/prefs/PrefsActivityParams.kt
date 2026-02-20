package io.github.digorydoo.goigoi.activity.prefs

import android.app.Activity
import android.content.Intent

// class PrefsActivityParams -- currently no params

fun Activity.startPrefsActivity() {
    val intent = Intent(this, PrefsActivity::class.java)
    // params.putInto(intent)
    startActivity(intent)
}
