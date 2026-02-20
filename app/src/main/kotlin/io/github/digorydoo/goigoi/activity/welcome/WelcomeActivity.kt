package io.github.digorydoo.goigoi.activity.welcome

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import ch.digorydoo.kutils.cjk.IntlString
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prefs.startPrefsActivity
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityParams
import io.github.digorydoo.goigoi.activity.prog_study.startProgStudyActivity
import io.github.digorydoo.goigoi.activity.topic.TopicActivityParams
import io.github.digorydoo.goigoi.activity.topic.startTopicActivity
import io.github.digorydoo.goigoi.activity.unyt.startUnytActivityAsync
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.dialog.HintDialogManager
import io.github.digorydoo.goigoi.drawable.HintBalloon
import io.github.digorydoo.goigoi.furigana.FuriganaBuilder
import io.github.digorydoo.goigoi.furigana.FuriganaSpan
import io.github.digorydoo.goigoi.helper.UserPrefs
import io.github.digorydoo.goigoi.list.*
import io.github.digorydoo.goigoi.listviewholder.ClickableItemDelegate
import io.github.digorydoo.goigoi.stats.Stats
import io.github.digorydoo.goigoi.utils.DeviceUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.withStudyLang

class WelcomeActivity: AppCompatActivity() {
    private lateinit var bindings: Bindings
    private lateinit var values: Values
    private lateinit var vocab: Vocabulary
    private lateinit var adapter: MyListAdapter
    private var lastItemSelected: AbstrListItem? = null
    private var uiDisabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        setContentView(R.layout.welcome_activity)

        val ctx = applicationContext
        bindings = Bindings(this)
        values = Values(this)
        vocab = Vocabulary.getSingleton(ctx)

        val itemDelegate = object: ClickableItemDelegate {
            override fun onClick(item: AbstrListItem) {
                if (uiDisabled) {
                    return
                }
                when (item) {
                    is ActionItem -> {
                        startProgStudyActivity(ProgStudyActivityParams())
                        lastItemSelected = item
                    }
                    is TopicListItem -> {
                        val topic = item.topic
                        if (BuildConfig.DEBUG || !topic.hidden) {
                            startTopicActivity(TopicActivityParams(topicId = topic.id))
                            lastItemSelected = item
                        }
                    }
                    is UnytListItem -> {
                        uiDisabled = true // make sure the item cannot be clicked again while the activity launches
                        lastItemSelected = item

                        startUnytActivityAsync(item.unyt) {
                            uiDisabled = false
                        }
                    }
                    else -> Unit
                }
            }
        }

        val screenSize = DeviceUtils.getScreenSize(this)

        val headerDelegate = object: HeaderViewHolder.Delegate {
            override fun prefsBtnClicked() {
                if (uiDisabled) return
                startPrefsActivity()
                finish() // PrefsActivity may change theme
            }

            override fun showHintBalloon(text: IntlString, anchor: View, anchor2: View?) {
                if (uiDisabled) return

                val studyText = text.withStudyLang
                val systemText = text.withSystemLang

                val str = if (studyText == systemText) {
                    studyText
                } else {
                    arrayOf(
                        studyText,
                        "\n",
                        systemText,
                    ).joinToString("")
                }

                if (str.isEmpty()) {
                    return
                }

                val options = FuriganaSpan.Options(fontSizeMin = values.furiganaFontSizeMin(screenSize))
                val span = FuriganaBuilder.buildSpan(str, options)
                HintBalloon.create(span, anchor, anchor2, bindings.rootView, screenSize, ctx)
            }
        }

        adapter = MyListAdapter(itemDelegate, layoutInflater) { parent, type ->
            when (type) {
                ItemViewType.HEADER -> HeaderViewHolder.create(
                    parent,
                    layoutInflater,
                    headerDelegate
                )
                else -> null
            }
        }

        bindings.topicsList.adapter = adapter
        bindings.topicsList.setHasFixedSize(false)
        bindings.topicsList.addItemDecoration(MyItemDecoration(ctx))

        val prefs = UserPrefs.getSingleton(ctx)

        if (!prefs.darkMode) {
            // Since this activity does not have a green toolbar, the green status bar
            // does not look good. Change it to a light status bar!
            DeviceUtils.setStatusBarAppearance(
                ResUtils.getARGBFromAttr(R.attr.statusBarBackground, ctx),
                false,
                window
            )
        }

        onBackPressedDispatcher.addCallback(
            this,
            object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when {
                        uiDisabled -> Unit
                        adapter.computeScrollOffsetWithHeuristic() <= values.minScrollTop -> finish()
                        else -> bindings.topicsList.smoothScrollToPosition(0)
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()

        val ctx = applicationContext
        val stats = Stats.getSingleton(ctx)
        stats.notifyMainActivityResume() // to clear old statistics

        rebuildList()

        lastItemSelected?.let { hiliteItem(it) }
        lastItemSelected = null

        HintDialogManager(stats).showRateUsDlgIfAppropriate(this)
    }

    private fun rebuildList() {
        val ctx = applicationContext
        val screenSize = DeviceUtils.getScreenSize(this)
        val orientation = DeviceUtils.getOrientation(this)
        val lrMargin = values.listLRMargin(screenSize, orientation).toInt()
        val imageMargin = values.topicImageMargin.toInt()

        // We might want to do the following from a Thread. I don't currently do this, because it
        // does not look good (jerky transition).

        adapter.items = ListBuilder.buildWelcomeList(
            vocab,
            screenSize,
            lrMargin,
            imageMargin,
            ctx
        )
    }

    private fun hiliteItem(item: AbstrListItem) {
        adapter.items
            .indexOfFirst { item == it }
            .takeIf { it >= 0 }
            ?.let { adapter.hiliteItemAfterUpdates(it) }
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "MainActivity"
    }
}
