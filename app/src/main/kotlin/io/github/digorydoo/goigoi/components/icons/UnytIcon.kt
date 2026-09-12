package io.github.digorydoo.goigoi.components.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.digorydoo.goigoi.legacy.drawable.BubbleIconDrawable
import io.github.digorydoo.goigoi.legacy.drawable.RingIconDrawable.Variant

@Composable
fun UnytIcon(progress: Float, rating: Float, asleep: Boolean, size: Dp, modifier: Modifier = Modifier) {
    when {
        asleep -> ZzzIcon(
            modifier = modifier,
            size = size
        )
        progress < 1f -> RingIcon(
            progress,
            Variant.DIAMOND,
            modifier = modifier,
            size = size
        )
        else -> BubbleIcon(
            rating,
            BubbleIconDrawable.Variant.DIAMOND,
            modifier = modifier,
            size = size
        )
    }
}
