package io.github.digorydoo.goigoi.activity.welcome.composables

import android.util.TypedValue
import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel
import io.github.digorydoo.goigoi.composables.HintBalloon
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.legacy.spannable.FuriganaSpan
import io.github.digorydoo.goigoi.legacy.spannable.buildSpan

@Composable
fun ProgressMessage(model: WelcomeActivityModel, horizPadding: Dp) {
    val showBalloon = remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val fontSize = GoigoiTheme.typography.listItemPrimaryText.fontSize
    val fontSizePx = with(density) { fontSize.toPx() }

    val textColour = GoigoiTheme.colours.onBackground

    val ja = model.progressMsgJa.collectAsState().value
    val translation = model.progressMsgTranslation.collectAsState().value

    HintBalloon(
        wrappedContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showBalloon.value = true }
                    .padding(top = 8.dp, bottom = 8.dp)
                    .padding(horizontal = horizPadding),
                contentAlignment = Alignment.TopStart,
            ) {
                AndroidView(
                    factory = { ctx -> TextView(ctx) },
                    update = { textView ->
                        textView.apply {
                            text = ja.buildSpan(
                                FuriganaSpan.Options(fontSizeFactor = 0.6f)
                            )
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSizePx)
                            setTextColor(textColour.toArgb())
                        }
                    }
                )
            }
        },
        lines = arrayOf(translation),
        open = showBalloon.value,
        onDismiss = { showBalloon.value = false },
    )
}
