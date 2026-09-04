package io.github.digorydoo.goigoi.activity.topic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import io.github.digorydoo.goigoi.activity.topic.TopicActivityModel.Subheader
import io.github.digorydoo.goigoi.activity.topic.TopicActivityModel.UnytInfo
import io.github.digorydoo.goigoi.activity.topic.TopicActivityModel.UnytsListItem
import io.github.digorydoo.goigoi.activity.topic.components.TopicScreen
import io.github.digorydoo.goigoi.activity.unyt.startUnytActivityAsync
import io.github.digorydoo.goigoi.core.db.Topic
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.providers.DevicePropsProvider
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.providers.SingletonsProvider
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.SingletonHolder

class TopicActivity: ComponentActivity() {
    private lateinit var topic: Topic
    private lateinit var model: TopicActivityModel
    private lateinit var tasks: TopicActivityTasks
    private var navigatedToUnyt: Unyt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        enableEdgeToEdge()

        val params = TopicActivityParams.fromIntent(intent)
        val vocab = SingletonHolder.vocab
        topic = vocab.findTopicById(params.topicId)!!

        val stats = SingletonHolder.stats
        tasks = TopicActivityTasks(vocab, stats, applicationContext, lifecycleScope)

        model = TopicActivityModel(tasks, lifecycleScope)
        fillListWithPlaceholders()

        setContent {
            SingletonsProvider(this) {
                DevicePropsProvider(this) {
                    GoigoiTheme {
                        TopicScreen(
                            topic,
                            model,
                            onUnytClicked = { unyt ->
                                startUnytActivityAsync(
                                    unyt,
                                    done = { navigatedToUnyt = unyt }
                                )
                            },
                            onBack = { finish() }
                        )
                    }
                }
            }
        }
    }

    private fun fillListWithPlaceholders() {
        val list = mutableListOf<UnytsListItem>()

        for (unyt in topic.unyts) {
            val subheader = unyt.subheader.withSystemLang

            if (subheader.isNotEmpty()) {
                list.add(Subheader(subheader))
            }

            list.add(UnytInfo(unyt, isMyWordsUnyt = false, data = null))
        }

        model.setList(list)
    }

    override fun onResume() {
        super.onResume()

        navigatedToUnyt?.let { unyt ->
            model.updateItemOfUnyt(unyt, onDone = { unyt.unload() })
            model.setHighlightedUnyt(unyt)
        }

        navigatedToUnyt = null
    }
}
