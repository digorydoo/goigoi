package io.github.digorydoo.goigoi.activity.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.goigoi.activity.welcome.startWelcomeActivity
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.AndroidLogStrategy
import io.github.digorydoo.goigoi.utils.SingletonHolder

/**
 * An application should not provide its own launch screen. Problem is, the animated logo needs to be an Animated Vector
 * Drawable (XML), but my AnimatedLogo is written in Kotlin. Also, this splash screen is not simply an icon, but it
 * initializes the singletons while the animation is taking place. As a workaround, I use a transparent icon for
 * windowSplashScreenAnimatedIcon to avoid showing two icons to the user.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        /* val splash = */ installSplashScreen() // this is needed, otherwise my styles Theme.App.Starting are ignored
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize kutil's Log facilities
        Log.strategies = mutableListOf(AndroidLogStrategy())

        setContent {
            GoigoiTheme(useFixedDarkModeAndAvoidAccessingSingletons = true) {
                SplashScreen(
                    init = { asyncInit() },
                    onInitComplete = {
                        startWelcomeActivity()
                        finish()
                    }
                )
            }
        }
    }

    // Called in a coroutine!
    private fun asyncInit() {
        SingletonHolder.createSingletons(applicationContext)
        SingletonHolder.stats.notifyAppLaunch()
    }
}
