package io.github.digorydoo.goigoi.components.bottom_sheet.word_info

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.core.net.toUri
import ch.digorydoo.kutils.cjk.isCJKNotKana
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.components.list.ListItem
import io.github.digorydoo.goigoi.components.list.ListSubheader
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.utils.SingletonHolder

private const val JISHO_KANJI_URL = "https://jisho.org/search/{KANJI}%23kanji"

private fun getJishoKanjiUri(kanji: Char) =
    JISHO_KANJI_URL.replace("{KANJI}", "$kanji").toUri()

@Composable
fun SheetKanjiDetails(word: Word, sheetHorizPadding: Dp, onLinkSelected: (Uri) -> Unit) {
    val kanjis = word.kanji.filter { it.isCJKNotKana() }.toSet()

    if (kanjis.isEmpty()) {
        return
    }

    ListSubheader(stringResource(R.string.kanji_details), sheetHorizPadding)

    val kanjiIndex = SingletonHolder.kanjiIndex

    for (kanji in kanjis) {
        val kanjiStr = kanji.toString()

        val readings = word.primaryForm.readings
            .filter { it.kanji == kanjiStr }
            .map { it.kana }
            .toMutableSet() // Kotlin Sets preserve ordering, so the reading appearing in the word will appear first
            .apply { addAll(kanjiIndex.getReadingsOfKanji(kanji).sorted()) }

        val uri = getJishoKanjiUri(kanji)

        ListItem(
            iconFromChar = kanji,
            primaryText = readings.joinToString("・"),
            secondaryText = kanjiIndex.levelOfKanji(kanji)?.toPrettyString()
                ?: stringResource(R.string.jlpt_level_unknown),
            horizontalPadding = sheetHorizPadding,
            onClick = { onLinkSelected(uri) }
        )
    }
}
