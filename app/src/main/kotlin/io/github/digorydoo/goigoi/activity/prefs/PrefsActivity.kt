package io.github.digorydoo.goigoi.activity.prefs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.digorydoo.goigoi.activity.about.startAboutActivity
import io.github.digorydoo.goigoi.activity.welcome.startWelcomeActivity
import io.github.digorydoo.goigoi.providers.DevicePropsProvider
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.providers.SingletonsProvider
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.SingletonHolder

class PrefsActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        enableEdgeToEdge()


        setContent {
            SingletonsProvider(this) {
                DevicePropsProvider(this) {
                    GoigoiTheme {
                        PrefsScreen(
                            onDarkModeChange = {
                                val prefs = SingletonHolder.prefs
                                prefs.darkMode = it
                                // recreate() -- NO, problems with status bar styling!
                                finish()
                                startPrefsActivity()
                            },
                            onAboutItemSelected = { startAboutActivity() },
                            onBack = {
                                // NavUtils.navigateUpFromSameTask(this) -- NO
                                // Since we may have changed the theme, we have to completely recreate WelcomeActivity.
                                // WelcomeActivity is expected to have called finish() before navigating to
                                // PrefsActivity.
                                startWelcomeActivity()
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}
