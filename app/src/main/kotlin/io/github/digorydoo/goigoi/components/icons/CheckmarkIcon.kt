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
import io.github.digorydoo.goigoi.drawable.CheckmarkIconDrawable
import io.github.digorydoo.goigoi.drawable.CheckmarkIconDrawable.Colours
import io.github.digorydoo.goigoi.drawable.CheckmarkIconDrawable.Dimensions
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private interface CheckmarkIconStyles {
    val colours: Colours
    val dims: Dimensions
}

@Composable
private fun getStyles(): CheckmarkIconStyles {
    val colours = GoigoiTheme.colours
    val density = LocalDensity.current

    return remember(colours, density) {
        object: CheckmarkIconStyles {
            override val colours = object: Colours {
                override val background = colours.primary
                override val mark = colours.onPrimary
            }
            override val dims = object: Dimensions {
                override val insetPx = with(density) { 1.dp.toPx() }.toInt()
                override val markMinSizePx = with(density) { 23.dp.toPx() }.toInt()
            }
        }
    }
}

@Composable
fun CheckmarkIcon(
    modifier: Modifier = Modifier,
    animValue: Float = 1f, // 0..1
    size: Dp = 32.dp,
) {
    val ctx = LocalContext.current
    val styles = getStyles()

    val drawable = remember(ctx, styles) {
        CheckmarkIconDrawable(styles.colours, styles.dims)
    }

    Canvas(modifier = modifier.size(size)) {
        drawable.animValue = animValue
        drawable.setBounds(0, 0, this.size.width.toInt(), this.size.height.toInt())
        drawIntoCanvas { drawable.draw(it.nativeCanvas) }
    }
}
