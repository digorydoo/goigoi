package io.github.digorydoo.goigoi.activity.prog_study.composables.keyboard

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.cjk.hasCJKOrKana
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.ScreenSize

private const val SMALL_TEXT_SCALING = 0.8f // used for making small kana more obvious
private val KANA_KEYBOARD_BUTTON_WIDTH = 56.dp

enum class KeyButtonSize { XXLARGE, XLARGE, LARGE, NORMAL }

@Composable
private fun KeyButton(
    content: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    size: KeyButtonSize = KeyButtonSize.NORMAL,
    background: Color = GoigoiTheme.colours.surface,
    enabled: Boolean = true,
    onClick: (() -> Unit)?,
) {
    val shape = remember { RoundedCornerShape(size = 8.dp) }
    val screenSize = DeviceProps.size
    val isPortrait = DeviceProps.isPortrait

    val minHeight = when (screenSize) {
        ScreenSize.LARGE -> when (size) {
            KeyButtonSize.XXLARGE -> 102.dp
            KeyButtonSize.XLARGE -> 80.dp
            KeyButtonSize.LARGE -> 72.dp
            KeyButtonSize.NORMAL -> 56.dp
        }
        ScreenSize.NORMAL -> when {
            isPortrait -> when (size) {
                KeyButtonSize.XXLARGE -> 102.dp
                KeyButtonSize.XLARGE -> 76.dp
                KeyButtonSize.LARGE -> 64.dp
                KeyButtonSize.NORMAL -> 56.dp
            }
            else -> when (size) {
                KeyButtonSize.XXLARGE -> 48.dp
                KeyButtonSize.XLARGE -> 40.dp
                KeyButtonSize.LARGE -> 38.dp
                KeyButtonSize.NORMAL -> 36.dp
            }
        }
        ScreenSize.SMALL -> when (size) {
            KeyButtonSize.XXLARGE -> 48.dp
            KeyButtonSize.XLARGE -> 40.dp
            KeyButtonSize.LARGE -> 38.dp
            KeyButtonSize.NORMAL -> 36.dp
        }
    }

    val minWidth = when (size) {
        KeyButtonSize.NORMAL -> KANA_KEYBOARD_BUTTON_WIDTH
        else -> maxOf(48.dp, minHeight)
    }

    val horizPadding = when (screenSize) {
        ScreenSize.LARGE -> when (size) {
            KeyButtonSize.XXLARGE -> 36.dp
            KeyButtonSize.XLARGE -> 28.dp
            KeyButtonSize.LARGE -> 24.dp
            KeyButtonSize.NORMAL -> 20.dp
        }
        ScreenSize.NORMAL -> when (size) {
            KeyButtonSize.XXLARGE -> 32.dp
            KeyButtonSize.XLARGE -> 24.dp
            KeyButtonSize.LARGE -> 20.dp
            KeyButtonSize.NORMAL -> 12.dp
        }
        ScreenSize.SMALL -> when (size) {
            KeyButtonSize.XXLARGE -> 20.dp
            KeyButtonSize.XLARGE -> 16.dp
            KeyButtonSize.LARGE -> 12.dp
            KeyButtonSize.NORMAL -> 8.dp
        }
    }

    Surface(
        modifier = modifier
            .sizeIn(minWidth = minWidth, minHeight = minHeight)
            .width(IntrinsicSize.Max) // never wrap
            .height(IntrinsicSize.Min)
            .then(
                if (!enabled) Modifier.alpha(0.5f)
                else Modifier
            ),
        shape = shape,
        color = background,
        shadowElevation = if (enabled) 2.dp else 0.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (onClick != null && enabled) Modifier.clickable(onClick = onClick)
                    else Modifier
                )
                .padding(horizontal = horizPadding),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
fun KeyButton(
    text: String,
    modifier: Modifier = Modifier,
    size: KeyButtonSize = KeyButtonSize.NORMAL,
    background: Color = GoigoiTheme.colours.surface,
    color: Color = GoigoiTheme.colours.onSurface,
    enabled: Boolean = true,
    smallText: Boolean = false,
    onClick: (() -> Unit)?,
) {
    val screenSize = DeviceProps.size
    val isPortrait = DeviceProps.isPortrait
    val density = LocalDensity.current

    // If smallText is true, we want the baseline to stay at the same y coordinate.
    // I tried computing the correction via rememberTextMeasurer, but the result was not exact for some reason.
    // Let's do it the simple instead.
    val baselineCorrection =
        if (smallText) ((1f - SMALL_TEXT_SCALING) * 12).dp
        else 0.dp

    // We don't want Android system font scaling here, that's why we specify sizes in dp.
    val fontSizeDp = when (screenSize) {
        ScreenSize.LARGE -> when (size) {
            KeyButtonSize.XXLARGE -> 64.dp
            KeyButtonSize.XLARGE -> 48.dp
            KeyButtonSize.LARGE -> 36.dp
            KeyButtonSize.NORMAL -> 24.dp
        }
        ScreenSize.NORMAL -> when {
            isPortrait -> when (size) {
                KeyButtonSize.XXLARGE -> 64.dp
                KeyButtonSize.XLARGE -> 42.dp
                KeyButtonSize.LARGE -> 36.dp
                KeyButtonSize.NORMAL -> 24.dp
            }
            else -> when (size) {
                KeyButtonSize.XXLARGE -> 38.dp
                KeyButtonSize.XLARGE -> 32.dp
                KeyButtonSize.LARGE -> 26.dp
                KeyButtonSize.NORMAL -> 20.dp
            }
        }
        ScreenSize.SMALL -> when (size) {
            KeyButtonSize.XXLARGE -> 38.dp
            KeyButtonSize.XLARGE -> 32.dp
            KeyButtonSize.LARGE -> 26.dp
            KeyButtonSize.NORMAL -> 20.dp
        }
    } * when (smallText) {
        true -> SMALL_TEXT_SCALING
        false -> 1f
    }

    val fontSize = with(density) { fontSizeDp.toSp() }

    // Japanese glyphs of Android system fonts appear to extend below the baseline
    val centringCorrection = remember(text) {
        if (text.hasCJKOrKana()) fontSizeDp / 16 else 0.dp
    }

    KeyButton(
        content = {
            Text(
                modifier = Modifier.offset(y = baselineCorrection - centringCorrection),
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                color = color,
                softWrap = false,
            )
        },
        modifier = modifier,
        size = size,
        background = background,
        enabled = enabled,
        onClick = onClick
    )
}

@Composable
fun KeyButton(
    @DrawableRes iconResId: Int,
    modifier: Modifier = Modifier,
    size: KeyButtonSize = KeyButtonSize.NORMAL,
    background: Color = GoigoiTheme.colours.surface,
    color: Color = GoigoiTheme.colours.onSurface,
    enabled: Boolean = true,
    onClick: (() -> Unit)?,
) {
    KeyButton(
        content = {
            Icon(
                modifier = when (size) {
                    KeyButtonSize.XXLARGE -> Modifier.scale(1.8f)
                    KeyButtonSize.XLARGE -> Modifier.scale(1.6f)
                    KeyButtonSize.LARGE -> Modifier.scale(1.4f)
                    KeyButtonSize.NORMAL -> Modifier.scale(1.2f)
                },
                imageVector = ImageVector.vectorResource(iconResId),
                contentDescription = null,
                tint = color,
            )
        },
        modifier = modifier,
        size = size,
        background = background,
        enabled = enabled,
        onClick = onClick
    )
}
