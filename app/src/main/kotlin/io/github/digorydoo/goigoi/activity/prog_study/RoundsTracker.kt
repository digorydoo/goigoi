package io.github.digorydoo.goigoi.activity.prog_study

import io.github.digorydoo.goigoi.db.Word

class RoundsTracker {
    var round = 1; private set
    var lastTrivial: Int? = null; private set
    private val map = mutableMapOf<String, Int>()

    fun of(word: Word, kind: QAKind): Int? =
        map.getOrElse(key(word, kind)) { null }

    fun since(r: Int?) = when (r) {
        null -> 10000
        else -> round - r
    }

    fun aboutToShow(word: Word, kind: QAKind) {
        if (kind.doesNotAskAnything) {
            lastTrivial = round
        }
        map[key(word, kind)] = round
        round++
    }

    private fun key(word: Word, kind: QAKind) =
        "${word.id}/${kind.intValue}"

    fun saveState(outState: ProgStudyState) {
        outState.round = round
        outState.lastTrivial = lastTrivial
        outState.roundsMap.clear()
        outState.roundsMap.putAll(map)
    }

    fun restoreState(state: ProgStudyState) {
        round = state.round
        lastTrivial = state.lastTrivial
        map.clear()
        map.putAll(state.roundsMap)
    }
}
