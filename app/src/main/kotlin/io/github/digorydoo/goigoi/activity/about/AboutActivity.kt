package io.github.digorydoo.goigoi.activity.about

import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.helper.UserPrefs
import io.github.digorydoo.goigoi.utils.ResUtils

class AboutActivity: AppCompatActivity() {
    private lateinit var bindings: Bindings
    private lateinit var values: Values
    private lateinit var prefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        setContentView(R.layout.about_activity)

        val ctx = applicationContext
        prefs = UserPrefs.getSingleton(ctx)
        bindings = Bindings(this)
        values = Values(this)

        setSupportActionBar(bindings.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            title = getString(R.string.aboutAppPrimary)
        }

        bindings.copyrightAndLicense.text = Html.fromHtml(getString(R.string.copyright_and_license), 0)
    }
}
