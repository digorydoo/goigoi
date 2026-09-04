package io.github.digorydoo.goigoi.activity.welcome

import android.content.Context
import ch.digorydoo.kutils.cjk.IntlString
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.components.list.UnytListItemData
import io.github.digorydoo.goigoi.core.db.Topic
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.stats.Stats
import io.github.digorydoo.goigoi.core.welcome.DailyProgressTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WelcomeActivityModel(private val vocab: Vocabulary, private val stats: Stats, private val ctx: Context) {
    sealed interface Item
    class TopicItem(val topic: Topic): Item
    class MyWordsUnytItem: Item

    private val dailyProgressTracker = DailyProgressTracker(stats)

    private val _dailyProgress = MutableStateFlow(arrayOf<Float>())
    val dailyProgress = _dailyProgress.asStateFlow()

    private val _todaysProgress = MutableStateFlow(0f)
    val todaysProgress = _todaysProgress.asStateFlow()

    private val _bigRingCentreText = MutableStateFlow(IntlString())
    val bigRingCentreText = _bigRingCentreText.asStateFlow()

    private val _progressMsg = MutableStateFlow("")
    val progressMsg = _progressMsg.asStateFlow()

    private val _myWordsData = MutableStateFlow(UnytListItemData())
    val myWordsData = _myWordsData.asStateFlow()

    private val _highlightedItem = MutableStateFlow(null as Item?)
    val highlightedItem = _highlightedItem.asStateFlow()

    fun setHighlightedItem(item: Item?) {
        _highlightedItem.update { item }
    }

    fun update() {
        dailyProgressTracker.update()

        _dailyProgress.update { _ -> dailyProgressTracker.daily.copyOf() } // copy to ensure re-render
        _todaysProgress.update { _ -> dailyProgressTracker.today }
        _bigRingCentreText.update { _ -> dailyProgressTracker.message }

        _progressMsg.update { _ ->
            ctx.getString(R.string.learnt_n_of_m_words)
                .replace("\${N}", "${stats.superProgressiveIdx}")
                .replace("\${M}", "${vocab.allWordFilenames.size}")
        }

        _myWordsData.update { _ ->
            UnytListItemData(vocab.myWordsUnyt, isMyWordsUnyt = true, vocab, stats, ctx)
        }
    }
}
