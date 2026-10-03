package io.github.digorydoo.goigoi.activity.welcome.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel

private const val ANIM_BASE_DURATION_MILLIS = 500
private const val ANIM_PROGRESS_DURATION_MILLIS = 1200

data class BigRingAnimProps(val shouldAnimate: Boolean, val duration: Int, val animDependencies: String)

@Composable
fun getBigRingAnimProps(model: WelcomeActivityModel): BigRingAnimProps {
    val todaysProgress = model.todaysProgress.collectAsState().value // 0..1
    val studyCountOfDay = model.studyCountOfDay.collectAsState().value
    val prevStudyCountOfDay = model.prevStudyCountOfDay.collectAsState().value
    val animatedForStudyCount = remember { mutableFloatStateOf(Float.NaN) }

    return BigRingAnimProps(
        shouldAnimate = studyCountOfDay != prevStudyCountOfDay && studyCountOfDay != animatedForStudyCount.floatValue,
        duration = ANIM_BASE_DURATION_MILLIS + (ANIM_PROGRESS_DURATION_MILLIS * todaysProgress).toInt(),
        animDependencies = "$studyCountOfDay,$prevStudyCountOfDay",
    )
}

@Composable
fun AnimatedBigRing(model: WelcomeActivityModel) {
    val todaysProgress = model.todaysProgress.collectAsState().value // 0..1

    val animProps = getBigRingAnimProps(model)
    val shouldAnimate = animProps.shouldAnimate
    val bigRingAnimDur = animProps.duration

    val anim = remember(shouldAnimate) { Animatable(if (shouldAnimate) 0f else 1f) }

    LaunchedEffect(animProps.animDependencies) {
        if (shouldAnimate) {
            anim.apply {
                snapTo(0f)
                animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = bigRingAnimDur, easing = EaseOutCubic)
                )
            }
        } else {
            anim.snapTo(1f)
        }
    }

    BigRing(todaysProgress, anim.value)
}
