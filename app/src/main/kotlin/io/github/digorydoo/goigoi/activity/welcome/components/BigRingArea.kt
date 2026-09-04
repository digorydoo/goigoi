package io.github.digorydoo.goigoi.activity.welcome.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.cjk.FuriganaString
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel
import io.github.digorydoo.goigoi.components.HintBalloon
import io.github.digorydoo.goigoi.drawable.BigRingDrawable
import io.github.digorydoo.goigoi.furigana.FuriganaBuilder
import io.github.digorydoo.goigoi.providers.DeviceProps
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ScreenSize
import io.github.digorydoo.goigoi.utils.clickableNoRipple

private interface BigRingStyles {
    val ringSize: Dp
    val clickableAreaSize: Dp
    val marginTop: Dp
    val marginBottom: Dp
    val drawableColours: BigRingDrawable.Colours
    val drawableDims: BigRingDrawable.Dimensions
}

@Composable
private fun getStyles(): BigRingStyles {
    val themeColours = GoigoiTheme.colours
    val density = LocalDensity.current
    val screenSize = DeviceProps.size
    val orientation = DeviceProps.orientation

    return remember(themeColours, density, screenSize, orientation) {
        object : BigRingStyles {
            override val ringSize = when (screenSize) {
                ScreenSize.LARGE -> 256.dp
                ScreenSize.NORMAL -> when (orientation) {
                    Orientation.PORTRAIT -> 224.dp
                    else -> 192.dp
                }
                ScreenSize.SMALL -> 192.dp
            }
            override val clickableAreaSize = ringSize * 0.6f
            override val marginTop = 8.dp
            override val marginBottom = 16.dp
            override val drawableColours = object : BigRingDrawable.Colours {
                override val trail = themeColours.ring
                override val track = themeColours.faintRing
                override val text = themeColours.onBackground
                override val checkmark = themeColours.primary
                override val headBg = themeColours.primary
                override val headFg = themeColours.onPrimary
            }
            override val drawableDims = object : BigRingDrawable.Dimensions {
                override val insetSizePx = with(density) { 1.dp.toPx() }.toInt()
                override val markInsetSizePx = with(density) { 48.dp.toPx() }.toInt()
                override val textSizePx = with(density) { 17.dp.toPx() }
            }
        }
    }
}

@Composable
fun BigRingArea(
    todaysProgress: Float,
    animateWithDurationMillis: Int?, // null = don't animate
    model: WelcomeActivityModel,
) {
    val showBalloon = remember { mutableStateOf(false) }
    val styles = getStyles()

    val centreText = model.bigRingCentreText.collectAsState().value
    val centreTextJa = FuriganaString(centreText.ja)

    val drawable = remember(todaysProgress, centreTextJa) {
        BigRingDrawable(todaysProgress, styles.drawableColours, styles.drawableDims, centreTextJa.kanji)
    }

    val animValue = remember(animateWithDurationMillis) {
        Animatable(if (animateWithDurationMillis != null) 0f else 1f)
    }

    LaunchedEffect(todaysProgress, animateWithDurationMillis) {
        if (animateWithDurationMillis != null) {
            animValue.snapTo(0f)
            animValue.animateTo(
                targetValue = 1f, // the animation is always 0..1f even if todaysProgress < 1f
                animationSpec = tween(
                    durationMillis = animateWithDurationMillis,
                    // easing = { fraction -> 1f - (1f - fraction).pow(1.5f) }
                    easing = EaseOutCubic,
                )
            )
        } else {
            animValue.snapTo(1f) // animation immediately completed
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = styles.marginTop, bottom = styles.marginBottom),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(styles.ringSize)
        ) {
            drawable.animValue = animValue.value
            drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
            drawIntoCanvas {
                drawable.draw(it.nativeCanvas)
            }
        }

        HintBalloon(
            wrappedContent = {
                Box(
                    modifier = Modifier
                        .size(styles.clickableAreaSize) // make the clickable area smaller than the actual drawable
                        .clickableNoRipple { showBalloon.value = true },
                )
            },
            lines = arrayOf(
                FuriganaBuilder.buildSpan(centreTextJa.raw),
                centreText.withSystemLang,
            ),
            open = showBalloon.value,
            onDismiss = { showBalloon.value = false },
        )
    }
}
