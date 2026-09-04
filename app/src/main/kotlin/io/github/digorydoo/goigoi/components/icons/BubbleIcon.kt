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
import io.github.digorydoo.goigoi.drawable.BubbleIconDrawable
import io.github.digorydoo.goigoi.drawable.BubbleIconDrawable.Colours
import io.github.digorydoo.goigoi.drawable.BubbleIconDrawable.Dimensions
import io.github.digorydoo.goigoi.drawable.BubbleIconDrawable.Variant
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private interface BubbleIconStyles {
    val colours: Colours
    val dims: Dimensions
}

@Composable
private fun getStyles(): BubbleIconStyles {
    val colours = GoigoiTheme.colours
    val density = LocalDensity.current

    fun dpToPx(value: Dp) = with(density) { value.toPx() }

    return remember(colours, density) {
        object: BubbleIconStyles {
            override val colours = object: Colours {
                override val poorRating = colours.poorRating
                override val goodRating = colours.primary
                override val background = colours.bubbleBackground
                override val outline = colours.bubbleOutline
            }
            override val dims = object: Dimensions {
                override val circularInsetPx = dpToPx(2.dp).toInt()
                override val minBubbleSizePx = dpToPx(2.dp)
                override val outlineStrokeWidthPx = dpToPx(1.dp)
            }
        }
    }
}

@Composable
fun BubbleIcon(
    rating: Float, // 0..1
    variant: Variant,
    modifier: Modifier = Modifier,
    animValue: Float = 1f, // 0..1
    size: Dp = 32.dp,
) {
    val ctx = LocalContext.current
    val styles = getStyles()

    val drawable = remember(ctx, rating, variant, styles) {
        BubbleIconDrawable(variant, rating, styles.colours, styles.dims)
    }

    Canvas(modifier = modifier.size(size)) {
        drawable.animValue = animValue
        drawable.setBounds(0, 0, this.size.width.toInt(), this.size.height.toInt())
        drawIntoCanvas { drawable.draw(it.nativeCanvas) }
    }
}
