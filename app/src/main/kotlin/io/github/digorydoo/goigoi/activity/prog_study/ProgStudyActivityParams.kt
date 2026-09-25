package io.github.digorydoo.goigoi.activity.prog_study

import android.app.Activity
import android.content.Intent

// class ProgStudyActivityParams -- currently no params

fun Activity.startProgStudyActivity() {
    val intent = Intent(this, ProgStudyActivity::class.java)
    // params.putInto(intent)
    startActivity(intent)
}
