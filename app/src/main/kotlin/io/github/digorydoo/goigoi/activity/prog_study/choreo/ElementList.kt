package io.github.digorydoo.goigoi.activity.prog_study.choreo

import android.view.View
import androidx.core.view.isVisible
import io.github.digorydoo.goigoi.activity.prog_study.Bindings
import io.github.digorydoo.goigoi.activity.prog_study.Values
import io.github.digorydoo.goigoi.activity.prog_study.choreo.Choreographer.State
import io.github.digorydoo.goigoi.core.prog_study.Layout
import io.github.digorydoo.goigoi.core.prog_study.QAKind

// The ElementList and its Elements are immutable
class ElementList(bindings: Bindings, values: Values) {
    class Element(
        val view: View,
        val spacing: Float,
        val attr: (l: Layout) -> Layout.Attributes,
        val expanded: (s: State, kind: QAKind) -> Boolean,
        val alpha: (s: State) -> Float, // not called when not expanded (always 0)
    )

    private val elements = arrayOf(
        Element(
            bindings.questionInDefaultFontTextView,
            values.elementSpacing,
            attr = { it.questionInDefaultFont },
            expanded = { _, _ -> true },
            alpha = {
                when (it) {
                    State.GONE -> 0.0f
                    State.READY_FOR_NEXT -> 0.0f
                    else -> 1.0f
                }
            }
        ),
        Element(
            bindings.questionInHiraganaFontTextView,
            values.elementSpacing,
            attr = { it.questionInHiraganaFont },
            expanded = { _, _ -> true },
            alpha = {
                when (it) {
                    State.GONE -> 0.0f
                    State.READY_FOR_NEXT -> 0.0f
                    else -> 1.0f
                }
            }
        ),
        Element(
            bindings.questionInKatakanaFontTextView,
            values.elementSpacing,
            attr = { it.questionInKatakanaFont },
            expanded = { _, _ -> true },
            alpha = {
                when (it) {
                    State.GONE -> 0.0f
                    State.READY_FOR_NEXT -> 0.0f
                    else -> 1.0f
                }
            }
        ),
        Element(
            bindings.questionInPencilFontTextView,
            values.elementSpacing,
            attr = { it.questionInPencilFont },
            expanded = { _, _ -> true },
            alpha = {
                when (it) {
                    State.GONE -> 0.0f
                    State.READY_FOR_NEXT -> 0.0f
                    else -> 1.0f
                }
            }
        ),
        Element(
            bindings.questionInCalligraphyFontTategakiView,
            values.elementSpacing,
            attr = { it.questionInCalligraphyFont },
            expanded = { _, _ -> true },
            alpha = {
                when (it) {
                    State.GONE -> 0.0f
                    State.READY_FOR_NEXT -> 0.0f
                    else -> 1.0f
                }
            }
        ),
        Element(
            bindings.questionHintTextView,
            values.elementSpacing,
            attr = { it.questionHint },
            expanded = { _, _ -> bindings.questionHintTextView.text.isNotEmpty() },
            alpha = {
                when (it) {
                    State.GONE -> 0.0f
                    State.READY_FOR_NEXT -> 0.0f
                    State.EXPLANATION -> 0.0f
                    else -> 1.0f
                }
            }
        ),
        Element(
            bindings.correctedTextView,
            values.elementSpacing,
            attr = { it.corrected },
            expanded = { _, _ -> bindings.correctedTextView.text.isNotEmpty() },
            alpha = {
                when (it) {
                    State.GONE -> 0.0f
                    State.READY_FOR_NEXT -> 0.0f
                    State.EXPLANATION -> 0.0f
                    else -> 1.0f
                }
            }
        ),
        Element(
            bindings.inputTextView,
            values.elementSpacing,
            attr = { it.input },
            expanded = { s, _ -> s != State.EXPLANATION },
            alpha = {
                when (it) {
                    State.INCORRECT -> WRONG_ANSWER_ALPHA
                    State.GONE -> 0.0f
                    State.READY_FOR_NEXT -> 0.0f
                    State.EXPLANATION -> 0.0f
                    else -> 1.0f
                }
            }
        ),
        Element(
            bindings.stateIconView,
            5 * values.elementSpacing,
            attr = { it.icon },
            expanded = { state, kind ->
                when (state) {
                    State.EXPLANATION -> false
                    State.QUESTION -> false
                    else -> !kind.doesNotAskAnything
                }
            },
            alpha = {
                when (it) {
                    State.CORRECT -> 1.0f
                    State.INCORRECT -> 1.0f
                    else -> 0.0f
                }
            }
        ),
        Element(
            bindings.revealedKanjiOrKanaTextView,
            values.elementSpacing,
            attr = { it.revealedKanjiOrKana },
            expanded = { s, _ -> s != State.QUESTION && bindings.revealedKanjiOrKanaTextView.text.isNotEmpty() },
            alpha = {
                when (it) {
                    State.CORRECT -> 1.0f
                    State.INCORRECT -> 1.0f
                    State.EXPLANATION -> 1.0f
                    else -> 0.0f
                }
            }
        ),
        Element(
            bindings.revealedTranslationTextView,
            values.elementSpacing,
            attr = { it.revealedTranslation },
            expanded = { s, _ -> s != State.QUESTION && bindings.revealedTranslationTextView.text.isNotEmpty() },
            alpha = {
                when (it) {
                    State.CORRECT -> 1.0f
                    State.INCORRECT -> 1.0f
                    State.EXPLANATION -> 1.0f
                    else -> 0.0f
                }
            }
        ),
        Element(
            bindings.revealedHintTextView,
            values.elementSpacing,
            attr = { it.revealedHint },
            expanded = { s, _ -> s != State.QUESTION && bindings.revealedHintTextView.text.isNotEmpty() },
            alpha = {
                when (it) {
                    State.CORRECT -> 1.0f
                    State.INCORRECT -> 1.0f
                    else -> 0.0f
                }
            }
        ),
        Element(
            bindings.explanationRow,
            values.elementSpacing,
            attr = { it.explanation },
            expanded = { s, _ -> s == State.EXPLANATION },
            alpha = {
                when (it) {
                    State.EXPLANATION -> 1.0f
                    else -> 0.0f
                }
            }
        ),
    )

    fun computeRequiredHeight(state: State, kind: QAKind): Float {
        return elements.fold(0.0f) { sum, e ->
            sum + if (e.view.isVisible && e.expanded(state, kind)) {
                e.view.measuredHeight + 2 * e.spacing
            } else {
                0.0f
            }
        }
    }

    fun forEach(lambda: (e: Element) -> Unit) {
        elements.forEach(lambda)
    }

    companion object {
        private const val WRONG_ANSWER_ALPHA = 0.13f
    }
}
