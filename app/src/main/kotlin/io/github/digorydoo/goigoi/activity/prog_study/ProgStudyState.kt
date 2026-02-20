package io.github.digorydoo.goigoi.activity.prog_study

import android.os.Bundle
import android.util.Log
import io.github.digorydoo.goigoi.study.Answer
import io.github.digorydoo.goigoi.study.StudyItemIteratorState
import io.github.digorydoo.goigoi.utils.getIntOrNull
import io.github.digorydoo.goigoi.utils.getSerializableOrNull

class ProgStudyState private constructor(
    var qaKind: QAKind,
    var qaIndex: Int,
    var questionHasFurigana: Boolean,
    var round: Int,
    var lastTrivial: Int?,
    val roundsMap: HashMap<String, Int>,
    var answer: Answer,
    var studyItemIteratorState: StudyItemIteratorState,
) {
    constructor(): this(
        qaKind = QAKind.SHOW_KANJI_ASK_KANA,
        qaIndex = 0,
        questionHasFurigana = false,
        round = 0,
        lastTrivial = null,
        roundsMap = HashMap(),
        answer = Answer.NONE,
        studyItemIteratorState = StudyItemIteratorState(),
    )

    fun writeTo(bundle: Bundle) {
        bundle.putInt(QAKIND_STATEID, qaKind.intValue)
        bundle.putInt(QAINDEX_STATEID, qaIndex)
        bundle.putBoolean(QUESTION_HAS_FURIGANA_STATEID, questionHasFurigana)
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
        private const val QUESTION_HAS_FURIGANA_STATEID = "${TAG}.qFuri"
        private const val ROUND_STATEID = "${TAG}.round"
        private const val LAST_TRIVIAL_STATEID = "${TAG}.lastTrivial"
        private const val ROUNDS_MAP_STATEID = "${TAG}.roundsMap"
        private const val ANSWER_STATEID = "${TAG}.answer"

        fun from(bundle: Bundle): ProgStudyState? {
            val qaKind = bundle.getIntOrNull(QAKIND_STATEID)?.let { QAKind.fromIntOrNull(it) }
            val qaIndex = bundle.getIntOrNull(QAINDEX_STATEID)
            val questionHasFurigana = bundle.getBoolean(QUESTION_HAS_FURIGANA_STATEID, false)
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
                Log.d(TAG, "Restored qaKind=$qaKind, qaIndex=$qaIndex, questionHasFurigana=$questionHasFurigana")
                Log.d(TAG, "   answer=$answer, round=$round, lastTrivial=$lastTrivial")
                Log.d(TAG, "   roundsMap=$roundsMap")
                return ProgStudyState(
                    qaKind = qaKind,
                    qaIndex = qaIndex,
                    questionHasFurigana = questionHasFurigana,
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
