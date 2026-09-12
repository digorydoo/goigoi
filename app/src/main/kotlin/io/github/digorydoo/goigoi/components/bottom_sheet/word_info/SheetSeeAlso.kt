package io.github.digorydoo.goigoi.components.bottom_sheet.word_info

import android.util.Log
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.digorydoo.kutils.cjk.Unicode
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.components.list.ListSubheader
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.db.WordLink
import io.github.digorydoo.goigoi.legacy.spannable.FuriganaBuilder
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import java.util.Locale

private const val SEE_ALSO_PRIMARY_FORM_SIZE_SP = 20f
private const val TAG = "SheetSeeAlso"

@Composable
fun SheetSeeAlso(word: Word, sheetHorizPadding: Dp) {
    if (word.links.isEmpty()) {
        return
    }

    val colours = GoigoiTheme.colours
    val typography = GoigoiTheme.typography
    val systemLang = Locale.getDefault().isO3Language ?: "" // eng, ger
    val similarMeaning = stringResource(R.string.similar_meaning)

    // De-duplicate links as compileGoigoi de-duplicates them only within the same kind
    val linksEmitted = mutableSetOf<String>()

    for (link in word.links) {
        if (linksEmitted.contains(link.wordId)) {
            Log.d(TAG, "Not showing duplicate link to ${link.wordId}: ${link.kind}")
        } else {
            val text = when (link.kind) {
                WordLink.Kind.SAME_READING -> stringResource(R.string.same_reading)
                WordLink.Kind.SAME_KANJI -> stringResource(R.string.same_kanji)
                WordLink.Kind.SAME_EN_TRANSLATION -> if (systemLang == "eng") similarMeaning else null
                WordLink.Kind.SAME_DE_TRANSLATION -> if (systemLang == "ger") similarMeaning else null
                WordLink.Kind.CLOSELY_RELATED -> stringResource(R.string.closely_related)
                WordLink.Kind.KEEP_APART -> null
                WordLink.Kind.TRANSITIVE_VERB -> stringResource(R.string.transitive_verb)
                WordLink.Kind.INTRANSITIVE_VERB -> stringResource(R.string.intransitive_verb)
                WordLink.Kind.NOUN -> stringResource(R.string.noun)
                WordLink.Kind.VERB -> stringResource(R.string.verb)
                WordLink.Kind.ADJECTIVE -> stringResource(R.string.adjective)
                WordLink.Kind.ANTONYM -> stringResource(R.string.antonym)
                null -> null
            }

            if (text != null) {
                val anyEmitted = linksEmitted.isNotEmpty()

                if (!anyEmitted) {
                    ListSubheader(text = stringResource(R.string.see_also), textHorizPadding = sheetHorizPadding)
                }

                val primaryText = FuriganaBuilder.buildSpan(link.primaryForm)
                val secondaryText = "${link.translation.withSystemLang} ${Unicode.MIDDLE_DOT} $text"

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = sheetHorizPadding)
                        .padding(top = if (anyEmitted) 16.dp else 4.dp),
                    factory = { ctx ->
                        TextView(ctx).apply {
                            setTextSize(TypedValue.COMPLEX_UNIT_SP, SEE_ALSO_PRIMARY_FORM_SIZE_SP)
                            setTextColor(colours.onBackground.toArgb())
                        }
                    },
                    update = { textView ->
                        textView.text = primaryText
                    }
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = sheetHorizPadding)
                        .padding(top = 4.dp), // keep this consistent with ListItem's secondaryTextTopMargin
                    text = secondaryText,
                    style = typography.listItemSecondaryText,
                    color = colours.onBackgroundSecondary,
                )

                linksEmitted.add(link.wordId)
            }
        }
    }
}
