package io.github.digorydoo.goigoi.composables.bottom_sheet.word_info

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.string.initCap
import ch.digorydoo.kutils.string.toPercent
import io.github.digorydoo.goigoi.composables.list.ListSubheader
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.composables.providers.Singletons
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.stats.StatsKey

@Composable
fun SheetDebugInfo(word: Word, unyt: Unyt, sheetHorizPadding: Dp) {
    ListSubheader("DEBUG", sheetHorizPadding)
    val stats = Singletons.stats

    // Word

    Text(
        modifier = Modifier
            .padding(horizontal = sheetHorizPadding)
            .padding(top = 8.dp),
        text = "Word",
        style = GoigoiTheme.typography.listItemPrimaryText,
        color = GoigoiTheme.colours.onBottomSheetContainer,
    )

    val wordInfo = arrayOf(
        "Progress: ${stats.getWordStudyProgress(word).toPercent()} (based on total correct)",
        "Rating: ${stats.getWordTotalRating(word).toPercent()}, " +
            "seen: ${stats.getWordTotalSeenCount(word)}, " +
            "correct: ${stats.getWordTotalCorrectCount(word)}, " +
            "wrong: ${stats.getWordTotalWrongCount(word)}",
        "Study moment: ${stats.getWordStudyMoment(word)?.formatAsZoneAgnosticDateTime()}",
    ).joinToString("\n")

    Text(
        modifier = Modifier
            .padding(horizontal = sheetHorizPadding)
            .padding(top = 4.dp),
        text = wordInfo,
        style = GoigoiTheme.typography.listItemSecondaryText,
        color = GoigoiTheme.colours.secondaryOnBackground,
    )

    // Unyt

    Text(
        modifier = Modifier
            .padding(horizontal = sheetHorizPadding)
            .padding(top = 16.dp),
        text = "Unyt",
        style = GoigoiTheme.typography.listItemPrimaryText,
        color = GoigoiTheme.colours.onBottomSheetContainer,
    )

    val unytInfo = "${unyt.name.withSystemLang}\n" +
        "Study moment: ${stats.getUnytStudyMoment(unyt)?.formatAsZoneAgnosticDateTime()}"

    Text(
        modifier = Modifier
            .padding(horizontal = sheetHorizPadding)
            .padding(top = 4.dp),
        text = unytInfo,
        style = GoigoiTheme.typography.listItemSecondaryText,
        color = GoigoiTheme.colours.secondaryOnBackground,
    )

    // Details per StatsKey

    Text(
        modifier = Modifier
            .padding(horizontal = sheetHorizPadding)
            .padding(top = 16.dp),
        text = "Details",
        style = GoigoiTheme.typography.listItemPrimaryText,
        color = GoigoiTheme.colours.onBottomSheetContainer,
    )

    var isFirst = true

    for (statsKey in StatsKey.entries) {
        val name = statsKey.name
            .let {
                if (it.startsWith("PROGSTUDY_")) it.substring(10)
                else it
            }
            .replace("_", " ")
            .let { initCap(it) }

        val rating = stats.getWordRating(word, statsKey).toPercent()
        val seenCount = stats.getWordSeenCount(word, statsKey)
        val correctCount = stats.getWordCorrectCount(word, statsKey)
        val wrongCount = stats.getWordWrongCount(word, statsKey)
        val details = "Rating: $rating   Seen: $seenCount   Correct: $correctCount   Wrong: $wrongCount"

        Text(
            modifier = Modifier
                .padding(horizontal = sheetHorizPadding)
                .padding(top = if (isFirst) 4.dp else 12.dp),
            text = "$name\n$details",
            style = GoigoiTheme.typography.listItemSecondaryText,
            color = GoigoiTheme.colours.secondaryOnBackground,
        )

        isFirst = false
    }
}
