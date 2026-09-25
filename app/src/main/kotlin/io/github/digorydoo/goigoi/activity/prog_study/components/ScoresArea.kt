package io.github.digorydoo.goigoi.activity.prog_study.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.core.study.Answer
import io.github.digorydoo.goigoi.providers.GoigoiTheme

private val SCORE_FONT_SIZE = 17.sp
private const val SCORE_BGND_ALPHA = 0.8f

@Composable
fun ScoresArea(model: ProgStudyActivityModel, modifier: Modifier = Modifier) {
    val colours = GoigoiTheme.colours
    val density = LocalDensity.current

    val numCorrect = model.numCorrect.collectAsState().value
    val numWrong = model.numWrong.collectAsState().value
    val correctness = model.answerCorrectness.collectAsState().value

    val correctBgnd = remember {
        val c = colours.primary
        Brush.linearGradient(
            colors = listOf(
                c.copy(alpha = 0f),
                c.copy(alpha = 0.1f),
                c.copy(alpha = 0.6f),
                c.copy(alpha = 0.8f),
                c.copy(alpha = 0.9f),
                c.copy(alpha = 1f),
            ),
        )
    }

    val wrongBgnd = remember {
        val c = colours.warningBackground
        Brush.linearGradient(
            colors = listOf(
                c.copy(alpha = 0f),
                c.copy(alpha = 0.1f),
                c.copy(alpha = 0.6f),
                c.copy(alpha = 0.8f),
                c.copy(alpha = 0.9f),
                c.copy(alpha = 1f),
            ),
        )
    }

    val bgnd = when (correctness) {
        Answer.CORRECT, Answer.CORRECT_EXCEPT_KANA_SIZE -> correctBgnd
        Answer.WRONG -> wrongBgnd
        else -> null
    }

    val correctTemplate = stringResource(R.string.correct_count)
    val wrongTemplate = stringResource(R.string.wrong_count)

    val text = remember(correctness, numCorrect, numWrong) {
        when (correctness) {
            Answer.CORRECT, Answer.CORRECT_EXCEPT_KANA_SIZE -> {
                correctTemplate.replace("\${N}", "$numCorrect")
            }
            Answer.WRONG -> {
                wrongTemplate.replace("\${N}", "$numWrong")
            }
            else -> ""
        }
    }

    val currentBgnd = remember { mutableStateOf<Brush>(SolidColor(Color.Transparent)) }
    val currentText = remember { mutableStateOf("") }
    val anim = remember { Animatable(0f) }

    LaunchedEffect(bgnd) {
        if (bgnd != null) {
            // Entering
            currentBgnd.value = bgnd
            currentText.value = text
            anim.snapTo(0f)
            anim.animateTo(1f, animationSpec = tween(durationMillis = 200, easing = EaseOutCubic))
        } else {
            // Exiting
            anim.animateTo(0f, animationSpec = tween(durationMillis = 400, easing = EaseInCubic))
        }
    }

    val maxTranslationPx = with(density) { 48.dp.toPx() }

    Box(
        modifier = modifier.heightIn(min = 32.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            modifier = Modifier
                .graphicsLayer(alpha = anim.value, translationX = (1f - anim.value) * maxTranslationPx)
                .background(currentBgnd.value, alpha = SCORE_BGND_ALPHA)
                .padding(start = 64.dp, end = 24.dp, top = 2.dp, bottom = 2.dp),
            color = colours.onBackground,
            text = currentText.value,
            fontSize = SCORE_FONT_SIZE,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}
