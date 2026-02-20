package io.github.digorydoo.goigoi.activity.flipthru.fragment

import android.view.View
import android.widget.TextView
import io.github.digorydoo.goigoi.R

class Bindings(root: View) {
    val text1 = root.findViewById<TextView>(R.id.text1)!!
    val text2 = root.findViewById<TextView>(R.id.text2)!!
    val text3 = root.findViewById<TextView>(R.id.text3)!!
    val text4 = root.findViewById<TextView>(R.id.text4)!!
    val text5 = root.findViewById<TextView>(R.id.text5)!!
    val middleGap = root.findViewById<View>(R.id.middle_gap)!!
}
