package io.github.digorydoo.goigoi.activity.welcome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.activity.prefs.startPrefsActivity
import io.github.digorydoo.goigoi.activity.prog_study.startProgStudyActivity
import io.github.digorydoo.goigoi.activity.topic.TopicActivityParams
import io.github.digorydoo.goigoi.activity.topic.startTopicActivity
import io.github.digorydoo.goigoi.activity.unyt.startUnytActivityAsync
import io.github.digorydoo.goigoi.activity.welcome.composables.WelcomeScreen
import io.github.digorydoo.goigoi.composables.providers.DevicePropsProvider
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.core.stats.IStats
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.getOrCreateSingletons

class WelcomeActivity: ComponentActivity() {
    private lateinit var model: WelcomeActivityModel
    private lateinit var stats: IStats
    private var navigatedToItem: WelcomeActivityModel.Item? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        enableEdgeToEdge()

        val ctx = applicationContext

        val singletons = getOrCreateSingletons()
        val listItemDataFactory = singletons.listItemDataFactory
        val prefs = singletons.prefs
        val vocab = singletons.vocab
        stats = singletons.stats

        model = WelcomeActivityModel(vocab, stats, listItemDataFactory, ctx)
        model.update(cachedStudyCount)

        setContent {
            DevicePropsProvider(this) {
                GoigoiTheme(prefs.theme) {
                    WelcomeScreen(
                        model,
                        onPrefsBtnClicked = {
                            navigatedToItem = null
                            startPrefsActivity()
                            finish() // PrefsActivity may change theme
                        },
                        onBigStudyBtnClicked = {
                            navigatedToItem = null
                            startProgStudyActivity()
                        },
                        onTopicClicked = { topic ->
                            navigatedToItem = null
                            startTopicActivity(TopicActivityParams(topicId = topic.id))
                            navigatedToItem = WelcomeActivityModel.TopicItem(topic)
                        },
                        onMyWordsUnytClicked = {
                            navigatedToItem = null
                            startUnytActivityAsync(
                                vocab.myWordsUnyt,
                                done = { navigatedToItem = WelcomeActivityModel.MyWordsUnytItem }
                            )
                        },
                        onBack = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        cachedStudyCount = stats.getUserStudyCountOfDay(Moment.now())
    }

    override fun onResume() {
        super.onResume()

        stats.notifyMainActivityResume() // to clear old statistics
        model.update(cachedStudyCount)

        navigatedToItem?.let { item ->
            model.setHighlightedItem(item)
        }

        navigatedToItem = null
    }

    companion object {
        private var cachedStudyCount = Float.NaN
    }
}
