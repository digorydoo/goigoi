package io.github.digorydoo.goigoi.core.prog_study

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.db.AcceptableAnswer
import io.github.digorydoo.goigoi.core.db.Word

// This class is immutable
class QuestionAndAnswer(
    val word: Word,
    val kind: QAKind, // defines the way how we're going to present this question
    val index: Int, // index of phrase or sentence if kind.involvesPhrasesOrSentences is true
    val showFuriganaWithQuestion: Boolean, // false = hide the furigana initially
    val question: OneOf<String, FuriganaString>,
    val questionAfterReveal: OneOf<String, FuriganaString>?, // null = no change
    val questionHint: OneOf<String, Hint>, // this hint is shown along with the question
    val expectedAnswer: String,
    val acceptableAnswers: List<AcceptableAnswer>,
    val kanjiOrKanaToReveal: String,
    val translationToReveal: String,
    val hintToReveal: String, // this hint is shown after the answer has been revealed
    val explanation: FuriganaString?, // the explanation is always shown only after the answer has been revealed
    val presentWholeWords: Boolean, // true = multiple choice style, false = allow typing individual characters
    val allowExtendedKeyboard: Boolean, // true = full kana keyboard, false = show only a limited number of keys
) {
    enum class Hint { PHRASE }
    data class Prefill(val prefix: String, val suffix: String)

    private val questionWithoutFurigana = when (question) {
        is OneOf.First -> question.first
        is OneOf.Second -> question.second.kanji
    }

    /**
     * If the word has a kana prefix, it may be an honorific prefix. Since the word without honorific prefix would also
     * be correct, but possibly not covered by acceptableAnswers, we need to pre-fill the prefix to make it non-
     * ambigous. On the other hand, if we're showing kana or kanji and are asking kanji or kana, and if there is a kana
     * prefix and/or suffix, we will also pre-fill the prefix or suffix, because the user would simply have to copy the
     * same kana in the answer, which is pointless.
     */
    val answerPrefill: Prefill? = when {
        presentWholeWords -> null
        kind.doesNotAskAnything -> null
        kind == QAKind.SHOW_ROMAJI_ASK_KANA -> null
        word.kanji == word.kana -> null
        else -> {
            val prefix = word.kanaPrefix
            val suffix = word.kanaSuffix
            val answerHasPrefix = expectedAnswer.startsWith(prefix)
            val answerHasSuffix = expectedAnswer.endsWith(suffix)
            val questionHasSuffix = questionWithoutFurigana.endsWith(suffix)
            Prefill(
                prefix = if (answerHasPrefix) prefix else "",
                suffix = if (answerHasSuffix && questionHasSuffix) suffix else "",
            )
        }
    }
}
