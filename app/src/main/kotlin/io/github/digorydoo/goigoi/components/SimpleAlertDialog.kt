package io.github.digorydoo.goigoi.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private const val ANIM_DELAY_MILLIS = 1000
private const val ANIM_DURATION_MILLIS = 1000

@Composable
fun SimpleAlertDialog(
    message: String,
    onConfirm: () -> Unit,
    onCancel: (() -> Unit)? = null, // null = call onDismissRequest
    onDismissRequest: () -> Unit,
    confirmLabel: String? = null, // OK if null
    cancelLabel: String? = null, // CANCEL if null
) {
    val alertTextColour = GoigoiTheme.colours.onSurface
    val emphasizedTextColour = GoigoiTheme.colours.emphasizedText
    val alertShape = MaterialTheme.shapes.small

    val stdOkLabel = stringResource(android.R.string.ok)
    val stdCancelLabel = stringResource(android.R.string.cancel)

    val confirmed = remember { mutableStateOf(false) }

    val progressIndicatorAlpha = animateFloatAsState(
        targetValue = if (confirmed.value) 1f else 0f,
        animationSpec = tween(
            delayMillis = ANIM_DELAY_MILLIS,
            durationMillis = ANIM_DURATION_MILLIS,
        ),
    )

    AlertDialog(
        shape = alertShape,
        title = null,
        text = {
            Text(
                text = message,
                color = alertTextColour,
            )
        },
        confirmButton = {
            Box(contentAlignment = Alignment.Center) {
                TextButton(
                    onClick = {
                        confirmed.value = true
                        onConfirm()
                    },
                    enabled = !confirmed.value,
                ) {
                    Text(
                        text = (confirmLabel ?: stdOkLabel).uppercase(),
                        color = emphasizedTextColour,
                    )
                }

                // If onConfirm() started a coroutine that does some work, we display a progress indicator over the
                // confirm button. Obviously, this won't work if onConfirm() blocks the UI thread.
                if (progressIndicatorAlpha.value > 0f) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .alpha(progressIndicatorAlpha.value),
                        color = GoigoiTheme.colours.primary,
                        strokeWidth = 2.dp,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel ?: onDismissRequest,
                enabled = !confirmed.value,
            ) {
                Text(
                    text = (cancelLabel ?: stdCancelLabel).uppercase(),
                    color = emphasizedTextColour,
                )
            }
        },
        onDismissRequest = {
            if (!confirmed.value) {
                onDismissRequest()
            }
        },
    )
}
