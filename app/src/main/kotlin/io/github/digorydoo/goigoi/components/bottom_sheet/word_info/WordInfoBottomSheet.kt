package io.github.digorydoo.goigoi.components.bottom_sheet.word_info

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.components.bottom_sheet.generic.GoigoiBottomSheet
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.providers.DeviceProps
import io.github.digorydoo.goigoi.utils.ScreenSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "WordInfoBtmSheet"

private interface WordInfoBottomSheetStyles {
    val horizPadding: Dp
}

@Composable
private fun getStyles(): WordInfoBottomSheetStyles {
    val density = LocalDensity.current
    val screenSize = DeviceProps.size
    val isPortrait = DeviceProps.isPortrait

    return remember(density) {
        object: WordInfoBottomSheetStyles {
            override val horizPadding = when (screenSize) {
                ScreenSize.LARGE -> 32.dp
                ScreenSize.NORMAL -> if (isPortrait) 24.dp else 32.dp
                ScreenSize.SMALL -> 16.dp
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordInfoBottomSheet(
    word: Word,
    unyt: Unyt,
    onDismissRequest: () -> Unit,
) {
    val styles = getStyles()
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
        SheetBasicInfo(word, unyt, styles.horizPadding)
        SheetExamples(word, styles.horizPadding)
        SheetDictionaryLinks(word, styles.horizPadding, onLinkSelected)
        SheetKanjiDetails(word, styles.horizPadding, onLinkSelected)
        // SheetCtxMenuItems() not implemeneted
        SheetSeeAlso(word, styles.horizPadding)
        SheetOtherLanguages(word, styles.horizPadding)

        if (BuildConfig.DEBUG) {
            SheetDebugInfo(word, unyt, styles.horizPadding)
        }
    }
}
