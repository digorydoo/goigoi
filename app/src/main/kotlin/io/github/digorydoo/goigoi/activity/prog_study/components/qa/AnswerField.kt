package io.github.digorydoo.goigoi.activity.prog_study.components.qa

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.cjk.Unicode.WIDE_WIDTH_SPACE
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.KeyboardMode
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.activity.prog_study.components.AnimatedElement
import io.github.digorydoo.goigoi.core.study.Answer
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private val CARET_BLINK_RATE_MILLIS = 500.milliseconds

val ANSWER_FONT_SIZE = 32.dp // not Sp, because we want this to stay fixed

@Composable
fun AnswerField(model: ProgStudyActivityModel, enterDelayIdx: Int) {
    val cursorRect = remember { mutableStateOf(null as Rect?) }
    val blink = remember { Animatable(0f) }
    val layoutResult = remember { mutableStateOf(null as TextLayoutResult?) }

    val qaKind = model.qaKind.collectAsState().value
    if (qaKind.doesNotAskAnything) return

    val pmode = model.presentationMode.collectAsState().value
    val kmode = model.keyboardMode.collectAsState().value
    val currentAnswer = model.currentAnswer.collectAsState().value
    val caretPos = model.caretPos.collectAsState().value
    val allowSetCaret = model.allowSetCaret.collectAsState().value
    val answerCorrectness = model.answerCorrectness.collectAsState().value
    val answerIsWrong = answerCorrectness == Answer.WRONG
    val caretColour = GoigoiTheme.colours.primary
    val hasCaret = pmode == PresentationMode.QUESTION && kmode != KeyboardMode.TRIVIAL

    LaunchedEffect(hasCaret, currentAnswer, caretPos) {
        if (hasCaret) {
            // Start with a visible caret when one of the effect dependencies have changed.
            blink.snapTo(1f)

            // The coroutine will be cancelled when one of the LaunchedEffect dependencies change.
            while (true) {
                delay(CARET_BLINK_RATE_MILLIS)
                blink.snapTo(0f)
                delay(CARET_BLINK_RATE_MILLIS)
                blink.snapTo(1f)
            }
        } else {
            blink.snapTo(0f)
        }
    }

    val density = LocalDensity.current
    val cursorHalfWidthPx = with(density) { 1.dp.toPx() }
    val fontSizePx = with(density) { ANSWER_FONT_SIZE.toPx() }
    val fontSize = with(density) { fontSizePx.toSp() }
    val lineHeight = with(density) { (fontSizePx + 2.dp.toPx()).toSp() }
    val caretHeightPx = with(density) { fontSizePx + 8.dp.toPx() }

    AnimatedElement(
        visible = pmode != PresentationMode.NOTHING && pmode != PresentationMode.BEFORE_QUESTION,
        enterDelayIdx = enterDelayIdx,
    ) {
        Text(
            modifier = Modifier
                .padding(top = 16.dp)
                .then(
                    if (allowSetCaret) {
                        Modifier.pointerInput(Unit) {
                            detectTapGestures { position ->
                                layoutResult.value?.let { layout ->
                                    model.setCaretPos(layout.getOffsetForPosition(position))
                                }
                            }
                        }
                    } else Modifier
                )
                .drawWithContent {
                    drawContent()

                    if (hasCaret && blink.value > 0.5f) {
                        cursorRect.value?.let { r ->
                            drawRect(caretColour, r.topLeft, r.size)
                        }
                    }
                },
            // To give the text a stable height, we put two wide spaces left and right of the caret if answer is empty.
            text = currentAnswer.ifEmpty { "$WIDE_WIDTH_SPACE$WIDE_WIDTH_SPACE" },
            color = if (answerIsWrong) GoigoiTheme.colours.faintOnBackground else GoigoiTheme.colours.onBackground,
            fontSize = fontSize,
            lineHeight = lineHeight,
            onTextLayout = { layout ->
                layoutResult.value = layout
                val r = layout.getCursorRect(
                    if (currentAnswer.isEmpty()) 1 else caretPos
                )
                val x = (r.left + r.right) / 2f // actually we expect r.width to be empty

                // r.top is wrong on first line due to platform-specific padding.
                // getLineTop(getLineForOffset(caretPos)) reports the same wrong value.
                // As a workaround, we use a fixed caret height starting from the bottom.

                cursorRect.value = Rect(
                    left = x - cursorHalfWidthPx,
                    right = x + cursorHalfWidthPx,
                    top = r.bottom - caretHeightPx,
                    bottom = r.bottom,
                )
            }
        )
    }
}
