package io.github.digorydoo.goigoi.composables.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun UnytIcon(progress: Float, rating: Float, asleep: Boolean, size: Dp, modifier: Modifier = Modifier) {
    when {
        asleep -> ZzzIcon(modifier = modifier, size = size)
        progress < 1f -> DiamondIcon(progress, modifier = modifier, size = size)
        else -> BubbleIcon(rating, BubbleIconVariant.DIAMOND, modifier = modifier, size = size)
    }
}
