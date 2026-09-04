package io.github.digorydoo.goigoi.activity.about

import android.text.Html
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.R.string
import io.github.digorydoo.goigoi.components.SheetHead
import io.github.digorydoo.goigoi.components.app_bar.GoigoiAppBar
import io.github.digorydoo.goigoi.providers.DeviceProps
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ScreenSize

private interface AboutScreenStyles {
    val contentPaddingLR: Dp
    val contentPaddingTop: Dp
    val contentPaddingBottom: Dp
}

@Composable
private fun getStyles(): AboutScreenStyles {
    val themeColours = GoigoiTheme.colours
    val density = LocalDensity.current
    val screenSize = DeviceProps.size
    val orientation = DeviceProps.orientation

    return remember(themeColours, density, screenSize, orientation) {
        object: AboutScreenStyles {
            override val contentPaddingLR = when (screenSize) {
                ScreenSize.LARGE -> 32.dp
                ScreenSize.NORMAL -> when (orientation) {
                    Orientation.PORTRAIT -> 24.dp
                    else -> 32.dp
                }
                ScreenSize.SMALL -> 16.dp
            }

            override val contentPaddingTop = when (screenSize) {
                ScreenSize.LARGE -> 24.dp
                ScreenSize.NORMAL -> when (orientation) {
                    Orientation.PORTRAIT -> 16.dp
                    else -> 8.dp
                }
                ScreenSize.SMALL -> 8.dp
            }

            override val contentPaddingBottom = when (screenSize) {
                ScreenSize.LARGE -> 24.dp
                ScreenSize.NORMAL -> when (orientation) {
                    Orientation.PORTRAIT -> 16.dp
                    else -> 8.dp
                }
                ScreenSize.SMALL -> 8.dp
            }
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val styles = getStyles()

    Scaffold(
        topBar = {
            GoigoiAppBar(
                titleResId = string.aboutAppPrimary,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = styles.contentPaddingBottom)
        ) {
            SheetHead(modifier = Modifier.padding(bottom = styles.contentPaddingTop))

            val copyrightAndLicense = stringResource(R.string.copyright_and_license)
            val textColor = MaterialTheme.colorScheme.onBackground.toArgb()

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = styles.contentPaddingLR),
                factory = { context ->
                    TextView(context).apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                    }
                },
                update = { textView ->
                    textView.text = Html.fromHtml(copyrightAndLicense, Html.FROM_HTML_MODE_LEGACY)
                    textView.setTextColor(textColor)
                }
            )
        }
    }
}
