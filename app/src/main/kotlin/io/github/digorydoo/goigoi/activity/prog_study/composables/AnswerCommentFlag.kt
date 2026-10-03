package io.github.digorydoo.goigoi.activity.prog_study.composables

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.core.study.Answer

private val SCORE_FONT_SIZE = 17.sp
private val gradientSteps = listOf(0f, 0.1f, 0.68f, 0.84f, 0.92f, 0.96f, 0.98f, 0.99f, 1f, 1f)

@Composable
fun AnswerCommentFlag(model: ProgStudyActivityModel, modifier: Modifier = Modifier) {
    val colours = GoigoiTheme.colours
    val density = LocalDensity.current

    val correctness = model.answerCorrectness.collectAsState().value

    val correctBgnd = remember {
        val c = colours.primary
        Brush.linearGradient(colors = gradientSteps.map { c.copy(alpha = it) })
    }

    val almostCorrectBgnd = remember {
        val c = colours.almostCorrectAnswerBackground
        Brush.linearGradient(colors = gradientSteps.map { c.copy(alpha = it) })
    }

    val wrongBgnd = remember {
        val c = colours.wrongAnswerBackground
        Brush.linearGradient(colors = gradientSteps.map { c.copy(alpha = it) })
    }

    val pmode = model.presentationMode.collectAsState().value

    val bgnd = when (pmode) {
        PresentationMode.ANSWER_CHECK -> when (correctness) {
            Answer.CORRECT -> correctBgnd
            Answer.CORRECT_EXCEPT_KANA_SIZE -> almostCorrectBgnd
            Answer.WRONG -> wrongBgnd
            else -> null
        }
        else -> null
    }

    val text = model.answerComment.collectAsState().value

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

    if (anim.value <= 0f || text.isEmpty()) return
    val maxTranslationPx = with(density) { 48.dp.toPx() }

    Box(
        modifier = modifier.heightIn(min = 32.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            modifier = Modifier
                .graphicsLayer(alpha = anim.value, translationX = (1f - anim.value) * maxTranslationPx)
                .background(currentBgnd.value)
                .padding(start = 72.dp, end = 24.dp, top = 2.dp, bottom = 2.dp),
            color = colours.onPrimary,
            text = currentText.value,
            fontSize = SCORE_FONT_SIZE,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
    }
}
