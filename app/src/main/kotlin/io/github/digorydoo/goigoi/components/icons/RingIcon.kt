package io.github.digorydoo.goigoi.components.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.drawable.RingIconDrawable
import io.github.digorydoo.goigoi.drawable.RingIconDrawable.Colours
import io.github.digorydoo.goigoi.drawable.RingIconDrawable.Dimensions
import io.github.digorydoo.goigoi.drawable.RingIconDrawable.Variant
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private interface RingIconStyles {
    val colours: Colours
    val dims: Dimensions
}

@Composable
private fun getStyles(): RingIconStyles {
    val colours = GoigoiTheme.colours
    val density = LocalDensity.current

    return remember(colours, density) {
        object: RingIconStyles {
            override val colours = object: Colours {
                override val trail = colours.ring
                override val track = colours.faintRing
            }
            override val dims = object: Dimensions {
                override val circularInsetSizePx = with(density) { 1.dp.toPx() }.toInt()
                override val minStrokeWidthPx = with(density) { 3.dp.toPx() }
            }
        }
    }
}

@Composable
fun RingIcon(
    progress: Float, // 0..1
    variant: Variant,
    modifier: Modifier = Modifier,
    animValue: Float = 1f, // 0..1
    size: Dp = 32.dp,
) {
    val ctx = LocalContext.current
    val styles = getStyles()

    val drawable = remember(ctx, progress, variant, styles) {
        RingIconDrawable(variant, progress, styles.colours, styles.dims)
    }

    Canvas(modifier = modifier.size(size)) {
        drawable.animValue = animValue
        drawable.setBounds(0, 0, this.size.width.toInt(), this.size.height.toInt())
        drawIntoCanvas { drawable.draw(it.nativeCanvas) }
    }
}
