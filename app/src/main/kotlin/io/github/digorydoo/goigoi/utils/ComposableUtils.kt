package io.github.digorydoo.goigoi.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit) =
    clickable(
        onClick = onClick,
        // We want to hide the ripple effect
        indication = null,
        // But the ripple effect's internal state (Press, Focus, etc.) is needed anyway
        interactionSource = remember { MutableInteractionSource() },
    )
