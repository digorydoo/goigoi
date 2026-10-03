package io.github.digorydoo.goigoi.composables.buttons

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.ScreenSize
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private const val SIZE_ANIM_DURATION_MILLIS = 150

enum class IconName { START, NEXT_WORD }

private interface GoigoiFabStyles {
    val horizontalMargin: Dp
    val marginBottom: Dp
}

@Composable
private fun getStyles(): GoigoiFabStyles {
    val density = LocalDensity.current
    val screenSize = DeviceProps.size
    val isPortrait = DeviceProps.isPortrait

    return remember(density) {
        object: GoigoiFabStyles {
            override val horizontalMargin = when (screenSize) {
                ScreenSize.LARGE -> 32.dp
                ScreenSize.NORMAL -> if (isPortrait) 32.dp else 40.dp
                ScreenSize.SMALL -> 16.dp
            }

            override val marginBottom = if (isPortrait) 32.dp else 16.dp
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
    background: Color = GoigoiTheme.colours.primary,
    iconName: IconName = IconName.START,
    iconColour: Color = GoigoiTheme.colours.onPrimary,
    contentDescription: String = "",
    size: Dp = 64.dp,
    onClick: () -> Unit,
) {
    val styles = getStyles()

    val img = ImageVector.vectorResource(
        when (iconName) {
            IconName.START -> R.drawable.ic_play_24dp
            IconName.NEXT_WORD -> R.drawable.ic_arrow_right_24dp
        }
    )

    val theContentDescr = contentDescription.takeIf { it.isNotEmpty() }
        ?: when (iconName) {
            IconName.START -> stringResource(R.string.start_btn)
            IconName.NEXT_WORD -> stringResource(R.string.next_word)
        }

    val sizeFactor = remember { Animatable(initialValue = 0f) }
    val sizeAnimSpec = tween<Float>(durationMillis = SIZE_ANIM_DURATION_MILLIS, easing = LinearEasing)
    val useInitialDelay = remember { mutableStateOf(true) }

    LaunchedEffect(shown, allowGlow) {
        if (shown) {
            if (useInitialDelay.value) delay(initialDelay)
            sizeFactor.animateTo(1f, sizeAnimSpec)
        } else {
            sizeFactor.animateTo(0f, sizeAnimSpec)
        }
        useInitialDelay.value = false // never again until component is removed from composition
    }

    Box(
        modifier = modifier
            .padding(horizontal = styles.horizontalMargin)
            .padding(bottom = if (autoMarginBottom) styles.marginBottom else 0.dp)
            .size(size),
        contentAlignment = Alignment.Center,
    ) {
        if (sizeFactor.value > 0f) {
            Surface(
                modifier = Modifier.size(size * sizeFactor.value),
                color = background,
                shape = CircleShape,
                tonalElevation = 0.dp,
                shadowElevation = 16.dp,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onClick)
                        .semantics { this.contentDescription = theContentDescr },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        modifier = Modifier.size(size * 0.6f * sizeFactor.value),
                        imageVector = img,
                        tint = iconColour,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}
