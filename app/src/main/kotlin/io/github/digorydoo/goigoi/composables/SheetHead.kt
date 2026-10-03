package io.github.digorydoo.goigoi.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme

val SHEET_CORNER_SIZE = 24.dp

// FIXME In landscape, the SheetHead should cover the area of the nav bar / status bar
//    Reproduce: PrefsScreen, landscape, NOT simulator: sheet head does not extend far enough, because we're rendering
//    it from content, not from Scaffold's topBar area.

@Composable
fun SheetHead(modifier: Modifier = Modifier) {
    val height = SHEET_CORNER_SIZE + 8.dp
    val colour = GoigoiTheme.colours.appBarContainer

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val cornerSize = SHEET_CORNER_SIZE.toPx()
        val width = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, h)
            arcTo(
                rect = Rect(
                    left = width - 2 * cornerSize,
                    top = h - cornerSize,
                    right = width,
                    bottom = h + cornerSize,
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false,
            )
            lineTo(cornerSize, h - cornerSize)
            arcTo(
                rect = Rect(
                    left = 0f,
                    top = h - cornerSize,
                    right = 2 * cornerSize,
                    bottom = h + cornerSize,
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = -90f,
                forceMoveTo = false,
            )
            close()
        }

        drawPath(path = path, color = colour)
    }
}
