package io.github.digorydoo.goigoi.activity.unyt

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import io.github.digorydoo.goigoi.components.list.WordListItemData
import io.github.digorydoo.goigoi.components.menus.WordCtxMenuModel
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.stats.Stats
import io.github.digorydoo.goigoi.core.stats.StatsKey
import io.github.digorydoo.goigoi.utils.UserPrefs
import io.github.digorydoo.goigoi.utils.UserPrefs.WordListItemMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.milliseconds

class UnytActivityModel(
    val unyt: Unyt,
    private val vocab: Vocabulary,
    private val prefs: UserPrefs,
    private val stats: Stats,
    private val tasks: UnytActivityTasks,
    private val lifecycleScope: LifecycleCoroutineScope,
): WordCtxMenuModel {
    sealed interface WordsListItem
    class WordInfo(val word: Word, var data: WordListItemData? = null): WordsListItem

    val unytName = unyt.name.withSystemLangExcept("ja") // use en if systemLang is ja
    val unytIsMyWords = unyt == vocab.myWordsUnyt

    private val _list = MutableStateFlow(listOf<WordsListItem>())
    val list = _list.asStateFlow()

    fun setList(newList: List<WordsListItem>) {
        _list.update { newList }

        val itemsThatNeedLoading = newList.filterIsInstance<WordInfo>().filter { it.data == null }

        lifecycleScope.launch {
            updateItemsOrchestrated(itemsThatNeedLoading)

            val progress = stats.getUnytStudyProgress(unyt)
            val rating = stats.getUnytRating(unyt)

            _unytProgress.update { progress }
            _unytRating.update { rating }
        }
    }

    private val _highlightedWord = MutableStateFlow(null as Word?)
    val highlightedWord = _highlightedWord.asStateFlow()

    fun setHighlightedWord(word: Word?) {
        _highlightedWord.update { word }
    }

    private val _numWordsInUnyt = MutableStateFlow(unyt.numWordsAvailable)
    val numWordsInUnyt = _numWordsInUnyt.asStateFlow()

    private val _unytProgress = MutableStateFlow(stats.getUnytStudyProgress(unyt))
    val unytProgress = _unytProgress.asStateFlow()

    private val _unytRating = MutableStateFlow(stats.getUnytRating(unyt))
    val unytRating = _unytRating.asStateFlow()

    private val _listDisplayMode = MutableStateFlow(prefs.wordListItemMode)
    val listDisplayMode = _listDisplayMode.asStateFlow()

    fun setListDisplayMode(mode: WordListItemMode) {
        _listDisplayMode.update { mode }
        prefs.wordListItemMode = mode
    }

    // This will never change, so it does not need to be a MutableStateFlow.
    val availableListDisplayModes = WordListItemMode.entries.filter {
        when (it) {
            WordListItemMode.SHOW_KANA -> unyt.hasFurigana
            WordListItemMode.SHOW_ROMAJI -> unyt.hasRomaji
            WordListItemMode.SHOW_TRANSLATION -> true
        }
    }

    fun updateAllItems() {
        for (item in _list.value) {
            if (item is WordInfo) {
                updateItemAsync(item, null)
            }
        }
    }

    fun updateAllItemsBlocking() {
        runBlocking {
            for (item in _list.value) {
                if (item is WordInfo) {
                    updateItem(item)
                }
            }
        }
    }

    private suspend fun updateItemsOrchestrated(list: List<WordInfo>) {
        for (info in list) {
            val startMillis = System.currentTimeMillis()
            updateItem(info)

            // If data loaded fast, we wait a minimum amount of time for a nice UI orchestration.
            val millisToWait = MIN_ITEM_LOAD_TIME_MILLIS - (System.currentTimeMillis() - startMillis)
            if (millisToWait > 0) delay(millisToWait.milliseconds)
        }
    }

    private fun updateItemAsync(info: WordInfo, onDone: (() -> Unit)?) {
        lifecycleScope.launch {
            updateItem(info)
            onDone?.invoke()
        }
    }

    private suspend fun updateItem(info: WordInfo) {
        val newData = tasks.getItemData(info.word)

        _list.update { oldList ->
            oldList.map { item ->
                if (item == info) {
                    // Replace the existing WordInfo to cause the necessary re-rendering
                    WordInfo(word = info.word, data = newData)
                } else {
                    item
                }
            }
        }
    }

    fun updateItemOfWord(word: Word, onDone: (() -> Unit)? = null) {
        val info = _list.value.find { it is WordInfo && it.word == word } as? WordInfo

        if (info == null) {
            Log.w(TAG, "Word ${word.id} not found in list, onDone not called!")
            return
        }

        updateItemAsync(info, onDone)
    }

    override suspend fun fakeGoodStats(word: Word) = fakeStats(word, 5, 1)
    override suspend fun fakeAvgStats(word: Word) = fakeStats(word, 4, 2)
    override suspend fun fakePoorStats(word: Word) = fakeStats(word, 3, 20)

    private suspend fun fakeStats(word: Word, numCorrect: Int, numWrong: Int) {
        tasks.fakeStats(word, numCorrect, numWrong)
        updateItemOfWord(word)
    }

    override suspend fun resetStats(word: Word) {
        tasks.resetStats(word)
        updateItemOfWord(word)
    }

    override fun setSuperProgIdx(word: Word) {
        tasks.setSuperProgIdx(word)
    }

    override fun addToMyWords(word: Word) {
        vocab.myWordsUnyt.add(0, word) // put it in front

        if (unytIsMyWords) {
            _numWordsInUnyt.update { unyt.numWordsAvailable }
        }

        updateItemOfWord(word) // components need to know when rendering the menu
    }

    override fun removeFromMyWords(word: Word) {
        vocab.myWordsUnyt.removeAllWithSameId(word)

        if (unytIsMyWords) {
            _numWordsInUnyt.update { unyt.numWordsAvailable }

            _list.update { oldList ->
                oldList.filter { it is WordInfo && it.word != word }
            }
        }

        updateItemOfWord(word) // components need to know when rendering the menu
    }

    fun didOpenBottomSheet() {
        stats.incUserStudyCountOfToday(StatsKey.BOTTOM_SHEET)
    }

    companion object {
        private const val TAG = "UnytActvModel"
        private const val MIN_ITEM_LOAD_TIME_MILLIS = 1L
    }
}
