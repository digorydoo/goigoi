package io.github.digorydoo.goigoi.components.bottom_sheet.word_info

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.core.net.toUri
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.components.list.ListItem
import io.github.digorydoo.goigoi.components.list.ListSubheader
import io.github.digorydoo.goigoi.core.db.Word

private const val JISHO_URL = "https://www.jisho.org/search/"
private const val WIKTIONARY_URL = "https://en.wiktionary.org/wiki/"

private val dictionaries = arrayOf(
    Pair("jisho.org", ::getJishoUri),
    Pair("wiktionary.org", ::getWiktionaryUri),
)

private fun getJishoUri(word: String) =
    (JISHO_URL + word).toUri()

private fun getWiktionaryUri(word: String) =
    (WIKTIONARY_URL + word).toUri()

@Composable
fun SheetDictionaryLinks(word: Word, sheetHorizPadding: Dp, onLinkSelected: (Uri) -> Unit) {
    val dw = word.dictionaryWordWithHeuristic

    if (dw.isEmpty()) {
        return
    }

    val links = dictionaries.map { (label, getUri) ->
        Pair(label, getUri(dw))
    }

    ListSubheader(
        text = stringResource(R.string.look_it_up).replace("\${word}", dw),
        textHorizPadding = sheetHorizPadding,
    )

    links.forEach { (label, uri) ->
        ListItem(
            iconResId = R.drawable.ic_local_library_black_24dp,
            primaryText = label,
            secondaryText = uri.toString(),
            horizontalPadding = sheetHorizPadding,
            onClick = { onLinkSelected(uri) }
        )
    }
}
