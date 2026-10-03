package io.github.digorydoo.goigoi.activity.prog_study.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.activity.prog_study.composables.keyboard.Keyboard
import io.github.digorydoo.goigoi.activity.prog_study.composables.qa.*
import io.github.digorydoo.goigoi.composables.app_bar.EmptyAppBar
import io.github.digorydoo.goigoi.composables.bottom_sheet.word_info.WordInfoBottomSheet
import io.github.digorydoo.goigoi.composables.buttons.GoigoiFab
import io.github.digorydoo.goigoi.composables.buttons.IconName
import io.github.digorydoo.goigoi.composables.buttons.OverlappingIconBtn
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.study.Answer
import io.github.digorydoo.goigoi.utils.ScreenSize

private interface ProgStudyScreenStyles {
    val contentHorizPadding: Dp
    val backBtnMarginStart: Dp
    val backBtnMarginTop: Dp
    val infoBtnMarginStart: Dp
    val infoBtnMarginBottom: Dp
    val fabMarginBottom: Dp
    val keyboardMaxWidth: Dp
    val overlappingBtnBgnd: Color
}

@Composable
private fun getStyles(): ProgStudyScreenStyles {
    val colours = GoigoiTheme.colours
    val screenSize = DeviceProps.size
    val isPortrait = DeviceProps.isPortrait

    return remember(colours, screenSize, isPortrait) {
        object: ProgStudyScreenStyles {
            override val contentHorizPadding = when (screenSize) {
                ScreenSize.LARGE -> 32.dp
                ScreenSize.NORMAL -> if (isPortrait) 16.dp else 24.dp
                ScreenSize.SMALL -> 8.dp
            }
            override val backBtnMarginStart = 4.dp
            override val backBtnMarginTop = 4.dp
            override val infoBtnMarginStart = 24.dp
            override val infoBtnMarginBottom = 32.dp
            override val fabMarginBottom = 24.dp
            override val keyboardMaxWidth = 512.dp
            override val overlappingBtnBgnd = colours.background.copy(alpha = 0.9f)
        }
    }
}

@Composable
fun ProgStudyScreen(model: ProgStudyActivityModel, onBack: () -> Unit) {
    val styles = getStyles()
    val dir = LocalLayoutDirection.current
    val density = LocalDensity.current
    val wordOfBottomSheet = remember { mutableStateOf(null as Word?) }
    val suppressFab = remember { mutableStateOf(false) }
    val mode = model.presentationMode.collectAsState().value
    val answerCorrectness = model.answerCorrectness.collectAsState().value
    val word = model.word.collectAsState().value
    val unytOfWord = model.unytOfWord.collectAsState().value
    val keyboardHeight = remember { mutableStateOf(0.dp) }

    Scaffold(
        topBar = { EmptyAppBar(onBack = onBack) }
    ) { innerPadding ->
        val innerPaddingTop = innerPadding.calculateTopPadding()
        val innerPaddingBottom = innerPadding.calculateBottomPadding()
        val innerPaddingStart = innerPadding.calculateStartPadding(dir)
        val innerPaddingEnd = innerPadding.calculateEndPadding(dir)

        Box(modifier = Modifier.fillMaxSize()) {
            ScrollContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                topArea = {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Start)
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                    ) {
                        InstructionHint(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(
                                    start = styles.contentHorizPadding + 40.dp,
                                    top = 18.dp,
                                    end = styles.contentHorizPadding + 40.dp,
                                ),
                            model = model,
                        )
                        StudyProgressIcon(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = styles.contentHorizPadding),
                            model = model,
                        )
                        AnswerCommentFlag(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            model = model,
                        )
                    }
                },
                centreArea = { maxIdealHeight ->
                    Question(model, outerContainerMaxIdealHeight = maxIdealHeight)
                    QuestionHint(model, enterDelayIdx = 1)
                    AnswerField(model, enterDelayIdx = 2)
                    CorrectedAnswer(model)
                    MessageAfterAnswer(model)

                    KanjiOrKanaToReveal(model)
                    TranslationToReveal(model)
                    HintToReveal(model)

                    Explanation(model)
                },
                centreAreaHorizPadding = styles.contentHorizPadding,
                keyboardHeight = when (mode) {
                    PresentationMode.BEFORE_QUESTION, PresentationMode.QUESTION -> keyboardHeight.value
                    else -> 0.dp // onGloballyPositioned may still see a height while Keyboard is going away
                },
                mode = mode,
            )

            // Back button
            OverlappingIconBtn(
                iconResId = R.drawable.ic_arrow_left_24dp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = styles.backBtnMarginStart + innerPaddingStart,
                        y = styles.backBtnMarginTop + innerPaddingTop,
                    ),
                onClick = onBack,
            )

            // Info button
            OverlappingIconBtn(
                iconResId = R.drawable.ic_info_24dp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(
                        x = styles.infoBtnMarginStart + innerPaddingStart,
                        y = -(styles.infoBtnMarginBottom + innerPaddingBottom),
                    ),
                shown = mode == PresentationMode.ANSWER_CHECK,
                onClick = { wordOfBottomSheet.value = word }
            )

            GoigoiFab(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(
                        x = -innerPaddingEnd,
                        y = -(styles.fabMarginBottom + innerPaddingBottom),
                    ),
                shown = !suppressFab.value && when (mode) {
                    PresentationMode.ANSWER_CHECK -> true
                    PresentationMode.REVEAL_TEXTS -> true
                    PresentationMode.EXPLANATION -> true
                    else -> false
                },
                allowGlow = false,
                autoMarginBottom = false,
                iconName = IconName.NEXT_WORD,
                background = when (answerCorrectness) {
                    Answer.CORRECT_EXCEPT_KANA_SIZE -> GoigoiTheme.colours.almostCorrectAnswerBackground
                    Answer.WRONG -> GoigoiTheme.colours.wrongAnswerBackground
                    else -> GoigoiTheme.colours.primary
                },
                onClick = { model.onNextBtnClicked() },
            )

            Keyboard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .widthIn(max = styles.keyboardMaxWidth)
                    .fillMaxWidth()
                    .onGloballyPositioned { coords ->
                        keyboardHeight.value = with(density) { coords.size.height.toDp() }
                    },
                model = model,
            )

            if (unytOfWord != null) {
                wordOfBottomSheet.value?.let { word ->
                    WordInfoBottomSheet(
                        word,
                        unytOfWord,
                        onDismissRequest = {
                            wordOfBottomSheet.value = null
                            suppressFab.value = false
                        }
                    )
                }
            }

            AnswerResponseOverlay(
                modifier = Modifier.align(Alignment.Center),
                model = model
            )
        }
    }
}
