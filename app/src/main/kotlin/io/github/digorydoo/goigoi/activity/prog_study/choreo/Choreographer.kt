package io.github.digorydoo.goigoi.activity.prog_study.choreo

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.core.view.isVisible
import ch.digorydoo.kutils.cjk.hasCJKIgnoringKana
import ch.digorydoo.kutils.cjk.hasCJKOrKana
import ch.digorydoo.kutils.cjk.isHiragana
import ch.digorydoo.kutils.cjk.isKana
import ch.digorydoo.kutils.cjk.isKatakana
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.activity.prog_study.Bindings
import io.github.digorydoo.goigoi.activity.prog_study.QAKind
import io.github.digorydoo.goigoi.activity.prog_study.QuestionAndAnswer
import io.github.digorydoo.goigoi.activity.prog_study.QuestionAndAnswer.FontType
import io.github.digorydoo.goigoi.activity.prog_study.Values
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard
import io.github.digorydoo.goigoi.furigana.FuriganaSpan
import io.github.digorydoo.goigoi.furigana.buildSpan
import io.github.digorydoo.goigoi.study.Answer
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ScreenSize
import kotlin.math.max
import kotlin.math.min

class Choreographer(
    private val delegate: Delegate,
    private val bindings: Bindings,
    private val values: Values,
) {
    interface Delegate {
        val iconWhenCorrect: Drawable?
        val iconWhenWrong: Drawable?
        val iconWhenAlmostCorrect: Drawable?
        val minTop: Float
        val screenSize: ScreenSize
        val screenOrientation: Orientation
        fun getWhatToDoHint(qa: QuestionAndAnswer): String
        fun getHint(hint: QuestionAndAnswer.Hint): String
        fun getAnswerComment(answer: Answer): String
    }

    enum class State { QUESTION, CORRECT, INCORRECT, EXPLANATION, GONE, READY_FOR_NEXT }

    private var state = State.READY_FOR_NEXT
    private var stateOfLastLayout = State.READY_FOR_NEXT
    private var qa: QuestionAndAnswer? = null
    private var kind = QAKind.SHOW_KANJI_ASK_KANA
    private var prevLayout = Layout()
    private val elements = ElementList(bindings, values)

    private val textAndCaret = object: Keyboard.TextAndCaret {
        override var text: CharSequence
            get() = bindings.inputTextView.text
            set(value) {
                bindings.inputTextView.text = value
            }
        override var caretPos: Int
            get() = bindings.inputTextView.caretPos
            set(value) {
                bindings.inputTextView.setCaretPos(value)
            }
    }

    fun canAcceptInput() =
        state == State.QUESTION

    fun updateLayoutIfNecessary() {
        val prevState = state

        if (state == State.READY_FOR_NEXT) {
            prevLayout = computeLayout()
            showNextQuestion()
        } else if (state != stateOfLastLayout) {
            val newLayout = computeLayout()
            Animator(bindings).start(prevLayout, newLayout, ::layoutAnimationFinished)
        }

        stateOfLastLayout = prevState
    }

    private fun layoutAnimationFinished(newLayout: Layout) {
        prevLayout = newLayout

        when (state) {
            State.QUESTION -> {
                val (prefix, suffix) = qa?.getPrefill() ?: Pair("", "")
                bindings.inputTextView.apply {
                    @SuppressLint("SetTextI18n")
                    text = prefix + suffix
                    setCaretPos(prefix.length)
                    setCaretEnabled(qa?.answers?.isNotEmpty() ?: false)
                }
            }
            State.GONE -> {
                // GONE has the positions like CORRECT; READY_FOR_NEXT has the positions like QUESTION.
                state = State.READY_FOR_NEXT
                updateLayoutIfNecessary()
            }
            else -> Unit
        }
    }

    private fun computeLayout(): Layout {
        val keyboardH = bindings.keyboardView.measuredHeight

        val availHeight = bindings.rootView.measuredHeight - delegate.minTop - when (state) {
            State.QUESTION -> keyboardH
            else -> 0
        }

        val requiredHeight = elements.computeRequiredHeight(state, kind)

        var curY = clamp(
            delegate.minTop + availHeight / 2.0f - requiredHeight / 2.0f,
            delegate.minTop, // never above minTop
            bindings.rootView.measuredHeight * 0.29f // never lower than this, otherwise transition may not look good
        )

        val layout = Layout()

        elements.forEach { e ->
            val expanded = e.view.isVisible && e.expanded(state, kind)

            if (expanded) {
                curY += e.spacing
            }

            e.attr(layout).apply {
                y = curY
                alpha = if (expanded) e.alpha(state) else 0.0f
            }

            if (expanded) {
                curY += e.view.measuredHeight + e.spacing
            }
        }

        layout.keyboard.apply {
            y = bindings.rootView.measuredHeight -
                keyboardH.toFloat() +
                if (state == State.QUESTION) 0.0f else 8.0f * values.spacing

            alpha = if (state == State.QUESTION) 1.0f else 0.0f
        }

        layout.infoBtn.alpha = if (state == State.CORRECT || state == State.INCORRECT) {
            1.0f
        } else {
            0.0f
        }

        return layout
    }

    fun setQuestionAndAnswer(newQA: QuestionAndAnswer) {
        qa = newQA

        if (state != State.READY_FOR_NEXT) {
            // In the initial render we're already READY_FOR_NEXT. Otherwise, fade out to GONE first.
            // Note that qa already reflects the new answer, but kind needs to stay until showNextQuestion is called,
            // otherwise the transition will base positions on the wrong kind!
            state = State.GONE
        }

        bindings.rootView.requestLayout()
    }

    private fun showNextQuestion() {
        if (state != State.READY_FOR_NEXT) {
            Log.e(TAG, "showNextQuestion: state is $state, but expected READY_FOR_NEXT")
        }

        val qa = qa ?: run {
            Log.e(TAG, "showNextQuestion: qa is null")
            return
        }

        kind = qa.kind

        updateQuestionTextViews(qa)
        updateQuestionHintTextView(qa)
        updateRevealedTranslationTextView(qa)
        updateRevealedHintTextView(qa)

        bindings.apply {
            whatToDoTextView.text = delegate.getWhatToDoHint(qa)
            inputTextView.text = "" // caret will be enabled from layoutAnimationFinished
            correctedTextView.text = ""

            revealedKanjiOrKanaTextView.text = qa.kanjiOrKanaToReveal // collapsed at this point

            stateIconView.setImageDrawable(delegate.iconWhenCorrect) // to get the correct height
            infoBtn.visibility = View.INVISIBLE // infoBtn is not part of ElementList
        }

        state = State.QUESTION
        bindings.rootView.requestLayout()
    }

    private fun updateQuestionTextViews(qa: QuestionAndAnswer) {
        bindings.apply {
            questionInDefaultFontTextView.visibility = View.GONE
            questionInHiraganaFontTextView.visibility = View.GONE
            questionInKatakanaFontTextView.visibility = View.GONE
            questionInPencilFontTextView.visibility = View.GONE
            questionInCalligraphyFontTategakiView.visibility = View.GONE
        }

        determineFontType(qa)

        qa.furiganaRelVOffset = when (qa.fontType) {
            FontType.CALLIGRAPHY -> 0.0f // furigana handled by TategakiView itself, offset is ignored
            FontType.PENCIL -> PENCIL_FONT_FURIGANA_REL_V_OFFSET
            FontType.BOLD_HIRAGANA -> 0.0f // hiragana-only word not expected to have furigana
            FontType.BOLD_KATAKANA -> 0.0f // katakana-only word not expected to have furigana
            FontType.DEFAULT -> DEFAULT_FONT_FURIGANA_REL_V_OFFSET
        }

        Log.d(TAG, "Using font type ${qa.fontType}, relVOffset=${qa.furiganaRelVOffset}")

        val theSize = getQuestionTextSizePx(qa)

        val theText = qa.question.let { q ->
            when (q) {
                is OneOf.First -> q.first // a String with no furigana
                is OneOf.Second -> q.second.buildSpan(FuriganaSpan.Options(relVOffset = qa.furiganaRelVOffset))
            }
        }

        val textView = when (qa.fontType) {
            FontType.CALLIGRAPHY -> null // TategakiView is not a TextView
            FontType.PENCIL -> bindings.questionInPencilFontTextView
            FontType.BOLD_HIRAGANA -> bindings.questionInHiraganaFontTextView
            FontType.BOLD_KATAKANA -> bindings.questionInKatakanaFontTextView
            FontType.DEFAULT -> bindings.questionInDefaultFontTextView
        }

        if (textView != null) {
            textView.apply {
                text = theText
                setTextSize(TypedValue.COMPLEX_UNIT_PX, theSize)
                visibility = View.VISIBLE
            }
        } else {
            // We update our TategakiView
            val availHeight = bindings.rootView.measuredHeight - delegate.minTop - values.topBottomMargin
            val allowedHeight = min(availHeight, values.tategakiMaxHeight).toInt()

            bindings.questionInCalligraphyFontTategakiView.apply {
                textSizePx = theSize
                viewMaxHeightPx = allowedHeight
                setText(theText)
                visibility = View.VISIBLE
            }
        }
    }

    private fun determineFontType(qa: QuestionAndAnswer) {
        val text = qa.questionWithoutFurigana

        if (text.isHiragana()) {
            qa.fontType = when (qa.kind.doesNotAskAnything) {
                true -> FontType.BOLD_HIRAGANA
                false -> FontType.PENCIL
            }
            return
        }

        if (text.isKatakana()) {
            qa.fontType = when (qa.kind.doesNotAskAnything) {
                true -> FontType.BOLD_KATAKANA
                false -> FontType.PENCIL
            }
            return
        }

        if (!qa.kind.doesNotAskAnything) {
            qa.fontType = FontType.DEFAULT // question is neither trivial nor kana-only
            return
        }

        if (!text.hasCJKOrKana()) {
            qa.fontType = FontType.DEFAULT // question is in English or other system language
            return
        }

        if (text.contains('〜')) {
            // Our calligraphy font doesn't have this glyph, unfortunately. Android would display the glyph in the
            // system font, which looks OK, but let's just use our pencil font, which has the glyph.
            qa.fontType = FontType.PENCIL
            return
        }

        val maxLenForCalligraphy = when (delegate.screenSize) {
            ScreenSize.SMALL -> MAX_LENGTH_FOR_CALLIGRAPHY_SMALL
            ScreenSize.NORMAL -> when (delegate.screenOrientation) {
                Orientation.PORTRAIT -> MAX_LENGTH_FOR_CALLIGRAPHY_MEDIUM
                else -> MAX_LENGTH_FOR_CALLIGRAPHY_SMALL
            }
            ScreenSize.LARGE -> MAX_LENGTH_FOR_CALLIGRAPHY_LARGE
        }

        qa.fontType = when {
            text.length <= maxLenForCalligraphy -> FontType.CALLIGRAPHY
            else -> FontType.PENCIL
        }
    }

    private fun getQuestionTextSizePx(qa: QuestionAndAnswer): Float {
        val text = qa.questionWithoutFurigana
        val len = text.length

        var size = when (len) {
            0, 1 -> values.len1QuestionTextSize
            2 -> values.len2QuestionTextSize
            3 -> values.len3QuestionTextSize
            4 -> values.len4QuestionTextSize
            5 -> values.len5QuestionTextSize
            6 -> values.len6QuestionTextSize
            7 -> values.len7QuestionTextSize
            8 -> values.len8QuestionTextSize
            9 -> values.len9QuestionTextSize
            10 -> values.len10QuestionTextSize
            11 -> values.len11QuestionTextSize
            12 -> values.len12QuestionTextSize
            13 -> values.len13QuestionTextSize
            14 -> values.len14QuestionTextSize
            15 -> values.len15QuestionTextSize
            16 -> values.len16QuestionTextSize
            17 -> values.len17QuestionTextSize
            18 -> values.len18QuestionTextSize
            19 -> values.len19QuestionTextSize
            20 -> values.len20QuestionTextSize
            else -> {
                // len >= 21
                lerp(values.len21QuestionTextSize, values.minQuestionTextSize, clamp((len - 21) / 25.0f))
            }
        }

        size *= when (qa.fontType) {
            FontType.CALLIGRAPHY -> 1.5f
            FontType.BOLD_KATAKANA -> 1.1f
            FontType.BOLD_HIRAGANA -> 1.0f
            FontType.PENCIL -> 1.1f
            FontType.DEFAULT -> 1.0f
        }

        val maxSize = when (qa.fontType) {
            FontType.CALLIGRAPHY -> Float.POSITIVE_INFINITY
            FontType.BOLD_KATAKANA -> values.len3QuestionTextSize
            FontType.BOLD_HIRAGANA -> values.len4QuestionTextSize
            FontType.PENCIL -> values.len4QuestionTextSize
            FontType.DEFAULT -> when {
                text.isKana() -> values.len4QuestionTextSize
                text.hasCJKIgnoringKana() -> values.len3QuestionTextSize
                else -> values.len6QuestionTextSize
            }
        }

        size = min(size, maxSize)

        size *= when (delegate.screenSize) {
            ScreenSize.SMALL -> 0.8f
            ScreenSize.NORMAL -> when (delegate.screenOrientation) {
                Orientation.PORTRAIT -> 1.0f
                else -> 0.8f
            }
            ScreenSize.LARGE -> 1.1f
        }

        size = max(size, values.minQuestionTextSize)

        Log.d(TAG, "Using text size $size px for length $len: $text")
        return size
    }

    private fun getHintTextSize(hint: String): Float {
        val (minSize, maxSize) = when (delegate.screenSize) {
            ScreenSize.LARGE -> Pair(values.mediumHintTextSize, values.largeHintTextSize)
            ScreenSize.SMALL -> Pair(values.minHintTextSize, values.smallHintTextSize)
            ScreenSize.NORMAL -> when (delegate.screenOrientation) {
                Orientation.PORTRAIT -> Pair(values.smallHintTextSize, values.mediumHintTextSize)
                else -> Pair(values.minHintTextSize, values.smallHintTextSize)
            }
        }

        val rel = clamp((hint.length - 16) / 27.0f)
        val size = lerp(maxSize, minSize, rel)
        return size
    }

    private fun updateQuestionHintTextView(qa: QuestionAndAnswer) {
        val text = qa.questionHint.let {
            when (it) {
                is OneOf.First -> it.first
                is OneOf.Second -> delegate.getHint(it.second)
            }
        }
        bindings.questionHintTextView.let {
            it.text = text
            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, getHintTextSize(text))
        }
    }

    private fun updateRevealedTranslationTextView(qa: QuestionAndAnswer) {
        val text = qa.translationToReveal
        bindings.revealedTranslationTextView.let {
            it.text = text
            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, getHintTextSize(text))
        }
    }

    private fun updateRevealedHintTextView(qa: QuestionAndAnswer) {
        val text = qa.hintToReveal
        bindings.revealedHintTextView.let {
            it.text = text
            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, getHintTextSize(text))
        }
    }

    private fun updateExplanationTextView(explanation: String) {
        bindings.explanationTextView.let {
            it.text = explanation
            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, getHintTextSize(explanation))
        }
    }

    fun revealAnswer(answer: Answer, questionWithGapFilled: CharSequence?) {
        // Answer elements are automatically revealed, because ElementList adjusts their alpha according to state.

        val qa = qa ?: return

        delegate.getAnswerComment(answer)
            .takeIf { it.isNotEmpty() }
            ?.let { bindings.whatToDoTextView.text = it }

        bindings.inputTextView.setCaretEnabled(false)

        if (questionWithGapFilled != null) {
            val textView = when (qa.fontType) {
                FontType.CALLIGRAPHY -> null // TategakiView is not a TextView
                FontType.PENCIL -> bindings.questionInPencilFontTextView
                FontType.BOLD_HIRAGANA -> bindings.questionInHiraganaFontTextView
                FontType.BOLD_KATAKANA -> bindings.questionInKatakanaFontTextView
                FontType.DEFAULT -> bindings.questionInDefaultFontTextView
            }

            if (textView != null) {
                textView.text = questionWithGapFilled
            } else {
                bindings.questionInCalligraphyFontTategakiView.setText(questionWithGapFilled)
            }
        }

        bindings.correctedTextView.text = if (answer == Answer.CORRECT) "" else qa.answers.firstOrNull() ?: ""

        bindings.stateIconView.setImageDrawable(
            when (answer) {
                Answer.CORRECT -> delegate.iconWhenCorrect
                Answer.WRONG -> delegate.iconWhenWrong
                Answer.CORRECT_EXCEPT_KANA_SIZE -> delegate.iconWhenAlmostCorrect
                Answer.TRIVIAL -> delegate.iconWhenCorrect
                Answer.NONE -> null
                Answer.SKIP -> null
            }
        )

        bindings.infoBtn.visibility = View.VISIBLE

        state = if (answer == Answer.CORRECT) State.CORRECT else State.INCORRECT
        bindings.rootView.requestLayout()
    }

    fun showExplanation(explanation: String) {
        updateExplanationTextView(explanation)

        bindings.apply {
            whatToDoTextView.text = ""
            inputTextView.setCaretEnabled(false)
            infoBtn.visibility = View.VISIBLE
        }

        state = State.EXPLANATION
        bindings.rootView.requestLayout()
    }

    // Called when a key is pressed, backspace, dakuten change, etc.
    fun applyInputTextTransform(trf: (Keyboard.TextAndCaret) -> Unit) {
        if (canAcceptInput()) {
            trf(textAndCaret)
        }
    }

    companion object {
        private const val TAG = "ProgStudyChoreo"

        private const val MAX_LENGTH_FOR_CALLIGRAPHY_LARGE = 30 // will result in minQuestionTextSize
        private const val MAX_LENGTH_FOR_CALLIGRAPHY_MEDIUM = 21 // will result in len21QuestionTextSize
        private const val MAX_LENGTH_FOR_CALLIGRAPHY_SMALL = 8

        private const val DEFAULT_FONT_FURIGANA_REL_V_OFFSET = 0.3f
        private const val PENCIL_FONT_FURIGANA_REL_V_OFFSET = -0.1f
    }
}
