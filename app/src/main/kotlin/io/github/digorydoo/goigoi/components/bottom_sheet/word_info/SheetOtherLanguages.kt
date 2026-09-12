package io.github.digorydoo.goigoi.components.bottom_sheet.word_info

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import ch.digorydoo.kutils.cjk.Unicode
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.components.list.ListItem
import io.github.digorydoo.goigoi.components.list.ListSubheader
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.utils.LangUtils

private data class LangInfo(val translation: String, val hint: String, val langId: String)

@Composable
fun SheetOtherLanguages(word: Word, sheetHorizPadding: Dp) {
    val sysTranslation = word.translation.withSystemLang

    val languages = word.translation.availableLanguages()
        .map { langId ->
            LangInfo(
                word.translation.withLanguage(langId),
                word.hintsWithLanguage(langId),
                langId
            )
        }
        .filter { it.translation != sysTranslation }

    if (languages.isEmpty()) {
        return
    }

    ListSubheader(stringResource(R.string.other_languages), sheetHorizPadding)

    val view = LocalView.current
    val ctx = view.context

    languages.forEach { (translation, hint, langId) ->
        val langAsString = LangUtils.humanizeLangId(langId, ctx)

        ListItem(
            primaryText = translation,
            secondaryText = arrayOf(langAsString, hint)
                .filter { it.isNotEmpty() }
                .joinToString(" ${Unicode.MIDDLE_DOT} "),
            horizontalPadding = sheetHorizPadding,
        )
    }
}
