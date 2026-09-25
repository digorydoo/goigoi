package io.github.digorydoo.goigoi.activity.prog_study.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.Companion.DELAY_BEFORE_NEXT_QUESTION_MILLIS

// We must make sure exit transitions have fully completed before showing the next question, because otherwise
// AnimatedVisibility would ignore our enter transition and just use the reverse of the exit transition with a
// default duration.
private const val EXIT_DURATION_MILLIS = DELAY_BEFORE_NEXT_QUESTION_MILLIS - 100

private const val ENTER_DURATION_MILLIS = 300
private const val ENTER_DELAY_SPREAD_MILLIS = 42

@Composable
fun AnimatedElement(
    visible: Boolean,
    modifier: Modifier = Modifier,
    allowShift: Boolean = true,
    enterDelayIdx: Int = 0, // 0 = no delay
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val maxShiftOffsetPx = with(density) { 16.dp.roundToPx() }
    val alphaAnim = remember { Animatable(if (visible) 1f else 0f) }
    val shiftAnim = remember { Animatable(0f) } // we don't know the direction initially

    val enterAnimSpec = remember(enterDelayIdx) {
        tween<Float>(
            delayMillis = ENTER_DELAY_SPREAD_MILLIS * enterDelayIdx,
            durationMillis = ENTER_DURATION_MILLIS,
            easing = EaseOutCubic,
        )
    }

    val exitAnimSpec = remember {
        tween<Float>(
            durationMillis = EXIT_DURATION_MILLIS,
            easing = EaseInCubic
        )
    }

    LaunchedEffect(visible) {
        if (visible) {
            alphaAnim.animateTo(targetValue = 1f, animationSpec = enterAnimSpec)
        } else {
            alphaAnim.animateTo(targetValue = 0f, animationSpec = exitAnimSpec)
        }
    }

    LaunchedEffect(visible) {
        if (allowShift) {
            if (visible) {
                // Shift in from below
                shiftAnim.snapTo(1f)
                shiftAnim.animateTo(targetValue = 0f, animationSpec = enterAnimSpec)
            } else {
                // Shift out towards the top
                shiftAnim.snapTo(0f)
                shiftAnim.animateTo(targetValue = -1f, animationSpec = exitAnimSpec)
            }
        } else {
            shiftAnim.snapTo(0f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(0, (maxShiftOffsetPx * shiftAnim.value).toInt()) }
            .alpha(alphaAnim.value),
        contentAlignment = Alignment.TopCenter
    ) {
        content()
    }
}
