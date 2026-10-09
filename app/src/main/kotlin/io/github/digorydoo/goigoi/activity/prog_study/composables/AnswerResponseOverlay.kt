package io.github.digorydoo.goigoi.activity.prog_study.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.digorydoo.kutils.cjk.Unicode
import ch.digorydoo.kutils.cjk.isCJKOrKana
import ch.digorydoo.kutils.math.lerp
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.core.study.Answer
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

private val OUTLINE_SIZE = 8.dp

private val ANIM_DELAY = 100.milliseconds

private const val CORRECT_ANIM_DURATION_MILLIS = 800
private const val CORRECT_START_ZOOM_FACTOR = 0.6f
private const val CORRECT_END_ZOOM_FACTOR = 1f

private const val ALMOST_CORRECT_ANIM_DURATION_MILLIS = 900
private const val ALMOST_CORRECT_MIN_ZOOM_FACTOR = 0.4f
private const val ALMOST_CORRECT_MAX_ZOOM_FACTOR = 0.9f

private const val WRONG_ANIM_DURATION_MILLIS = 900
private const val WRONG_MIN_ZOOM_FACTOR = 0.85f
private const val WRONG_MAX_ZOOM_FACTOR = 0.9f
private const val WRONG_ZOOM_NUM_PERIODS = 9f
private val WRONG_YSHIFT_AMPLITUDE = 12.dp
private const val WRONG_YSHIFT_NUM_PERIODS = 17f

@Composable
fun AnswerResponseOverlay(model: ProgStudyActivityModel, modifier: Modifier = Modifier) {
    val kanji = model.answerCommentJa.collectAsState().value?.kanji ?: ""
    val correctness = model.answerCorrectness.collectAsState().value

    val textStyle = remember {
        TextStyle(
            // The font size is relevant even though we're going to scale the text dynamically, because it defines
            // the scale factor applied to the outline.
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.W900,
        )
    }

    val measurer = rememberTextMeasurer()

    val measuredText = remember(kanji) {
        // Use AnnotatedString to make the exclamation mark italic
        val annotated = buildAnnotatedString {
            if (kanji.isNotEmpty()) {
                append(kanji.slice(0 ..< kanji.length - 1))

                if (kanji.last() == '!' || kanji.last() == '！') {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        // Ideally, we would append a wide exclamation mark here, but that glyph is off-centre in
                        // Android system font, so I use this workaround instead.
                        val spc = Unicode.NARROW_NO_BREAK_SPACE
                        append("${spc}!${spc}")
                    }
                } else {
                    append(kanji.last())
                }
            }
        }
        measurer.measure(text = annotated, style = textStyle)
    }

    val anim = remember { Animatable(0.0f) }

    val density = LocalDensity.current
    val outlineSizePx = with(density) { OUTLINE_SIZE.toPx() }
    val wrongYShiftAmplitudePx = with(density) { WRONG_YSHIFT_AMPLITUDE.toPx() }

    val outlineStroke = remember(outlineSizePx) { Stroke(width = outlineSizePx, join = StrokeJoin.Round) }

    LaunchedEffect(correctness) {
        val dur = when (correctness) {
            Answer.CORRECT -> CORRECT_ANIM_DURATION_MILLIS
            Answer.WRONG -> WRONG_ANIM_DURATION_MILLIS
            Answer.CORRECT_EXCEPT_KANA_SIZE -> ALMOST_CORRECT_ANIM_DURATION_MILLIS
            else -> 0
        }

        if (dur > 0) {
            anim.snapTo(0f)
            delay(ANIM_DELAY)
            anim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = dur, easing = LinearEasing)
            )
        }
    }

    if (anim.value <= 0f || kanji.isEmpty() || measuredText.size.width <= 0 || measuredText.size.height <= 0) {
        return // may be better for performance
    }

    val textColour = when (correctness) {
        Answer.CORRECT -> GoigoiTheme.colours.correctAnswerOutlinedText
        Answer.WRONG -> GoigoiTheme.colours.wrongAnswerOutlinedText
        else -> GoigoiTheme.colours.almostCorrectAnswerOutlinedText
    }

    val outlineColour = when (correctness) {
        Answer.CORRECT -> GoigoiTheme.colours.correctAnswerOutline
        Answer.WRONG -> GoigoiTheme.colours.wrongAnswerOutline
        else -> GoigoiTheme.colours.almostCorrectAnswerOutline
    }

    val alpha: Float
    val zoomFactor: Float
    val translationY: Float

    when (correctness) {
        Answer.CORRECT -> {
            alpha = (1.1f * (1f - abs(anim.value * 2f - 1f).pow(3f))).coerceAtMost(1f)
            zoomFactor = lerp(CORRECT_START_ZOOM_FACTOR, CORRECT_END_ZOOM_FACTOR, anim.value)
            translationY = 0f
        }
        Answer.WRONG -> {
            alpha = 1f - anim.value.pow(4f)
            zoomFactor = lerp(
                WRONG_MIN_ZOOM_FACTOR,
                WRONG_MAX_ZOOM_FACTOR,
                (0.5 + 0.5 * (1f - anim.value) * sin(WRONG_ZOOM_NUM_PERIODS * anim.value * 2 * PI)).toFloat()
            )
            translationY = wrongYShiftAmplitudePx *
                (0.5 + 0.5 * (1f - anim.value) * sin(WRONG_YSHIFT_NUM_PERIODS * anim.value * 2 * PI)).toFloat()
        }
        Answer.CORRECT_EXCEPT_KANA_SIZE -> {
            alpha = 1f - abs(anim.value * 2f - 1f).pow(2.5f)
            zoomFactor = lerp(ALMOST_CORRECT_MIN_ZOOM_FACTOR, ALMOST_CORRECT_MAX_ZOOM_FACTOR, alpha)
            translationY = 0f
        }
        else -> {
            alpha = 1f - anim.value
            zoomFactor = 1f
            translationY = 0f
        }
    }

    val textWidth = measuredText.size.width.toFloat()
    val textHeight = measuredText.size.height.toFloat()
    val maxSize = minOf(DeviceProps.widthDp, DeviceProps.heightDp)

    Canvas(
        modifier = modifier
            .size(
                width = maxSize * zoomFactor,
                height = maxSize *
                    (zoomFactor * (textHeight + 2 * outlineSizePx) / (textWidth + 2 * outlineSizePx))
            )
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = translationY
            }
    ) {
        val sx = size.width / (textWidth + 2 * outlineSizePx)
        val sy = size.height / (textHeight + 2 * outlineSizePx)

        // drawRect(topLeft = Offset.Zero, size = size, color = outlineColour, style = Stroke(width = 10f))

        // Visual glitches can occur, very visibly with StrokeJoin.Miter, less so with StrokeJoin.Round, but still
        // visible with certain glyphs. The likely cause is overlapping of the thick outline with itself. Gemini tried
        // using getTextPath to obtain the path of the text, and then getFillPath to get the path of the background, but
        // the problem persisted, because it already happens inside getFillPath when given a paint with a large stroke
        // width. Changing the path's fillType to FillType.EVEN_ODD did not help, in fact it got worse, because font
        // glyphs rely on the default fillType.

        // As a workaround, we draw a circle behind problematic characters to fill the holes.

        val estimatedRelCharWidths = kanji.map { if (it.isCJKOrKana() || it in Unicode.WIDE_DIGITS) 1f else 0.5f }
        val estimatedRelCharWidthsSum = estimatedRelCharWidths.sum()

        val charHeight = size.height - 2 * outlineSizePx
        var x = outlineSizePx
        val y = size.height / 2

        class Dot(val cx: Float, val cy: Float, val rx: Float, val ry: Float = rx)

        kanji.forEachIndexed { idx, c ->
            val charWidth = estimatedRelCharWidths[idx] * (size.width - 2 * outlineSizePx) / estimatedRelCharWidthsSum

            val dot = when (c) {
                '正' -> Dot(cx = 0.32f * charWidth, cy = 0.1f * charHeight, rx = 0.21f * charWidth)
                '５', '６' -> Dot(cx = 0.09f * charWidth, cy = 0.07f * charHeight, rx = 0.18f * charWidth)
                '９' -> Dot(cx = 0.09f * charWidth, cy = -0.13f * charHeight, rx = 0.18f * charWidth)
                '5', '6' -> Dot(cx = 0.09f * charWidth, cy = 0.07f * charHeight, rx = 0.32f * charWidth)
                '8' -> Dot(cx = 0.09f * charWidth, cy = 0f, rx = 0.19f * charWidth, ry = 0.19f * charHeight)
                '9' -> Dot(cx = 0.09f * charWidth, cy = -0.11f * charHeight, rx = 0.33f * charWidth)
                else -> null
            }

            if (dot != null) {
                val cx = x + dot.cx + charWidth / 2
                val cy = y + dot.cy
                drawOval(
                    topLeft = Offset(cx - dot.rx, cy - dot.ry),
                    size = Size(2 * dot.rx, 2 * dot.ry),
                    color = outlineColour,
                    style = Fill,
                )
            }

            x += charWidth
        }

        withTransform({
            translate(outlineSizePx * sx, outlineSizePx * sy)
            scale(scaleX = sx, scaleY = sy, pivot = Offset.Zero)
        }) {
            drawText(
                textLayoutResult = measuredText,
                topLeft = Offset.Zero,
                color = outlineColour,
                drawStyle = outlineStroke,
            )
            drawText(
                textLayoutResult = measuredText,
                topLeft = Offset.Zero,
                color = textColour,
                drawStyle = Fill,
            )
        }
    }
}
