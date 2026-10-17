package io.github.digorydoo.goigoi.composables.bottom_sheet.word_info

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.composables.list.ListSubheader
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.core.db.Word

@Composable
fun SheetAltSpellings(word: Word, horizPadding: Dp) {
    val spellings = word.alternativeSpellings
    if (spellings.isEmpty()) return

    ListSubheader(stringResource(R.string.alternative_spellings).replace("\${kana}", word.kana), horizPadding)

    for (spelling in spellings) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizPadding)
                .padding(top = 2.dp),
            text = spelling,
            style = GoigoiTheme.typography.listItemPrimaryText,
            color = GoigoiTheme.colours.onSurface,
        )
    }
}
