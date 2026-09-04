package io.github.digorydoo.goigoi.activity.welcome.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel
import io.github.digorydoo.goigoi.components.app_bar.EmptyAppBar
import io.github.digorydoo.goigoi.core.db.Topic
import io.github.digorydoo.goigoi.providers.DeviceProps
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ScreenSize
import kotlinx.coroutines.launch

private interface WelcomeScreenStyles {
    val paddingLR: Dp
    val paddingBottom: Dp
    val dividerMargin: Dp
    val goBackThresholdPx: Int
}

@Composable
private fun getStyles(): WelcomeScreenStyles {
    val density = LocalDensity.current
    val screenSize = DeviceProps.size
    val orientation = DeviceProps.orientation

    fun dpToPx(value: Dp) = with(density) { value.toPx() }

    return remember(density) {
        object: WelcomeScreenStyles {
            override val paddingLR = when (screenSize) {
                ScreenSize.LARGE -> 48.dp
                ScreenSize.NORMAL -> when (orientation) {
                    Orientation.PORTRAIT -> 24.dp
                    else -> 32.dp
                }
                ScreenSize.SMALL -> 16.dp
            }

            override val paddingBottom = 16.dp
            override val dividerMargin = 8.dp
            override val goBackThresholdPx = dpToPx(64.dp).toInt()
        }
    }
}

@Composable
fun WelcomeScreen(
    model: WelcomeActivityModel,
    topics: List<Topic>,
    onPrefsBtnClicked: () -> Unit,
    onBigStudyBtnClicked: () -> Unit,
    onTopicClicked: (Topic) -> Unit,
    onMyWordsUnytClicked: () -> Unit,
    onBack: () -> Unit,
) {
    val dims = getStyles()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            // WelcomeTopBar is part of the content; EmptyAppBar handles status bar colouring
            EmptyAppBar(
                onBack = {
                    // The WelcomeScreen is the top activity; closing it will close the app. Scroll to the top first to
                    // let the user see we're indeed on the WelcomeScreen. Close the app only when already at the top.
                    if (scrollState.value <= dims.goBackThresholdPx) {
                        onBack()
                    } else {
                        scope.launch {
                            scrollState.animateScrollTo(0)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(bottom = dims.paddingBottom)
        ) {
            WelcomeTopBar(dims.paddingLR, onPrefsBtnClicked)
            BigRingAndDayIcons(model)
            ProgressMessage(dims.paddingLR, model)
            BigStudyBtn(dims.paddingLR, onBigStudyBtnClicked)
            TopicsAndMyWordsList(
                model,
                paddingLR = dims.paddingLR,
                dividerMargin = dims.dividerMargin,
                topics,
                onTopicClicked,
                onMyWordsUnytClicked
            )
        }
    }
}
