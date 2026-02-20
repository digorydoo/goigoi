package io.github.digorydoo.goigoi.activity.welcome

import android.app.Activity
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import io.github.digorydoo.goigoi.R

class Bindings(a: Activity) {
    val rootView = a.findViewById<RelativeLayout>(R.id.root_view)!!
    val topicsList = a.findViewById<RecyclerView>(R.id.topics_list)!!
}
