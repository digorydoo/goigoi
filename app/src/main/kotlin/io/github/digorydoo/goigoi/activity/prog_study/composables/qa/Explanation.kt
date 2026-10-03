package io.github.digorydoo.goigoi.activity.prog_study.composables.qa

import android.graphics.Typeface
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.activity.prog_study.composables.AnimatedElement
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.legacy.spannable.buildSpan

@Composable
fun Explanation(model: ProgStudyActivityModel) {
    val qaKind = model.qaKind.collectAsState().value
    if (qaKind.doesNotAskAnything) return

    val pmode = model.presentationMode.collectAsState().value
    val colours = GoigoiTheme.colours
    val typography = GoigoiTheme.typography
    val density = LocalDensity.current
    val textSizePx = with(density) { typography.listItemPrimaryText.fontSize.toPx() }
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
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                imageVector = ImageVector.vectorResource(R.drawable.ic_point_right_black_24dp),
                contentDescription = null,
                tint = GoigoiTheme.colours.decorativeIconTint
            )
            AndroidView(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 2.dp),
                factory = { ctx -> TextView(ctx) },
                update = { textView ->
                    textView.apply {
                        text = explanation.buildSpan()
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx)
                        setTextColor(colours.onBackground.toArgb())
                        typeface = Typeface.defaultFromStyle(Typeface.ITALIC)
                    }
                }
            )
        }
    }
}
