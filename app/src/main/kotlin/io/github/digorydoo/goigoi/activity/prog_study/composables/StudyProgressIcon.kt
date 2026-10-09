package io.github.digorydoo.goigoi.activity.prog_study.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.composables.HintBalloon
import io.github.digorydoo.goigoi.composables.icons.CheckmarkIcon
import io.github.digorydoo.goigoi.composables.icons.DayIcon
import io.github.digorydoo.goigoi.core.welcome.DailyProgressTracker.Companion.CHECKMARK_THRESHOLD
import kotlin.math.roundToInt

private const val ANIM_DURATION_MILLIS = 300

@Composable
fun StudyProgressIcon(model: ProgStudyActivityModel, modifier: Modifier = Modifier) {
    val firstRender = remember { mutableStateOf(true) }
    val showBalloon = remember { mutableStateOf(false) }

    val progress = model.todaysProgress.collectAsState().value
    val numCorrect = model.numCorrect.collectAsState().value
    val numWrong = model.numWrong.collectAsState().value
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

    val visible = !firstRender.value && !hasScoresFlag

    HintBalloon(
        modifier = modifier,
        wrappedContent = {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(fadeInSpec),
                exit = fadeOut(fadeOutSpec),
            ) {
                IconButton(onClick = { showBalloon.value = true }, enabled = visible) {
                    if (progress >= CHECKMARK_THRESHOLD) {
                        CheckmarkIcon(animValue = animValue.value)
                    } else {
                        DayIcon(progress = progress)
                    }
                }
            }
        },
        lines = arrayOf(
            stringResource(R.string.todays_progress_percent)
                .replace("\${N}", minOf(100, (progress * 100f).roundToInt()).toString()),
            stringResource(R.string.correct_count).replace("\${N}", numCorrect.toString()),
            stringResource(R.string.wrong_count).replace("\${N}", numWrong.toString()),
        ),
        open = showBalloon.value,
        onDismiss = { showBalloon.value = false },
    )

    LaunchedEffect(Unit) {
        firstRender.value = false
    }
}
