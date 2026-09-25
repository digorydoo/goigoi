package io.github.digorydoo.goigoi.activity.prog_study.components.keyboard

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.core.prog_study.KeyDef.KeyLensPart
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private val KEY_LENS_SIZE = 96.dp
private const val DIMMED_PART_ALPHA = 0.42f

private const val ICON_SCALE_FACTOR = 1.125f
private val ICON_SHIFT_DISTANCE = 8.dp

@Composable
fun KeyLens(
    modifier: Modifier = Modifier,
    centreText: String,
    leftText: String,
    @DrawableRes leftIconResId: Int?, // overrides leftText if not null
    topText: String,
    rightText: String,
    @DrawableRes rightIconResId: Int?, // overrides rightText if not null
    bottomText: String,
    highlight: KeyLensPart,
    highlightedAlone: Boolean,
) {
    val textStyle = GoigoiTheme.typography.keyLens
    val textColour = GoigoiTheme.colours.onBackground

    fun getAlpha(part: KeyLensPart) =
        if (highlight == part) 1f
        else if (highlightedAlone) 0f
        else DIMMED_PART_ALPHA

    Surface(
        modifier = modifier.size(KEY_LENS_SIZE),
        shape = CircleShape,
        color = GoigoiTheme.colours.keyLensBackground,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(getAlpha(KeyLensPart.CENTRE)),
                text = centreText,
                style = textStyle,
                color = textColour,
            )

            val leftModifier = Modifier
                .align(Alignment.CenterStart)
                .alpha(getAlpha(KeyLensPart.LEFT))

            if (leftIconResId == null) {
                Text(
                    modifier = leftModifier.padding(start = 8.dp),
                    text = leftText,
                    style = textStyle,
                    color = textColour,
                )
            } else {
                Icon(
                    modifier = leftModifier
                        .offset(x = ICON_SHIFT_DISTANCE)
                        .scale(ICON_SCALE_FACTOR),
                    imageVector = ImageVector.vectorResource(leftIconResId),
                    contentDescription = null,
                    tint = textColour,
                )
            }

            val rightModifier = Modifier
                .align(Alignment.CenterEnd)
                .alpha(getAlpha(KeyLensPart.RIGHT))

            if (rightIconResId == null) {
                Text(
                    modifier = rightModifier.padding(end = 8.dp),
                    text = rightText,
                    style = textStyle,
                    color = textColour,
                )
            } else {
                Icon(
                    modifier = rightModifier
                        .offset(x = -ICON_SHIFT_DISTANCE)
                        .scale(ICON_SCALE_FACTOR),
                    imageVector = ImageVector.vectorResource(rightIconResId),
                    contentDescription = null,
                    tint = textColour,
                )
            }

            Text(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .alpha(getAlpha(KeyLensPart.TOP)),
                text = topText,
                style = textStyle,
            )
            Text(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .alpha(getAlpha(KeyLensPart.BOTTOM)),
                text = bottomText,
                style = textStyle,
                color = textColour,
            )
        }
    }
}
