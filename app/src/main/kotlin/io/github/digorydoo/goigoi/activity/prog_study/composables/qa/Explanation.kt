package io.github.digorydoo.goigoi.activity.prog_study.composables.qa

import android.graphics.Typeface
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.activity.prog_study.composables.AnimatedElement
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.composables.text.FuriganaText

@Composable
fun Explanation(model: ProgStudyActivityModel) {
    val qaKind = model.qaKind.collectAsState().value
    if (qaKind.doesNotAskAnything) return

    val pmode = model.presentationMode.collectAsState().value
    val colours = GoigoiTheme.colours
    val typography = GoigoiTheme.typography
    val explanation = model.explanation.collectAsState().value ?: return

    AnimatedElement(
        visible = pmode == PresentationMode.EXPLANATION,
        allowCollapse = pmode != PresentationMode.NOTHING,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                modifier = Modifier.padding(
                    start = 16.dp,
                    end = 24.dp,
                    top = if (explanation.hasFurigana) 6.dp else 0.dp,
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
                textFontSize = typography.listItemPrimaryText.fontSize,
                color = colours.onBackground,
                typeface = Typeface.defaultFromStyle(Typeface.ITALIC),
            )
        }
    }
}
