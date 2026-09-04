package io.github.digorydoo.goigoi.activity.welcome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.activity.prefs.startPrefsActivity
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityParams
import io.github.digorydoo.goigoi.activity.prog_study.startProgStudyActivity
import io.github.digorydoo.goigoi.activity.topic.TopicActivityParams
import io.github.digorydoo.goigoi.activity.topic.startTopicActivity
import io.github.digorydoo.goigoi.activity.unyt.startUnytActivityAsync
import io.github.digorydoo.goigoi.activity.welcome.components.WelcomeScreen
import io.github.digorydoo.goigoi.providers.DevicePropsProvider
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.providers.SingletonsProvider
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.SingletonHolder

class WelcomeActivity: ComponentActivity() {
    private lateinit var model: WelcomeActivityModel
    private var navigatedToItem: WelcomeActivityModel.Item? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        enableEdgeToEdge()

        val vocab = SingletonHolder.vocab
        val stats = SingletonHolder.stats

        // The model contains the values that might change

        model = WelcomeActivityModel(vocab, stats, applicationContext)
        model.update()

        // Topics never change, therefore we don't need to wrap them in the model

        val topics = vocab.topics.asSequence().filter { BuildConfig.DEBUG || !it.hidden }.toList()

        setContent {
            SingletonsProvider(this) {
                DevicePropsProvider(this) {
                    GoigoiTheme {
                        WelcomeScreen(
                            model,
                            topics,
                            onPrefsBtnClicked = {
                                startPrefsActivity()
                                finish() // PrefsActivity may change theme
                            },
                            onBigStudyBtnClicked = {
                                startProgStudyActivity(ProgStudyActivityParams())
                            },
                            onTopicClicked = { topic ->
                                startTopicActivity(TopicActivityParams(topicId = topic.id))
                                navigatedToItem = WelcomeActivityModel.TopicItem(topic)
                            },
                            onMyWordsUnytClicked = {
                                startUnytActivityAsync(
                                    vocab.myWordsUnyt,
                                    done = { navigatedToItem = WelcomeActivityModel.MyWordsUnytItem() }
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
    }

    override fun onResume() {
        super.onResume()

        val stats = SingletonHolder.stats
        stats.notifyMainActivityResume() // to clear old statistics

        model.update()

        navigatedToItem?.let { item ->
            model.setHighlightedItem(item)
        }

        navigatedToItem = null
    }
}
