package io.github.digorydoo.goigoi.composables.text

import android.graphics.Typeface
import android.text.Spanned
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import ch.digorydoo.kutils.cjk.FuriganaString
import io.github.digorydoo.goigoi.legacy.DEFAULT_FURIGANA_OPACITY
import io.github.digorydoo.goigoi.legacy.FuriganaSpan
import io.github.digorydoo.goigoi.legacy.buildSpan

@Composable
fun FuriganaText(
    text: Spanned,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    color: Color = Color.Unspecified,
    typeface: Typeface = Typeface.DEFAULT, // TODO use FontStyle/FontWeight/FontFamily
) {
    val density = LocalDensity.current
    val fontSizePx = with(density) { fontSize.toPx() }
    val textColour = color.takeOrElse { LocalContentColor.current }

    AndroidView(
        modifier = modifier,
        factory = { ctx -> TextView(ctx) },
        update = { view ->
            view.text = text
            view.typeface = typeface
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSizePx)
            view.setTextColor(textColour.toArgb())
        }
    )
}

@Composable
fun FuriganaText(
    text: FuriganaString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    typeface: Typeface = Typeface.DEFAULT,
    textFontSize: TextUnit = 16.sp,
    furiganaFontSizeFactor: Float = 0.5f,
    furiganaFontSizeMin: Dp = 0.dp,
    furiganaFontSizeMax: Dp = Dp.Unspecified,
    furiganaOpacity: Float = DEFAULT_FURIGANA_OPACITY,
    furiganaRelVOffset: Float = 0.3f,
    showFurigana: Boolean = true,
) {
    val density = LocalDensity.current
    val furiganaFontSizeMinPx = with(density) { furiganaFontSizeMin.toPx() }

    val furiganaFontSizeMaxPx =
        if (furiganaFontSizeMax.isUnspecified) Float.POSITIVE_INFINITY
        else with(density) { furiganaFontSizeMax.toPx() }

    val options = remember(
        showFurigana,
        furiganaFontSizeFactor,
        furiganaFontSizeMinPx,
        furiganaFontSizeMaxPx,
        furiganaOpacity,
        furiganaRelVOffset,
    ) {
        FuriganaSpan.Options(
            canSeeFurigana = showFurigana,
            fontSizeFactor = furiganaFontSizeFactor,
            fontSizeMin = furiganaFontSizeMinPx,
            fontSizeMax = furiganaFontSizeMaxPx,
            opacity = furiganaOpacity,
            relVOffset = furiganaRelVOffset,
        )
    }

    val span = remember(text, options) { text.buildSpan(options) }

    FuriganaText(
        text = span,
        modifier = modifier,
        fontSize = textFontSize,
        color = color,
        typeface = typeface,
    )
}
