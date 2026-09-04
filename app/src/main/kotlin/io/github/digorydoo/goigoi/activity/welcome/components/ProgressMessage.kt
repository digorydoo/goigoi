package io.github.digorydoo.goigoi.activity.welcome.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private interface ProgressMessageStyles {
    val marginTop: Dp
    val marginBottom: Dp
}

@Composable
private fun getStyles(): ProgressMessageStyles {
    val density = LocalDensity.current
    return remember(density) {
        object: ProgressMessageStyles {
            override val marginTop = 4.dp
            override val marginBottom = 16.dp
        }
    }
}

@Composable
fun ProgressMessage(paddingLR: Dp, model: WelcomeActivityModel) {
    val progressMsg = model.progressMsg.collectAsState().value
    val dims = getStyles()

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = paddingLR,
                end = paddingLR,
                top = dims.marginTop,
                bottom = dims.marginBottom
            ),
        text = progressMsg,
        textAlign = TextAlign.Center,
        style = GoigoiTheme.typography.listItemSecondaryText,
        color = GoigoiTheme.colours.onBackgroundSecondary,
    )
}
