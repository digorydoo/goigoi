package io.github.digorydoo.goigoi.activity.unyt.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.digorydoo.goigoi.activity.unyt.UnytActivityModel
import io.github.digorydoo.goigoi.activity.unyt.UnytActivityModel.WordInfo
import io.github.digorydoo.goigoi.composables.app_bar.GoigoiAppBar
import io.github.digorydoo.goigoi.composables.bottom_sheet.word_info.WordInfoBottomSheet
import io.github.digorydoo.goigoi.composables.buttons.GoigoiFab
import io.github.digorydoo.goigoi.composables.buttons.IconName
import io.github.digorydoo.goigoi.composables.menus.WordCtxMenu
import io.github.digorydoo.goigoi.core.db.Word

private const val MIN_NUM_WORDS_TO_ALLOW_STUDY = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnytScreen(model: UnytActivityModel, onLaunchFlipThruActivity: () -> Unit, onBack: () -> Unit) {
    val wordInfoOfCtxMenu = remember { mutableStateOf(null as WordInfo?) }
    val wordOfBottomSheet = remember { mutableStateOf(null as Word?) }
    val suppressFab = remember { mutableStateOf(false) }
    val numWords = model.numWordsInUnyt.collectAsState().value

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

            GoigoiFab(
                modifier = Modifier.align(Alignment.BottomEnd),
                shown = numWords >= MIN_NUM_WORDS_TO_ALLOW_STUDY && !suppressFab.value,
                iconName = IconName.START,
                onClick = onLaunchFlipThruActivity,
            )
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
