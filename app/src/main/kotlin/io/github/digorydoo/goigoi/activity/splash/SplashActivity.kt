package io.github.digorydoo.goigoi.activity.splash

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.welcome.startWelcomeActivity
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.stats.Stats
import io.github.digorydoo.goigoi.utils.AndroidLogStrategy
import io.github.digorydoo.goigoi.utils.SingletonHolder

/**
 * An application should not provide its own launch screen. Problem is, the animated logo needs to be an Animated Vector
 * Drawable (XML), but my AnimatedLogo is written in Kotlin. So, in order to correctly use the official splash screen,
 * I'd have to:
 *
 *    - Make sure the official androidx implementation also works for API level 23.
 *    - Find a way to tell the official splash screen to use my Drawable, or rewrite the animation as an AVD.
 *    - Find a way to set the animation duration programmatically based on hasAllSingletons.
 *    - Implement KeepOnScreenCondition and check the existence of the singletons there, see
 *      https://developer.android.com/reference/androidx/core/splashscreen/SplashScreen.KeepOnScreenCondition
 *    - Find a way to trigger the re-evaluation of KeepOnScreenCondition when the init phase has completed
 *    - Use splash.setOnKeepScreenCondition to install the condition
 *    - Remove no_splash_icon.xml
 *    - Rename this activity and its TAG to LaunchActivity, launch_activity.xml, LaunchTheme (styles)
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity: AppCompatActivity() {
    private lateinit var bindings: Bindings
    private var waitForAnim = true

    override fun onCreate(savedInstanceState: Bundle?) {
        /* val splash = */ installSplashScreen() // this is needed, otherwise my styles Theme.App.Starting are ignored

        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)

        val ctx = applicationContext
        bindings = Bindings(this)

        // Initialize kutil's Log facilities.
        Log.strategies = mutableListOf(AndroidLogStrategy())

        val drawable = AnimatedLogo(ctx)
        bindings.logo.setImageDrawable(drawable)

        if (SingletonHolder.singletonsExist) {
            // Startup won't take long. Suppress the animation!
            drawable.animValue = 1.0f
            bindings.logo.invalidate()
            waitForAnim = false
        } else {
            waitForAnim = true
            ValueAnimator.ofFloat(0.0f, 1.0f).apply {
                addUpdateListener { a ->
                    drawable.animValue = a.animatedValue as Float
                    bindings.logo.invalidate()
                }
                duration = (1000.0f * ANIM_DURATION).toLong()
                interpolator = LinearInterpolator()
                start()
            }
        }
    }

    /**
     * Called when the activity is resumed. It is also called when it first starts.
     */
    override fun onResume() {
        super.onResume()
        runInitThread()
    }

    private fun runInitThread() {
        val self: AppCompatActivity = this

        Thread {
            val startMillis = System.currentTimeMillis()
            val ctx = applicationContext

            SingletonHolder.createSingletons(ctx)
            val vocab = SingletonHolder.vocab
            val stats = SingletonHolder.stats
            stats.notifyAppLaunch()
            prefillCaches(vocab, stats)

            // The following exports the stats in the downloads folder. Use Android Studio's device explorer to
            // retrieve them from /storage/self/primary/Download

            // if (BuildConfig.DEBUG) {
            // stats.export(contentResolver)
            // }

            // If we were fast, wait until the animation completes

            if (waitForAnim) {
                val millisPassed = System.currentTimeMillis() - startMillis
                val millisLeft = (1000.0f * PROCEED_AFTER).toInt() - millisPassed
                Log.debug(TAG, "Done after ${millisPassed.toFloat() / 1000.0f}s")

                if (millisLeft > 0) {
                    try {
                        Thread.sleep(millisLeft)
                    } catch (_: InterruptedException) {
                        // ignore
                    }
                }
            }

            // Launch the next activity

            startWelcomeActivity()
            self.finish()
        }.start()
    }

    private fun prefillCaches(vocab: Vocabulary, stats: Stats) {
        var toGo = 30

        for (t in vocab.topics) {
            for (u in t.unyts) {
                // None of the unyt ratings are going to be looked up on first launch as study
                // progress values will still be 0. On later launches, UnytStatsFile should have the
                // ratings ready in a single value. So, let's just pre-fill the progress values.
                stats.getUnytStudyProgress(u)
                toGo--

                if (toGo <= 0) {
                    // We stop here to make sure the first launch does not take forever
                    return
                }
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("SplashActivity")
        private const val ANIM_DURATION = 2.0f // seconds
        private const val PROCEED_AFTER = ANIM_DURATION + 0.1f
    }
}
