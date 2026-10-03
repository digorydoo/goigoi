package io.github.digorydoo.goigoi.core.study

import ch.digorydoo.kutils.cjk.toHiragana
import ch.digorydoo.kutils.cjk.toNormalSizedKana
import io.github.digorydoo.goigoi.core.db.WordHint
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer

class AnswerChecker {
    fun check(qa: QuestionAndAnswer, answerEntered: String): Answer {
        val correctness = when {
            qa.kind.doesNotAskAnything -> Answer.TRIVIAL
            qa.answers.any { it == answerEntered } -> Answer.CORRECT
            else -> Answer.WRONG
        }

        if (correctness != Answer.WRONG) {
            return correctness
        }

        // FIXME should have return value CORRECT_EXCEPT_DAKUTEN

        return when {
            isCorrectExceptWrongKanaSize(qa, answerEntered) -> Answer.CORRECT_EXCEPT_KANA_SIZE
            isCorrectExceptDifferentKindOfKana(qa, answerEntered) -> Answer.CORRECT
            isCorrectExceptUnexpectedSuru(qa, answerEntered) -> Answer.CORRECT
            isCorrectExceptMissingExpectedSuru(qa, answerEntered) -> Answer.CORRECT
            isCorrectExceptMissingSpecialChars(qa, answerEntered) -> Answer.CORRECT
            else -> Answer.WRONG
        }
    }

    /**
     * Checks if the user just confused small kana with normal-sized kana.
     */
    private fun isCorrectExceptWrongKanaSize(qa: QuestionAndAnswer, answerEntered: String): Boolean =
        qa.answers.any { it.toNormalSizedKana() == answerEntered.toNormalSizedKana() }

    /**
     * Checks whether the user just used different kinds of kana. This is only possible if kana was presented as
     * FixedKeys with an overlap of their reading. It is important we allow this at least for hiragana へ and katakana
     * ヘ, because they're visually indistinguishable.
     */
    private fun isCorrectExceptDifferentKindOfKana(qa: QuestionAndAnswer, answerEntered: String) =
        qa.answers.any { it.toHiragana() == answerEntered.toHiragana() }

    /**
     * Checks whether the user added a suru to a suru noun, e.g. when we asked for 掃除, and the user entered 掃除する
     */
    private fun isCorrectExceptUnexpectedSuru(qa: QuestionAndAnswer, answerEntered: String) =
        if (qa.word.hint2 != WordHint.NOUN_SURU && !qa.word.hint.en.lowercase().contains("suru")) {
            false // no tolerance without these criteria
        } else {
            suruSuffix.any { suru ->
                answerEntered.endsWith(suru) && qa.answers.any { it == answerEntered.dropLast(suru.length) }
            }
        }

    /**
     * Checks whether the user forgot the suru suffix, e.g. when we asked for 掃除する, and the user entered 掃除
     */
    private fun isCorrectExceptMissingExpectedSuru(qa: QuestionAndAnswer, answerEntered: String) =
        if (!qa.kind.involvesPhrasesOrSentences) {
            false // no tolerance without this criterion
        } else {
            suruSuffix.any { suru ->
                qa.answers.any {
                    it.endsWith(suru) && it.length > suru.length && it == "$answerEntered$suru"
                }
            }
        }

    /**
     * Checks whether some special characters appearing in the answer were omitted. In general, we should avoid asking
     * phrases and sentence containing these characters, though.
     */
    private fun isCorrectExceptMissingSpecialChars(qa: QuestionAndAnswer, answerEntered: String) =
        qa.answers.any { ans ->
            val a = specialChars.fold(ans) { result, c -> result.replace(c, "") }
            val b = specialChars.fold(answerEntered) { result, c -> result.replace(c, "") }
            return@any a == b
        }

    companion object {
        private val suruSuffix = arrayOf("する", "をする")
        private val specialChars = arrayOf("~", "〜", "～", "〰︎", "。")
    }
}
