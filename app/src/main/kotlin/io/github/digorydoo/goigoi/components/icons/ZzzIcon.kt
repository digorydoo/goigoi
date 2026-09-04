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
import io.github.digorydoo.goigoi.drawable.ZzzIconDrawable
import io.github.digorydoo.goigoi.drawable.ZzzIconDrawable.Colours
import io.github.digorydoo.goigoi.drawable.ZzzIconDrawable.Dimensions
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private interface ZzzIconStyles {
    val colours: Colours
    val dims: Dimensions
}

@Composable
private fun getStyles(): ZzzIconStyles {
    val colours = GoigoiTheme.colours
    val density = LocalDensity.current

    return remember(colours, density) {
        object: ZzzIconStyles {
            override val colours = object: Colours {
                override val background = colours.dimmedDecorativeIconBackground
                override val foreground = colours.onDimmedDecorativeIconBackground
            }
            override val dims = object: Dimensions {
                override val insetPx = with(density) { 1.dp.toPx() }.toInt()
            }
        }
    }
}

@Composable
fun ZzzIcon(
    modifier: Modifier = Modifier,
    animValue: Float = 1f, // 0..1
    size: Dp = 32.dp,
) {
    val ctx = LocalContext.current
    val styles = getStyles()

    val drawable = remember(ctx, styles) {
        ZzzIconDrawable(styles.colours, styles.dims)
    }

    Canvas(modifier = modifier.size(size)) {
        drawable.animValue = animValue
        drawable.setBounds(0, 0, this.size.width.toInt(), this.size.height.toInt())
        drawIntoCanvas { drawable.draw(it.nativeCanvas) }
    }
}
