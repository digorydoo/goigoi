package io.github.digorydoo.goigoi.activity.unyt

import android.app.Activity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.view.FloatingActionBtn

class Bindings(a: Activity) {
    val toolbar = a.findViewById<Toolbar>(R.id.my_toolbar)!!
    val recyclerView = a.findViewById<RecyclerView>(R.id.recycler_view)!!
    val startBtn = a.findViewById<FloatingActionBtn>(R.id.start_btn)!!
}
