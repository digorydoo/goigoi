package io.github.digorydoo.goigoi.activity.unyt.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.digorydoo.goigoi.activity.unyt.UnytActivityModel
import io.github.digorydoo.goigoi.activity.unyt.UnytActivityModel.WordInfo
import io.github.digorydoo.goigoi.composables.app_bar.GoigoiAppBar
import io.github.digorydoo.goigoi.composables.bottom_sheet.word_info.WordInfoBottomSheet
import io.github.digorydoo.goigoi.composables.bottom_sheet.word_info.WordInfoBottomSheetModel
import io.github.digorydoo.goigoi.composables.menus.WordCtxMenu
import io.github.digorydoo.goigoi.core.db.Word

@Composable
fun UnytScreen(model: UnytActivityModel, bottomSheetModel: WordInfoBottomSheetModel, onBack: () -> Unit) {
    val wordInfoOfCtxMenu = remember { mutableStateOf(null as WordInfo?) }
    val wordOfBottomSheet = remember { mutableStateOf(null as Word?) }
    val suppressFab = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GoigoiAppBar(
                title = model.unytName,
                onBack = onBack,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            UnytContent(
                modifier = Modifier.fillMaxSize(), // UnytContent has a LazyColumn
                model = model,
                onWordClicked = { info ->
                    wordOfBottomSheet.value = info.word
                    model.didOpenBottomSheet()
                    suppressFab.value = true
                },
                onWordLongPressed = { info ->
                    wordInfoOfCtxMenu.value = info
                    suppressFab.value = true
                }
            )

            /*
                Legacy FlipThruActivity has been removed!
                We might re-implement it in Compose (but I hardly ever used it).
                We might also use the FAB to launch progressive mode restricted to this unyt (but that mode is gone,
                too).

                val numWords = model.numWordsInUnyt.collectAsState().value

                if (numWords >= MIN_NUM_WORDS_TO_ALLOW_STUDY) {
                    GoigoiFab(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        shown = numWords >= MIN_NUM_WORDS_TO_ALLOW_STUDY && !suppressFab.value,
                        iconName = IconName.START,
                        onClick = { println("Flashcard AKA flip-thru is no longer available") }
                    )
                }
            */
        }

        wordInfoOfCtxMenu.value?.let { info ->
            WordCtxMenu(
                model,
                info.word,
                isWordInMyWords = info.data?.isInMyWords ?: false,
                onDismissRequest = {
                    wordInfoOfCtxMenu.value = null
                    suppressFab.value = false
                }
            )
        }

        wordOfBottomSheet.value?.let { word ->
            WordInfoBottomSheet(
                bottomSheetModel,
                word,
                model.unyt,
                onDismissRequest = {
                    wordOfBottomSheet.value = null
                    suppressFab.value = false
                    model.updateItemOfWord(word)
                    model.setHighlightedWord(word)
                }
            )
        }
    }
}
