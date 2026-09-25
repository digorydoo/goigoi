package io.github.digorydoo.goigoi.activity.prog_study.utils

import android.os.Bundle
import android.util.Log
import io.github.digorydoo.goigoi.core.prog_study.QAKind
import io.github.digorydoo.goigoi.core.prog_study.RoundsTracker
import io.github.digorydoo.goigoi.core.study.Answer
import io.github.digorydoo.goigoi.utils.getIntOrNull
import io.github.digorydoo.goigoi.utils.getSerializableOrNull

class ProgStudyState private constructor(
    var qaKind: QAKind,
    var qaIndex: Int,
    var showFuriganaWithQuestion: Boolean,
    var round: Int,
    var lastTrivial: Int?,
    val roundsMap: HashMap<String, Int>,
    var answer: Answer,
    var studyItemIteratorState: StudyItemIteratorState,
) {
    constructor(): this(
        qaKind = QAKind.SHOW_KANJI_ASK_KANA,
        qaIndex = 0,
        showFuriganaWithQuestion = false,
        round = 0,
        lastTrivial = null,
        roundsMap = HashMap(),
        answer = Answer.NONE,
        studyItemIteratorState = StudyItemIteratorState(),
    )

    fun writeTo(bundle: Bundle) {
        bundle.putInt(QAKIND_STATEID, qaKind.intValue)
        bundle.putInt(QAINDEX_STATEID, qaIndex)
        bundle.putBoolean(SHOW_FURIGANA_WITH_QUESTION_STATEID, showFuriganaWithQuestion)
        bundle.putInt(ROUND_STATEID, round)
        lastTrivial?.let { bundle.putInt(LAST_TRIVIAL_STATEID, it) }
        bundle.putSerializable(ROUNDS_MAP_STATEID, roundsMap)
        bundle.putInt(ANSWER_STATEID, answer.intValue)
        studyItemIteratorState.writeTo(bundle)
    }

    companion object {
        private const val TAG = "ProgStudyState"
        private const val QAKIND_STATEID = "${TAG}.qaKind"
        private const val QAINDEX_STATEID = "${TAG}.qaIdx"
        private const val SHOW_FURIGANA_WITH_QUESTION_STATEID = "${TAG}.qFuri"
        private const val ROUND_STATEID = "${TAG}.round"
        private const val LAST_TRIVIAL_STATEID = "${TAG}.lastTrivial"
        private const val ROUNDS_MAP_STATEID = "${TAG}.roundsMap"
        private const val ANSWER_STATEID = "${TAG}.answer"

        fun from(bundle: Bundle): ProgStudyState? {
            val qaKind = bundle.getIntOrNull(QAKIND_STATEID)?.let { QAKind.fromIntOrNull(it) }
            val qaIndex = bundle.getIntOrNull(QAINDEX_STATEID)
            val showFuriganaWithQuestion = bundle.getBoolean(SHOW_FURIGANA_WITH_QUESTION_STATEID, false)
            val round = bundle.getIntOrNull(ROUND_STATEID)
            val lastTrivial = bundle.getIntOrNull(LAST_TRIVIAL_STATEID)
            val roundsMap = bundle.getSerializableOrNull<HashMap<String, Int>>(ROUNDS_MAP_STATEID)
            val answer = bundle.getIntOrNull(ANSWER_STATEID)?.let { Answer.fromIntOrNull(it) }
            val state = StudyItemIteratorState.from(bundle)

            if (qaKind == null
                || qaIndex == null
                || round == null
                // lastTrivial may be null
                || roundsMap == null
                || answer == null
                || state == null
            ) {
                Log.w(TAG, "Could not restore state from bundle since some values are missing!")
                return null
            } else {
                Log.d(TAG, "Restored qaKind=$qaKind, qaIndex=$qaIndex")
                Log.d(TAG, "   showFuriganaWithQuestion=$showFuriganaWithQuestion")
                Log.d(TAG, "   answer=$answer, round=$round, lastTrivial=$lastTrivial, roundsMap=$roundsMap")
                return ProgStudyState(
                    qaKind = qaKind,
                    qaIndex = qaIndex,
                    showFuriganaWithQuestion = showFuriganaWithQuestion,
                    round = round,
                    lastTrivial = lastTrivial,
                    roundsMap = roundsMap,
                    answer = answer,
                    studyItemIteratorState = state
                )
            }
        }
    }
}

fun RoundsTracker.saveState(outState: ProgStudyState) {
    outState.round = round
    outState.lastTrivial = lastTrivial
    outState.roundsMap.clear()
    outState.roundsMap.putAll(map)
}

fun RoundsTracker.restoreState(state: ProgStudyState) {
    round = state.round
    lastTrivial = state.lastTrivial
    map.clear()
    map.putAll(state.roundsMap)
}
