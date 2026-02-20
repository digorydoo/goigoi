package io.github.digorydoo.goigoi.activity.about

import android.app.Activity
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import io.github.digorydoo.goigoi.R

class Bindings(a: Activity) {
    val toolbar = a.findViewById<Toolbar>(R.id.my_toolbar)!!
    val copyrightAndLicense = a.findViewById<TextView>(R.id.copyright_and_license)!!
}
