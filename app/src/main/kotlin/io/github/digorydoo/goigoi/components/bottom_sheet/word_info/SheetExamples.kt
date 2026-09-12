package io.github.digorydoo.goigoi.components.bottom_sheet.word_info

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.components.list.ListSubheader
import io.github.digorydoo.goigoi.core.db.PhraseOrSentence
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.legacy.spannable.FuriganaBuilder
import io.github.digorydoo.goigoi.legacy.spannable.buildSpan
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.appendColoured
import io.github.digorydoo.goigoi.utils.appendStyled

private const val EXAMPLE_PRIMARY_FORM_SIZE_SP = 24f

@Composable
private fun Example(phraseOrSentence: PhraseOrSentence, word: Word, marginTop: Dp, sheetHorizPadding: Dp) {
    val colours = GoigoiTheme.colours
    val typography = GoigoiTheme.typography
    val density = LocalDensity.current
    val secondaryTextSizePx = with(density) { typography.listItemSecondaryText.fontSize.toPx() }

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

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sheetHorizPadding)
            .padding(top = marginTop),
        factory = { ctx ->
            TextView(ctx).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, EXAMPLE_PRIMARY_FORM_SIZE_SP)
                setTextColor(colours.onBackground.toArgb())
            }
        },
        update = { textView ->
            textView.text = primaryForm
        }
    )

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sheetHorizPadding)
            .padding(top = 4.dp), // keep this consistent with ListItem's secondaryTextTopMargin
        text = phraseOrSentence.translation.withSystemLang,
        style = typography.listItemSecondaryText,
        color = colours.onBackgroundSecondary,
    )

    val explanation = phraseOrSentence.explanation.withSystemLang

    if (explanation.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = sheetHorizPadding)
                .padding(top = 16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                modifier = Modifier.padding(end = 24.dp),
                imageVector = ImageVector.vectorResource(R.drawable.ic_point_right_black_24dp),
                contentDescription = null,
                tint = GoigoiTheme.colours.decorativeIconTint
            )
            AndroidView(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp),
                factory = { ctx ->
                    TextView(ctx).apply {
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, secondaryTextSizePx)
                        setTextColor(colours.onBackground.toArgb())
                        setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC))
                    }
                },
                update = { textView ->
                    textView.text = FuriganaBuilder.buildSpan(explanation)
                }
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
