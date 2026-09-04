package io.github.digorydoo.goigoi.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.drawable.SheetHeadDrawable
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private interface SheetHeadStyles {
    val sheetHeadHeight: Dp
    val sheetHeadDims: SheetHeadDrawable.Dimensions
    val sheetHeadColours: SheetHeadDrawable.Colours
}

// FIXME In landscape, the SheetHead should cover the area of the nav bar / status bar
//    Alternative: Don't cover that area from AppBar
//    Alternative: Don't call edgeToEdge. But then we can no longer control that status bar/nav bar area easily
//       And it may also affect the positioning of the hint balloon

@Composable
private fun getStyles(): SheetHeadStyles {
    val density = LocalDensity.current
    val themeColours = GoigoiTheme.colours

    fun dpToPx(value: Dp) = with(density) { value.toPx() }

    val cornerSize = 24.dp

    return remember(density) {
        object: SheetHeadStyles {
            override val sheetHeadHeight = cornerSize + 8.dp

            override val sheetHeadDims = object: SheetHeadDrawable.Dimensions {
                override val cornerSizePx = dpToPx(cornerSize).toInt()
                override val headHeightPx = dpToPx(sheetHeadHeight).toInt()
            }

            override val sheetHeadColours = object: SheetHeadDrawable.Colours {
                override val appBar = themeColours.appBarContainer
            }
        }
    }
}

@Composable
fun SheetHead(modifier: Modifier = Modifier) {
    val styles = getStyles()
    val sheetHead = remember { SheetHeadDrawable(styles.sheetHeadColours, styles.sheetHeadDims) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(styles.sheetHeadHeight)
    ) {
        sheetHead.setBounds(0, 0, size.width.toInt(), size.height.toInt())
        drawIntoCanvas { sheetHead.draw(it.nativeCanvas) }
    }
}
