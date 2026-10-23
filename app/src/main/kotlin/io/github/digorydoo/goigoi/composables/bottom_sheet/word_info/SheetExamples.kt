package io.github.digorydoo.goigoi.composables.bottom_sheet.word_info

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.digorydoo.kutils.cjk.FuriganaString
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.composables.list.ListSubheader
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.composables.text.FuriganaText
import io.github.digorydoo.goigoi.core.db.PhraseOrSentence
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.legacy.buildSpan
import io.github.digorydoo.goigoi.utils.appendColoured
import io.github.digorydoo.goigoi.utils.appendStyled

private const val EXAMPLE_PRIMARY_FORM_SIZE_SP = 24f

@Composable
private fun Example(phraseOrSentence: PhraseOrSentence, word: Word, marginTop: Dp, sheetHorizPadding: Dp) {
    val colours = GoigoiTheme.colours
    val typography = GoigoiTheme.typography

    val parts = phraseOrSentence.primaryFormInParts(word)

    val primaryForm = SpannableStringBuilder().apply {
        append(parts.begin.buildSpan())

        appendColoured(colours.emphasizedText) {
            appendStyled(Typeface.BOLD) {
                append(parts.wordStem.buildSpan())
            }
            append(parts.wordSuffix) // doesn't have furigana
        }

        append(parts.end.buildSpan())
    }

    FuriganaText(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sheetHorizPadding)
            .padding(top = marginTop),
        text = primaryForm,
        fontSize = EXAMPLE_PRIMARY_FORM_SIZE_SP.sp,
        color = colours.onBackground,
    )

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sheetHorizPadding)
            .padding(top = 4.dp), // keep this consistent with ListItem's secondaryTextTopMargin
        text = phraseOrSentence.translation.withSystemLang,
        style = typography.listItemSecondaryText,
        color = colours.secondaryOnBackground,
    )

    val explanation = FuriganaString(phraseOrSentence.explanation.withSystemLang)

    if (explanation.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = sheetHorizPadding)
                .padding(top = 16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                modifier = Modifier.padding(
                    top = if (explanation.hasFurigana) 6.dp else 0.dp,
                    end = 24.dp,
                ),
                imageVector = ImageVector.vectorResource(R.drawable.ic_point_right_black_24dp),
                contentDescription = null,
                tint = GoigoiTheme.colours.decorativeIconTint
            )
            FuriganaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp),
                text = explanation,
                textFontSize = typography.listItemSecondaryText.fontSize,
                color = colours.onBackground,
                typeface = Typeface.defaultFromStyle(Typeface.ITALIC),
            )
        }
    }
}

@Composable
fun SheetExamples(word: Word, sheetHorizPadding: Dp) {
    val phrases = word.phrases
    val sentences = word.sentences

    if (phrases.isEmpty() && sentences.isEmpty()) {
        return
    }

    ListSubheader(stringResource(R.string.examples), sheetHorizPadding)

    var marginTop = 4.dp
    val marginTopOfNext = 16.dp

    for (phrase in phrases) {
        Example(phrase, word, marginTop, sheetHorizPadding)
        marginTop = marginTopOfNext
    }

    for (sentence in sentences) {
        Example(sentence, word, marginTop, sheetHorizPadding)
        marginTop = marginTopOfNext
    }
}
