package io.github.digorydoo.goigoi.components.bottom_sheet.generic

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.components.SHEET_CORNER_SIZE
import io.github.digorydoo.goigoi.providers.DeviceProps
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.ScreenSize
import kotlinx.coroutines.launch

private const val EARLY_DISMISS_WORKAROUND_MILLIS = 1100

interface GoigoiBottomSheetScope {
    fun dismiss()
}

private interface GoigoiBottomSheetStyles {
    val sheetPaddingTop: Dp
    val sheetPaddingBottom: Dp
    val sheetMarginTopPx: Int
    val sheetCornerSizePx: Int
    val sheetMaxWidth: Dp
}

@Composable
private fun getStyles(): GoigoiBottomSheetStyles {
    val density = LocalDensity.current
    val screenSize = DeviceProps.size
    val screenWidth = DeviceProps.widthDp
    val isPortrait = DeviceProps.isPortrait

    var statusBarHeightPx = WindowInsets.statusBars.getTop(density)

    if (statusBarHeightPx == 0) {
        // Workaround for legacy activities that don't call enableEdgeToEdge()
        // FIXME remove this once all activities have been rewritten in compose
        statusBarHeightPx = with(density) { 48.dp.roundToPx() } // twice the expected height
    }

    val navBarHeightPx = WindowInsets.navigationBars.getBottom(density)

    return remember(density) {
        object: GoigoiBottomSheetStyles {
            override val sheetPaddingBottom = with(density) { navBarHeightPx.toDp() } + when (screenSize) {
                ScreenSize.LARGE -> 32.dp
                ScreenSize.NORMAL -> if (isPortrait) 24.dp else 32.dp
                ScreenSize.SMALL -> 16.dp
            }

            override val sheetPaddingTop = with(density) { statusBarHeightPx.toDp() }
            override val sheetMarginTopPx = statusBarHeightPx
            override val sheetCornerSizePx = with(density) { SHEET_CORNER_SIZE.roundToPx() }

            override val sheetMaxWidth = when (screenSize) {
                ScreenSize.LARGE -> screenWidth - (if (isPortrait) 96.dp else 128.dp)
                ScreenSize.NORMAL -> if (isPortrait) screenWidth else screenWidth - 64.dp
                ScreenSize.SMALL -> screenWidth
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoigoiBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable GoigoiBottomSheetScope.() -> Unit,
) {
    val styles = getStyles()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val timeOfFirstRender = remember { System.currentTimeMillis() }
    val sheetState = rememberModalBottomSheetState()

    val sheetScope = remember {
        object: GoigoiBottomSheetScope {
            override fun dismiss() {
                scope.launch {
                    sheetState.hide()
                    onDismissRequest()
                }
            }
        }
    }

    ModalBottomSheet(
        contentWindowInsets = {
            // The default insets force the content away from the status bar. However, since our sheet is scrollable,
            // this would leave the content stationary while the sheet's top corners continue moving, which doesn't look
            // good. Therefore, we disable this feature by passing zero insets.
            WindowInsets(left = 0, top = 0, right = 0, bottom = 0)
        },
        dragHandle = null, // we render our own from the content in order that it will scroll along
        containerColor = GoigoiTheme.colours.bottomSheetContainer,
        contentColor = GoigoiTheme.colours.onBottomSheetContainer,
        shape = GoigoiBottomSheetShape(
            // FIXME The corners need to be drawn outside just like the DragHandle, because they need to scroll along
            cornerSizePx = styles.sheetCornerSizePx,

            // We want the sheet to stop right below the status bar when fully expanded while not scrolled.
            // Unfortunately, the default implementation stops at the very top of the screen, causing an ugly overlap
            // of the status bar with the content. To fix the problem, our custom shape clips away the top of the sheet
            // that would otherwise be overlapped by the status bar.
            topSpacePx = styles.sheetMarginTopPx
        ),
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false), // we have our own BackHandler
        tonalElevation = 0.dp,
        sheetState = sheetState,
        onDismissRequest = {
            val timePassed = System.currentTimeMillis() - timeOfFirstRender

            if (timePassed < EARLY_DISMISS_WORKAROUND_MILLIS) {
                // Work around a problem in ModalBottomSheet that causes the sheet to be dismissed if the user taps the
                // sheet while it is still in the initial animation phase. Don't make DISMISS_WORKAROUND_MILLIS too
                // high though, or you'll risk re-opening the sheet when the user made a genuine close gesture too
                // early.
                scope.launch {
                    if (sheetState.targetValue == SheetValue.Expanded) {
                        sheetState.expand()
                    } else {
                        sheetState.partialExpand()
                    }
                }
            } else {
                onDismissRequest()
            }
        },
        sheetMaxWidth = styles.sheetMaxWidth,
    ) {
        BackHandler(enabled = sheetState.isVisible) {
            when (sheetState.currentValue) {
                SheetValue.Expanded -> {
                    if (scrollState.value > 0) {
                        // Scroll the content to the top first before closing
                        scope.launch { scrollState.animateScrollTo(0) }
                    } else {
                        // Close the sheet (we don't go through partially expanded)
                        scope.launch {
                            sheetState.hide() // suspends until the animation has completed
                            onDismissRequest()
                        }
                    }
                }
                SheetValue.PartiallyExpanded -> {
                    scope.launch {
                        sheetState.hide()
                        onDismissRequest()
                    }
                }
                SheetValue.Hidden -> Unit // shouldn't ever come here, because we check sheetState.isVisible
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Any padding before (outside) the verticalScroll would allow dragging of the sheet window
                    // independently of the scrollable content. Since we don't want that, all padding must go after
                    // (inside) the verticalScroll.
                    .verticalScroll(scrollState)
                    .padding(
                        top = styles.sheetPaddingTop,
                        bottom = styles.sheetPaddingBottom,
                    )
            ) {
                DragHandle()
                sheetScope.content()
            }

            StatusBarAboveBottomSheet()
        }
    }
}
