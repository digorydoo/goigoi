package io.github.digorydoo.goigoi.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import androidx.appcompat.widget.AppCompatImageButton
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.drawable.FabIcon

class FloatingActionBtn: AppCompatImageButton {
    private lateinit var fabIcon: FabIcon
    private var animDuration = DEFAULT_ANIM_DURATION
    private var shouldGlow = false
    private var glowAnimator: ValueAnimator? = null

    constructor(ctx: Context, attrs: AttributeSet?): super(ctx, attrs) {
        initWithContext(ctx, attrs)
    }

    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) {
        initWithContext(ctx, attrs)
    }

    private fun initWithContext(ctx: Context, attrs: AttributeSet?) {
        val sattr = ctx.obtainStyledAttributes(attrs, R.styleable.FloatingActionBtn)
        val initiallyShown = sattr.getBoolean(R.styleable.FloatingActionBtn_initiallyShown, true)
        shouldGlow = sattr.getBoolean(R.styleable.FloatingActionBtn_glowing, false)
        animDuration = sattr.getInt(R.styleable.FloatingActionBtn_animDuration, DEFAULT_ANIM_DURATION)
        val strIconName = sattr.getString(R.styleable.FloatingActionBtn_iconName)
        sattr.recycle()

        val iconName = when (strIconName) {
            "play" -> FabIcon.IconName.PLAY
            "arrowRight" -> FabIcon.IconName.ARROW_RIGHT
            else -> FabIcon.IconName.NONE
        }

        fabIcon = FabIcon(ctx, iconName)
        background = fabIcon

        if (initiallyShown) {
            fabIcon.animValue = 1.0f
            if (shouldGlow) startGlowing()
        } else {
            fabIcon.animValue = 0.0f
            visibility = INVISIBLE // don't use GONE, can break ongoing transition
        }
    }

    private fun startGlowing() {
        if (glowAnimator != null) return

        glowAnimator = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            addUpdateListener { a ->
                fabIcon.glow = a.animatedValue as Float
                invalidate()
            }
            startDelay = GLOW_START_DELAY
            duration = GLOW_CYCLE_DURATION
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stopGlowing() {
        glowAnimator?.apply { cancel() }
        glowAnimator = null

        if (fabIcon.glow > 0.0f) {
            fabIcon.glow = 0.0f
            invalidate()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isEnabled) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> addState(android.R.attr.state_pressed)
                MotionEvent.ACTION_UP -> removeState(android.R.attr.state_pressed)
            }
        }
        return super.onTouchEvent(event)
    }

    private fun addState(s: Int) {
        fabIcon.state = mutableListOf(s)
            .apply {
                fabIcon.state.forEach {
                    if (it != s) {
                        add(it)
                    }
                }
            }
            .toIntArray()
        invalidate()
    }

    private fun removeState(s: Int) {
        fabIcon.state = mutableListOf<Int>()
            .apply {
                fabIcon.state.forEach {
                    if (it != s) {
                        add(it)
                    }
                }
            }
            .toIntArray()
        invalidate()
    }

    fun show(withTransition: Boolean = true) {
        isEnabled = true
        visibility = VISIBLE

        if (shouldGlow) {
            startGlowing()
        }

        if (fabIcon.animValue >= 1.0f) {
            return // already fully expanded
        }

        if (withTransition) {
            ValueAnimator.ofFloat(0.0f, 1.0f).apply {
                addUpdateListener { a ->
                    fabIcon.animValue = a.animatedValue as Float
                    invalidate()
                }
                duration = animDuration.toLong()
                interpolator = LinearInterpolator()
                start()
            }
        } else {
            fabIcon.animValue = 1.0f
            invalidate()
        }
    }

    fun hide() {
        isEnabled = false
        stopGlowing()

        if (fabIcon.animValue <= 0.0f) {
            return // already fully collapsed
        }

        ValueAnimator.ofFloat(1.0f, 0.0f).apply {
            addUpdateListener { a ->
                fabIcon.animValue = a.animatedValue as Float

                if (fabIcon.animValue <= 0.0f) {
                    visibility = INVISIBLE // don't use GONE, can break ongoing transition
                } else {
                    invalidate()
                }
            }
            duration = animDuration.toLong()
            interpolator = LinearInterpolator()
            start()
        }
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "FloatingActionBtn"

        private const val DEFAULT_ANIM_DURATION = 200
        private const val GLOW_START_DELAY = 300L
        private const val GLOW_CYCLE_DURATION = 1000L
    }
}
