package io.github.digorydoo.goigoi.activity.welcome.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.activity.welcome.WelcomeActivityModel
import io.github.digorydoo.goigoi.composables.app_bar.EmptyAppBar
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.core.db.Topic
import io.github.digorydoo.goigoi.utils.ScreenSize
import kotlinx.coroutines.launch

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
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val density = LocalDensity.current
    val screenSize = DeviceProps.size
    val isPortrait = DeviceProps.isPortrait

    val horizPadding = when (screenSize) {
        ScreenSize.LARGE -> 48.dp
        ScreenSize.NORMAL -> if (isPortrait) 24.dp else 32.dp
        ScreenSize.SMALL -> 16.dp
    }

    val goBackThresholdPx = with(density) { 64.dp.roundToPx() }

    Scaffold(
        topBar = {
            // WelcomeTopBar is part of the content; EmptyAppBar handles status bar colouring
            EmptyAppBar(
                onBack = {
                    // The WelcomeScreen is the top activity; closing it will close the app. Scroll to the top first to
                    // let the user see we're indeed on the WelcomeScreen. Close the app only when already at the top.
                    if (scrollState.value <= goBackThresholdPx) {
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
                .padding(bottom = 16.dp)
        ) {
            WelcomeTopBar(horizPadding, onPrefsBtnClicked)
            AnimatedBigRing(model)

            Encouragement(model, horizPadding)
            BigStudyBtn(horizPadding, onBigStudyBtnClicked)
            DayIconsArea(model, horizPadding)

            HorizontalDivider(modifier = Modifier.padding(top = 32.dp, bottom = 8.dp))
            TopicsAndMyWordsList(
                model = model,
                horizPadding = horizPadding,
                topics = topics,
                onTopicClicked = onTopicClicked,
                onMyWordsUnytClicked = onMyWordsUnytClicked
            )

            HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 32.dp))
            ProgressMessage(model, horizPadding)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
