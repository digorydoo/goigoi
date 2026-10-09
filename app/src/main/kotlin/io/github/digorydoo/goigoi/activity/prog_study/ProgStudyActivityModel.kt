package io.github.digorydoo.goigoi.activity.prog_study

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.utils.Moment
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.prog_study.FixedKeysProvider
import io.github.digorydoo.goigoi.core.prog_study.KeyActionHandler
import io.github.digorydoo.goigoi.core.prog_study.KeyActionHandler.Action
import io.github.digorydoo.goigoi.core.prog_study.KeyActionHandler.TextAndCaret
import io.github.digorydoo.goigoi.core.prog_study.KeyDef
import io.github.digorydoo.goigoi.core.prog_study.QAKind
import io.github.digorydoo.goigoi.core.prog_study.QAProvider
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer.Hint
import io.github.digorydoo.goigoi.core.stats.Stats
import io.github.digorydoo.goigoi.core.study.Answer
import io.github.digorydoo.goigoi.core.study.AnswerChecker
import io.github.digorydoo.goigoi.core.study.AnswerCommentator
import io.github.digorydoo.goigoi.core.study.StudyItemIterator
import io.github.digorydoo.goigoi.core.welcome.DailyProgressTracker.Companion.STUDY_COUNT_OF_FULL_MARK
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

class ProgStudyActivityModel(
    private val qaProvider: QAProvider,
    private val studyItemIterator: StudyItemIterator,
    private val answerChecker: AnswerChecker,
    private val answerCommentator: AnswerCommentator,
    private val fixedKeysProvider: FixedKeysProvider,
    private val keyActionHandler: KeyActionHandler,
    private val stats: Stats,
    private val vocab: Vocabulary,
    private val lifecycleScope: LifecycleCoroutineScope,
) {
    enum class PresentationMode { BEFORE_QUESTION, QUESTION, ANSWER_CHECK, REVEAL_TEXTS, EXPLANATION, NOTHING }
    enum class KeyboardMode { TRIVIAL, FIXED_KEYS, HIRAGANA, KATAKANA }

    private var _presentationMode = MutableStateFlow(PresentationMode.NOTHING)
    val presentationMode = _presentationMode.asStateFlow()

    private var _word = MutableStateFlow(null as Word?)
    val word = _word.asStateFlow()

    private var _unytOfWord = MutableStateFlow(null as Unyt?)
    val unytOfWord = _unytOfWord.asStateFlow()

    private val _qaKind = MutableStateFlow(QAKind.SHOW_WORD_ASK_NOTHING)
    val qaKind = _qaKind.asStateFlow()

    private val _question = MutableStateFlow<OneOf<String, FuriganaString>>(OneOf.First(""))
    val question = _question.asStateFlow()

    private val _showFurigana = MutableStateFlow(false)
    val showFurigana = _showFurigana.asStateFlow()

    private val _questionAfterReveal = MutableStateFlow<OneOf<String, FuriganaString>>(OneOf.First(""))
    val questionAfterReveal = _questionAfterReveal.asStateFlow()

    private val _questionHint = MutableStateFlow<OneOf<String, Hint>>(OneOf.First(""))
    val questionHint = _questionHint.asStateFlow()

    private val _acceptableAnswers = MutableStateFlow(listOf<String>())
    val acceptableAnswers = _acceptableAnswers.asStateFlow()

    private val _currentAnswer = MutableStateFlow("")
    val currentAnswer = _currentAnswer.asStateFlow()

    private val _answerPrefill = MutableStateFlow("")
    val answerPrefill = _answerPrefill.asStateFlow()

    private val _answerCommentJa = MutableStateFlow(null as FuriganaString?)
    val answerCommentJa = _answerCommentJa.asStateFlow()

    private val _answerCommentTranslation = MutableStateFlow("")
    val answerCommentTranslation = _answerCommentTranslation.asStateFlow()

    private val _caretPos = MutableStateFlow(0)
    val caretPos = _caretPos.asStateFlow()

    private val _allowSetCaret = MutableStateFlow(false)
    val allowSetCaret = _allowSetCaret.asStateFlow()

    private val _answerCorrectness = MutableStateFlow(Answer.NONE)
    val answerCorrectness = _answerCorrectness.asStateFlow()

    private val _kanjiOrKanaToReveal = MutableStateFlow("")
    val kanjiOrKanaToReveal = _kanjiOrKanaToReveal.asStateFlow()

    private val _translationToReveal = MutableStateFlow("")
    val translationToReveal = _translationToReveal.asStateFlow()

    private val _hintToReveal = MutableStateFlow("")
    val hintToReveal = _hintToReveal.asStateFlow()

    private val _explanation = MutableStateFlow(null as FuriganaString?)
    val explanation = _explanation.asStateFlow()

    private val _keyboardMode = MutableStateFlow(KeyboardMode.HIRAGANA)
    val keyboardMode = _keyboardMode.asStateFlow()

    private val _fixedKeys = MutableStateFlow(listOf<String>())
    val fixedKeys = _fixedKeys.asStateFlow()

    private val _todaysProgress = MutableStateFlow(0f)
    val todaysProgress = _todaysProgress.asStateFlow()

    private val _numCorrect = MutableStateFlow(0)
    val numCorrect = _numCorrect.asStateFlow()

    private val _numWrong = MutableStateFlow(0)
    val numWrong = _numWrong.asStateFlow()

    fun showNextQuestion(initial: Boolean = false) {
        _presentationMode.update { PresentationMode.NOTHING }

        lifecycleScope.launch {
            delay(
                if (initial) INITIAL_DELAY_MILLIS.milliseconds
                else DELAY_BEFORE_NEXT_QUESTION_MILLIS.milliseconds
            )

            if (!initial) {
                vocab.writeMyWordsUnytIfNecessary()
                qaProvider.next()
            }

            val qa = qaProvider.qa
            val qaKind = qa.kind

            _word.update { qa.word }
            _unytOfWord.update { vocab.findFirstUnytContainingWordWithSameFile(qa.word) }
            _qaKind.update { qaKind }
            _question.update { qa.question }
            _showFurigana.update { qa.showFuriganaWithQuestion }
            _questionAfterReveal.update { qa.questionAfterReveal ?: qa.question }
            _questionHint.update { qa.questionHint }
            _acceptableAnswers.update { qa.answers }
            _answerCorrectness.update { if (initial) studyItemIterator.answer else Answer.NONE }
            _answerCommentJa.update { null }
            _answerCommentTranslation.update { "" }
            _kanjiOrKanaToReveal.update { qa.kanjiOrKanaToReveal }
            _translationToReveal.update { qa.translationToReveal }
            _hintToReveal.update { qa.hintToReveal }
            _explanation.update { qa.explanation }
            _allowSetCaret.update { !qa.presentWholeWords }

            val prefill = qa.answerPrefill

            if (prefill == null) {
                _answerPrefill.update { "" }
                _currentAnswer.update { "" }
                _caretPos.update { 0 }
            } else {
                _answerPrefill.update { prefill.prefix + prefill.suffix }
                _currentAnswer.update { prefill.prefix + prefill.suffix }
                _caretPos.update { prefill.prefix.length }
            }

            val primaryAnswer = qa.answers.firstOrNull() ?: ""

            val newKeyboardMode = when {
                qaKind.doesNotAskAnything -> KeyboardMode.TRIVIAL
                !qa.allowExtendedKeyboard -> KeyboardMode.FIXED_KEYS
                primaryAnswer.all { KeyDef.supportedHiraganaAndPunctuation.contains(it) } -> KeyboardMode.HIRAGANA
                primaryAnswer.all { KeyDef.supportedKatakanaAndPunctuation.contains(it) } -> KeyboardMode.KATAKANA
                else -> KeyboardMode.FIXED_KEYS
            }

            _keyboardMode.update { newKeyboardMode }

            _fixedKeys.update {
                if (newKeyboardMode == KeyboardMode.FIXED_KEYS) {
                    fixedKeysProvider.get(qa)
                } else {
                    listOf()
                }
            }

            val studyCount = stats.getUserStudyCountOfDay(Moment.now())
            _todaysProgress.update { min(1.0f, studyCount / STUDY_COUNT_OF_FULL_MARK) }
            _numCorrect.update { studyItemIterator.numCorrect }
            _numWrong.update { studyItemIterator.numWrong }

            // Wait until the hidden components have updated for the above changes
            delay(AWAIT_RENDER_DELAY_MILLIS.milliseconds)

            // Let the keyboard appear while the rest is still hidden. The keyboard determines the available space for
            // the rest, therefore its height must be known when the rest appears.
            _presentationMode.update { PresentationMode.BEFORE_QUESTION }

            // Wait until the change has been seen by the components
            delay(AWAIT_RENDER_DELAY_MILLIS.milliseconds)

            // Now show the rest
            _presentationMode.update {
                if (initial && answerCorrectness.value != Answer.NONE) PresentationMode.ANSWER_CHECK
                else PresentationMode.QUESTION
            }
        }
    }

    fun setCaretPos(index: Int) {
        _caretPos.value = clamp(index, 0, _currentAnswer.value.length)
    }

    fun onKeyBtnClicked(key: String, action: Action = Action.LITERAL) {
        val qa = qaProvider.qa

        if (action == Action.LITERAL && qa.presentWholeWords) {
            _currentAnswer.update { key }
            _caretPos.update { _currentAnswer.value.length }
        } else {
            val tac = object: TextAndCaret {
                override var text: CharSequence = _currentAnswer.value
                override var caretPos = _caretPos.value
            }

            keyActionHandler.handle(tac, action, key)
            _currentAnswer.update { tac.text.toString() }
            _caretPos.update { tac.caretPos }
        }
    }

    fun onBackspaceBtnClicked() {
        val qa = qaProvider.qa

        if (qa.presentWholeWords) {
            // Backspace clears all text
            _currentAnswer.update { "" }
            _caretPos.update { 0 }
        } else {
            val tac = object: TextAndCaret {
                override var text: CharSequence = _currentAnswer.value
                override var caretPos = _caretPos.value
            }
            keyActionHandler.handleBackspace(tac)
            _currentAnswer.update { tac.text.toString() }
            _caretPos.update { tac.caretPos }
        }
    }

    fun onEnterBtnClicked() {
        checkAndRevealAnswer()
    }

    fun onNextBtnClicked() {
        val pmode = _presentationMode.value
        when (pmode) {
            PresentationMode.QUESTION -> checkAndRevealAnswer()
            PresentationMode.ANSWER_CHECK -> showExplanationRevealTextsOrNextQuestion()
            PresentationMode.REVEAL_TEXTS, PresentationMode.EXPLANATION -> showNextQuestion()
            else -> Log.w(TAG, "Unexpected call to onNextBtnClicked while presentationMode=$pmode")
        }
    }

    private fun checkAndRevealAnswer() {
        val pmode = _presentationMode.value

        if (pmode != PresentationMode.QUESTION) {
            // This can happen if the user hits the enter button during BEFORE_QUESTION.
            Log.w(TAG, "Unexpected call to checkAndRevealAnswer while presentationMode=$pmode")
            return
        }

        val qa = qaProvider.qa
        val correctness = answerChecker.check(qa, _currentAnswer.value)
        qaProvider.notifyAnswer(correctness)

        _question.update { qa.questionAfterReveal ?: qa.question }
        _showFurigana.update { true }
        _presentationMode.update { PresentationMode.ANSWER_CHECK }
        _answerCorrectness.update { correctness }
        _numCorrect.update { studyItemIterator.numCorrect }
        _numWrong.update { studyItemIterator.numWrong }

        val comment = answerCommentator.getComment(correctness, studyItemIterator.streak)
        _answerCommentJa.update { comment?.ja }
        _answerCommentTranslation.update { comment?.translation ?: "" }
    }

    private fun showExplanationRevealTextsOrNextQuestion() {
        val qa = qaProvider.qa

        val shouldShowExplanation = when {
            _explanation.value == null -> false
            qa.kind.doesNotAskAnything -> false // no room when using calligraphy font!
            _answerCorrectness.value == Answer.CORRECT_EXCEPT_KANA_SIZE -> false // hide when simple spelling mistake
            _answerCorrectness.value == Answer.WRONG -> true // show explanation whenever answer is wrong
            qa.kind.involvesPhrases -> stats.phraseTotalSeenCount(qa.word, qa.index) < 2
            qa.kind.involvesSentences -> stats.sentenceTotalSeenCount(qa.word, qa.index) < 2
            else -> stats.getWordTotalSeenCount(qa.word) < 2
        }

        if (shouldShowExplanation) {
            _presentationMode.update { PresentationMode.EXPLANATION }
            return
        }

        val shouldRevealTexts = when {
            qa.kind.doesNotAskAnything -> false // we showed the revealed texts during ANSWER_CHECK here
            _answerCorrectness.value == Answer.CORRECT_EXCEPT_KANA_SIZE -> false // hide when simple spelling mistake
            else -> qa.kanjiOrKanaToReveal.isNotEmpty() ||
                qa.translationToReveal.isNotEmpty() ||
                qa.hintToReveal.isNotEmpty()
        }

        if (shouldRevealTexts) {
            _presentationMode.update { PresentationMode.REVEAL_TEXTS }
        } else {
            showNextQuestion()
        }
    }

    companion object {
        private const val TAG = "ProgStudyActvModel"
        private const val INITIAL_DELAY_MILLIS = 4 // just to avoid doing too much work in the initial frame
        private const val AWAIT_RENDER_DELAY_MILLIS = 100 // wait until AnimatedElements have been rendered
        const val DELAY_BEFORE_NEXT_QUESTION_MILLIS = 400 // also controls AnimatedElement's exit transition
    }
}
