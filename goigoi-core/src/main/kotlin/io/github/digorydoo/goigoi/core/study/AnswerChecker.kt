package io.github.digorydoo.goigoi.core.study

import ch.digorydoo.kutils.cjk.toHiragana
import ch.digorydoo.kutils.cjk.toNormalSizedKana
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer

class AnswerChecker {
    private enum class CheckResult { YES_ACCURATELY, YES_INACCURATELY, NO }

    fun check(qa: QuestionAndAnswer, answerEntered: String): Answer {
        val correctness = when {
            qa.kind.doesNotAskAnything -> Answer.TRIVIAL
            qa.expectedAnswer == answerEntered -> Answer.CORRECT
            qa.acceptableAnswers.any { it.answer == answerEntered && !it.inaccurate } -> Answer.CORRECT
            qa.acceptableAnswers.any { it.answer == answerEntered && it.inaccurate } -> Answer.CORRECT_BUT_INACCURATE
            else -> Answer.WRONG
        }

        if (correctness != Answer.WRONG) {
            return correctness
        }

        when (isCorrectExceptWrongKanaSize(qa, answerEntered)) {
            CheckResult.YES_ACCURATELY -> return Answer.CORRECT_EXCEPT_KANA_SIZE
            CheckResult.YES_INACCURATELY -> return Answer.CORRECT_EXCEPT_KANA_SIZE
            CheckResult.NO -> Unit
        }

        when (isCorrectExceptDifferentKindOfKana(qa, answerEntered)) {
            CheckResult.YES_ACCURATELY -> return Answer.CORRECT
            CheckResult.YES_INACCURATELY -> return Answer.CORRECT_BUT_INACCURATE
            CheckResult.NO -> Unit
        }

        // TODO also check whether user just forgot to type the dakuten/handakuten

        when (isCorrectExceptSuru(qa, answerEntered)) {
            CheckResult.YES_ACCURATELY -> return Answer.CORRECT
            CheckResult.YES_INACCURATELY -> return Answer.CORRECT_BUT_INACCURATE
            CheckResult.NO -> Unit
        }

        return when (isCorrectExceptMissingSpecialChars(qa, answerEntered)) {
            CheckResult.YES_ACCURATELY -> Answer.CORRECT
            CheckResult.YES_INACCURATELY -> Answer.CORRECT_BUT_INACCURATE
            CheckResult.NO -> Answer.WRONG
        }
    }

    /**
     * Checks if the user just confused small kana with normal-sized kana.
     */
    private fun isCorrectExceptWrongKanaSize(qa: QuestionAndAnswer, answerEntered: String): CheckResult {
        val a = answerEntered.toNormalSizedKana()
        return checkAnswers(qa) { it.toNormalSizedKana() == a }
    }

    /**
     * Checks whether the user just used different kinds of kana. This is only possible if kana was presented as
     * FixedKeys with an overlap of their reading. It is important we allow this at least for hiragana へ and katakana
     * ヘ, because they're visually indistinguishable.
     */
    private fun isCorrectExceptDifferentKindOfKana(qa: QuestionAndAnswer, answerEntered: String): CheckResult {
        val a = answerEntered.toHiragana()
        return checkAnswers(qa) { it.toHiragana() == a }
    }

    /**
     * Checks whether the answer the user is entered is acceptable even though they added or forgot to write suru.
     */
    private fun isCorrectExceptSuru(qa: QuestionAndAnswer, answerEntered: String): CheckResult {
        // Check whether the user just forgot to write suru,
        // e.g. when we asked for 掃除する, and they entered 掃除

        val result = checkAnswers(qa) { a ->
            suruSuffix.any { suru ->
                a.endsWith(suru) && a.length > suru.length && a == "$answerEntered$suru"
            }
        }

        if (result != CheckResult.NO) {
            return result
        }

        if (qa.word.isSuruWord()) {
            // If phrases or sentences are involved, the word may not appear in dictionary form, and therefore the
            // following checks cannot be used. The variants need to be declared explicitly per phrase and sentence
            // and will appear as separate entries in qa.acceptableAnswers.

            if (!qa.kind.involvesPhrasesOrSentences) {
                val suruVariantAnswerEndsWith = suruSuffix.firstOrNull { answerEntered.endsWith(it) }

                if (suruVariantAnswerEndsWith != null) {
                    // Check whether the user added suru to the suru noun,
                    // e.g. when we asked for 掃除, and they entered 掃除する

                    val answerWithoutSuru = answerEntered.dropLast(suruVariantAnswerEndsWith.length)
                    var result = checkAnswers(qa) { it == answerWithoutSuru }

                    if (result != CheckResult.NO) {
                        return result
                    }

                    // Check whether the user used suru differently,
                    // e.g. when we asked for 掃除する, and they entered 掃除をする

                    result = checkAnswers(qa) { a ->
                        suruSuffix.any { suru ->
                            a.endsWith(suru) && a.length > suru.length && a == "$answerWithoutSuru$suru"
                        }
                    }

                    if (result != CheckResult.NO) {
                        return CheckResult.YES_INACCURATELY // we never treat this as accurate
                    }
                }
            }
        }

        return CheckResult.NO
    }

    /**
     * Checks whether some special characters appearing in the answer were omitted. In general, we should avoid asking
     * phrases and sentence containing these characters, though.
     */
    private fun isCorrectExceptMissingSpecialChars(qa: QuestionAndAnswer, answerEntered: String): CheckResult {
        val a = answerEntered.removeSpecialChars()
        return checkAnswers(qa) { it.removeSpecialChars() == a }
    }

    /**
     * Checks the expectedAnswer as well as all acceptableAnswers against the given predicate.
     */
    private fun checkAnswers(qa: QuestionAndAnswer, predicate: (String) -> Boolean): CheckResult {
        if (predicate(qa.expectedAnswer)) {
            return CheckResult.YES_ACCURATELY
        }

        val candidates = qa.acceptableAnswers.filter { predicate(it.answer) }

        return when {
            candidates.any { !it.inaccurate } -> CheckResult.YES_ACCURATELY
            candidates.isNotEmpty() -> CheckResult.YES_INACCURATELY
            else -> CheckResult.NO
        }
    }

    companion object {
        private val suruSuffix = arrayOf("をする", "する") // longest first!
        private val specialChars = arrayOf("~", "〜", "～", "〰︎", "。")

        private fun String.removeSpecialChars() =
            specialChars.fold(this) { result, c -> result.replace(c, "") }
    }
}
