package io.github.digorydoo.goigoi.activity.unyt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import io.github.digorydoo.goigoi.activity.unyt.UnytActivityModel.WordInfo
import io.github.digorydoo.goigoi.activity.unyt.UnytActivityModel.WordsListItem
import io.github.digorydoo.goigoi.activity.unyt.composables.UnytScreen
import io.github.digorydoo.goigoi.composables.providers.DevicePropsProvider
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.composables.providers.SingletonsProvider
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.legacy.activity.flip_thru.FlipThruActivityParams
import io.github.digorydoo.goigoi.legacy.activity.flip_thru.startFlipThruActivity
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.SingletonHolder

class UnytActivity: ComponentActivity() {
    private lateinit var unyt: Unyt
    private lateinit var model: UnytActivityModel
    private lateinit var tasks: UnytActivityTasks
    private var shouldUpdateOnResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        enableEdgeToEdge()

        val vocab = SingletonHolder.vocab
        val prefs = SingletonHolder.prefs
        val stats = SingletonHolder.stats

        val params = UnytActivityParams.fromIntent(intent)
        unyt = vocab.findUnytById(params.unytId)!!

        stats.notifyUnytActivityLaunched(unyt)

        tasks = UnytActivityTasks(unyt, vocab, stats, lifecycleScope)
        model = UnytActivityModel(unyt, vocab, prefs, stats, tasks, lifecycleScope)

        fillListWithPlaceholders()
        model.updateAllItemsBlocking() // should be OK, because stuff was preloaded by startUnytActivity()

        setContent {
            SingletonsProvider(this) {
                DevicePropsProvider(this) {
                    GoigoiTheme {
                        UnytScreen(
                            model,
                            onLaunchFlipThruActivity = { startFlipThruActivity(FlipThruActivityParams(unyt.id)) },
                            onBack = { finish() }
                        )
                    }
                }
            }
        }
    }

    private fun fillListWithPlaceholders() {
        val list = mutableListOf<WordsListItem>()

        unyt.forEachWord { word ->
            list.add(WordInfo(word, data = null))
        }

        model.setList(list)
        shouldUpdateOnResume = false
    }

    override fun onResume() {
        super.onResume()

        if (shouldUpdateOnResume) {
            model.updateAllItems()
        } else {
            shouldUpdateOnResume = true
        }
    }
}
