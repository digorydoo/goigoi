package io.github.digorydoo.goigoi.activity.welcome.composables

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import ch.digorydoo.kutils.filter.envDelay
import ch.digorydoo.kutils.math.decel
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel
import io.github.digorydoo.goigoi.activity.welcome.composables.big_ring.getBigRingAnimProps
import io.github.digorydoo.goigoi.composables.CircularAlignment
import io.github.digorydoo.goigoi.composables.CircularLayout
import io.github.digorydoo.goigoi.composables.icons.DayIconWithHintBalloon
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.utils.ScreenSize
import kotlin.math.PI
import kotlin.math.pow
import kotlin.time.Duration.Companion.days

private const val ANIM_DAY_ICONS_DURATION_MILLIS = 600
private const val ANIM_DAY_ICON_REL_DELAY = 0.6f // must be < 1f
private const val ANIM_DAY_ICON_DECEL = 1.5f // 1..oo

@Composable
fun DayIconsArea(model: WelcomeActivityModel, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
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

    val iconMinSize = when (screenSize) {
        ScreenSize.LARGE -> 48.dp
        ScreenSize.NORMAL -> if (isPortrait) 40.dp else 32.dp
        ScreenSize.SMALL -> 32.dp // not too small; these icons are clickable
    }

    val iconMaxSize = when (screenSize) {
        ScreenSize.LARGE -> 72.dp
        ScreenSize.NORMAL -> if (isPortrait) 64.dp else 40.dp
        ScreenSize.SMALL -> 40.dp
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
                        easing = { 1f - (1f - it).pow(1.5f) }
                    )
                )
            }
        } else {
            anim.snapTo(1f)
        }
    }

    val now = Moment.now()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularLayout(
            startAngle = 0.52 * PI,
            spacing = 0.031 * PI,
            align = CircularAlignment.OUTSIDE,
        ) {
            for (idx in dailyProgress.size - 1 downTo 1) {
                val rel = (idx - 1).toFloat() / (dailyProgress.size - 2) // 1..0
                val day = now - idx.days

                val dayAnimValue = when {
                    !shouldAnimate -> 1f
                    else -> {
                        val pre = ANIM_DAY_ICON_REL_DELAY * rel
                        envDelay(
                            anim.value,
                            pre = pre,
                            post = 1f - ANIM_DAY_ICON_REL_DELAY + pre,
                        )
                    }
                }

                DayIconWithHintBalloon(
                    modifier = Modifier.scale(decel(dayAnimValue, ANIM_DAY_ICON_DECEL)),
                    day = day,
                    progress = dailyProgress[idx],
                    size = lerp(iconMinSize, iconMaxSize, 1f - rel),
                )
            }
        }

        Box(modifier = Modifier.padding(iconMaxSize + 8.dp)) {
            content()
        }
    }
}
