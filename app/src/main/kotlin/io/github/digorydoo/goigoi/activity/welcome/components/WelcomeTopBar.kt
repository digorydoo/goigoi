package io.github.digorydoo.goigoi.activity.welcome.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.dateToIntlStringLong
import ch.digorydoo.kutils.cjk.japaneseDayOfWeek
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.components.HintBalloon
import io.github.digorydoo.goigoi.furigana.FuriganaBuilder

@Composable
fun WelcomeTopBar(paddingLR: Dp, onPrefsBtnClicked: () -> Unit) {
    // The WelcomeScreen's top bar is part of the content, so we don't call TopAppBar here.

    val showBalloon = remember { mutableStateOf(false) }
    val now = Moment.now()
    val intlDate = now.dateToIntlStringLong(true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(start = paddingLR),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // We wrap the text in another row, because we want the ripple effect caused by clickable() to extend only
        // across the text, not across the entire area.
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f), // fill the remaining width
            verticalAlignment = Alignment.CenterVertically
        ) {
            HintBalloon(
                wrappedContent = {
                    Text(
                        modifier = Modifier.clickable(onClick = { showBalloon.value = true }),
                        text = FuriganaString(now.japaneseDayOfWeek).kanji,
                    )
                },
                lines = arrayOf(
                    FuriganaBuilder.buildSpan(intlDate.ja),
                    intlDate.en,
                ),
                open = showBalloon.value,
                onDismiss = { showBalloon.value = false },
            )
        }
        IconButton(
            modifier = Modifier.padding(end = paddingLR - 16.dp),
            onClick = onPrefsBtnClicked,
        ) {
            Icon(
                imageVector = Outlined.Settings,
                contentDescription = null
            )
        }
    }
}
