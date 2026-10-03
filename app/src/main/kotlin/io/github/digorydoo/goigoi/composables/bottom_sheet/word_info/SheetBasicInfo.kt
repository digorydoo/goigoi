package io.github.digorydoo.goigoi.composables.bottom_sheet.word_info

import android.util.TypedValue
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.IntlString
import ch.digorydoo.kutils.cjk.JLPTLevel
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.composables.GoigoiBadge
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.composables.providers.Singletons
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.db.WordCategory
import io.github.digorydoo.goigoi.legacy.spannable.FuriganaSpan
import io.github.digorydoo.goigoi.legacy.spannable.buildSpan
import kotlin.math.roundToInt

private const val XLARGE_PRIMARY_TEXT_MAX_SIZE = 5
private const val XLARGE_PRIMARY_TEXT_SIZE_SP = 48f
private const val LARGE_PRIMARY_TEXT_SIZE_SP = 32f

@Composable
fun SheetBasicInfo(word: Word, unyt: Unyt, horizPadding: Dp) {
    val kanji = word.kanji
    val kana = word.kana

    val showKanaAsPrimary = when {
        kana.isEmpty() -> false
        kana == word.primaryForm.raw -> false
        word.usuallyInKana -> true
        else -> false
    }

    val stats = Singletons.stats
    val seenCount = stats.getWordTotalSeenCount(word)
    val progress = stats.getWordStudyProgress(word)
    val rating = stats.getWordTotalRating(word)

    PrimaryForm(
        primaryForm = word.primaryForm,
        kana = kana,
        kanji = kanji,
        usuallyInKana = word.usuallyInKana,
        showKanaAsPrimary = showKanaAsPrimary,
        horizPadding = horizPadding,
        marginTop = 8.dp,
    )
    Romaji(romaji = word.romaji, horizPadding = horizPadding, marginTop = 4.dp)
    SometimesWithKanji(
        kanji = kanji,
        usuallyInKana = word.usuallyInKana,
        showKanaAsPrimary = showKanaAsPrimary,
        horizPadding = horizPadding,
        marginTop = 4.dp
    )

    Translation(translation = word.translation, horizPadding = horizPadding, marginTop = 16.dp)
    Hint(hint = word.hintsWithSystemLang, horizPadding = horizPadding, marginTop = 4.dp)
    Categories(categories = word.cats, horizPadding = horizPadding, marginTop = 4.dp)

    JLPTLevelBadge(level = word.level, horizPadding = horizPadding, marginTop = 16.dp)

    KanjiDifficultForLevel(
        kanji = kanji,
        kana = kana,
        usuallyInKana = word.usuallyInKana,
        unytLevel = unyt.levelOfMostDifficultWord,
        horizPadding = horizPadding,
        marginTop = 16.dp
    )

    WordStats(
        seenCount = seenCount,
        progress = progress,
        rating = rating,
        horizPadding = horizPadding,
        marginTop = 16.dp
    )
}

@Composable
private fun PrimaryForm(
    primaryForm: FuriganaString,
    kana: String,
    kanji: String,
    usuallyInKana: Boolean,
    showKanaAsPrimary: Boolean,
    horizPadding: Dp,
    marginTop: Dp,
) {
    val colours = GoigoiTheme.colours
    val density = LocalDensity.current
    val furiganaMinFontSizePx = with(density) { 12.dp.toPx() }
    val furiganaMaxFontSizePx = with(density) { 14.dp.toPx() }

    val primaryFormText = when {
        primaryForm.isEmpty() -> "?"
        showKanaAsPrimary -> kana
        else -> primaryForm.buildSpan(
            FuriganaSpan.Options(
                fontSizeMin = furiganaMinFontSizePx,
                fontSizeMax = furiganaMaxFontSizePx,
            )
        )
    }

    val primaryFormVisuallyRelevantLength = when {
        usuallyInKana -> kana.length
        else -> kanji.length
    }

    val primaryFormSize =
        if (primaryFormVisuallyRelevantLength <= XLARGE_PRIMARY_TEXT_MAX_SIZE) XLARGE_PRIMARY_TEXT_SIZE_SP
        else LARGE_PRIMARY_TEXT_SIZE_SP

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizPadding)
            .padding(top = marginTop),
        factory = { ctx -> TextView(ctx) },
        update = { textView ->
            textView.apply {
                text = primaryFormText
                setTextSize(TypedValue.COMPLEX_UNIT_SP, primaryFormSize)
                setTextColor(colours.onBackground.toArgb())
            }
        }
    )
}

@Composable
private fun Romaji(romaji: String, horizPadding: Dp, marginTop: Dp) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizPadding)
            .padding(top = marginTop),
        text = romaji,
        style = GoigoiTheme.typography.hint,
        fontSize = GoigoiTheme.typography.listItemSecondaryText.fontSize,
        color = GoigoiTheme.colours.secondaryOnBackground,
    )
}

@Composable
private fun Translation(translation: IntlString, horizPadding: Dp, marginTop: Dp) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizPadding)
            .padding(top = marginTop),
        text = translation.withSystemLang,
        style = GoigoiTheme.typography.listItemPrimaryText,
        color = GoigoiTheme.colours.onBackground,
    )
}

@Composable
private fun Hint(hint: String, horizPadding: Dp, marginTop: Dp) {
    if (hint.isEmpty()) return

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizPadding)
            .padding(top = marginTop),
        text = hint,
        style = GoigoiTheme.typography.hint,
        fontSize = GoigoiTheme.typography.listItemSecondaryText.fontSize,
        color = GoigoiTheme.colours.secondaryOnBackground,
    )
}

@Composable
private fun Categories(categories: List<WordCategory>, horizPadding: Dp, marginTop: Dp) {
    val cats = categories.joinToString(" ") { "#${it.text}" }
    if (cats.isEmpty()) return

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizPadding)
            .padding(top = marginTop),
        text = cats,
        style = GoigoiTheme.typography.hint,
        fontSize = GoigoiTheme.typography.listItemSecondaryText.fontSize,
        color = GoigoiTheme.colours.secondaryOnBackground,
    )
}

@Composable
private fun JLPTLevelBadge(level: JLPTLevel?, horizPadding: Dp, marginTop: Dp) {
    val jlptLevel = level?.takeIf { it != JLPTLevel.Nx }?.toPrettyString()
    if (jlptLevel == null) return

    GoigoiBadge(
        modifier = Modifier
            .padding(horizontal = horizPadding)
            .padding(top = marginTop),
        text = jlptLevel
    )
}

@Composable
private fun SometimesWithKanji(
    kanji: String,
    usuallyInKana: Boolean,
    showKanaAsPrimary: Boolean,
    horizPadding: Dp,
    marginTop: Dp,
) {
    if (showKanaAsPrimary && usuallyInKana) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizPadding)
                .padding(top = marginTop),
            text = stringResource(R.string.sometimes_with_kanji).replace("\${kanji}", kanji),
            style = GoigoiTheme.typography.listItemSecondaryText, // not italic, because text contains kanji
            color = GoigoiTheme.colours.secondaryOnBackground,
        )
    }
}

@Composable
private fun KanjiDifficultForLevel(
    kanji: String,
    kana: String,
    usuallyInKana: Boolean,
    unytLevel: JLPTLevel,
    horizPadding: Dp,
    marginTop: Dp,
) {
    val kanjiIndex = Singletons.kanjiIndex

    val kanjiDifficultForLevel = when {
        usuallyInKana || kanji == kana -> null
        else -> {
            when (unytLevel) {
                JLPTLevel.Nx, JLPTLevel.N1 -> null
                else -> {
                    val kanjiLevel = kanjiIndex.levelOfMostDifficultKanji(kanji) ?: JLPTLevel.N5
                    when {
                        !kanjiLevel.isMoreDifficultThan(unytLevel) -> null
                        else -> {
                            stringResource(R.string.kanji_more_difficult)
                                .replace("\${level}", unytLevel.toPrettyString())
                                .replace("\${kana}", kana)
                        }
                    }
                }
            }
        }
    }

    if (kanjiDifficultForLevel != null) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizPadding)
                .padding(top = marginTop),
            text = kanjiDifficultForLevel,
            style = GoigoiTheme.typography.listItemSecondaryText, // keep this consistent with SometimesWithKanji
            color = GoigoiTheme.colours.secondaryOnBackground,
        )
    }
}

@Composable
private fun WordStats(seenCount: Int, progress: Float, rating: Float, horizPadding: Dp, marginTop: Dp) {
    val seenCountMsg = when {
        seenCount <= 0 -> stringResource(R.string.not_studied_yet)
        else -> stringResource(R.string.studied_n_times).replace("\${N}", "" + seenCount)
    }

    val progressOrRatingMsg = when {
        seenCount <= 0 -> null
        progress < 1.0f -> stringResource(R.string.progress) + ": ${(100.0f * progress).roundToInt()}%"
        else -> stringResource(R.string.rating) + ": ${(100.0f * rating).roundToInt()}%"
    }

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizPadding)
            .padding(top = marginTop),
        text = seenCountMsg,
        style = GoigoiTheme.typography.listItemSecondaryText,
        color = GoigoiTheme.colours.secondaryOnBackground,
    )

    if (progressOrRatingMsg != null) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizPadding),
            text = progressOrRatingMsg,
            style = GoigoiTheme.typography.listItemSecondaryText,
            color = GoigoiTheme.colours.secondaryOnBackground,
        )
    }
}
