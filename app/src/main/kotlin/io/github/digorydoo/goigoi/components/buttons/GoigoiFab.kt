package io.github.digorydoo.goigoi.components.buttons

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.legacy.drawable.FabIconDrawable
import io.github.digorydoo.goigoi.legacy.drawable.FabIconDrawable.IconName
import io.github.digorydoo.goigoi.providers.DeviceProps
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.ScreenSize
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val SIZE_ANIM_DURATION_MILLIS = 150
private const val GLOW_ANIM_DURATION_MILLIS = 1200

private interface GoigoiFabStyles {
    val horizontalMargin: Dp
    val marginBottom: Dp
    val shimWidth: Dp
    val colours: FabIconDrawable.Colours
    val dims: FabIconDrawable.Dimensions
}

@Composable
private fun getStyles(): GoigoiFabStyles {
    val density = LocalDensity.current
    val colours = GoigoiTheme.colours
    val screenSize = DeviceProps.size
    val isPortrait = DeviceProps.isPortrait

    return remember(density) {
        object: GoigoiFabStyles {
            override val horizontalMargin = when (screenSize) {
                ScreenSize.LARGE -> 32.dp
                ScreenSize.NORMAL -> if (isPortrait) 24.dp else 32.dp
                ScreenSize.SMALL -> 16.dp
            }

            override val marginBottom = if (isPortrait) 32.dp else 16.dp
            override val shimWidth = 8.dp

            override val colours = object: FabIconDrawable.Colours {
                override val normal = colours.fabBackground // green800
                override val pressed = colours.pressedFabBackground // green_700
                override val shim = colours.fabShimColour // see attr
                override val glow = colours.fabGlowColour // see attr
                override val icon = colours.onFabBackground // white
            }
            override val dims = object: FabIconDrawable.Dimensions {
                override val shimWidthPx = with(density) { shimWidth.roundToPx() }
                override val glowRadiusPx = with(density) { 8.dp.toPx() }
                override val outlinedIconStrokeWidthPx = with(density) { 2.5f.dp.toPx() }
            }
        }
    }
}

@Composable
fun GoigoiFab(
    modifier: Modifier = Modifier,
    shown: Boolean = true,
    allowGlow: Boolean = true,
    autoMarginBottom: Boolean = true,
    initialDelay: Duration = 200.milliseconds,
    iconName: IconName,
    contentDescription: String = "",
    background: Color? = null,
    size: Dp = 64.dp,
    onClick: () -> Unit,
) {
    val styles = getStyles()
    val drawable = remember { FabIconDrawable(iconName, styles.colours, styles.dims) }
    val sizeFactor = remember { Animatable(initialValue = 0f) }
    val interactionSrc = remember { MutableInteractionSource() }
    val stateSet = remember { mutableStateOf(intArrayOf()) }
    val glow = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val animationJob = remember { mutableStateOf<Job?>(null) }
    val sizeAnimSpec = tween<Float>(durationMillis = SIZE_ANIM_DURATION_MILLIS, easing = LinearEasing)
    val glowAnimSpec = tween<Float>(durationMillis = GLOW_ANIM_DURATION_MILLIS, easing = LinearEasing)
    val useInitialDelay = remember { mutableStateOf(true) }

    fun startGlowing() {
        if (!allowGlow) return
        animationJob.value?.cancel()
        animationJob.value = scope.launch {
            while (isActive) {
                glow.animateTo(1f, glowAnimSpec)
                glow.animateTo(0f, glowAnimSpec)
            }
        }
    }

    fun pauseGlowing() {
        if (!allowGlow) return
        animationJob.value?.cancel()
        animationJob.value = null

        scope.launch {
            // Make sure we leave glow value at 0f.
            glow.animateTo(0f, glowAnimSpec)
        }
    }

    LaunchedEffect(interactionSrc) {
        interactionSrc.interactions.collectLatest { interaction ->
            when (interaction) {
                is PressInteraction.Press -> stateSet.value = intArrayOf(android.R.attr.state_pressed)

                is PressInteraction.Cancel,
                is PressInteraction.Release,
                -> stateSet.value = intArrayOf()
            }
        }
    }

    LaunchedEffect(shown, allowGlow) {
        if (shown) {
            if (useInitialDelay.value) delay(initialDelay)
            sizeFactor.animateTo(1f, sizeAnimSpec)
            startGlowing()
        } else {
            sizeFactor.animateTo(0f, sizeAnimSpec)
            pauseGlowing()
        }
        useInitialDelay.value = false // never again until component is removed from composition
    }

    val theContentDescr = contentDescription.takeIf { it.isNotEmpty() }
        ?: when (iconName) {
            IconName.PLAY -> stringResource(R.string.start_btn)
            IconName.ARROW_RIGHT -> stringResource(R.string.next_word)
            IconName.NONE -> ""
        }

    Box(
        modifier = modifier
            .padding(horizontal = styles.horizontalMargin)
            .padding(bottom = if (autoMarginBottom) styles.marginBottom else 0.dp)
            .semantics {
                this.contentDescription = theContentDescr
            }
    ) {
        if (sizeFactor.value > 0f) {
            Canvas(
                modifier = Modifier
                    .size(size + styles.shimWidth * 2)
                    .clickable(
                        onClick = onClick,
                        indication = null, // hide the ripple effect
                        interactionSource = interactionSrc,
                    )
            ) {
                drawable.animValue = sizeFactor.value // let the drawable handle the scaling
                drawable.backgroundOverride = background
                drawable.glow = glow.value
                drawable.state = stateSet.value
                val sizePx = this.size
                drawable.setBounds(0, 0, sizePx.width.toInt(), sizePx.height.toInt())
                drawIntoCanvas { drawable.draw(it.nativeCanvas) }
            }
        }
    }
}
