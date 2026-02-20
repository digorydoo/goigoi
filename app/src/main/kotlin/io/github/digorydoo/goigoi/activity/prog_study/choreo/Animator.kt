package io.github.digorydoo.goigoi.activity.prog_study.choreo

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import io.github.digorydoo.goigoi.activity.prog_study.Bindings
import ch.digorydoo.kutils.math.decel
import ch.digorydoo.kutils.math.lerp

class Animator(private val bindings: Bindings) {
    fun start(fromLayout: Layout, toLayout: Layout, onFinish: (newLayout: Layout) -> Unit) {
        ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            addUpdateListener { a: ValueAnimator ->
                val vlin = a.animatedValue as Float
                val rel = decel(vlin, 1.1f)
                setAttrs(fromLayout, toLayout, rel)

                if (vlin >= 1.0f) {
                    onFinish(toLayout)
                }
            }

            startDelay = 0
            duration = ANIM_DURATION + startDelay
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun setAttrs(l1: Layout, l2: Layout, rel: Float) {
        setAttrs(bindings.questionInDefaultFontTextView, l1.questionInDefaultFont, l2.questionInDefaultFont, rel)
        setAttrs(bindings.questionInHiraganaFontTextView, l1.questionInHiraganaFont, l2.questionInHiraganaFont, rel)
        setAttrs(bindings.questionInKatakanaFontTextView, l1.questionInKatakanaFont, l2.questionInKatakanaFont, rel)
        setAttrs(bindings.questionInPencilFontTextView, l1.questionInPencilFont, l2.questionInPencilFont, rel)
        setAttrs(
            bindings.questionInCalligraphyFontTategakiView,
            l1.questionInCalligraphyFont,
            l2.questionInCalligraphyFont,
            rel
        )
        setAttrs(bindings.questionHintTextView, l1.questionHint, l2.questionHint, rel)
        setAttrs(bindings.inputTextView, l1.input, l2.input, rel)
        setAttrs(bindings.correctedTextView, l1.corrected, l2.corrected, rel)
        setAttrs(bindings.stateIconView, l1.icon, l2.icon, rel)
        setAttrs(bindings.revealedKanjiOrKanaTextView, l1.revealedKanjiOrKana, l2.revealedKanjiOrKana, rel)
        setAttrs(bindings.revealedTranslationTextView, l1.revealedTranslation, l2.revealedTranslation, rel)
        setAttrs(bindings.revealedHintTextView, l1.revealedHint, l2.revealedHint, rel)
        setAttrs(bindings.explanationRow, l1.explanation, l2.explanation, rel)
        setAttrs(bindings.keyboardView, l1.keyboard, l2.keyboard, rel)
        setAttrs(bindings.infoBtn, l1.infoBtn, l2.infoBtn, rel)
    }

    private fun setAttrs(view: View, a1: Layout.Attributes, a2: Layout.Attributes, rel: Float) {
        view.translationY = lerp(a1.y, a2.y, rel)
        view.alpha = lerp(a1.alpha, a2.alpha, rel)
    }

    companion object {
        private const val ANIM_DURATION = 300L
    }
}
