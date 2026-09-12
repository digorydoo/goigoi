package io.github.digorydoo.goigoi.components.bottom_sheet.generic

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private interface DragHandleStyles {
    val width: Dp
    val height: Dp
    val marginTop: Dp
    val marginBottom: Dp
}

@Composable
private fun getStyles(): DragHandleStyles {
    val density = LocalDensity.current

    return remember(density) {
        object: DragHandleStyles {
            override val width = 48.dp
            override val height = 4.dp
            override val marginTop = 16.dp
            override val marginBottom = 16.dp
        }
    }
}

@Composable
fun DragHandle(modifier: Modifier = Modifier) {
    val styles = getStyles()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = styles.marginTop, bottom = styles.marginBottom),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(styles.width)
                .height(styles.height),
            color = GoigoiTheme.colours.dragHandle,
            shape = RoundedCornerShape(styles.height / 2),
        ) {
        }
    }
}
