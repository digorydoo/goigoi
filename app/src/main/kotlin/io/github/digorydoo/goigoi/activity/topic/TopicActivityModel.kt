package io.github.digorydoo.goigoi.activity.topic

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import io.github.digorydoo.goigoi.components.list.UnytListItemData
import io.github.digorydoo.goigoi.core.db.Unyt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

class TopicActivityModel(private val tasks: TopicActivityTasks, private val lifecycleScope: LifecycleCoroutineScope) {
    sealed interface UnytsListItem
    class Subheader(val text: String): UnytsListItem
    class UnytInfo(val unyt: Unyt, val isMyWordsUnyt: Boolean, var data: UnytListItemData? = null): UnytsListItem

    private val _list = MutableStateFlow(listOf<UnytsListItem>())
    val list = _list.asStateFlow()

    fun setList(newList: List<UnytsListItem>) {
        _list.update { newList }

        val itemsThatNeedLoading = newList.filterIsInstance<UnytInfo>().filter { it.data == null }

        lifecycleScope.launch {
            updateItems(itemsThatNeedLoading)
        }
    }

    private val _highlightedUnyt = MutableStateFlow(null as Unyt?)
    val highlightedUnyt = _highlightedUnyt.asStateFlow()

    fun setHighlightedUnyt(unyt: Unyt?) {
        _highlightedUnyt.update { unyt }
    }

    private suspend fun updateItems(list: List<UnytInfo>) {
        for (info in list) {
            val startMillis = System.currentTimeMillis()
            updateItem(info)

            // If data loaded fast, we wait a minimum amount of time for a nice UI orchestration.
            val millisToWait = MIN_ITEM_LOAD_TIME_MILLIS - (System.currentTimeMillis() - startMillis)
            if (millisToWait > 0) delay(millisToWait.milliseconds)
        }
    }

    private fun updateItem(info: UnytInfo, onDone: (() -> Unit)? = null) {
        lifecycleScope.launch {
            val newData = tasks.getItemData(info.unyt, info.isMyWordsUnyt)

            _list.update { oldList ->
                oldList.map { item ->
                    if (item == info) {
                        // Replace the existing UnytInfo to cause the necessary re-rendering
                        Log.d(TAG, "Updating item of unyt ${info.unyt.id}")
                        UnytInfo(unyt = info.unyt, isMyWordsUnyt = info.isMyWordsUnyt, data = newData)
                    } else {
                        item
                    }
                }
            }

            onDone?.invoke()
        }
    }

    private suspend fun updateItem(info: UnytInfo) =
        suspendCancellableCoroutine { cont ->
            updateItem(info) { cont.resume(Unit) }
        }

    fun updateItemOfUnyt(unyt: Unyt, onDone: (() -> Unit)? = null) {
        val info = _list.value.find { it is UnytInfo && it.unyt == unyt } as? UnytInfo

        if (info == null) {
            Log.w(TAG, "Unyt ${unyt.id} not found in list, onDone not called!")
            return
        }

        updateItem(info, onDone)
    }

    fun fakeGoodStats(unyt: Unyt) = fakeStats(unyt, 5, 1)
    fun fakeAvgStats(unyt: Unyt) = fakeStats(unyt, 4, 2)
    fun fakePoorStats(unyt: Unyt) = fakeStats(unyt, 3, 20)

    private fun fakeStats(unyt: Unyt, numCorrect: Int, numWrong: Int) {
        lifecycleScope.launch {
            tasks.fakeStats(unyt, numCorrect, numWrong)
            updateItemOfUnyt(unyt)
        }
    }

    fun resetStats(unyt: Unyt) {
        lifecycleScope.launch {
            tasks.resetStats(unyt)
            updateItemOfUnyt(unyt)
        }
    }

    fun setSuperProgIdx(unyt: Unyt) {
        tasks.setSuperProgIdx(unyt)
    }

    companion object {
        private const val TAG = "TopicActvModel"
        private const val MIN_ITEM_LOAD_TIME_MILLIS = 10L
    }
}
