package io.github.digorydoo.goigoi.components.menus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.components.list.ListItem
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private const val LOADING_ANIM_DELAY_MILLIS = 1000
private const val LOADING_ANIM_DURATION_MILLIS = 1000

@Composable
fun <Action> ContextMenu(
    items: List<CtxMenuItemDefs<Action>>,
    onAction: (Action) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val horizontalPadding = 16.dp
    val verticalPadding = 8.dp

    val clickedAction = remember { mutableStateOf<Action?>(null) }

    val progressIndicatorAlpha = animateFloatAsState(
        targetValue = if (clickedAction.value != null) 1f else 0f,
        animationSpec = tween(
            delayMillis = LOADING_ANIM_DELAY_MILLIS,
            durationMillis = LOADING_ANIM_DURATION_MILLIS,
        ),
    )

    Dialog(onDismissRequest = {
        if (clickedAction.value == null) {
            onDismissRequest()
        }
    }) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = GoigoiTheme.colours.surface,
        ) {
            Column(modifier = Modifier.padding(vertical = verticalPadding)) {
                for (item in items) {
                    if (!item.debugOnly || BuildConfig.DEBUG) {
                        Box(contentAlignment = Alignment.Center) {
                            val onClick = {
                                clickedAction.value = item.action
                                onAction(item.action)
                            }

                            ListItem(
                                iconResId = item.iconResId,
                                primaryText = item.text,
                                horizontalPadding = horizontalPadding,
                                onClick = if (clickedAction.value == null) onClick else null
                            )

                            if (item.action == clickedAction.value && progressIndicatorAlpha.value > 0f) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .alpha(progressIndicatorAlpha.value),
                                    color = GoigoiTheme.colours.primary,
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
