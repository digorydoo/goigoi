package io.github.digorydoo.goigoi.dialog

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import ch.digorydoo.kutils.cjk.hasDakuten
import ch.digorydoo.kutils.cjk.hasHandakuten
import ch.digorydoo.kutils.cjk.hasSmallKana
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.QuestionAndAnswer
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard
import io.github.digorydoo.goigoi.stats.HintDlgKey
import io.github.digorydoo.goigoi.stats.Stats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class HintDialogManager(private val stats: Stats) {
    private var activeDlg: AlertDialog? = null
    private var activeJob: Job? = null

    fun showRateUsDlgIfAppropriate(activity: AppCompatActivity) {
        if (activeDlg != null) {
            cancel()
            return
        }

        if (MARKET_PLAY_STORE_URI.isNotEmpty()) {
            if (stats.launchCount > 3 && !stats.hasHintBeenShown(HintDlgKey.RATE_US_REQUEST_SHOWN)) {
                stats.didShowHint(HintDlgKey.RATE_US_REQUEST_SHOWN)
                doAsyncOnUIThread(activity) {
                    MyDlgBuilder.showRateUsDlg(activity) { ok ->
                        if (ok) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, MARKET_PLAY_STORE_URI.toUri())
                                activity.startActivity(intent)
                            } catch (_: ActivityNotFoundException) {
                                // We come here if Play Store app is not installed. Open url in browser!
                                val intent = Intent(Intent.ACTION_VIEW, BROWSER_PLAY_STORE_URI.toUri())
                                activity.startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
    }

    fun showKeyboardHintIfAppropriate(qa: QuestionAndAnswer, keyboardMode: Keyboard.Mode, activity: AppCompatActivity) {
        if (activeDlg != null) {
            cancel()
            return
        }

        val isExtKeyboardShown = when (keyboardMode) {
            Keyboard.Mode.FIXED_KEYS -> false
            Keyboard.Mode.JUST_REVEAL -> false
            Keyboard.Mode.HIRAGANA -> true
            Keyboard.Mode.KATAKANA -> true
        }

        if (!isExtKeyboardShown) {
            return // all hints are for the extended keyboard
        }

        val availHints = mutableSetOf(HintDlgKey.FIRST_TIME_EXTENDED_KEYBOARD_SHOWN)

        if (qa.answers.any { it.hasSmallKana() }) {
            availHints.add(HintDlgKey.FIRST_TIME_EXTKB_SMALL_KANA)
        }

        if (qa.answers.any { it.hasDakuten() } || qa.answers.any { it.hasHandakuten() }) {
            availHints.add(HintDlgKey.FIRST_TIME_EXTKB_DAKUTEN_HANDAKUTEN)
        }

        availHints.removeAll { stats.hasHintBeenShown(it) }

        if (availHints.isNotEmpty()) {
            doAsyncOnUIThread(activity) {
                while (availHints.isNotEmpty()) {
                    delay(DELAY_BEFORE_EACH_DLG_MILLIS)

                    val hint = availHints.first()
                    availHints.remove(hint)
                    stats.didShowHint(hint)

                    suspendCoroutine { c ->
                        showHintDlg(
                            hint,
                            activity,
                            onDismiss = { c.resume(true) }
                        )
                    }
                }
            }
        }
    }

    private fun doAsyncOnUIThread(activity: AppCompatActivity, lambda: suspend CoroutineScope.() -> Unit) {
        activeJob = activity.lifecycleScope.launch(Dispatchers.Main) {
            Log.d(TAG, "Job started")
            delay(INITIAL_DELAY_MILLIS)
            lambda()
            Log.d(TAG, "Job finished")
            activeJob = null
        }
    }

    fun cancel() {
        activeDlg?.dismiss()
        activeDlg = null

        activeJob?.cancel()
        activeJob = null
    }

    private fun showHintDlg(hint: HintDlgKey, activity: AppCompatActivity, onDismiss: () -> Unit) {
        val hintTextResId = when (hint) {
            HintDlgKey.FIRST_TIME_EXTENDED_KEYBOARD_SHOWN -> R.string.first_time_extkb_shown
            HintDlgKey.FIRST_TIME_EXTKB_SMALL_KANA -> R.string.first_time_extkb_small_kana
            HintDlgKey.FIRST_TIME_EXTKB_DAKUTEN_HANDAKUTEN -> R.string.first_time_extkb_dakuten_handakuten
            HintDlgKey.RATE_US_REQUEST_SHOWN -> throw Exception("Should have called a two-way dialog: $hint")
        }
        // NOTE: We need to pass activity as Context here. applicationContext leads to a crash!
        activeDlg = MyDlgBuilder.showHintDlg(hintTextResId, activity) {
            // We come here when the dialogue is dismissed.
            Log.d(TAG, "Dialogue for $hint was dismissed.")
            activeDlg = null
            onDismiss()
        }
    }

    companion object {
        private const val TAG = "HintDialogMgr"
        private const val INITIAL_DELAY_MILLIS = 1000L
        private const val DELAY_BEFORE_EACH_DLG_MILLIS = 1000L // also adds to initial delay!

        // Goigoi is no longer in the play store
        // "market://details?id=${BuildConfig.APPLICATION_ID}"
        // "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        private const val MARKET_PLAY_STORE_URI = ""
        private const val BROWSER_PLAY_STORE_URI = ""
    }
}
