package io.github.digorydoo.goigoi.core.prog_study

import io.github.digorydoo.goigoi.core.db.Word

class RoundsTracker {
    var round = 1
    var lastTrivial: Int? = null
    val map = mutableMapOf<String, Int>()

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
}
