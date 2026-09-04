package io.github.digorydoo.goigoi.components.icons

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import io.github.digorydoo.goigoi.core.welcome.DailyProgressTracker.Companion.CHECKMARK_THRESHOLD
import io.github.digorydoo.goigoi.drawable.RingIconDrawable.Variant

@Composable
fun AnimatedDayIcon(
    progress: Float, // 0..1
    delayMillis: Int,
    durationMillis: Int,
) {
    val animValue = remember { Animatable(0f) }

    LaunchedEffect(progress, delayMillis, durationMillis) {
        animValue.snapTo(0f)
        animValue.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                delayMillis = delayMillis,
                durationMillis = durationMillis,
                // easing = { fraction -> 1f - (1f - fraction).pow(1.5f) }
                easing = EaseOutCubic,
            )
        )
    }

    if (progress >= CHECKMARK_THRESHOLD) {
        CheckmarkIcon(animValue = animValue.value)
    } else {
        RingIcon(progress, Variant.CIRCULAR, animValue = animValue.value)
    }
}
