package io.github.digorydoo.goigoi.composables.bottom_sheet.generic

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ch.digorydoo.kutils.math.sign
import io.github.digorydoo.goigoi.composables.SHEET_CORNER_SIZE
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.utils.ScreenSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private enum class SheetState { PEEK, FULLSCREEN, EXITING }

private const val TAG = "GoigoiBottomSheet"
private const val MIN_FLING_GESTURE_TIME_SECONDS = 0.024f

private const val ENTER_ANIM_DURATION_MILLIS = 300
private val ENTER_ANIM_DISTANCE = 128.dp

private const val EXIT_ANIM_DURATION_MILLIS = 200
private val EXIT_ANIM_DISTANCE = 256.dp

private val MIN_FLING_SPEED = 48.dp // Dp per second
private val MAX_FLING_SPEED = 4800.dp // Dp per second
private val FLING_STOP_VELOCITY = 0.038f.dp // Dp per second
private val FLING_DECEL = 1180.dp // Dp per second^2
private val MIN_PEEK_HEIGHT = 8.dp
private val BOUNCY_BOTTOM_PADDING = 32.dp

/**
 * Wraps the given content in a modal bottom sheet. We do not use material ModalBottomSheet here, because that component
 * is rather buggy when the content is higher than the screen. (In particular, drag operations would sometimes fall
 * through the scrollable content and reach the window, causing the window to be moved instead of the content. Another
 * problem was the corners of the sheet, which stayed at the top of the screen instead of scrolling along. There was
 * also a problem with the sheet background when it reached the bottom with a fast speed: the sheet jumped, revealing
 * the Activity background, which looked rather odd. Moreover, I don't like ModalBottomSheet's handling of the scrolling
 * offset and window position in case of dismiss.)
 */
@Composable
fun GoigoiBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    val screenSize = DeviceProps.size
    val screenWidth = DeviceProps.widthDp
    val isPortrait = DeviceProps.isPortrait
    val density = LocalDensity.current

    val sheetPaddingBottom = when (screenSize) {
        ScreenSize.LARGE -> 32.dp
        ScreenSize.NORMAL -> if (isPortrait) 24.dp else 32.dp
        ScreenSize.SMALL -> 16.dp
    }

    val shape = remember { RoundedCornerShape(topStart = SHEET_CORNER_SIZE, topEnd = SHEET_CORNER_SIZE) }

    val sheetMaxWidth = when (screenSize) {
        ScreenSize.LARGE -> screenWidth - (if (isPortrait) 96.dp else 128.dp)
        ScreenSize.NORMAL -> if (isPortrait) screenWidth else screenWidth - 128.dp
        ScreenSize.SMALL -> screenWidth
    }

    val sheetState = remember { mutableStateOf(SheetState.PEEK) }
    val alphaAnim = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val dlgProps = remember {
        DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        )
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = dlgProps,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .alpha(alphaAnim.value)
        ) {
            val availWidth = maxWidth
            val availHeight = maxHeight
            val peekScrollPosDp = (availHeight / 2.5f).coerceAtLeast(48.dp).coerceAtMost(512.dp)
            val peekScrollPosPx = with(density) { peekScrollPosDp.roundToPx() }
            val fullscreenScrollPosPx = with(density) { (availHeight - MIN_PEEK_HEIGHT).roundToPx() }
            val thresholdPx = with(density) { 48.dp.roundToPx() }
            val enterDistancePx = with(density) { ENTER_ANIM_DISTANCE.roundToPx() }

            val scrollState = rememberScrollState(initial = (peekScrollPosPx - enterDistancePx).coerceAtLeast(0))

            LaunchedEffect(Unit) {
                launch {
                    scrollState.animateScrollTo(
                        peekScrollPosPx,
                        animationSpec = tween(durationMillis = ENTER_ANIM_DURATION_MILLIS, easing = EaseOutCubic)
                    )
                }
                launch {
                    alphaAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = ENTER_ANIM_DURATION_MILLIS, easing = EaseOutCubic)
                    )
                }
            }

            fun startDismiss() {
                if (sheetState.value == SheetState.EXITING) return
                sheetState.value = SheetState.EXITING

                scope.launch {
                    val exitDistancePx = with(density) { EXIT_ANIM_DISTANCE.roundToPx() }

                    scrollState.animateScrollTo(
                        (scrollState.value - exitDistancePx).coerceAtLeast(0),
                        animationSpec = tween(durationMillis = EXIT_ANIM_DURATION_MILLIS, easing = LinearEasing)
                    )
                }

                scope.launch {
                    alphaAnim.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = EXIT_ANIM_DURATION_MILLIS, easing = EaseInCubic)
                    )
                    onDismissRequest()
                }
            }

            fun checkScrollPos() {
                when (sheetState.value) {
                    SheetState.PEEK -> {
                        when {
                            scrollState.value <= peekScrollPosPx - thresholdPx -> {
                                startDismiss()
                            }
                            scrollState.value <= peekScrollPosPx + thresholdPx -> {
                                sheetState.value = SheetState.PEEK
                                scope.launch { scrollState.animateScrollTo(peekScrollPosPx) }
                            }
                            else -> {
                                sheetState.value = SheetState.FULLSCREEN
                                scope.launch { scrollState.animateScrollTo(fullscreenScrollPosPx) }
                            }
                        }
                    }
                    SheetState.FULLSCREEN -> {
                        when {
                            scrollState.value <= peekScrollPosPx - thresholdPx -> {
                                startDismiss()
                            }
                            scrollState.value <= fullscreenScrollPosPx - thresholdPx -> {
                                sheetState.value = SheetState.PEEK
                                scope.launch { scrollState.animateScrollTo(peekScrollPosPx) }
                            }
                            scrollState.value <= fullscreenScrollPosPx + thresholdPx -> {
                                sheetState.value = SheetState.FULLSCREEN
                                scope.launch { scrollState.animateScrollTo(fullscreenScrollPosPx) }
                            }
                            else -> {
                                sheetState.value = SheetState.FULLSCREEN
                            }
                        }
                    }
                    SheetState.EXITING -> Unit
                }
            }

            val flingHandler = remember(scrollState, density) {
                val bouncyBottomPaddingPx = with(density) { BOUNCY_BOTTOM_PADDING.roundToPx() }

                FlingHandler(
                    scrollState = scrollState,
                    minSpeed = with(density) { MIN_FLING_SPEED.toPx() },
                    maxSpeed = with(density) { MAX_FLING_SPEED.toPx() },
                    stopVelocity = with(density) { FLING_STOP_VELOCITY.toPx() },
                    deceleration = with(density) { FLING_DECEL.toPx() },
                    getMinPos = {
                        // If scrolled beyond fullscreen, fling should stop at fullscreen.
                        // Otherwise, fling can reach dismiss position.
                        when {
                            sheetState.value != SheetState.FULLSCREEN -> 0
                            scrollState.value <= fullscreenScrollPosPx + thresholdPx -> 0
                            else -> fullscreenScrollPosPx
                        }
                    },
                    getMaxPos = {
                        // If still at PEEK position, fling should stop at fullscreen.
                        // Otherwise, fling can reach the sheet's far end.
                        when {
                            sheetState.value != SheetState.PEEK -> scrollState.maxValue - bouncyBottomPaddingPx
                            else -> fullscreenScrollPosPx
                        }
                    },
                    onFlingEnded = ::checkScrollPos,
                )
            }

            val containerFling = remember {
                object: FlingBehavior {
                    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                        // Sometimes the pointer event does not reach the outer Box, but is consumed by the
                        // verticalScroll. This typically happens when the event is over an active element inside the
                        // sheet content, or if the position is almost at the edge of the screen. We delegate the fling
                        // to our flingHandler.
                        //
                        // This behaviour appears to be the cause of the problem that sometimes our fling stop positions
                        // are ignored. But I consider this a minor problem. If it gets more annoying in the future, I
                        // should get rid of verticalScroll and implement scrolling via Modifier.offset() instead.
                        // However, it's unclear whether this would break scrolling over active elements.

                        if (sheetState.value != SheetState.EXITING) {
                            Log.w(TAG, "Pointer event was sent to scroll container rather than to outer Box")
                            scope.launch {
                                // We need to add a short delay, because all attempts to modify scrollState outside
                                // ScrollSope are cancelled until performFling() returns.
                                delay(1.milliseconds)
                                flingHandler.fling(initialVelocity)
                            }
                        }
                        // We don't have any nested scroll elements, so we simply consumed all velocity.
                        return 0f
                    }
                }
            }

            BackHandler(onBack = ::startDismiss)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .pointerInput(Unit) {
                        val sheetStartX = (availWidth / 2 - sheetMaxWidth / 2).coerceAtLeast(0.dp)
                        val sheetEndX = (availWidth / 2 + sheetMaxWidth / 2).coerceAtMost(availWidth)

                        handleGestures(
                            scrollState,
                            sheetStartX = with(density) { sheetStartX.roundToPx() },
                            sheetEndX = with(density) { sheetEndX.roundToPx() },
                            fullscreenScrollPosPx = fullscreenScrollPosPx,
                            scope,
                            flingHandler,
                            onGestureEndedWithoutFling = ::checkScrollPos,
                            onDismiss = ::startDismiss
                        )
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .verticalScroll(
                            state = scrollState,
                            enabled = true, // needs to be true, otherwise clickable elements consume our pointer event
                            flingBehavior = containerFling, // because enabled is true, this may sometimes be called
                        )
                        .padding(top = availHeight - MIN_PEEK_HEIGHT),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Surface(
                        modifier = Modifier
                            .widthIn(max = sheetMaxWidth)
                            .fillMaxWidth(),
                        shape = shape,
                        shadowElevation = 24.dp,
                        tonalElevation = 0.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = sheetPaddingBottom + BOUNCY_BOTTOM_PADDING)
                        ) {
                            DragHandle()
                            content()
                        }
                    }
                }
            }
        }
    }
}

private suspend fun PointerInputScope.handleGestures(
    scrollState: ScrollState,
    sheetStartX: Int,
    sheetEndX: Int,
    fullscreenScrollPosPx: Int,
    scope: CoroutineScope,
    flingHandler: FlingHandler,
    onGestureEndedWithoutFling: () -> Unit,
    onDismiss: () -> Unit,
) {
    awaitEachGesture {
        val down = awaitFirstDown()
        down.consume()

        val outsideSheet = down.position.y < fullscreenScrollPosPx - scrollState.value ||
            down.position.x < sheetStartX || down.position.x > sheetEndX

        if (outsideSheet) {
            // Event position is outside sheet

            var delta = Offset(0f, 0f)

            do {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                change.consume()
                delta = change.position - down.position
            } while (change.pressed)

            if (delta.getDistance() <= viewConfiguration.touchSlop) {
                onDismiss()
            }
        } else {
            // Event position is inside sheet

            val startScrollPos = scrollState.value
            var prevScrollPos = startScrollPos
            var flingStartScrollPos = startScrollPos
            var flingStartTime = down.uptimeMillis
            var flingEndTime = down.uptimeMillis
            val startPointerY = down.position.y
            var speed1 = 0f
            var flingGestureTime: Float

            do {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                change.consume()
                flingEndTime = change.uptimeMillis

                val deltaY = change.position.y - startPointerY
                val targetScrollPos = (startScrollPos - deltaY).roundToInt()
                val gestureDir = sign(targetScrollPos - prevScrollPos)
                val flingDir = sign(targetScrollPos - flingStartScrollPos)
                prevScrollPos = targetScrollPos

                if (flingDir != 0 && gestureDir != 0 && flingDir != gestureDir) {
                    // Direction of gesture changed, start flinging from here.
                    flingStartScrollPos = scrollState.value
                    flingStartTime = change.uptimeMillis
                    speed1 = 0f
                } else {
                    flingGestureTime = (flingEndTime - flingStartTime) / 1000f

                    if (flingGestureTime >= MIN_FLING_GESTURE_TIME_SECONDS) {
                        // Approach the speed to smooth out the curve
                        speed1 = (speed1 + (prevScrollPos - flingStartScrollPos) / flingGestureTime) / 2
                    }
                }

                scope.launch {
                    flingHandler.abort()
                    scrollState.scrollTo(targetScrollPos)
                }
            } while (change.pressed)

            flingGestureTime = (flingEndTime - flingStartTime) / 1000f

            if (flingGestureTime >= MIN_FLING_GESTURE_TIME_SECONDS) {
                val speed2 = (prevScrollPos - flingStartScrollPos) / flingGestureTime

                scope.launch {
                    // speed1 is mostly based on the delta of the last frame
                    // speed2 is the average speed since flingStartTime
                    // We use the average of both speeds for the fling speed.
                    flingHandler.fling((speed1 + speed2) / 2)
                }
            } else {
                onGestureEndedWithoutFling()
            }
        }
    }
}
