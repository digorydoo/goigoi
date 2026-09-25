package io.github.digorydoo.goigoi.activity.prog_study.components.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.KeyboardMode
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.components.buttons.Button
import io.github.digorydoo.goigoi.providers.DeviceProps
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.ScreenSize

private const val ENTER_ANIM_DURATION_MILLIS = 300
private const val EXIT_ANIM_DURATION_MILLIS = 300

private interface KeyboardHandlerStyles {
    val horizPadding: Dp
    val paddingTop: Dp
    val paddingBottom: Dp
    val slideDistancePx: Int
    val background: Color
    val bgndShape: Shape
    val bgndBorder: BorderStroke
}

@Composable
private fun getStyles(): KeyboardHandlerStyles {
    val colours = GoigoiTheme.colours
    val density = LocalDensity.current
    val screenSize = DeviceProps.size
    val navBarHeightPx = WindowInsets.navigationBars.getBottom(density)

    return remember(colours, density, screenSize) {
        object: KeyboardHandlerStyles {
            override val horizPadding = 24.dp
            override val paddingTop = when (screenSize) {
                ScreenSize.LARGE -> 24.dp
                ScreenSize.NORMAL -> 8.dp
                ScreenSize.SMALL -> 8.dp
            }
            override val paddingBottom = paddingTop + with(density) { navBarHeightPx.toDp() }
            override val slideDistancePx = with(density) { 16.dp.roundToPx() }
            override val background = colours.keyboardBackground
            override val bgndShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            override val bgndBorder = BorderStroke(width = 1.dp, color = colours.keyboardOutline)
        }
    }
}

@Composable
fun Keyboard(model: ProgStudyActivityModel, modifier: Modifier = Modifier) {
    val pmode = model.presentationMode.collectAsState().value
    val kmode = model.keyboardMode.collectAsState().value
    val styles = getStyles()

    val enter = remember(styles.slideDistancePx) {
        fadeIn(
            tween(
                durationMillis = ENTER_ANIM_DURATION_MILLIS,
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = ENTER_ANIM_DURATION_MILLIS,
                easing = FastOutSlowInEasing
            ),
            initialOffsetY = { styles.slideDistancePx }
        )
    }

    val exit = remember(styles.slideDistancePx) {
        fadeOut(
            tween(
                durationMillis = EXIT_ANIM_DURATION_MILLIS,
                easing = FastOutSlowInEasing
            )
        ) + slideOutVertically(
            animationSpec = tween(
                durationMillis = EXIT_ANIM_DURATION_MILLIS,
                easing = FastOutSlowInEasing
            ),
            targetOffsetY = { styles.slideDistancePx }
        )
    }

    AnimatedVisibility(
        modifier = modifier.fillMaxWidth(),
        visible = pmode == PresentationMode.BEFORE_QUESTION || pmode == PresentationMode.QUESTION,
        enter = enter,
        exit = exit,
    ) {
        Surface(
            color = styles.background,
            shape = styles.bgndShape,
            border = styles.bgndBorder,
        ) {
            when (kmode) {
                KeyboardMode.TRIVIAL -> {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = styles.horizPadding)
                            .padding(top = styles.paddingTop, bottom = styles.paddingBottom),
                    ) {
                        Button(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            text = stringResource(R.string.reveal_btn),
                            onClick = { model.onEnterBtnClicked() },
                        )
                    }
                }
                KeyboardMode.FIXED_KEYS -> {
                    FixedKeys(
                        model = model,
                        modifier = Modifier
                            .padding(horizontal = styles.horizPadding)
                            .padding(top = styles.paddingTop, bottom = styles.paddingBottom),
                    )
                }
                KeyboardMode.HIRAGANA -> {
                    KanaKeyboard(
                        mode = KanaMode.HIRAGANA,
                        model = model,
                        modifier = Modifier
                            .padding(horizontal = styles.horizPadding)
                            .padding(top = styles.paddingTop, bottom = styles.paddingBottom),
                    )
                }
                KeyboardMode.KATAKANA -> {
                    KanaKeyboard(
                        mode = KanaMode.KATAKANA,
                        model = model,
                        modifier = Modifier
                            .padding(horizontal = styles.horizPadding)
                            .padding(top = styles.paddingTop, bottom = styles.paddingBottom),
                    )
                }
            }
        }
    }
}
