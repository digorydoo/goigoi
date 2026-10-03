package io.github.digorydoo.goigoi.activity.welcome.composables

import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.colour.Colour
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.legacy.drawable.Artist
import io.github.digorydoo.goigoi.legacy.drawable.BitmapPool
import io.github.digorydoo.goigoi.legacy.spannable.FuriganaBuilder
import io.github.digorydoo.goigoi.utils.ScreenSize

@Composable
fun BigStudyBtn(horizPadding: Dp, onClick: () -> Unit) {
    val ctx = LocalContext.current
    val primaryTextWithFurigana = "つづきへ"
    val onPrimary = GoigoiTheme.colours.onPrimary

    val density = LocalDensity.current
    val screenSize = DeviceProps.size

    val leftPaddingPx = with(density) { 24.dp.toPx() }
    val primaryTextVDeltaPx = with(density) { 24.dp.toPx() }
    val textShadowSizePx = with(density) { 1.dp.toPx() }

    val primaryTextSizePx = when (screenSize) {
        ScreenSize.LARGE -> with(density) { 40.dp.toPx() }
        else -> with(density) { 32.dp.toPx() }
    }

    val bgndPaint = remember {
        Paint().apply {
            style = Style.FILL
            // This colour is not part of the theme; it matches the average colour of our bitmap.
            color = Color(0x26, 0x34, 0x47).toArgb()
        }
    }

    val primaryTextPaint = remember(onPrimary, primaryTextSizePx) {
        Paint().apply {
            isAntiAlias = true
            style = Style.FILL
            color = onPrimary.toArgb()
            textSize = primaryTextSizePx
        }
    }

    val bgndBitmap = remember(ctx) {
        BitmapPool.getFromAssets("img/btn-00-continue.webp", ctx)
    }

    val primaryTextSpan = remember(primaryTextWithFurigana) {
        FuriganaBuilder.buildSpan(primaryTextWithFurigana, canSeeFurigana = false)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .padding(start = horizPadding, end = horizPadding)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 7f)
                .clip(RoundedCornerShape(size = 16.dp))
                .clickable(onClick = onClick)
        ) {
            val width = size.width
            val height = size.height

            if (width <= 0f || height <= 0f) return@Canvas

            drawIntoCanvas { canvas ->
                val nativeCanvas = canvas.nativeCanvas
                val dstR = RectF(0f, 0f, width, height)

                if (bgndBitmap == null) {
                    nativeCanvas.drawRect(dstR, bgndPaint)
                } else {
                    Artist.drawBitmapScaleToFit(bgndBitmap, dstR, bgndPaint, nativeCanvas, 0, 0)
                }

                val y = height - primaryTextVDeltaPx
                primaryTextPaint.setShadowLayer(textShadowSizePx, 0.0f, 0.0f, Colour.black.toARGB())
                Artist.drawSpan(primaryTextSpan, leftPaddingPx, y, primaryTextPaint, nativeCanvas)
            }
        }
    }
}
