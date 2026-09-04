package io.github.digorydoo.goigoi.activity.welcome.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.drawable.BigStudyBtnDrawable
import io.github.digorydoo.goigoi.drawable.BitmapPool
import io.github.digorydoo.goigoi.providers.DeviceProps
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.ScreenSize

private interface BigStudyBtnStyles {
    val marginTop: Dp
    val marginBottom: Dp
    val drawableColours: BigStudyBtnDrawable.Colours
    val drawableDims: BigStudyBtnDrawable.Dimensions
}

@Composable
private fun getStyles(): BigStudyBtnStyles {
    val themeColours = GoigoiTheme.colours
    val density = LocalDensity.current
    val screenSize = DeviceProps.size

    // This colour is not part of the theme; it should match the colour of our drawable's margin area
    val bgColour = Color(0x26, 0x34, 0x47)

    fun dpToPx(value: Dp) = with(density) { value.toPx() }

    return remember(themeColours, density, screenSize) {
        object : BigStudyBtnStyles {
            override val marginTop = 8.dp
            override val marginBottom = 16.dp
            override val drawableColours = object : BigStudyBtnDrawable.Colours {
                override val background = bgColour
                override val text = themeColours.onPrimary
            }
            override val drawableDims = object : BigStudyBtnDrawable.Dimensions {
                override val leftPaddingPx = dpToPx(24.dp)
                override val primaryTextVDeltaPx = dpToPx(48.dp)
                override val secondaryTextVDeltaPx = dpToPx(24.dp)
                override val outerCornerSizePx = dpToPx(16.dp)
                override val textShadowSizePx = dpToPx(1.dp)

                override val primaryTextSizePx = when (screenSize) {
                    ScreenSize.LARGE -> dpToPx(36.dp)
                    else -> dpToPx(28.dp)
                }

                override val secondaryTextSizePx = when (screenSize) {
                    ScreenSize.LARGE -> dpToPx(16.dp)
                    else -> dpToPx(13.dp)
                }
            }
        }
    }
}

@Composable
fun BigStudyBtn(paddingLR: Dp, onClick: () -> Unit) {
    val ctx = LocalContext.current
    val secondaryText = stringResource(R.string.continue_studying)
    val styles = getStyles()

    val drawable = remember(ctx, styles) {
        val primaryText = "つづきへ"
        val bgndBitmap = BitmapPool.getFromAssets("img/btn-00-continue.webp", ctx)
        BigStudyBtnDrawable(styles.drawableColours, styles.drawableDims, primaryText, secondaryText, bgndBitmap)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = styles.marginTop,
                bottom = styles.marginBottom
            )
            // Ripple effect is inside top-bottom padding, but around start-end padding!
            .clickable(onClick = onClick)
            .padding(start = paddingLR, end = paddingLR)
    ) {
        // We can use aspectRatio modifier to ensure a certain aspect ratio based on width.
        // More generally, BoxWithConstraints would give us the size of the box.
        // Another possibility is the onSizeChanged modifier, which calls a lambda whenever the size changes.
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 7f)
        ) {
            drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
            drawIntoCanvas { drawable.draw(it.nativeCanvas) }
        }
    }
}
