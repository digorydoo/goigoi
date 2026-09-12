package io.github.digorydoo.goigoi.components.bottom_sheet.word_info

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.components.list.ListSubheader
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.stats.StatsKey
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.providers.Singletons

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
        "Progress: ${stats.getWordStudyProgress(word)} (based on total correct)",
        "Rating: ${stats.getWordTotalRating(word)}, " +
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
        color = GoigoiTheme.colours.onBackgroundSecondary,
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

    val unytInfo = "Study moment: ${stats.getUnytStudyMoment(unyt)?.formatAsZoneAgnosticDateTime()}"

    Text(
        modifier = Modifier
            .padding(horizontal = sheetHorizPadding)
            .padding(top = 4.dp),
        text = unytInfo,
        style = GoigoiTheme.typography.listItemSecondaryText,
        color = GoigoiTheme.colours.onBackgroundSecondary,
    )

    // Details

    Text(
        modifier = Modifier
            .padding(horizontal = sheetHorizPadding)
            .padding(top = 16.dp),
        text = "Details",
        style = GoigoiTheme.typography.listItemPrimaryText,
        color = GoigoiTheme.colours.onBottomSheetContainer,
    )

    val details = StatsKey.entries.joinToString("\n\n") { statsKey ->
        val rating = stats.getWordRating(word, statsKey)
        val seenCount = stats.getWordSeenCount(word, statsKey)
        val correctCount = stats.getWordCorrectCount(word, statsKey)
        val wrongCount = stats.getWordWrongCount(word, statsKey)
        val name = if (statsKey.name.startsWith("PROGSTUDY_")) statsKey.name.substring(10) else statsKey.name
        return@joinToString "$name\n    Rating: $rating, seen: $seenCount, correct: $correctCount, wrong: $wrongCount"
    }

    Text(
        modifier = Modifier
            .padding(horizontal = sheetHorizPadding)
            .padding(top = 4.dp),
        text = details,
        style = GoigoiTheme.typography.listItemSecondaryText,
        color = GoigoiTheme.colours.onBackgroundSecondary,
    )
}
