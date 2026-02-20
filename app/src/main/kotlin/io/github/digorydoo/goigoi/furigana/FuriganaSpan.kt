package io.github.digorydoo.goigoi.furigana

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan
import ch.digorydoo.kutils.math.clamp
import kotlin.math.ceil
import kotlin.math.max

class FuriganaSpan(
    val primaryText: CharSequence,
    val secondaryText: CharSequence, // furigana
    val options: Options,
): ReplacementSpan() {
    class Options(
        val canSeeFurigana: Boolean = true,
        val fontSizeBase: Float = 0.0f,
        val fontSizeFactor: Float = 0.5f,
        val fontSizeMin: Float = 0.0f,
        val fontSizeMax: Float = 8192.0f,
        val opacity: Float = 0.56f,
        val relVOffset: Float = 0.3f,
    )

    private var primaryWidth = 0.0f
    private var secondaryWidth = 0.0f
    private var totalHeight = 0.0f

    private fun Options.getFuriganaTextSize(mainTextSize: Float): Float {
        return clamp(fontSizeBase + fontSizeFactor * mainTextSize, fontSizeMin, fontSizeMax)
    }

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        metrics: Paint.FontMetricsInt?,
    ): Int {
        val primarySize = paint.textSize
        val secondarySize = options.getFuriganaTextSize(primarySize)

        val pm = measureText(primaryText, paint)
        primaryWidth = pm.first
        val primaryHeight = pm.second

        secondaryWidth = 0.0f
        var secondaryHeight = 0.0f

        if (options.canSeeFurigana) {
            paint.textSize = secondarySize

            val sm = measureText(secondaryText, paint)
            secondaryWidth = sm.first
            secondaryHeight = sm.second

            paint.textSize = primarySize
        }

        val voff = secondaryHeight * options.relVOffset
        totalHeight = ceil(primaryHeight + secondaryHeight - voff)

        if (metrics != null) {
            // Beware: We need to write all the values of metrics! If we fail to set top and bottom
            // to some height, draw will not be called when our span spans the entire text, because
            // then the total line height will be zero! Moreover, we *must* not use the old values
            // of metrics when computing the new ones, otherwise there will be strange bugs, because
            // values would sum up when we're called multiple times!

            metrics.ascent = -(totalHeight - paint.fontMetricsInt.descent).toInt()
            metrics.descent = paint.fontMetricsInt.descent
            metrics.bottom = paint.fontMetricsInt.bottom
            metrics.top = paint.fontMetricsInt.bottom - totalHeight.toInt()
        }

        return max(primaryWidth, secondaryWidth).toInt()
    }

    private fun measureText(text: CharSequence, paint: Paint): Pair<Float, Float> {
        val fm = paint.fontMetrics
        val w = paint.measureText(text, 0, text.length)
        val h = fm.bottom - fm.top
        return Pair(w, h)
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        left: Float,
        top: Int,
        yBaseLine: Int,
        bottom: Int,
        paint: Paint,
    ) {
        val spanWidth = max(primaryWidth, secondaryWidth)
        val cx = left + spanWidth / 2.0f
        val x1 = cx - primaryWidth / 2.0f
        canvas.drawText(primaryText, 0, primaryText.length, x1, yBaseLine.toFloat(), paint)

        if (options.canSeeFurigana) {
            val primarySize = paint.textSize
            val secondarySize = options.getFuriganaTextSize(primarySize)

            val primaryAlpha = paint.alpha // 0..255
            val secondaryAlpha = (primaryAlpha * options.opacity).toInt()

            paint.textSize = secondarySize
            paint.alpha = secondaryAlpha

            val y2 = top - paint.ascent() // ascent is negative!

            if (primaryWidth <= secondaryWidth || primaryText.length < 2 || secondaryText.length < 2) {
                // Draw secondaryText centred in spanWidth.
                val x2 = cx - secondaryWidth / 2.0f
                canvas.drawText(secondaryText, 0, secondaryText.length, x2, y2, paint)
            } else {
                // Draw the chars of secondaryText spread to cover spanWidth.

                val gap = (spanWidth - secondaryWidth) / (secondaryText.length + 3)
                var sum = 3.0f * gap // two to the left, one to the right

                val arr = secondaryText.map { c ->
                    val s = "$c"
                    val w = paint.measureText(s)
                    sum += w + gap // add a gap after each char
                    w
                }

                var x2 = cx - (sum / 2.0f) + (2.0f * gap)

                secondaryText.forEachIndexed { i, c ->
                    val s = "$c"
                    canvas.drawText(s, x2, y2, paint)
                    x2 += arr[i] + gap
                }
            }

            paint.textSize = primarySize
            paint.alpha = primaryAlpha
        }
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "FuriganaSpan"
    }
}
