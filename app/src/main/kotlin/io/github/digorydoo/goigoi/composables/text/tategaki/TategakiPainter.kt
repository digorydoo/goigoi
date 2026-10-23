package io.github.digorydoo.goigoi.composables.text.tategaki

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import ch.digorydoo.kutils.cjk.Unicode
import ch.digorydoo.kutils.cjk.isKatakana
import io.github.digorydoo.goigoi.core.tategaki.CharacterPlacement
import io.github.digorydoo.goigoi.core.tategaki.TategakiLayout
import kotlin.math.max

class TategakiPainter {
    // Assumes that (0; 0) represents the View origin.
    fun draw(layout: TategakiLayout, canvas: Canvas, showCharFrames: Boolean, props: TategakiProps) {
        if (layout.isEmpty()) {
            return
        }

        val textPaint = props.textPaint
        val textFM = textPaint.fontMetrics

        val furiganaPaint = props.furiganaPaint
        val furiganaFM = furiganaPaint.fontMetrics

        val textAscent = -textFM.top.toInt() // top is negative!
        val textCharHeight = textAscent + textFM.bottom

        val furiganaAscent = -furiganaFM.top.toInt()
        val furiganaCharHeight = furiganaAscent + 0.5f * furiganaFM.bottom // slightly condense furigana vertically
        var prevC = Char(0)

        val furiganaXDelta = props.furiganaXDelta
        val furiganaCharWidth = props.furiganaCharWidth
        val charWidth = props.charWidth

        val dottedLineMargin = props.dottedLineMargin
        val dottedLinePaint = props.dottedLinePaint

        layout.forEachElement { element ->
            var nextCharY = element.y.toFloat()

            // The characters of an element are never broken apart
            element.text.forEach { c ->
                if (showCharFrames) {
                    canvas.drawFrame(element.x, nextCharY.toInt(), charWidth, textPaint)
                }

                // Unicode distinguishes between 'ー' (KATAKANA_LONG_VOWEL_MARK) and '一' (CJK_NUMBER_ONE). We need to
                // rotate the katakana while keeping the number horizontal. But we cannot rely on Goigoi XML to use
                // that consistently; it's probably all mixed up. So, we rely on the fact that katakana prolonged sound
                // mark must have a katakana character preceding it. The ambiguous situation that the number ichi
                // immediately follows a katakana character is probably rare (hopefully).
                val treatAsLongVowel =
                    (c == Unicode.KATAKANA_LONG_VOWEL_MARK || c == Unicode.CJK_NUMBER_ONE) && prevC.isKatakana()
                prevC = c

                val placement = when {
                    treatAsLongVowel -> CharacterPlacement.justRotate
                    else -> CharacterPlacement.get(c)
                }

                val charX = element.x.toFloat() + placement.relXOffset * charWidth
                val charY = nextCharY + placement.relYOffset * textCharHeight

                if (placement.rotateCW) {
                    canvas.withRotation(90.0f, charX + charWidth * 0.5f, charY + textCharHeight * 0.5f) {
                        canvas.drawText("$c", charX, charY + textAscent, textPaint)
                    }
                } else {
                    canvas.drawText("$c", charX, charY + textAscent, textPaint)
                }

                val h = when (c) {
                    ' ' -> textCharHeight / 2.0f
                    else -> textCharHeight
                }

                nextCharY += h + textPaint.letterSpacing
            }

            if (element.furigana.isNotEmpty()) {
                val elementBottom = max(0.0f, nextCharY - textPaint.letterSpacing)
                val elementHeight = elementBottom - element.y
                val furiganaX = element.x.toFloat() + charWidth + furiganaXDelta
                val furiganaUnscaledHeight = element.furigana.length * furiganaCharHeight

                val scaleY = when {
                    furiganaUnscaledHeight <= elementHeight -> 1.0f
                    else -> elementHeight / furiganaUnscaledHeight
                }

                val furiganaMidY = element.y + elementHeight * 0.5f

                canvas.withScale(1.0f, scaleY, furiganaX, furiganaMidY) {
                    var fy = furiganaMidY - furiganaUnscaledHeight / 2.0f + furiganaAscent

                    element.furigana.forEach { furiganaChar ->
                        canvas.drawText("$furiganaChar", furiganaX, fy, furiganaPaint)
                        fy += furiganaCharHeight
                    }
                }

                if (scaleY == 1.0f && element.text.length > 1) {
                    // Check if we should draw a line to make clear which characters are covered by the furigana.
                    val endOfUpperLineY = furiganaMidY - furiganaUnscaledHeight / 2.0f - dottedLineMargin

                    if (endOfUpperLineY - element.y >= textCharHeight * DOTTED_LINE_MIN_REL_SIZE) {
                        val lineX = furiganaX + 0.5f * furiganaCharWidth - 0.5f * dottedLinePaint.strokeWidth
                        val startOfLowerLineY = furiganaMidY + furiganaUnscaledHeight / 2.0f + dottedLineMargin

                        // To align the first dot of each line with the end near the kanji, we draw the upper line in
                        // bottom-to-top direction, and the lower line in top-to-bottom direction.
                        // Draw the bottom line from top to botto to align the first dot with the end near the kanji.

                        canvas.drawLine(lineX, endOfUpperLineY, lineX, element.y.toFloat(), dottedLinePaint)
                        canvas.drawLine(lineX, startOfLowerLineY, lineX, elementBottom, dottedLinePaint)
                    }
                }
            }
        }
    }

    private fun Canvas.drawFrame(x: Int, y: Int, charWidth: Int, paint: Paint) {
        val oldStyle = paint.style
        val oldAlpha = paint.alpha

        paint.style = Style.STROKE
        paint.alpha = 127 // 50% opacity

        val textCharHeight = paint.fontMetrics.run { bottom - top }
        val inset = 4.0f
        val left = x + inset
        val top = y + inset
        val right = left + charWidth - 2 * inset
        val bottom = top + textCharHeight - 2 * inset
        val cx = (left + right) * 0.5f
        val cy = (top + bottom) * 0.5f

        drawRect(left, top, right, bottom, paint)
        drawRect(cx, top, cx + 1, bottom, paint)
        drawRect(left, cy, right, cy + 1, paint)

        paint.style = oldStyle
        paint.alpha = oldAlpha
    }

    companion object {
        private const val DOTTED_LINE_MIN_REL_SIZE = 0.33f
    }
}
