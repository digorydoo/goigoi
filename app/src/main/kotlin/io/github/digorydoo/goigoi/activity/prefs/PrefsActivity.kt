package io.github.digorydoo.goigoi.activity.prefs

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.about.startAboutActivity
import io.github.digorydoo.goigoi.activity.welcome.startWelcomeActivity
import io.github.digorydoo.goigoi.helper.UserPrefs
import io.github.digorydoo.goigoi.utils.ResUtils

class PrefsActivity: AppCompatActivity() {
    private lateinit var bindings: Bindings
    private lateinit var values: Values
    private lateinit var prefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        setContentView(R.layout.prefs_activity)

        val ctx = applicationContext
        prefs = UserPrefs.getSingleton(ctx)
        bindings = Bindings(this)
        values = Values(this)

        setSupportActionBar(bindings.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            title = getString(R.string.preferences)
        }

        bindings.changeThemeBtn.apply {
            isChecked = prefs.darkMode

            setOnClickListener {
                changeThemeBtnClicked()
            }
        }

        bindings.changeThemeItem.setOnClickListener {
            bindings.changeThemeBtn.isChecked = !bindings.changeThemeBtn.isChecked
            // Changing isChecked does not automatically call its onClick listener
            changeThemeBtnClicked()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    this@PrefsActivity.handleOnBackPressed()
                }
            }
        )

        bindings.aboutAppItem.setOnClickListener {
            startAboutActivity()
        }
    }

    private fun changeThemeBtnClicked() {
        prefs.darkMode = bindings.changeThemeBtn.isChecked // isChecked already has new value
        // recreate() -- NO, problems with status bar styling!
        finish()
        startPrefsActivity()
    }

    /**
     * Called when the toolbar's Home button is pressed.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            handleOnBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun handleOnBackPressed() {
        // NavUtils.navigateUpFromSameTask(this) -- NO
        // Since we may have changed the theme, we have to completely recreate WelcomeActivity.
        // WelcomeActivity is expected to have called finish() before navigating to PrefsActivity.
        startWelcomeActivity()
        finish()
    }
}
