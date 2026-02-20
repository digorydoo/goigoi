package io.github.digorydoo.goigoi.activity.prog_study.keyboard

import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import io.github.digorydoo.goigoi.activity.prog_study.Bindings
import io.github.digorydoo.goigoi.activity.prog_study.Values
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.KeyDef.Action
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.KeyLensPart.ABOVE
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.KeyLensPart.BELOW
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.KeyLensPart.CENTRE
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.KeyLensPart.LEFT
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.KeyLensPart.RIGHT
import kotlin.math.abs

class KeyTouchListener(
    private val delegate: Delegate,
    private val keyDef: KeyDef,
    private val bindings: Bindings,
    private val values: Values,
): OnTouchListener {
    interface Delegate {
        val keyLens: KeyLensDrawable?
        fun showKeyLensAbove(anchor: View)
        fun hideKeyLens()
        fun onKeyLensPartSelected(action: Action, key: String)
    }

    private var startX = 0.0f
    private var startY = 0.0f
    private var isDown = false

    override fun onTouch(chip: View?, event: MotionEvent?): Boolean {
        if (chip?.visibility != View.VISIBLE || bindings.keyboardView.alpha <= 0.0f) {
            return false
        }

        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                isDown = true
                chip.let { delegate.showKeyLensAbove(it) }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDown) {
                    val dx = event.x - startX
                    val dy = event.y - startY
                    val ax = abs(dx)
                    val ay = abs(dy)

                    val select = when {
                        ax > ay -> {
                            when {
                                ax < values.minChipSwipeDelta -> null
                                dx > 0 -> RIGHT
                                else -> LEFT
                            }
                        }
                        ay < values.minChipSwipeDelta -> null
                        dy > 0 -> BELOW
                        else -> ABOVE
                    }

                    if (select != delegate.keyLens?.selected) {
                        delegate.keyLens?.selected = select
                        bindings.keyLensImageView.invalidate()
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isDown) {
                    chip.performClick()

                    if (delegate.keyLens?.selected == null) {
                        delegate.keyLens?.selected = CENTRE
                    }

                    delegate.keyLens?.selected?.let { part ->
                        val ak = keyDef.getActionAndText(part)
                        delegate.onKeyLensPartSelected(ak.action, ak.text)
                    }

                    delegate.hideKeyLens()
                }
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                isDown = false
                return true
            }
            else -> return false
        }
    }
}
