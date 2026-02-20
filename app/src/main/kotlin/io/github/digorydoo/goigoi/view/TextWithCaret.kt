package io.github.digorydoo.goigoi.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withTranslation
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import ch.digorydoo.kutils.math.clamp

class TextWithCaret: AppCompatTextView {
    private var caretColour = 0
    private var caretWidth = 0.0f
    private var caretEnabled = false
    private var caretPos = 0
    private var blinkState = true
    private var blinker: Runnable? = null

    constructor(ctx: Context, attrs: AttributeSet?): super(ctx, attrs) {
        initWithContext(ctx)
    }

    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) {
        initWithContext(ctx)
    }

    private fun initWithContext(ctx: Context) {
        caretColour = ResUtils.getARGBFromRes(R.color.green_800, ctx)
        caretWidth = DimUtils.dpToPx(3.0f, ctx)
    }

    fun setCaretEnabled(enabled: Boolean) {
        caretEnabled = enabled
        invalidate()

        if (enabled) {
            restartBlinker()
        }
    }

    fun setCaretPos(pos: Int) {
        caretPos = pos

        if (caretEnabled) {
            invalidate()
            restartBlinker()
        }
    }

    private fun restartBlinker() {
        val handler = Handler(Looper.getMainLooper())

        val runnable = object: Runnable {
            override fun run() {
                // If the caret is no longer enabled, or another blinker has been created,
                // this runnable does not call postDelayed, thus it will destroy itself.

                if (blinker == this && caretEnabled) {
                    blinkState = !blinkState
                    invalidate()
                    handler.postDelayed(this, BLINK_DELAY)
                }
            }
        }

        blinker = runnable
        blinkState = true
        handler.postDelayed(runnable, BLINK_DELAY)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (caretEnabled && blinkState) {
            val paint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = caretWidth
                color = caretColour
            }

            val path = Path()
            layout.getCursorPath(clamp(caretPos, 0, text.length), path, text)

            val dx = paddingLeft + caretWidth / 2
            val dy = paddingTop.toFloat()

            canvas.withTranslation(dx, dy) {
                drawPath(path, paint)
            }
        }
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "TextWithCaret"

        private const val BLINK_DELAY = 500L
    }
}
