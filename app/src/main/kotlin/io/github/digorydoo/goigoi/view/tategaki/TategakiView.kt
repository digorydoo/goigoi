package io.github.digorydoo.goigoi.view.tategaki

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.text.SpannableString
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.withRotation
import androidx.core.graphics.withScale
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.furigana.FuriganaSpan
import io.github.digorydoo.goigoi.utils.DimUtils
import ch.digorydoo.kutils.cjk.Unicode.CJK_NUMBER_ONE
import ch.digorydoo.kutils.cjk.Unicode.KATAKANA_LONG_VOWEL_MARK
import ch.digorydoo.kutils.cjk.isKatakana
import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.math.clamp
import kotlin.math.max
import kotlin.math.min

/**
 * Similar to TextView, but the characters are laid out vertically. Note that the furigana is grabbed from FuriganaSpan
 * and drawn here directly. Also note that other kinds of Span are not currently supported.
 */
class TategakiView: View {
    var viewMaxHeightPx = Int.MAX_VALUE

    var textSizePx: Float
        get() = textPaint.textSize
        set(px) {
            textPaint.textSize = px
        }

    private var columnSpacing = 0.0f // px
    private var emptyFuriganaWidth = 0.0f // px
    private var furiganaXDelta = 0.0f // px
    private var furiganaMaxSize = 0.0f // px
    private var dottedLineMargin = 0.0f // px
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val furiganaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val dottedLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var charWidth = 0
    private var furiganaCharWidth = 0
    private val layout = TategakiLayout()

    constructor(ctx: Context, attrs: AttributeSet?): super(ctx, attrs) {
        initWithContext(attrs, ctx)
    }

    constructor(ctx: Context, attrs: AttributeSet?, defStyleAttr: Int): super(ctx, attrs, defStyleAttr) {
        initWithContext(attrs, ctx)
    }

    private fun initWithContext(attrs: AttributeSet?, ctx: Context) {
        context.withStyledAttributes(attrs, R.styleable.TategakiView) {
            // Attributes need to be declared in attrs.xml

            textPaint.typeface = getResourceId(R.styleable.TategakiView_android_fontFamily, 0)
                .takeIf { it > 0 }
                ?.let { ResourcesCompat.getFont(ctx, it) }

            textPaint.color = getColor(R.styleable.TategakiView_android_textColor, Colour.black.toARGB())
            textPaint.textSize = DimUtils.dpToPx(DEFAULT_FONT_SIZE_DP, ctx)
        }

        columnSpacing = DimUtils.dpToPx(COLUMN_SPACING_DP, ctx)
        emptyFuriganaWidth = DimUtils.dpToPx(EMPTY_FURIGANA_WIDTH_DP, ctx)
        furiganaXDelta = DimUtils.dpToPx(FURIGANA_X_DELTA_DP, ctx)
        furiganaMaxSize = DimUtils.dpToPx(FURIGANA_MAX_SIZE_DP, ctx)
        dottedLineMargin = DimUtils.dpToPx(DOTTED_LINE_MARGIN_DP, ctx)

        furiganaPaint.let {
            it.color = textPaint.color
            it.alpha = clamp((textPaint.alpha.toFloat() * FURIGANA_OPACITY).toInt(), 0, 255)
        }

        dottedLinePaint.let {
            it.color = textPaint.color
            it.alpha = furiganaPaint.alpha
            it.style = Paint.Style.STROKE
            it.strokeWidth = DimUtils.dpToPx(DOTTED_LINE_STROKE_WIDTH_DP, ctx)
            it.pathEffect = DashPathEffect(
                floatArrayOf(
                    DimUtils.dpToPx(DOTTED_LINE_DOT_LENGTH_DP, ctx),
                    DimUtils.dpToPx(DOTTED_LINE_GAP_LENGTH_DP, ctx),
                ),
                0.0f
            )
        }
    }

    fun setText(text: CharSequence) {
        layout.clear()

        when (text) {
            is String -> text.forEachIndexed { i, c -> layout.add(i, c) }
            is SpannableString -> addElements(text)
            else -> throw NotImplementedError("text is an unsupported subclass of CharSequence")
        }

        requestLayout()
    }

    private fun addElements(text: SpannableString) {
        val len = text.length
        var startIdx = 0

        while (startIdx < len) {
            val nextIdx = text.nextSpanTransition(startIdx, len, null)
            require(nextIdx > startIdx)

            val spans = text.getSpans(startIdx, nextIdx, Any::class.java)

            when {
                spans == null || spans.isEmpty() -> {
                    // This is an unstyled range
                    text.substring(startIdx, nextIdx).forEachIndexed { i, c -> layout.add(startIdx + i, c) }
                }
                spans.size == 1 -> {
                    val span = spans[0]

                    when (span) {
                        is FuriganaSpan -> {
                            if (!span.options.canSeeFurigana) {
                                // Furigana is disabled, so each character gets an independent element
                                span.primaryText.forEachIndexed { i, c -> layout.add(startIdx + i, c) }
                            } else {
                                // Furigana is enabled, so we must treat the entire span as a block
                                val elementText = span.primaryText.toString()
                                val furigana = span.secondaryText.toString()
                                layout.add(startIdx, elementText, furigana)
                            }
                        }
                        else -> throw NotImplementedError("Span at idx=$startIdx not a FuriganaString: $text")
                    }
                }
                else -> throw NotImplementedError("More than one span covers idx=$startIdx of: $text")
            }

            startIdx = nextIdx
        }
    }

    override fun getSuggestedMinimumWidth(): Int {
        // Suggest the width of one character plus padding
        return paddingLeft + paddingRight + textPaint.measureText("あ").toInt()
    }

    override fun getSuggestedMinimumHeight(): Int {
        // Suggest the height of one character plus padding and spacing
        val fm = textPaint.fontMetrics
        val charHeight = fm.bottom - fm.top
        return paddingTop + paddingBottom + (charHeight + textPaint.letterSpacing).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec) // AT_MOST, EXACTLY, UNSPECIFIED
        val specWidth = MeasureSpec.getSize(widthMeasureSpec)

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val specHeight = MeasureSpec.getSize(heightMeasureSpec)

        val maxHeight = when (heightMode) {
            MeasureSpec.AT_MOST -> min(specHeight, viewMaxHeightPx)
            MeasureSpec.EXACTLY -> specHeight
            else -> viewMaxHeightPx
        }

        furiganaPaint.textSize = min(furiganaMaxSize, textPaint.textSize * FURIGANA_REL_SIZE)
        furiganaCharWidth = furiganaPaint.measureText("あ").toInt()

        charWidth = textPaint.measureText("あ").toInt()
        val fm = textPaint.fontMetrics

        val desiredSize = layout.arrangeElements(
            charWidth = charWidth,
            charHeight = (fm.bottom - fm.top).toInt(), // fm.top is negative!
            maxHeight = maxHeight,
            columnSpacing = columnSpacing.toInt(),
            letterSpacing = textPaint.letterSpacing.toInt(),
            furiganaCharWidth = furiganaCharWidth,
            furiganaXDelta = furiganaXDelta.toInt(),
            emptyFuriganaWidth = emptyFuriganaWidth.toInt(),
        )

        val w = when (widthMode) {
            MeasureSpec.AT_MOST -> min(desiredSize.x, specWidth)
            MeasureSpec.EXACTLY -> specWidth
            else -> desiredSize.x
        }

        val h = when (heightMode) {
            MeasureSpec.AT_MOST -> min(desiredSize.y, specHeight)
            MeasureSpec.EXACTLY -> specHeight
            else -> desiredSize.y
        }

        setMeasuredDimension(w, h)
    }

    // Not needed since this View has no child Views.
    // override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    //     super.onLayout(changed, left, top, right, bottom)
    // }

    override fun onDraw(canvas: Canvas) {
        // Note that the canvas is already translated such that (0; 0) represents the View's origin.

        if (layout.isEmpty()) {
            return
        }

        // canvas.drawRect(0.0f, 0.0f, measuredWidth - 1.0f, measuredHeight - 1.0f, dottedLinePaint)

        val textFM = textPaint.fontMetrics
        val furiganaFM = furiganaPaint.fontMetrics

        val textAscent = -textFM.top.toInt() // top is negative!
        val textCharHeight = textAscent + textFM.bottom

        val furiganaAscent = -furiganaFM.top.toInt()
        val furiganaCharHeight = furiganaAscent + 0.5f * furiganaFM.bottom // slightly condense furigana vertically
        var prevC = Char(0)

        layout.forEachElement { element ->
            var nextCharY = element.y.toFloat()

            // The characters of an element are never broken apart
            element.text.forEach { c ->
                // canvas.drawFrame(element.x, nextCharY.toInt(), charWidth, textPaint)

                // Unicode distinguishes between 'ー' (KATAKANA_LONG_VOWEL_MARK) and '一' (CJK_NUMBER_ONE). We need to
                // rotate the katakana while keeping the number horizontal. But we cannot rely on Goigoi XML to use
                // that consistently; it's probably all mixed up. So, we rely on the fact that katakana prolonged sound
                // mark must have a katakana character preceding it. The ambiguous situation that the number ichi
                // immediately follows a katakana character is probably rare (hopefully).
                val treatAsLongVowel = (c == KATAKANA_LONG_VOWEL_MARK || c == CJK_NUMBER_ONE) && prevC.isKatakana()
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

    companion object {
        private const val DEFAULT_FONT_SIZE_DP = 16.0f
        private const val COLUMN_SPACING_DP = 16.0f // adds to the width of any furigana present
        private const val EMPTY_FURIGANA_WIDTH_DP = 4.0f // must be less than the minimal expected furigana size
        private const val FURIGANA_X_DELTA_DP = 4.0f
        private const val FURIGANA_OPACITY = 0.56f
        private const val FURIGANA_REL_SIZE = 0.42f
        private const val FURIGANA_MAX_SIZE_DP = 18.0f
        private const val DOTTED_LINE_MARGIN_DP = 16.0f
        private const val DOTTED_LINE_STROKE_WIDTH_DP = 1.5f
        private const val DOTTED_LINE_DOT_LENGTH_DP = 2.0f
        private const val DOTTED_LINE_GAP_LENGTH_DP = 8.0f
        private const val DOTTED_LINE_MIN_REL_SIZE = 0.33f

        /**
         * Useful when debugging furigana placement or aligning individual characters. Showing this to the user is not
         * good, though, because it would require manual adjustment of many kanjis since some overlap their frame.
         */
        @Suppress("unused")
        fun Canvas.drawFrame(x: Int, y: Int, charWidth: Int, paint: Paint) {
            val oldStyle = paint.style
            val oldAlpha = paint.alpha

            paint.style = Paint.Style.STROKE
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
    }
}
