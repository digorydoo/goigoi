package io.github.digorydoo.goigoi.activity.prog_study.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.composables.icons.CheckmarkIcon
import io.github.digorydoo.goigoi.composables.icons.DayIcon
import io.github.digorydoo.goigoi.core.welcome.DailyProgressTracker.Companion.CHECKMARK_THRESHOLD

private const val ANIM_DURATION_MILLIS = 300

@Composable
fun StudyProgressIcon(model: ProgStudyActivityModel, modifier: Modifier = Modifier, size: Dp = 32.dp) {
    val firstRender = remember { mutableStateOf(true) }

    val progress = model.todaysProgress.collectAsState().value
    val pmode = model.presentationMode.collectAsState().value
    val qaKind = model.qaKind.collectAsState().value

    val hasScoresFlag = pmode == PresentationMode.ANSWER_CHECK && !qaKind.doesNotAskAnything

    val fadeInSpec = tween<Float>(
        durationMillis = ANIM_DURATION_MILLIS,
        easing = LinearEasing
    )

    val fadeOutSpec = tween<Float>(
        durationMillis = ANIM_DURATION_MILLIS,
        easing = EaseInCubic
    )

    val animValue = remember { Animatable(1f) }
    val prevProgress = remember { mutableFloatStateOf(Float.NaN) }

    LaunchedEffect(progress) {
        val shouldAnimate = prevProgress.floatValue < CHECKMARK_THRESHOLD && progress >= CHECKMARK_THRESHOLD
        prevProgress.floatValue = progress

        if (shouldAnimate) {
            animValue.snapTo(0f)
            animValue.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = ANIM_DURATION_MILLIS, easing = EaseOutCubic)
            )
        }
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = !firstRender.value && !hasScoresFlag,
        enter = fadeIn(fadeInSpec),
        exit = fadeOut(fadeOutSpec),
    ) {
        if (progress >= CHECKMARK_THRESHOLD) {
            CheckmarkIcon(animValue = animValue.value, size = size)
        } else {
            DayIcon(progress = progress, animValue = animValue.value, size = size)
        }
    }

    LaunchedEffect(Unit) {
        firstRender.value = false
    }
}
