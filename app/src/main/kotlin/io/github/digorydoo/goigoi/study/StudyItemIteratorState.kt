package io.github.digorydoo.goigoi.study

import android.os.Bundle
import android.util.Log
import io.github.digorydoo.goigoi.utils.getIntOrNull
import io.github.digorydoo.goigoi.utils.getStringOrNull

class StudyItemIteratorState(
    var index: Int = 0,
    var curWordId: String = "",
    var numCorrect: Int = 0,
    var numWrong: Int = 0,
) {
    fun writeTo(bundle: Bundle) {
        bundle.putInt(INDEX_STATEID, index)
        bundle.putString(CUR_WORD_ID_STATEID, curWordId)
        bundle.putInt(NUM_CORRECT_STATEID, numCorrect)
        bundle.putInt(NUM_WRONG_STATEID, numWrong)
    }

    companion object {
        private const val TAG = "StudyItemIteratorState"
        private const val INDEX_STATEID = "$TAG.index"
        private const val CUR_WORD_ID_STATEID = "$TAG.curWordId"
        private const val NUM_CORRECT_STATEID = "$TAG.numCorrect"
        private const val NUM_WRONG_STATEID = "$TAG.numWrong"

        fun from(bundle: Bundle): StudyItemIteratorState? {
            val index = bundle.getIntOrNull(INDEX_STATEID)
            val curWordId = bundle.getStringOrNull(CUR_WORD_ID_STATEID)
            val numCorrect = bundle.getIntOrNull(NUM_CORRECT_STATEID)
            val numWrong = bundle.getIntOrNull(NUM_WRONG_STATEID)

            if (index == null || curWordId == null || numCorrect == null || numWrong == null) {
                Log.w(TAG, "Could not restore state from bundle since some values are missing!")
                return null
            } else {
                Log.d(TAG, "Restored: index=$index, curWordId=$curWordId, numCorrect=$numCorrect, numWrong=$numWrong")
                return StudyItemIteratorState(
                    index = index,
                    curWordId = curWordId,
                    numCorrect = numCorrect,
                    numWrong = numWrong
                )
            }
        }
    }
}
