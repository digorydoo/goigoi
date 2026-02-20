package io.github.digorydoo.goigoi.activity.flipthru

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import io.github.digorydoo.goigoi.activity.flipthru.fragment.FlipThruFragment
import io.github.digorydoo.goigoi.drawable.AnimatedDrawable
import ch.digorydoo.kutils.filter.delay
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.waveforms.nsin
import kotlin.math.abs

class Choreographer(private val delegate: Delegate, private val bindings: Bindings) {
    abstract class Delegate {
        abstract val oneDip: Float
        abstract val correctIcon: AnimatedDrawable
        abstract val wrongIcon: AnimatedDrawable
        abstract val textForSkipAction: String
        abstract val textForInfoAction: String
        abstract fun createNewCard(): FlipThruFragment
        abstract fun aboutToStartWordAppearing(newWord: Boolean)
        abstract fun didFlingFarLeft()
        abstract fun didFlingFarRight()
        abstract fun didFlingFarUp()
        abstract fun didFlingDown()
        abstract fun didFlingFarDown()
    }

    enum class State {
        INITIAL, WORD_APPEARING, IDLE, FLING_LEFT, FLING_RIGHT, FLING_UP, FLING_DOWN,
        FLING_FAR_LEFT, FLING_FAR_RIGHT, FLING_FAR_UP, FLING_FAR_DOWN, HORIZ_SHAKE,
        VERT_SHAKE, PAUSED
    }

    var state = State.INITIAL
        private set

    var card: FlipThruFragment? = null
        private set

    private var nextShakeIsHoriz = true

    private val containerWidth: Float
        get() = bindings.scrollContainer.width.toFloat()

    private val containerHeight: Float
        get() = bindings.scrollContainer.height.toFloat()

    fun pause() {
        state = State.PAUSED
    }

    fun resume() {
        if (state == State.PAUSED) {
            state = State.WORD_APPEARING
            startWordAppearing(false)
        }
    }

    fun startWordAppearing(newWord: Boolean = true) {
        if (state == State.PAUSED) {
            return
        }

        delegate.aboutToStartWordAppearing(newWord)

        state = State.WORD_APPEARING
        bindings.cardArea.translationX = 0.0f
        bindings.cardArea.translationY = if (newWord) containerHeight else 0.0f

        card = delegate.createNewCard()
        animatedTranslateCardTo(null, 0.0f, false)
    }

    private fun completeNewWordAppearing() {
        if (state != State.PAUSED) {
            state = State.IDLE
        }
    }

    fun startFlingLeft() {
        if (state != State.PAUSED) {
            state = State.FLING_LEFT
            animatedTranslateCardTo(-1.0f * containerWidth, null, false)
        }
    }

    private fun completeFlingLeft() {
        if (state != State.PAUSED) {
            // At this point, the icon will be in the centre
            state = State.FLING_FAR_LEFT
            animatedTranslateCardTo(-2.0f * containerWidth, null, true)
        }
    }

    private fun completeFlingFarLeft() {
        if (state != State.PAUSED) {
            delegate.didFlingFarLeft()
            startWordAppearing()
        }
    }

    fun startFlingRight() {
        if (state != State.PAUSED) {
            state = State.FLING_RIGHT
            animatedTranslateCardTo(1.0f * containerWidth, null, false)
        }
    }

    private fun completeFlingRight() {
        if (state != State.PAUSED) {
            // At this point, the icon will be in the centre
            state = State.FLING_FAR_RIGHT
            animatedTranslateCardTo(2.0f * containerWidth, null, true)
        }
    }

    private fun completeFlingFarRight() {
        if (state != State.PAUSED) {
            delegate.didFlingFarRight()
            startWordAppearing()
        }
    }

    fun startFlingUp() {
        if (state != State.PAUSED) {
            state = State.FLING_UP
            animatedTranslateCardTo(null, -1.0f * containerHeight, false)
        }
    }

    private fun completeFlingUp() {
        if (state != State.PAUSED) {
            // At this point, the message will be in the centre
            state = State.FLING_FAR_UP
            animatedTranslateCardTo(null, -2.0f * containerHeight, true)
        }
    }

    private fun completeFlingFarUp() {
        if (state != State.PAUSED) {
            delegate.didFlingFarUp()
            startWordAppearing()
        }
    }

    fun startFlingDown() {
        if (state != State.PAUSED) {
            state = State.FLING_DOWN
            animatedTranslateCardTo(null, 1.0f * containerHeight, false)
        }
    }

    private fun completeFlingDown() {
        if (state != State.PAUSED) {
            // At this point, the message will be in the centre
            delegate.didFlingDown()
        }
    }

    fun continueFlingDown() {
        state = State.FLING_FAR_DOWN
        animatedTranslateCardTo(null, 2.0f * containerHeight, true)
    }

    private fun completeFlingFarDown() {
        if (state != State.PAUSED) {
            delegate.didFlingFarDown()
            startWordAppearing()
        }
    }

    fun animatedTranslateCardTo(targetX: Float?, targetY: Float?, accel: Boolean) {
        if (state == State.PAUSED) {
            return
        }

        val fromX = bindings.cardArea.translationX
        val fromY = bindings.cardArea.translationY
        val toX = targetX ?: fromX
        val toY = targetY ?: fromY

        ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            addUpdateListener { a: ValueAnimator ->
                val p = a.animatedValue as Float
                val q = 1.0f - p
                val x = q * fromX + p * toX
                val y = q * fromY + p * toY
                translateCardTo(x, y)
            }
            addListener(object: Animator.AnimatorListener {
                override fun onAnimationStart(a: Animator) {}

                override fun onAnimationEnd(a: Animator) {
                    when (state) {
                        State.WORD_APPEARING -> completeNewWordAppearing()
                        State.FLING_LEFT -> completeFlingLeft()
                        State.FLING_FAR_LEFT -> completeFlingFarLeft()
                        State.FLING_RIGHT -> completeFlingRight()
                        State.FLING_FAR_RIGHT -> completeFlingFarRight()
                        State.FLING_UP -> completeFlingUp()
                        State.FLING_FAR_UP -> completeFlingFarUp()
                        State.FLING_DOWN -> completeFlingDown()
                        State.FLING_FAR_DOWN -> completeFlingFarDown()
                        else -> {
                        }
                    }
                }

                override fun onAnimationCancel(a: Animator) {}
                override fun onAnimationRepeat(a: Animator) {}
            })
            duration = 500
            interpolator = if (accel) {
                AccelerateInterpolator(2.0f)
            } else {
                DecelerateInterpolator(2.0f)
            }
            start()
        }
    }

    private fun updateResultImg(dx: Float) {
        val isBack = card?.state == FlipThruFragment.State.BACK

        val icon = when {
            isBack && dx < 0 -> delegate.correctIcon
            isBack && dx > 0 -> delegate.wrongIcon
            else -> null
        }

        if (icon == null) {
            bindings.resultImg.apply {
                if (drawable != null) {
                    visibility = View.INVISIBLE
                    setImageDrawable(null)
                }
            }

            return
        }

        val edgeOffset = 2 * delegate.oneDip
        val cardHalfWidth = (bindings.cardArea.width) / 2.0f
        val iconHalfWidth = (bindings.resultImg.width) / 2.0f
        val delta: Float

        // xrel 0 is at card edge
        // xrel 1 is at container centre
        // xrel 2 is beyond far container boundary
        var xrel = abs(dx) / containerWidth

        if (xrel < 1.0f) {
            val pre = clamp(1.8f * iconHalfWidth / containerWidth)
            xrel = delay(xrel, pre)
            val edge = cardHalfWidth - iconHalfWidth - edgeOffset
            delta = (1.0f - xrel) * edge
        } else {
            val boundary = -containerWidth / 2.0f - iconHalfWidth - edgeOffset
            delta = (xrel - 1.0f) * boundary
        }

        bindings.resultImg.apply {
            @Suppress("KotlinConstantConditions")
            translationX = when {
                dx > 0 -> -delta
                dx < 0 -> delta
                else -> 0.0f
            }

            if (drawable != icon) {
                setImageDrawable(icon)
                visibility = View.VISIBLE
            }
        }
    }

    private fun updateMsgTextView(dy: Float) {
        val supposedText = when {
            state == State.INITIAL -> ""
            state == State.WORD_APPEARING -> ""
            dy < 0 -> delegate.textForSkipAction
            dy > 0 -> delegate.textForInfoAction
            else -> ""
        }

        val shouldChangeText = (bindings.actionMsg.text ?: "") != supposedText

        if (supposedText.isEmpty()) {
            if (shouldChangeText) {
                bindings.actionMsg.apply {
                    visibility = View.INVISIBLE
                    text = ""
                }
            }

            return
        }

        val edgeOffset = 4 * delegate.oneDip
        val cardHalfHeight = (bindings.cardArea.height) / 2.0f
        val textHalfHeight = (bindings.actionMsg.height) / 2.0f
        val delta: Float

        // yrel 0 is at card edge
        // yrel 1 is at container centre
        // yrel 2 is beyond far container boundary
        var yrel = abs(dy) / containerHeight

        if (yrel < 1.0f) {
            val pre = clamp(1.8f * textHalfHeight / containerHeight)
            yrel = delay(yrel, pre)
            val edge = cardHalfHeight - textHalfHeight - edgeOffset
            delta = (1.0f - yrel) * edge
        } else {
            val boundary = -containerHeight / 2.0f - textHalfHeight - edgeOffset
            delta = (yrel - 1.0f) * boundary
        }

        bindings.actionMsg.apply {
            translationY = when {
                dy > 0 -> -delta
                dy < 0 -> delta
                else -> 0.0f
            }

            if (shouldChangeText) {
                text = supposedText
                visibility = View.VISIBLE
            }
        }
    }

    fun translateCardTo(dx: Float, dy: Float) {
        if (state == State.PAUSED) {
            return
        }

        updateResultImg(dx)
        updateMsgTextView(dy)

        bindings.cardArea.apply {
            translationX = dx
            translationY = dy
        }
    }

    fun startShaking() {
        if (nextShakeIsHoriz) {
            startHorizShake()
        } else {
            startVertShake()
        }

        nextShakeIsHoriz = !nextShakeIsHoriz
    }

    private fun startHorizShake() {
        if (state != State.PAUSED) {
            state = State.HORIZ_SHAKE
            animatedShakeCard(1.0f, 0.0f)
        }
    }

    private fun startVertShake() {
        if (state != State.PAUSED) {
            state = State.VERT_SHAKE
            animatedShakeCard(0.0f, 1.0f)
        }
    }

    private fun completeShake() {
        state = State.IDLE
    }

    private fun animatedShakeCard(scaleX: Float, scaleY: Float) {
        if (state == State.PAUSED) {
            return
        }

        val cx = bindings.cardArea.translationX
        val cy = bindings.cardArea.translationY
        val dx = scaleX * 16.0f * delegate.oneDip
        val dy = scaleY * 16.0f * delegate.oneDip

        ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            addUpdateListener { a: ValueAnimator ->
                val v = a.animatedValue as Float
                val r = 1.0f - delay(v, 0.3f)
                val w = r * nsin(3.0f * v)
                val x = cx + dx * w
                val y = cy + dy * w
                translateCardTo(x, y)
            }
            addListener(object: Animator.AnimatorListener {
                override fun onAnimationStart(a: Animator) {}

                override fun onAnimationEnd(a: Animator) {
                    completeShake()
                }

                override fun onAnimationCancel(a: Animator) {}
                override fun onAnimationRepeat(a: Animator) {}
            })
            duration = 500
            interpolator = LinearInterpolator()
            start()
        }
    }
}
