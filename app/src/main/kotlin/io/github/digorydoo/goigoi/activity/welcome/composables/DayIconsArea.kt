package io.github.digorydoo.goigoi.activity.welcome.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.filter.envDelay
import ch.digorydoo.kutils.math.decel
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel
import io.github.digorydoo.goigoi.composables.icons.DayIconWithHintBalloon
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.utils.ScreenSize
import kotlin.time.Duration.Companion.days

private const val ANIM_DAY_ICONS_DURATION_MILLIS = 800
private const val ANIM_DAY_ICON_REL_DELAY = 0.6f // must be < 1f
private const val ANIM_DAY_ICON_DECEL = 1.5f // 1..oo

// NOTE: If we won't move DayIconsArea back up into the top half of the screen again, we could remove the logic of the
// animation, because the user is not going to see it anyway.
@Composable
fun DayIconsArea(model: WelcomeActivityModel, horizPadding: Dp) {
    val dailyProgress = model.dailyProgress.collectAsState().value

    val bigRingAnimProps = getBigRingAnimProps(model) // we base our own animation on the BigRing's
    val shouldAnimate = bigRingAnimProps.shouldAnimate
    val bigRingAnimDur = bigRingAnimProps.duration

    val anim = remember(shouldAnimate) { Animatable(if (shouldAnimate) 0f else 1f) }

    val anyPastProgress = dailyProgress.foldIndexed(false) { idx, result, progress ->
        result || (idx > 0 && progress > 0f)
    }

    if (!anyPastProgress) {
        return
    }

    val screenSize = DeviceProps.size
    val isPortrait = DeviceProps.isPortrait

    val spacing = when (screenSize) {
        ScreenSize.LARGE -> 24.dp
        ScreenSize.NORMAL -> if (isPortrait) 16.dp else 24.dp
        ScreenSize.SMALL -> 16.dp
    }

    val iconMaxSize = when (screenSize) {
        ScreenSize.LARGE -> 48.dp
        ScreenSize.NORMAL -> if (isPortrait) 40.dp else 32.dp
        ScreenSize.SMALL -> 32.dp
    }

    LaunchedEffect(bigRingAnimProps.animDependencies) {
        if (shouldAnimate) {
            anim.apply {
                snapTo(0f)
                animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        delayMillis = (bigRingAnimDur - ANIM_DAY_ICONS_DURATION_MILLIS).coerceAtLeast(0),
                        durationMillis = ANIM_DAY_ICONS_DURATION_MILLIS,
                        easing = EaseOutCubic
                    )
                )
            }
        } else {
            anim.snapTo(1f)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        val availWidth = this.maxWidth
        val iconSize = ((availWidth - spacing * 5 - horizPadding * 2) / 6).coerceAtMost(iconMaxSize)

        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
        ) {
            val now = Moment.now()

            for (idx in dailyProgress.size - 1 downTo 1) {
                val day = now - idx.days

                val dayAnimValue = when {
                    !shouldAnimate -> 1f
                    else -> {
                        val relIdx = idx.toFloat() / (dailyProgress.size - 1)
                        val pre = ANIM_DAY_ICON_REL_DELAY * relIdx
                        envDelay(
                            anim.value,
                            pre = pre,
                            post = 1f - ANIM_DAY_ICON_REL_DELAY + pre,
                        )
                    }
                }

                DayIconWithHintBalloon(
                    day,
                    dailyProgress[idx],
                    decel(dayAnimValue, ANIM_DAY_ICON_DECEL),
                    size = iconSize
                )
            }
        }
    }
}
