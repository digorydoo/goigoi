package io.github.digorydoo.goigoi.activity.flipthru.fragment

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.furigana.FuriganaBuilder
import io.github.digorydoo.goigoi.furigana.FuriganaSpan
import io.github.digorydoo.goigoi.furigana.buildSpan
import io.github.digorydoo.goigoi.stats.Stats
import io.github.digorydoo.goigoi.utils.DeviceUtils
import io.github.digorydoo.goigoi.utils.ScreenSize
import ch.digorydoo.kutils.cjk.*
import ch.digorydoo.kutils.math.accel
import ch.digorydoo.kutils.math.decel
import ch.digorydoo.kutils.math.lerp
import kotlin.math.min

class FlipThruFragment: Fragment() {
    enum class State {
        FRONT, BACK
    }

    private lateinit var params: FlipThruFragmentParams
    private lateinit var bindings: Bindings
    private lateinit var values: Values
    private lateinit var stats: Stats
    private lateinit var unyt: Unyt
    private lateinit var data: FlipThruData
    private var text1Height = 0
    private var text2Height = 0
    private var text3Height = 0
    private var text4Height = 0
    private var text5Height = 0
    private var middleGapHeight = 0
    private var needToComputeHeight = false
    private var theState: State = State.FRONT

    var state: State
        get() = theState
        set(newState) = setState(newState, true)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val ctx = requireContext()
        values = Values(ctx)
        stats = Stats.getSingleton(ctx)
        val vocab = Vocabulary.getSingleton(ctx)

        params = FlipThruFragmentParams.fromBundle(requireArguments())
        unyt = vocab.findUnytById(params.unytId)!!

        data = FlipThruData(
            word = vocab.findWordById(params.wordId)!!,
            phraseIdx = params.phraseIdx.takeIf { it >= 0 },
            sentenceIdx = params.sentenceIdx.takeIf { it >= 0 },
        )

        // Inflate views

        val rootView = inflater.inflate(R.layout.flip_thru_card_fragment, container, false)

        bindings = Bindings(rootView).apply {
            text1.alpha = 0.0f
            text2.alpha = 0.0f
            text3.alpha = 0.0f
            text4.alpha = 0.0f
            text5.alpha = 0.0f
        }

        if (shouldSwapForms()) {
            bindings.apply {
                setTranslationIntoView(text1)
                text2.text = data.hint
                setPrimaryFormIntoView(text3)
                setRomajiIntoView(text4)
                text5.text = data.explanation
            }
        } else {
            bindings.apply {
                setPrimaryFormIntoView(text1)
                setRomajiIntoView(text2)
                setTranslationIntoView(text3)
                text4.text = data.hint
                text5.text = data.explanation
            }
        }

        // Compute sizes when layout completes

        needToComputeHeight = true
        val vto = rootView.viewTreeObserver
        vto.addOnGlobalLayoutListener { layoutChanged() }

        return rootView
    }

    private fun setPrimaryFormIntoView(tv: TextView) {
        val isBack = theState == State.BACK

        val options = FuriganaSpan.Options(
            fontSizeBase = values.furiganaFontSizeBase,
            fontSizeFactor = FURIGANA_SIZE_FACTOR
        )

        val text: CharSequence
        var size: Float
        val canSeeKanji = !data.usuallyInKana && (params.studyPrimaryForm || params.studyTranslation || isBack)

        if (canSeeKanji) {
            size = getTextSizeInMMForString(data.kanji)

            if (params.studyFurigana || params.studyTranslation || isBack) {
                text = data.primaryForm.buildSpan(options)
            } else {
                // Trick to reserve the vertical space for the hidden furigana
                val trick = "【：】${data.kanji}"
                text = FuriganaBuilder.buildSpan(trick, options)
            }
        } else {
            size = getTextSizeInMMForString(data.kana)

            if (unyt.hasFurigana) {
                // Trick to reserve the vertical space for the hidden furigana
                val trick = "【：】${data.kana}"
                text = FuriganaBuilder.buildSpan(trick, options)
            } else {
                text = data.kana
            }
        }

        if ((params.studyPrimaryForm || params.studyFurigana) && !isBack && unyt.studyLang == "ja") {
            val activity = activity

            if (activity != null) {
                // Make the kanji on the front-side of the card larger if screen size allows it.
                size *= when (DeviceUtils.getScreenSize(activity)) {
                    ScreenSize.NORMAL -> 1.3f
                    ScreenSize.LARGE -> 1.5f
                    else -> 1.0f
                }

                // But don't exceed the maximum size.
                size = min(size, mmList[0])
            }
        }

        tv.text = text
        tv.setTextSize(TypedValue.COMPLEX_UNIT_MM, size)
    }

    private fun setTranslationIntoView(tv: TextView) {
        data.translation.let {
            tv.text = it
            tv.setTextSize(TypedValue.COMPLEX_UNIT_MM, getTextSizeInMMForString(it))
        }
    }

    private fun setRomajiIntoView(tv: TextView) {
        val isBack = theState == State.BACK
        val isSolo = !isBack && params.studyRomaji && !params.studyPrimaryForm && !params.studyFurigana

        // In an N5 unyt, studyRomaji may be set to false by FlipThruActivity if the word is known.
        // We want to reveal the rōmaji here on the backside. But if it's not an N5 unyt, we don't
        // want to show rōmaji, ever.

        tv.apply {
            text = if (unyt.levelOfMostDifficultWord == JLPTLevel.N5) data.romaji else ""

            if (isSolo) {
                setTextColor(values.romajiSoloTextColour)
                tv.setTextSize(TypedValue.COMPLEX_UNIT_MM, getTextSizeInMMForString(data.romaji))
            } else {
                setTextColor(values.romajiTextColour)
                setTextSize(TypedValue.COMPLEX_UNIT_PX, values.romajiFontSize)
            }

        }
    }

    private fun layoutChanged() {
        if (!needToComputeHeight) {
            return
        }

        bindings.apply {
            text1Height = text1.height
            text2Height = text2.height
            text3Height = text3.height
            text4Height = text4.height
            text5Height = text5.height
            middleGapHeight = middleGap.height
        }

        needToComputeHeight = false
        setState(theState, false)
    }

    private fun setState(newState: State, animated: Boolean) {
        theState = newState

        val swap = shouldSwapForms()
        val isBack = newState == State.BACK

        if (isBack) {
            if (!swap) {
                // reveal the text1's hidden furigana or kanji
                setPrimaryFormIntoView(bindings.text1)
            }
        }

        val hasText1 = (isBack || params.studyPrimaryForm || params.studyFurigana || swap) &&
            bindings.text1.text.isNotEmpty()
        val hasText2 = (isBack || params.studyRomaji || swap) && bindings.text2.text.isNotEmpty()
        val hasText3 = isBack && bindings.text3.text.isNotEmpty()
        val hasText4 = isBack && bindings.text4.text.isNotEmpty()
        val hasText5 = isBack && bindings.text5.text.isNotEmpty()

        val text1NewAlpha = if (hasText1) 1.0f else 0.0f
        val text2NewAlpha = if (hasText2) 1.0f else 0.0f
        val text3NewAlpha = if (hasText3) 1.0f else 0.0f
        val text4NewAlpha = if (hasText4) 1.0f else 0.0f
        val text5NewAlpha = if (hasText5) 1.0f else 0.0f

        val text1NewHeight = if (hasText1) text1Height else 0
        val text2NewHeight = if (hasText2) text2Height else 0
        val text3NewHeight = if (hasText3) text3Height else 0
        val text4NewHeight = if (hasText4) text4Height else 0
        val text5NewHeight = if (hasText5) text5Height else 0
        val middleGapNewHeight = if (isBack) middleGapHeight else 0

        val text1NewMarginTop = if (hasText1) bindings.text1.marginTop else 0
        val text2NewMarginTop = if (hasText2) bindings.text2.marginTop else 0
        val text3NewMarginTop = if (hasText3) bindings.text3.marginTop else 0
        val text4NewMarginTop = if (hasText4) bindings.text4.marginTop else 0
        val text5NewMarginTop = if (hasText5) bindings.text5.marginTop else 0

        val totalHeight = text1Height + bindings.text1.marginTop +
            text2Height + bindings.text2.marginTop +
            middleGapHeight +
            text3Height + bindings.text3.marginTop +
            text4Height + bindings.text4.marginTop +
            text5Height + bindings.text5.marginTop

        val newHeight = text1NewHeight + text1NewMarginTop +
            text2NewHeight + text2NewMarginTop +
            middleGapNewHeight +
            text3NewHeight + text3NewMarginTop +
            text4NewHeight + text4NewMarginTop +
            text5NewHeight + text5NewMarginTop

        var offset = (totalHeight - newHeight) / 2.0f
        val text1NewOff = offset
        offset -= bindings.text1.marginTop - text1NewMarginTop
        offset -= text1Height - text1NewHeight
        val text2NewOff = offset
        offset -= bindings.text2.marginTop - text2NewMarginTop
        offset -= text2Height - text2NewHeight
        offset -= middleGapHeight - middleGapNewHeight
        val text3NewOff = offset
        offset -= bindings.text3.marginTop - text3NewMarginTop
        offset -= text3Height - text3NewHeight
        val text4NewOff = offset
        offset -= bindings.text4.marginTop - text4NewMarginTop
        offset -= text4Height - text4NewHeight
        val text5NewOff = offset

        if (!animated) {
            bindings.apply {
                text1.alpha = text1NewAlpha
                text2.alpha = text2NewAlpha
                text3.alpha = text3NewAlpha
                text4.alpha = text4NewAlpha
                text5.alpha = text5NewAlpha

                text1.translationY = text1NewOff
                text2.translationY = text2NewOff
                text3.translationY = text3NewOff
                text4.translationY = text4NewOff
                text5.translationY = text5NewOff
            }
        } else {
            val text1OldAlpha = bindings.text1.alpha
            val text2OldAlpha = bindings.text2.alpha
            val text3OldAlpha = bindings.text3.alpha
            val text4OldAlpha = bindings.text4.alpha
            val text5OldAlpha = bindings.text5.alpha

            val text1OldOff = bindings.text1.translationY
            val text2OldOff = bindings.text2.translationY
            val text3OldOff = bindings.text3.translationY
            val text4OldOff = bindings.text4.translationY
            val text5OldOff = bindings.text5.translationY

            val anim = ValueAnimator.ofFloat(0.0f, 1.0f)
            anim.addUpdateListener { a: ValueAnimator ->
                val v = a.animatedValue as Float
                val m = accel(v, 1.3f)
                val n = decel(v, 1.1f)

                bindings.apply {
                    text1.alpha = lerp(text1OldAlpha, text1NewAlpha, m)
                    text2.alpha = lerp(text2OldAlpha, text2NewAlpha, m)
                    text3.alpha = lerp(text3OldAlpha, text3NewAlpha, m)
                    text4.alpha = lerp(text4OldAlpha, text4NewAlpha, m)
                    text5.alpha = lerp(text5OldAlpha, text5NewAlpha, m)

                    text1.translationY = lerp(text1OldOff, text1NewOff, n)
                    text2.translationY = lerp(text2OldOff, text2NewOff, n)
                    text3.translationY = lerp(text3OldOff, text3NewOff, n)
                    text4.translationY = lerp(text4OldOff, text4NewOff, n)
                    text5.translationY = lerp(text5OldOff, text5NewOff, n)
                }
            }
            anim.duration = 300
            anim.interpolator = LinearInterpolator()
            anim.start()
        }
    }

    /**
     * @return text size in millimeters
     */
    private fun getTextSizeInMMForString(s: String): Float {
        // We use millimeters instead of dip, because millimeters is more accurate in terms of
        // equal physical size across devices. We don't use sp either, because we don't want to
        // allow to scale these texts with user preference.

        // The following gives complex characters a weight less than 1 to make those texts bigger.
        // At the moment, we give each part of a surrogate pair a weight of 1.
        // To correctly support surrogate pairs, we would have to iterate through code points.

        val sum = s.fold(0.0f) { result, c ->
            val weight = when {
                c == 'l' -> 1.1f
                c == 'i' -> 1.1f
                c == 'I' -> 1.1f
                c.isHiragana() -> 0.6f
                c.isKatakana() -> 0.6f
                c.isOneStrokeKanji() -> 0.6f
                c.isTwoStrokeKanji() -> 0.6f
                c.isCJK() -> 0.55f
                else -> 1.0f
            }
            result + weight
        }

        val idx = sum.toInt()

        return if (idx >= mmList.size - 1) {
            mmList[mmList.size - 1]
        } else {
            val a = mmList[idx]
            val b = mmList[idx + 1]
            val p = sum - idx // 0..1
            val q = 1.0f - p
            q * a + p * b
        }
    }

    private fun shouldSwapForms() =
        params.studyTranslation

    companion object {
        @Suppress("unused")
        private const val TAG = "FlipThruCardFragment"

        private const val FURIGANA_SIZE_FACTOR = 0.12f // multiplied by kanji font size

        private val mmList = arrayOf(
            11.43f, 10.16f, 8.89f, 7.62f,
            6.35f, 5.40f, 4.60f, 4.29f,
            4.13f, 3.97f, 3.89f, 3.81f,
            3.73f, 3.65f, 3.57f, 3.49f,
            3.41f, 3.33f, 3.25f, 3.18f
        )

        fun create(params: FlipThruFragmentParams) =
            FlipThruFragment().apply {
                arguments = params.toBundle()
            }
    }
}
