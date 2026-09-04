package io.github.digorydoo.goigoi.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

private const val ANIM_DURATION_MILLIS = 3000
private const val FADE_IN_MILLIS = 3000f
private const val WAVE_LENGTH = 512 // dp

private val shimmerColours by lazy {
    fun grey(intensity: Float): Color {
        val g = intensity * 0.5f
        return Color(g, g, g, g)
    }

    return@lazy listOf(
        grey(0.0f),
        grey(0.5f),
        grey(1.0f),
        grey(0.0f),
    )
}

@Composable
fun SkeletonSurface(modifier: Modifier = Modifier, shape: Shape = RectangleShape) {
    val density = LocalDensity.current
    val waveLengthPx = with(density) { WAVE_LENGTH.dp.toPx() }

    val startMillis = remember { System.currentTimeMillis() }
    val transition = rememberInfiniteTransition()

    val animValue = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(ANIM_DURATION_MILLIS, easing = LinearEasing)),
    )

    fun getBrush(): Brush {
        val phaseShiftPx = waveLengthPx * animValue.value
        return Brush.linearGradient(
            colors = shimmerColours,
            start = Offset(phaseShiftPx, 0f),
            end = Offset(waveLengthPx + phaseShiftPx, 0f),
            tileMode = TileMode.Repeated,
        )
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .drawWithContent {
                // drawContent() -- unnecessary, there is no content
                val millisPassed = System.currentTimeMillis() - startMillis
                val alpha = (millisPassed / FADE_IN_MILLIS).coerceAtMost(1f)
                val brush = getBrush()
                drawRect(brush, alpha = alpha)
            },
        shape = shape,
    ) {}
}
