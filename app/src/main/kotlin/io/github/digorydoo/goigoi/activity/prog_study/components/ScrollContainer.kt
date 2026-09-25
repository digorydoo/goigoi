package io.github.digorydoo.goigoi.activity.prog_study.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

private const val BOTTOM_PADDING_SNAP_DELAY_MILLIS = 20
private const val BOTTOM_PADDING_ANIM_DELAY_MILLIS = 10
private const val BOTTOM_PADDING_ANIM_DURATION_MILLIS = 400

/**
 * Implements a scroll container with a topArea and a centreArea. The topArea is the part of the scrollable area that
 * sticks to the top of the content. The ScrollContainer may push the centreArea down to ensure it remains centred if it
 * fits into the visible area. If it doesn't fit, it will immediately follow the topArea. The Keyboard overlaps the
 * ScrollContainer, but we ensure that all content can be scrolled into the visible area by the user by adding a bottom
 * padding that equals the keyboard height.
 *
 */
@Composable
fun ScrollContainer(
    topArea: @Composable ColumnScope.() -> Unit,
    centreArea: @Composable ColumnScope.(maxIdealHeight: Dp) -> Unit,
    centreAreaHorizPadding: Dp,
    keyboardHeight: Dp,
    mode: PresentationMode,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val visibleHeightPx = remember { mutableIntStateOf(0) }
    val headerHeightPx = remember { mutableIntStateOf(0) }
    val contentHeightPx = remember { mutableIntStateOf(0) }

    // The minBottomPaddingPx pushes the centreArea a little towards the top when the keyboard is hidden, to make the
    // jump less extreme when the keyboard appears. Don't make this too large, or the tategaki text would be too
    // off-centre.
    val minBottomPaddingPx = remember(density) { with(density) { 96.dp.toPx() } }

    val bottomPaddingPx = remember { Animatable(minBottomPaddingPx) }
    val hasEmptyContent = mode == PresentationMode.NOTHING
    val snapNext = remember { mutableStateOf(hasEmptyContent) }

    val targetBottomPaddingPx = with(density) { maxOf(minBottomPaddingPx, keyboardHeight.toPx()) }

    // We tell the centreArea what's the maximum height it can become before some part would be outside visible area.
    val maxIdealCentreAreaHeight = with(density) {
        (visibleHeightPx.intValue - headerHeightPx.intValue - targetBottomPaddingPx).toDp()
    }

    LaunchedEffect(keyboardHeight, density, hasEmptyContent) {
        if (hasEmptyContent) {
            // Keep same bottom padding as content is vanishing, but snap to new position when it appears.
            snapNext.value = true
        } else {
            if (snapNext.value) {
                // If we snap immediately, we may snap to the wrong value when keyboardHeight comes in late. Therefore,
                // add a delay. But if we wait too long, elements may already be faded in enough that the user sees the
                // snapping, which we don't want either. Therefore, keep the snap delay short.

                delay(BOTTOM_PADDING_SNAP_DELAY_MILLIS.milliseconds)
                bottomPaddingPx.snapTo(targetBottomPaddingPx)
                snapNext.value = false
            } else {
                bottomPaddingPx.animateTo(
                    targetValue = targetBottomPaddingPx,
                    animationSpec = tween(
                        delayMillis = BOTTOM_PADDING_ANIM_DELAY_MILLIS,
                        durationMillis = BOTTOM_PADDING_ANIM_DURATION_MILLIS,
                        easing = EaseOutCubic,
                    ),
                )
            }
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coords ->
                visibleHeightPx.intValue = coords.size.height
            }
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .onGloballyPositioned { coords ->
                        headerHeightPx.intValue = coords.size.height
                    }
            ) {
                topArea()
            }

            Spacer(
                modifier = Modifier.padding(
                    top = run {
                        val availHeightPx = visibleHeightPx.intValue - headerHeightPx.intValue - bottomPaddingPx.value
                        val innerPaddingPx = (availHeightPx - contentHeightPx.intValue).coerceAtLeast(0f) / 2
                        return@run with(density) { innerPaddingPx.toDp() }
                    }
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = centreAreaHorizPadding)
                    .height(IntrinsicSize.Min)
                    .onGloballyPositioned { coords ->
                        contentHeightPx.intValue = coords.size.height
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                centreArea(maxIdealCentreAreaHeight)
            }

            Spacer(modifier = Modifier.padding(top = keyboardHeight))
        }
    }
}
