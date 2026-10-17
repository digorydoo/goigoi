package io.github.digorydoo.goigoi.activity.unyt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import io.github.digorydoo.goigoi.activity.unyt.UnytActivityModel.WordInfo
import io.github.digorydoo.goigoi.activity.unyt.UnytActivityModel.WordsListItem
import io.github.digorydoo.goigoi.activity.unyt.composables.UnytScreen
import io.github.digorydoo.goigoi.composables.bottom_sheet.word_info.WordInfoBottomSheetModel
import io.github.digorydoo.goigoi.composables.providers.DevicePropsProvider
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.getOrCreateSingletons

class UnytActivity: ComponentActivity() {
    private lateinit var unyt: Unyt
    private lateinit var model: UnytActivityModel
    private lateinit var tasks: UnytActivityTasks
    private var shouldUpdateOnResume = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        enableEdgeToEdge()

        val singletons = getOrCreateSingletons()
        val kanjiIndex = singletons.kanjiIndex
        val listItemDataFactory = singletons.listItemDataFactory
        val prefs = singletons.prefs
        val stats = singletons.stats
        val vocab = singletons.vocab

        val params = UnytActivityParams.fromIntent(intent)
        unyt = vocab.findUnytById(params.unytId)!!

        stats.notifyUnytActivityLaunched(unyt)

        tasks = UnytActivityTasks(unyt, vocab, stats, listItemDataFactory, lifecycleScope)
        model = UnytActivityModel(unyt, prefs, stats, tasks, vocab, lifecycleScope)
        val bottomSheetModel = WordInfoBottomSheetModel(kanjiIndex, stats)

        fillListWithPlaceholders()
        model.updateAllItemsBlocking() // should be OK, because stuff was preloaded by startUnytActivity()

        setContent {
            DevicePropsProvider(this) {
                GoigoiTheme(prefs.theme) {
                    UnytScreen(
                        model,
                        bottomSheetModel,
                        onBack = { finish() }
                    )
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
