package io.github.digorydoo.goigoi.core.study

import kotlin.random.Random

class AnswerCommentator {
    fun getComment(answer: Answer, streak: Int): String {
        if (answer == Answer.CORRECT && streak >= MIN_STREAK_FOR_DISPLAY) {
            val streakAsChar = if (streak <= 9) '０' + streak else "$streak"
            return "${streakAsChar}連！"
        }

        val comments = when (answer) {
            Answer.CORRECT -> responsesIfCorrect
            Answer.CORRECT_EXCEPT_KANA_SIZE -> responseIfAlmostCorrect
            Answer.WRONG -> responsesIfWrong
            else -> null
        }

        return comments?.let { it[Random.nextInt(it.size)] } ?: ""
    }

    companion object {
        private const val MIN_STREAK_FOR_DISPLAY = 3

        private val responsesIfCorrect = arrayOf(
            "正解！", // せいかい
            "その通り！", // そのとおり
            "合ってる！", // あってる
            "そう！"
        )
        private val responsesIfWrong = arrayOf(
            "不正解！", // ふせいかい
            "違う！", // ちがう
            "残念！", // ざんねん
            "ハズレ！"
        )
        private val responseIfAlmostCorrect = arrayOf(
            "惜しい！", // おしい
            "おっと！"
        )
    }
}
