package io.github.digorydoo.goigoi.activity.prog_study.components.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.core.prog_study.KeyActionHandler.Action
import io.github.digorydoo.goigoi.core.prog_study.KeyDef
import io.github.digorydoo.goigoi.core.prog_study.KeyDef.KeyLensPart
import kotlin.math.abs

private const val KEYLENS_DELAY_MILLIS = 80
private const val KEYLENS_FADE_IN_DURATION_MILLIS = 100
private const val KEYLENS_FADE_OUT_DURATION_MILLIS = 250

@Composable
fun FiveWayButton(def: KeyDef, modifier: Modifier = Modifier, onAction: (text: String, action: Action) -> Unit) {
    val centreAction = def.getAction(KeyLensPart.CENTRE)
    val centreText = def.getText(KeyLensPart.CENTRE)
    val popupShown = remember { mutableStateOf(false) }
    val keyLensPart = remember { mutableStateOf(KeyLensPart.CENTRE) }

    fun Modifier.handleGestures() = pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown()
            var part = KeyLensPart.CENTRE
            keyLensPart.value = KeyLensPart.CENTRE
            popupShown.value = true

            // Keep watching the gesture until the user lets go
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                change.consume()
                if (!change.pressed) break

                val delta = change.position - down.position

                part = when {
                    delta.getDistance() < viewConfiguration.touchSlop -> {
                        KeyLensPart.CENTRE
                    }
                    abs(delta.x) > abs(delta.y) -> {
                        if (delta.x > 0) KeyLensPart.RIGHT
                        else KeyLensPart.LEFT
                    }
                    else -> {
                        if (delta.y > 0) KeyLensPart.BOTTOM
                        else KeyLensPart.TOP
                    }
                }

                keyLensPart.value = part
            }

            popupShown.value = false
            onAction(def.getText(part), def.getAction(part))
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        val transition = updateTransition(targetState = popupShown.value)

        if (transition.currentState || transition.targetState) {
            Popup(
                popupPositionProvider = remember {
                    object: PopupPositionProvider {
                        override fun calculatePosition(
                            anchorBounds: IntRect,
                            windowSize: IntSize,
                            layoutDirection: LayoutDirection,
                            popupContentSize: IntSize,
                        ): IntOffset {
                            val anchorCentreX = (anchorBounds.left + anchorBounds.right) / 2 // x centre of Box
                            val x = anchorCentreX - popupContentSize.width / 2
                            val y = anchorBounds.top - popupContentSize.height
                            return IntOffset(x, y)
                        }
                    }
                },
                properties = PopupProperties(
                    clippingEnabled = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                )
            ) {
                val fadeInSpec = tween<Float>(
                    delayMillis = KEYLENS_DELAY_MILLIS,
                    durationMillis = KEYLENS_FADE_IN_DURATION_MILLIS,
                    easing = LinearEasing
                )
                val fadeOutSpec = tween<Float>(
                    durationMillis = KEYLENS_FADE_OUT_DURATION_MILLIS,
                    easing = EaseInCubic
                )

                transition.AnimatedVisibility(
                    visible = { it },
                    enter = fadeIn(fadeInSpec),
                    exit = fadeOut(fadeOutSpec),
                ) {
                    KeyLens(
                        centreText = centreText,
                        leftText = def.getText(KeyLensPart.LEFT),
                        leftIconResId =
                            if (def.leftAction == Action.DAKUTEN) R.drawable.ic_tenten_24dp
                            else null,
                        topText = def.getText(KeyLensPart.TOP),
                        rightText = def.getText(KeyLensPart.RIGHT),
                        rightIconResId =
                            if (def.rightAction == Action.HANDAKUTEN) R.drawable.ic_maru_24dp
                            else null,
                        bottomText = def.getText(KeyLensPart.BOTTOM),
                        highlight = keyLensPart.value,
                        highlightedAlone = !transition.targetState,
                    )
                }
            }
        }

        if (centreAction == Action.AUTO_TRANSFORM) {
            KeyButton(
                modifier = Modifier.handleGestures(),
                iconResId = R.drawable.ic_tenten_maru_24dp,
                onClick = null,
            )
        } else {
            KeyButton(
                modifier = Modifier.handleGestures(),
                text = centreText,
                onClick = null,
            )
        }
    }
}
