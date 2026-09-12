package io.github.digorydoo.goigoi.legacy.activity.flip_thru

import android.app.Activity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.compose.ui.platform.ComposeView
import io.github.digorydoo.goigoi.R

class Bindings(a: Activity) {
    val toolbar = a.findViewById<Toolbar>(R.id.my_toolbar)!!
    val scrollContainer = a.findViewById<View>(R.id.scroll_container)!!
    val cardArea = a.findViewById<View>(R.id.card_area)!!
    val resultImg = a.findViewById<ImageView>(R.id.result_img)!!
    val actionMsg = a.findViewById<TextView>(R.id.action_msg)!!
    val composeView = a.findViewById<ComposeView>(R.id.compose_view)!!
}
