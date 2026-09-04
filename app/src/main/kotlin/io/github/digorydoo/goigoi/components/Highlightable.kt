package io.github.digorydoo.goigoi.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import ch.digorydoo.kutils.math.accel
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private const val ANIM_DELAY_MILLIS = 150
private const val ANIM_DURATION_MILLIS = 800

/**
 * Highlights the area for a short time when highlightOnce goes from false to true. Does not do anything particular when
 * highlightOnce goes from true to false. To run the animation again, the caller needs to set highlightOnce to false
 * before setting it to true again.
 */
@Composable
fun Highlightable(
    highlightOnce: Boolean,
    modifier: Modifier = Modifier,
    onAnimationCompleted: () -> Unit,
    content: @Composable () -> Unit,
) {
    val themeColours = GoigoiTheme.colours

    val gradientColours = remember(themeColours) {
        val c = themeColours.highlight
        listOf(
            c.copy(alpha = 0f),
            c.copy(alpha = c.alpha * 0.95f),
            c.copy(alpha = c.alpha * 0.98f),
            c.copy(alpha = c.alpha * 1.0f),
            c.copy(alpha = 0f),
        )
    }

    val animValue = remember { Animatable(1f) }

    LaunchedEffect(highlightOnce) {
        if (highlightOnce) {
            animValue.snapTo(1f)
            animValue.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    delayMillis = ANIM_DELAY_MILLIS,
                    durationMillis = ANIM_DURATION_MILLIS,
                    // easing = { fraction -> 1f - (1f - fraction).pow(1.5f) }
                    easing = LinearEasing,
                )
            )
            // We come here when the animation has completed (we're in a coroutine!)
            onAnimationCompleted()
        }
    }

    fun getBrush(widthPx: Float): Brush {
        val gradientWidthPx = widthPx * 3
        // The gradient starts at the component's start border at phase = 0.
        // The gradient is fully pushed out of the end border at phase = widthPx, which is the end of the transition.
        // The anim value sweeps from 1 to 0.
        val phase = widthPx - (gradientWidthPx + widthPx) * animValue.value
        return Brush.linearGradient(
            colors = gradientColours,
            start = Offset(phase, 0f),
            end = Offset(gradientWidthPx + phase, 0f),
            tileMode = TileMode.Clamp,
        )
    }

    Box(
        modifier = modifier.drawWithContent {
            drawContent()

            if (highlightOnce) {
                val brush = getBrush(size.width)
                val a = 1f - animValue.value // 0..1
                drawRect(brush, alpha = 1f - accel(a, 1.3f))
            }
        },
    ) {
        content()
    }
}
