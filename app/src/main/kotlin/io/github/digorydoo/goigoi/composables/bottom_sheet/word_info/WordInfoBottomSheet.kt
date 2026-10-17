package io.github.digorydoo.goigoi.composables.bottom_sheet.word_info

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.composables.bottom_sheet.generic.GoigoiBottomSheet
import io.github.digorydoo.goigoi.composables.providers.DeviceProps
import io.github.digorydoo.goigoi.core.db.KanjiIndex
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.stats.IWordAndUnytStats
import io.github.digorydoo.goigoi.utils.ScreenSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "WordInfoBtmSheet"

class WordInfoBottomSheetModel(
    val kanjiIndex: KanjiIndex,
    val stats: IWordAndUnytStats,
) {
    // TODO actions for SheetCtxMenuItems
}

@Composable
fun WordInfoBottomSheet(
    model: WordInfoBottomSheetModel,
    word: Word,
    unyt: Unyt,
    onDismissRequest: () -> Unit,
) {
    val screenSize = DeviceProps.size
    val isPortrait = DeviceProps.isPortrait

    val horizPadding = when (screenSize) {
        ScreenSize.LARGE -> 32.dp
        ScreenSize.NORMAL -> if (isPortrait) 24.dp else 32.dp
        ScreenSize.SMALL -> 16.dp
    }

    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val onLinkSelected: (Uri) -> Unit = { uri ->
        scope.launch {
            try {
                delay(100.milliseconds) // let the user see the tap ripple before navigating away
                val intent = Intent(Intent.ACTION_VIEW, uri)
                view.context.startActivity(intent)
            } catch (e: Exception) {
                Log.d(TAG, "Failed to open URI in browser: $e")
            }
        }
    }

    GoigoiBottomSheet(onDismissRequest = onDismissRequest) {
        SheetBasicInfo(model, word, unyt, horizPadding)
        SheetExamples(word, horizPadding)
        SheetAltReadings(word, horizPadding)
        SheetAltSpellings(word, horizPadding)
        SheetDictionaryLinks(word, horizPadding, onLinkSelected)
        // TODO SheetCtxMenuItems(model, word, horizPadding)
        SheetSeeAlso(word, horizPadding)
        SheetKanjiDetails(model, word, horizPadding, onLinkSelected)
        SheetOtherLanguages(word, horizPadding)

        if (BuildConfig.DEBUG) {
            SheetDebugInfo(model, word, unyt, horizPadding)
        }
    }
}
