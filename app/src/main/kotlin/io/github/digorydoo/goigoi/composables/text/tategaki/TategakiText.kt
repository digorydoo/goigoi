package io.github.digorydoo.goigoi.composables.text.tategaki

import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Typeface
import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.digorydoo.kutils.cjk.FuriganaString
import io.github.digorydoo.goigoi.core.tategaki.TategakiLayout
import io.github.digorydoo.goigoi.legacy.DEFAULT_FURIGANA_OPACITY
import kotlin.math.roundToInt

private val COLUMN_SPACING = 16.dp // adds to the width of any furigana present

private val FURIGANA_X_DELTA = 4.dp
private val FURIGANA_MAX_SIZE = 18.dp
private val EMPTY_FURIGANA_WIDTH = 4.dp // must be less than the minimal expected furigana size
private const val FURIGANA_REL_SIZE = 0.42f

private val DOTTED_LINE_MARGIN = 16.dp
private val DOTTED_LINE_STROKE_WIDTH = 1.5.dp
private val DOTTED_LINE_DOT_LENGTH = 2.dp
private val DOTTED_LINE_GAP_LENGTH = 4.dp
private const val DOTTED_LINE_OPACITY = 0.42f

class TategakiProps(
    val columnSpacing: Int, // px
    val charWidth: Int, // px
    val charHeight: Int, // px
    val furiganaCharWidth: Int, // px
    val furiganaXDelta: Int, // px
    val emptyFuriganaWidth: Int, // px
    val textPaint: TextPaint,
    val furiganaPaint: TextPaint,
    val dottedLineMargin: Float, // px
    val dottedLinePaint: Paint,
)

@Composable
fun TategakiText(
    text: FuriganaString,
    modifier: Modifier = Modifier,
    showFurigana: Boolean = true,
    showCharFrames: Boolean = false,
    maxHeight: Dp? = null,
    fontSize: TextUnit = 24.sp,
    color: Color = Color.Unspecified,
    typeface: Typeface = Typeface.DEFAULT,
) {
    val density = LocalDensity.current
    val textColor = color.takeOrElse { LocalContentColor.current }

    val props = remember(density, fontSize, textColor, typeface) {
        val furiganaMaxSize = with(density) { FURIGANA_MAX_SIZE.toPx() }
        val fontSizePx = with(density) { fontSize.toPx() }

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = textColor.toArgb()
            it.typeface = typeface
            it.textSize = fontSizePx
        }

        val furiganaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = textColor.toArgb()
            it.alpha = (255 * DEFAULT_FURIGANA_OPACITY * textColor.alpha).toInt()
            it.textSize = minOf(furiganaMaxSize, fontSizePx * FURIGANA_REL_SIZE)
        }

        val dottedLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).also {
            it.color = textColor.toArgb()
            it.alpha = (255 * DOTTED_LINE_OPACITY * textColor.alpha).toInt()
            it.style = Style.STROKE
            it.strokeWidth = with(density) { DOTTED_LINE_STROKE_WIDTH.toPx() }
            it.pathEffect = DashPathEffect(
                floatArrayOf(
                    with(density) { DOTTED_LINE_DOT_LENGTH.toPx() },
                    with(density) { DOTTED_LINE_GAP_LENGTH.toPx() },
                ),
                0.0f
            )
        }

        TategakiProps(
            columnSpacing = with(density) { COLUMN_SPACING.roundToPx() },
            charWidth = textPaint.measureText("あ").toInt(),
            charHeight = textPaint.fontMetrics.let { (it.bottom - it.top).toInt() }, // it.top is negative!
            furiganaCharWidth = furiganaPaint.measureText("あ").toInt(),
            furiganaXDelta = with(density) { FURIGANA_X_DELTA.roundToPx() },
            emptyFuriganaWidth = with(density) { EMPTY_FURIGANA_WIDTH.roundToPx() },
            textPaint = textPaint,
            furiganaPaint = furiganaPaint,
            dottedLineMargin = with(density) { DOTTED_LINE_MARGIN.toPx() },
            dottedLinePaint = dottedLinePaint,
        )
    }

    val layout = remember(text, showFurigana) {
        TategakiLayout().apply {
            set(text, showFurigana)
        }
    }

    val painter = remember { TategakiPainter() }

    Layout(
        modifier = modifier.fillMaxWidth(),
        content = {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // drawRect(color = Color(0.5f, 0.5f, 0.5f, 0.1f), topLeft = Offset(0f, 0f), size = size)
                drawIntoCanvas { canvas ->
                    painter.draw(layout, canvas.nativeCanvas, showCharFrames, props)
                }
            }
        },
    ) { measurables, constraints ->
        require(measurables.size == 1)

        val argMaxHeightPx = with(density) { maxHeight?.roundToPx() }

        val actualMaxHeightPx =
            if (argMaxHeightPx == null) constraints.maxHeight
            else minOf(argMaxHeightPx, constraints.maxHeight).coerceAtLeast(0)

        // Currently, if layout requires more width than available, it will overlap.
        // The caller is responsible for providing a text and fontSize that ensures this won't happen.
        val layoutSize = layout.arrange(
            charWidth = props.charWidth,
            charHeight = props.charHeight,
            maxHeight = actualMaxHeightPx,
            columnSpacing = props.columnSpacing,
            letterSpacing = props.textPaint.letterSpacing.roundToInt(),
            furiganaCharWidth = props.furiganaCharWidth,
            furiganaXDelta = props.furiganaXDelta,
            emptyFuriganaWidth = props.emptyFuriganaWidth,
        )

        val placeable = measurables[0].measure(
            constraints.copy(
                minWidth = 0,
                minHeight = 0,
                maxWidth = layoutSize.x,
                maxHeight = layoutSize.y,
            )
        )

        val reportedWidth = constraints.maxWidth
        val reportedHeight = layoutSize.y.coerceIn(constraints.minHeight, constraints.maxHeight)

        // We use fillMaxWidth(), so reportedWidth can only be Int.MAX_VALUE if parent width is unbounded.
        // We cannot allow this, because then centring fails.
        require(reportedWidth < Int.MAX_VALUE) { "Horizontal centring impossible when parent width is unbounded" }

        layout(reportedWidth, reportedHeight) {
            placeable.placeRelative(
                x = reportedWidth / 2 - layoutSize.x / 2,
                y = 0,
            )
        }
    }
}
