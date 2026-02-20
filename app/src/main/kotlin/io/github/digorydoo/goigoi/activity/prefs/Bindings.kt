package io.github.digorydoo.goigoi.activity.prefs

import android.app.Activity
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import io.github.digorydoo.goigoi.R

class Bindings(a: Activity) {
    val toolbar = a.findViewById<Toolbar>(R.id.my_toolbar)!!
    val changeThemeItem = a.findViewById<View>(R.id.change_theme_item)!!
    val changeThemeBtn = a.findViewById<SwitchCompat>(R.id.change_theme_switch)!!
    val aboutAppItem = a.findViewById<View>(R.id.about_app_item)!!
}
