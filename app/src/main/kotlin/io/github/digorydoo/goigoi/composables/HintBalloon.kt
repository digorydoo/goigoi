package io.github.digorydoo.goigoi.composables

import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Path
import android.text.Spanned
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import ch.digorydoo.kutils.math.clamp
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme

const val TIP_HEIGHT = 12 // dp
const val SHADOW_MARGIN = 12 // dp
const val SHADOW_MARGIN_TIP_TOP = 2 // dp
const val SHADOW_MARGIN_TIP_BOTTOM = 8 // dp

enum class HintBalloonDirection { UPWARDS, DOWNWARDS }

@Composable
private fun HintBalloon(
    wrappedContent: @Composable BoxScope.() -> Unit,
    balloonContent: @Composable BoxScope.() -> Unit,
    open: Boolean,
    onDismiss: () -> Unit,
) {
    val bgColour = GoigoiTheme.colours.primary
    val shadowColour = GoigoiTheme.colours.popupShadow
    val density = LocalDensity.current

    val innerPadding = 24.dp

    val dir = remember { mutableStateOf<HintBalloonDirection?>(null) }
    val tipXOffsetPx = remember { mutableIntStateOf(0) }

    val parentView = LocalView.current

    Box(
        modifier = Modifier
            .onGloballyPositioned { coords ->
                val r = coords.boundsInWindow()

                dir.value =
                    if ((r.top + r.bottom) / 2 < parentView.height / 2) HintBalloonDirection.DOWNWARDS
                    else HintBalloonDirection.UPWARDS
            }
    ) {
        wrappedContent()

        if (open && dir.value != null) {
            Popup(
                popupPositionProvider = remember(dir.value) {
                    object: PopupPositionProvider {
                        override fun calculatePosition(
                            anchorBounds: IntRect,
                            windowSize: IntSize,
                            layoutDirection: LayoutDirection,
                            popupContentSize: IntSize,
                        ): IntOffset {
                            val anchorCentreX = (anchorBounds.left + anchorBounds.right) / 2

                            val x = clamp(
                                anchorCentreX - popupContentSize.width / 2,
                                0,
                                windowSize.width - popupContentSize.width
                            )

                            val y = when (dir.value) {
                                HintBalloonDirection.UPWARDS -> anchorBounds.top - popupContentSize.height
                                HintBalloonDirection.DOWNWARDS, null -> anchorBounds.bottom
                            }

                            tipXOffsetPx.intValue = anchorCentreX - x
                            return IntOffset(x, y)
                        }
                    }
                },
                onDismissRequest = onDismiss,
                properties = PopupProperties(clippingEnabled = false)
            ) {
                Box(
                    Modifier
                        .widthIn(max = with(density) { parentView.width.toDp() })
                        .drawBehind {
                            dir.value?.let { currentDir ->
                                drawBalloon(
                                    dir = currentDir,
                                    anchorX = tipXOffsetPx.intValue,
                                    bgColour = bgColour,
                                    shadowColour = shadowColour,
                                    density = density,
                                )
                            }
                        }
                        .padding(
                            start = innerPadding,
                            end = innerPadding,
                            top = innerPadding +
                                (if (dir.value == HintBalloonDirection.DOWNWARDS) SHADOW_MARGIN_TIP_TOP else 0).dp,
                            bottom = innerPadding +
                                (if (dir.value == HintBalloonDirection.UPWARDS) SHADOW_MARGIN_TIP_BOTTOM else 0).dp,
                        )
                ) {
                    balloonContent()
                }
            }
        }
    }
}

@Composable
fun HintBalloon(
    wrappedContent: @Composable BoxScope.() -> Unit,
    lines: Array<CharSequence>,
    open: Boolean,
    onDismiss: () -> Unit,
) {
    val textColour = GoigoiTheme.colours.onPrimary
    val textStyle: TextStyle = GoigoiTheme.typography.listItemPrimaryText

    require(textStyle.fontSize.type == TextUnitType.Sp)
    val textSizeSp = textStyle.fontSize.value

    HintBalloon(
        open = open,
        wrappedContent = wrappedContent,
        balloonContent = {
            Column {
                for (line in lines) {
                    when (line) {
                        is AnnotatedString -> Text(text = line, color = textColour, style = textStyle)

                        // Our furigana is still using legacy Spanned
                        is Spanned -> AndroidView(
                            factory = { ctx -> TextView(ctx) },
                            update = { textView ->
                                textView.apply {
                                    text = line
                                    setTextColor(textColour.toArgb())
                                    setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)
                                }
                            }
                        )

                        else -> Text(text = line.toString(), color = textColour, style = textStyle)
                    }
                }
            }
        },
        onDismiss = onDismiss
    )
}

private fun DrawScope.drawBalloon(
    dir: HintBalloonDirection,
    anchorX: Int,
    bgColour: Color,
    shadowColour: Color,
    density: Density,
) {
    val width = size.width
    val height = size.height

    val shadowMarginPx = with(density) { SHADOW_MARGIN.dp.roundToPx() }
    val shadowMarginTipTopPx = with(density) { SHADOW_MARGIN_TIP_TOP.dp.roundToPx() }
    val shadowMarginTipBottomPx = with(density) { SHADOW_MARGIN_TIP_BOTTOM.dp.roundToPx() }
    val cornerSizePx = with(density) { 16.dp.toPx() }
    val tipWidthPx = with(density) { 12.dp.toPx() }
    val tipHeightPx = with(density) { TIP_HEIGHT.dp.toPx() }
    val shadowRadiusPx = with(density) { 6.4.dp.toPx() }
    val shadowDyPx = with(density) { 3.dp.toPx() }

    val shadowMarginTop =
        if (dir == HintBalloonDirection.DOWNWARDS) shadowMarginTipTopPx
        else shadowMarginPx

    val shadowMarginBottom =
        if (dir == HintBalloonDirection.UPWARDS) shadowMarginTipBottomPx
        else shadowMarginPx

    val x1 = shadowMarginPx.toFloat()
    val x2 = width - shadowMarginPx
    val y1 = shadowMarginTop.toFloat()
    val y2 = height - shadowMarginBottom

    var ax1 = anchorX - tipWidthPx / 2f
    var ax2 = anchorX + tipWidthPx / 2f

    if (ax1 < x1 + cornerSizePx) {
        val dx = x1 + cornerSizePx - ax1
        ax1 += dx
        ax2 += dx
    } else if (ax2 > x2 - cornerSizePx) {
        val dx = ax2 - (x2 - cornerSizePx)
        ax1 -= dx
        ax2 -= dx
    }

    val x1c = x1 + cornerSizePx
    val x2c = x2 - cornerSizePx
    val y1c = y1 + cornerSizePx
    val y2c = y2 - cornerSizePx

    val path = Path().apply {
        when (dir) {
            HintBalloonDirection.DOWNWARDS -> {
                arcTo(x1, y1 + tipHeightPx, x1c, y1c + tipHeightPx, 270f, -90f, false)
                arcTo(x1, y2c, x1c, y2, 180f, -90f, false)
                arcTo(x2c, y2c, x2, y2, 90f, -90f, false)
                arcTo(x2c, y1 + tipHeightPx, x2, y1c + tipHeightPx, 0f, -90f, false)
                lineTo(ax2, y1 + tipHeightPx)
                lineTo((ax1 + ax2) / 2f, y1)
                lineTo(ax1, y1 + tipHeightPx)
            }
            HintBalloonDirection.UPWARDS -> {
                arcTo(x1, y1, x1c, y1c, 270f, -90f, false)
                arcTo(x1, y2c - tipHeightPx, x1c, y2 - tipHeightPx, 180f, -90f, false)
                lineTo(ax1, y2 - tipHeightPx)
                lineTo((ax1 + ax2) / 2f, y2)
                lineTo(ax2, y2 - tipHeightPx)
                arcTo(x2c, y2c - tipHeightPx, x2, y2 - tipHeightPx, 90f, -90f, false)
                arcTo(x2c, y1, x2, y1c, 0f, -90f, false)
            }
        }
        close()
    }

    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            isAntiAlias = true
            color = bgColour.toArgb()
            style = Style.FILL
            setShadowLayer(shadowRadiusPx, 0.0f, shadowDyPx, shadowColour.toArgb())
        }
        canvas.nativeCanvas.drawPath(path, paint)
    }
}
