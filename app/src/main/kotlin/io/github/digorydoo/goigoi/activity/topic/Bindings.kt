package io.github.digorydoo.goigoi.activity.topic

import android.app.Activity
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import io.github.digorydoo.goigoi.R

class Bindings(a: Activity) {
    val toolbar = a.findViewById<Toolbar>(R.id.my_toolbar)!!
    val unytsList = a.findViewById<RecyclerView>(R.id.unyts_list)!!
    val leftGap = a.findViewById<View>(R.id.left_gap)!!
    val rightGap = a.findViewById<View>(R.id.right_gap)!!
}
