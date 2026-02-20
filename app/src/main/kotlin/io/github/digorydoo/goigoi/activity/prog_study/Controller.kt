package io.github.digorydoo.goigoi.activity.prog_study

import android.util.Log
import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.cjk.toNormalSizedKana
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.activity.prog_study.choreo.Choreographer
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.FixedKeysProvider
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard
import io.github.digorydoo.goigoi.core.WordHint
import io.github.digorydoo.goigoi.db.KanjiIndex
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.furigana.FuriganaSpan
import io.github.digorydoo.goigoi.furigana.buildSpan
import io.github.digorydoo.goigoi.stats.Stats
import io.github.digorydoo.goigoi.study.Answer
import kotlin.math.ceil
import kotlin.math.max
import kotlin.random.Random

class Controller(
    private val delegate: Delegate,
    private val bindings: Bindings,
    private val choreo: Choreographer,
    private val kanjiIndex: KanjiIndex,
    private val keyboard: Keyboard,
    private val qaProvider: QAProvider,
    private val stats: Stats,
    private val unyt: Unyt?, // null = super progressive mode
    private val vocab: Vocabulary,
) {
    interface Delegate {
        fun isInTestLab(): Boolean
        fun showKeyboardHintIfAppropriate(qa: QuestionAndAnswer, mode: Keyboard.Mode)
    }

    var answer = Answer.NONE

    fun nextBtnClicked() {
        if (shouldShowExplanation()) {
            showExplanation()
            return
        }

        qaProvider.next()
        answer = Answer.NONE
        updateContent()
        bindings.nextBtn.hide()
    }

    private fun shouldShowExplanation(): Boolean {
        val qa = qaProvider.qa
        val word = qa.word
        val kind = qa.kind
        return when {
            answer == Answer.NONE -> false // we've just shown the explanation
            qa.explanation.isEmpty() -> false
            kind.doesNotAskAnything -> false // no room when using calligraphy font!

            // Note: The seen counts for this word and kind have already been incremented!
            kind.involvesPhrases -> phraseTotalSeenCount(word, qa.index) == 2
            kind.involvesSentences -> sentenceTotalSeenCount(word, qa.index) == 2
            else -> stats.getWordTotalSeenCount(word) == 2
        }
    }

    private fun phraseTotalSeenCount(word: Word, phraseIdx: Int): Int {
        require(phraseIdx in word.phrases.indices)

        fun seenCount(kind: QAKind) = stats.getWordSeenCount(word, kind.toStatsKey())
        val seenAskNothing = seenCount(QAKind.SHOW_PHRASE_ASK_NOTHING)
        val seenAskKanji = seenCount(QAKind.SHOW_PHRASE_ASK_KANJI)
        val seenTranslationAskKana = seenCount(QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA)

        val numPhrasesWithWordRemoved = word.phrases.filter { it.canRemoveWordFromPrimaryForm(word) }.size

        val result = (ceil(max(0, seenAskNothing - phraseIdx) / word.phrases.size.toFloat()) +
            ceil(max(0, seenAskKanji - phraseIdx) / numPhrasesWithWordRemoved.toFloat()) +
            ceil(max(0, seenTranslationAskKana - phraseIdx) / word.phrases.size.toFloat())).toInt()

        Log.d(TAG, "Phrase #$phraseIdx seen $result times")
        return result
    }

    private fun sentenceTotalSeenCount(word: Word, sentenceIdx: Int): Int {
        require(sentenceIdx in word.sentences.indices)

        fun seenCount(kind: QAKind) = stats.getWordSeenCount(word, kind.toStatsKey())
        val seenAskNothing = seenCount(QAKind.SHOW_SENTENCE_ASK_NOTHING)
        val seenAskKanji = seenCount(QAKind.SHOW_SENTENCE_ASK_KANJI)

        val numSentencesWithWordRemoved = word.sentences.filter { it.canRemoveWordFromPrimaryForm(word) }.size

        val result = (ceil(max(0, seenAskNothing - sentenceIdx) / word.phrases.size.toFloat()) +
            ceil(max(0, seenAskKanji - sentenceIdx) / numSentencesWithWordRemoved.toFloat())).toInt()

        Log.d(TAG, "Sentence #$sentenceIdx seen $result times")
        return result
    }

    private fun showExplanation() {
        answer = Answer.NONE
        choreo.showExplanation(qaProvider.qa.explanation)
        bindings.nextBtn.show()
    }

    fun updateContent() {
        val qa = qaProvider.qa

        val allowExtendedKeyboard = when {
            delegate.isInTestLab() -> Random.nextFloat() > 0.5f // random when in Google Play Test Lab
            qa.presentWholeWords -> false // whole words need FIXED_KEYS
            qa.word.level == JLPTLevel.N5 -> false // N5 learners have rōmaji instead
            qa.kind == QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> false // answers would get too ambiguous
            stats.getWordTotalSeenCount(qa.word) < 5 -> false // FIXED_KEYS is easier
            stats.getWordTotalRating(qa.word) < 0.75f -> false
            else -> true
        }

        val primaryAnswer = qa.answers.firstOrNull() ?: ""

        when {
            primaryAnswer.isEmpty() -> {
                // This is one of the QAKinds that just reveal something without asking anything.
                keyboard.setMode(Keyboard.Mode.JUST_REVEAL)
            }
            allowExtendedKeyboard && keyboard.supportsCharsInMode(primaryAnswer, Keyboard.Mode.HIRAGANA) -> {
                keyboard.setMode(Keyboard.Mode.HIRAGANA)
            }
            allowExtendedKeyboard && keyboard.supportsCharsInMode(primaryAnswer, Keyboard.Mode.KATAKANA) -> {
                keyboard.setMode(Keyboard.Mode.KATAKANA)
            }
            else -> {
                val unytToTakeWordsFrom = unyt ?: vocab.myWordsUnyt
                keyboard.setMode(
                    mode = Keyboard.Mode.FIXED_KEYS,
                    keys = FixedKeysProvider(qa, unytToTakeWordsFrom, kanjiIndex).get(),
                    backspaceClearsAllText = qa.presentWholeWords,
                )
            }
        }

        choreo.setQuestionAndAnswer(qa)
        delegate.showKeyboardHintIfAppropriate(qa, keyboard.mode)
    }

    fun checkAndRevealAnswer() {
        val qa = qaProvider.qa
        val answerEntered = bindings.inputTextView.text.toString()

        answer = when {
            qa.kind.doesNotAskAnything -> Answer.TRIVIAL
            qa.answers.any { it == answerEntered } -> Answer.CORRECT
            else -> Answer.WRONG
        }

        if (answer == Answer.WRONG) {
            if (isCorrectExceptWrongKanaSize(answerEntered)) {
                // The user just confused small kana with normal-sized kana.
                answer = Answer.CORRECT_EXCEPT_KANA_SIZE
            } else if (isCorrectExceptUnexpectedSuru(answerEntered)) {
                // We allow this, e.g. when we asked for 掃除, and the user entered 掃除する
                answer = Answer.CORRECT
            } else if (isCorrectExceptMissingExpectedSuru(answerEntered)) {
                // We allow this, e.g. when we asked for 掃除する, and the user entered 掃除
                answer = Answer.CORRECT
            }
        }

        val text = qa.questionWithGapFilled.let {
            when (it) {
                is OneOf.First -> it.first
                is OneOf.Second -> it.second.buildSpan(FuriganaSpan.Options(relVOffset = qa.furiganaRelVOffset))
            }
        }

        choreo.revealAnswer(answer, text)
        qaProvider.notifyAnswer(answer)
        bindings.nextBtn.show()
    }

    private fun isCorrectExceptWrongKanaSize(answerEntered: String): Boolean =
        qaProvider.qa.answers.any { it.toNormalSizedKana() == answerEntered.toNormalSizedKana() }

    private fun isCorrectExceptUnexpectedSuru(answerEntered: String): Boolean {
        val qa = qaProvider.qa
        return if (qa.word.hint2 != WordHint.NOUN_SURU && !qa.word.hint.en.lowercase().contains("suru")) {
            false // no tolerance without these criteria
        } else {
            suruSuffix.any { suru ->
                answerEntered.endsWith(suru) && qa.answers.any { it == answerEntered.dropLast(suru.length) }
            }
        }
    }

    private fun isCorrectExceptMissingExpectedSuru(answerEntered: String): Boolean {
        val qa = qaProvider.qa
        return if (!qa.kind.involvesPhrasesOrSentences) {
            false // no tolerance without this criterion
        } else {
            suruSuffix.any { suru ->
                qa.answers.any {
                    it.endsWith(suru) && it.length > suru.length && it == "$answerEntered$suru"
                }
            }
        }
    }

    companion object {
        private const val TAG = "Controller"
        private val suruSuffix = arrayOf("する", "をする")
    }
}
