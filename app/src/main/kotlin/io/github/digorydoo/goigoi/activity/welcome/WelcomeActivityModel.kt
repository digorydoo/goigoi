package io.github.digorydoo.goigoi.activity.welcome

import android.content.Context
import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.composables.list.UnytListItemData
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
    object MyWordsUnytItem: Item

    private val dailyProgressTracker = DailyProgressTracker(stats)

    private val _dailyProgress = MutableStateFlow(arrayOf<Float>())
    val dailyProgress = _dailyProgress.asStateFlow()

    private val _todaysProgress = MutableStateFlow(0f)
    val todaysProgress = _todaysProgress.asStateFlow()

    private val _studyCountOfDay = MutableStateFlow(0f)
    val studyCountOfDay = _studyCountOfDay.asStateFlow()

    private val _prevStudyCountOfDay = MutableStateFlow(0f)
    val prevStudyCountOfDay = _prevStudyCountOfDay.asStateFlow()

    private val _encouragementJa = MutableStateFlow(FuriganaString())
    val encouragementJa = _encouragementJa.asStateFlow()

    private val _encouragementTranslation = MutableStateFlow("")
    val encouragementTranslation = _encouragementTranslation.asStateFlow()

    private val _progressMsgJa = MutableStateFlow(FuriganaString())
    val progressMsgJa = _progressMsgJa.asStateFlow()

    private val _progressMsgTranslation = MutableStateFlow("")
    val progressMsgTranslation = _progressMsgTranslation.asStateFlow()

    private val _myWordsData = MutableStateFlow(UnytListItemData.createEmpty())
    val myWordsData = _myWordsData.asStateFlow()

    private val _highlightedItem = MutableStateFlow(null as Item?)
    val highlightedItem = _highlightedItem.asStateFlow()

    fun setHighlightedItem(item: Item?) {
        _highlightedItem.update { item }
    }

    fun update(cachedStudyCount: Float) {
        dailyProgressTracker.update()

        _dailyProgress.update { dailyProgressTracker.daily.copyOf() } // copy to ensure re-render
        _todaysProgress.update { dailyProgressTracker.today }
        _studyCountOfDay.update { stats.getUserStudyCountOfDay(Moment.now()) }
        _prevStudyCountOfDay.update { cachedStudyCount }

        _encouragementJa.update { FuriganaString(dailyProgressTracker.message.ja) }
        _encouragementTranslation.update { dailyProgressTracker.message.withSystemLang }

        val wordsStudied = stats.superProgressiveIdx
        val wordsStudiedWide = if (wordsStudied < 9) '０' + wordsStudied else "$wordsStudied"
        val totalNumWords = vocab.allWordFilenames.size

        _progressMsgJa.update {
            FuriganaString("${totalNumWords}【語：ご】【中：ちゅう】、${wordsStudiedWide}【語：ご】を【覚：おぼ】えた。")
        }

        _progressMsgTranslation.update {
            ctx.getString(R.string.learnt_n_of_m_words)
                .replace("\${N}", "$wordsStudied")
                .replace("\${M}", "$totalNumWords")
        }

        _myWordsData.update {
            UnytListItemData.create(vocab.myWordsUnyt, isMyWordsUnyt = true, vocab, stats, ctx)
        }
    }
}
