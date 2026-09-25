package io.github.digorydoo.goigoi.components.buttons

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private const val ANIM_DURATION_MILLIS = 150

/**
 * This component wraps IconButton in a way that is suitable when it may potentially overlap scrollable content. Prefer
 * IconButton directly if you can. If the button should also be prominent and visually contained with a shadow, use
 * GoigoiFab instead.
 */
@Composable
fun OverlappingIconBtn(
    @DrawableRes iconResId: Int,
    modifier: Modifier = Modifier,
    shown: Boolean = true,
    onClick: () -> Unit,
) {
    val animValue = remember { Animatable(initialValue = if (shown) 1f else 0f) }
    val animSpec = tween<Float>(durationMillis = ANIM_DURATION_MILLIS, easing = LinearEasing)

    LaunchedEffect(shown) {
        if (shown) {
            animValue.animateTo(1f, animSpec)
        } else {
            animValue.animateTo(0f, animSpec)
        }
    }

    if (animValue.value <= 0f) return

    Surface(
        modifier = modifier.alpha(animValue.value), // .scale(sizeFactor.value),
        shape = CircleShape,
        color = GoigoiTheme.colours.background.copy(alpha = 0.9f),
    ) {
        IconButton(
            onClick = { if (shown) onClick() },
        ) {
            Icon(
                // Special case for the info icon: It looks too small in its original size, so we scale it.
                modifier = if (iconResId == R.drawable.ic_info_24dp) Modifier.scale(4f / 3f) else Modifier,
                imageVector = ImageVector.vectorResource(iconResId),
                contentDescription = null,
                tint = GoigoiTheme.colours.onBackground,
            )
        }
    }
}
