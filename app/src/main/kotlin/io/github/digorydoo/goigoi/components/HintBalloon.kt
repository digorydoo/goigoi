package io.github.digorydoo.goigoi.components

import android.graphics.Rect
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
import io.github.digorydoo.goigoi.drawable.HintBalloonDrawable
import io.github.digorydoo.goigoi.drawable.HintBalloonDrawable.Companion.SHADOW_MARGIN
import io.github.digorydoo.goigoi.drawable.HintBalloonDrawable.Companion.SHADOW_MARGIN_TIP_BOTTOM
import io.github.digorydoo.goigoi.drawable.HintBalloonDrawable.Companion.SHADOW_MARGIN_TIP_TOP
import io.github.digorydoo.goigoi.drawable.HintBalloonDrawable.Companion.TIP_HEIGHT
import io.github.digorydoo.goigoi.drawable.HintBalloonDrawable.Direction
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private interface HintBalloonStyles {
    val innerPadding: Dp // includes the shadow margin
    val shadowMarginPx: Int
    val shadowMarginTipTopPx: Int
    val shadowMarginTipBottomPx: Int
    val drawableColours: HintBalloonDrawable.Colours
    val drawableDims: HintBalloonDrawable.Dimensions
}

@Composable
private fun getStyles(): HintBalloonStyles {
    val themeColours = GoigoiTheme.colours
    val density = LocalDensity.current
    fun dpToPx(value: Dp) = with(density) { value.toPx() }

    return remember(themeColours, density) {
        object: HintBalloonStyles {
            override val innerPadding = 24.dp
            override val shadowMarginPx = dpToPx(SHADOW_MARGIN.dp).toInt()
            override val shadowMarginTipTopPx = dpToPx(SHADOW_MARGIN_TIP_TOP.dp).toInt()
            override val shadowMarginTipBottomPx = dpToPx(SHADOW_MARGIN_TIP_BOTTOM.dp).toInt()
            override val drawableColours = object: HintBalloonDrawable.Colours {
                override val background = themeColours.primary
                override val outline = themeColours.popupOutline
                override val shadow = themeColours.popupShadow
            }
            override val drawableDims = object: HintBalloonDrawable.Dimensions {
                override val cornerSizePx = dpToPx(16.dp)
                override val tipWidthPx = dpToPx(12.dp)
                override val tipHeightPx = dpToPx(TIP_HEIGHT.dp)
                override val shadowRadiusPx = dpToPx(6.4.dp)
                override val shadowDyPx = dpToPx(3.dp)
                override val outlineStrokeWidthPx = dpToPx(2.dp)
            }
        }
    }
}

@Composable
private fun HintBalloon(
    wrappedContent: @Composable BoxScope.() -> Unit,
    balloonContent: @Composable BoxScope.() -> Unit,
    open: Boolean,
    onDismiss: () -> Unit,
) {
    val styles = getStyles()

    val dir = remember { mutableStateOf<Direction?>(null) }
    val tipXOffsetPx = remember { mutableIntStateOf(0) }

    val drawable = remember(tipXOffsetPx.intValue, dir.value) {
        dir.value?.let { theDir ->
            val shadowMargin = Rect(
                styles.shadowMarginPx,
                if (theDir == Direction.DOWNWARDS) styles.shadowMarginTipTopPx else styles.shadowMarginPx,
                styles.shadowMarginPx,
                if (theDir == Direction.UPWARDS) styles.shadowMarginTipBottomPx else styles.shadowMarginPx,
            )
            HintBalloonDrawable(
                tipXOffsetPx.intValue,
                theDir,
                shadowMargin,
                styles.drawableColours,
                styles.drawableDims
            )
        }
    }

    val parentView = LocalView.current
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            // .background(Color(1f, 1f, 0f, 0.1f))
            .onGloballyPositioned { coords ->
                val r = coords.boundsInWindow()

                dir.value =
                    if ((r.top + r.bottom) / 2 < parentView.height / 2) Direction.DOWNWARDS
                    else Direction.UPWARDS
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
                                Direction.UPWARDS -> anchorBounds.top - popupContentSize.height
                                Direction.DOWNWARDS, null -> anchorBounds.bottom
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
                            drawable?.let {
                                it.setBounds(0, 0, size.width.toInt(), size.height.toInt())
                                drawIntoCanvas { canvas -> it.draw(canvas.nativeCanvas) }
                            }
                        }
                        .padding(
                            start = styles.innerPadding,
                            end = styles.innerPadding,
                            top = styles.innerPadding +
                                (if (dir.value == Direction.DOWNWARDS) SHADOW_MARGIN_TIP_TOP else 0).dp,
                            bottom = styles.innerPadding +
                                (if (dir.value == Direction.UPWARDS) SHADOW_MARGIN_TIP_BOTTOM else 0).dp,
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
                            factory = { context ->
                                TextView(context).apply {
                                    setTextColor(textColour.toArgb())
                                    setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)
                                }
                            },
                            update = { it.text = line }
                        )

                        else -> Text(text = line.toString(), color = textColour, style = textStyle)
                    }
                }
            }
        },
        onDismiss = onDismiss
    )
}
