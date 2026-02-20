package io.github.digorydoo.goigoi.list

import android.content.Context
import androidx.core.content.ContextCompat
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.db.Topic
import io.github.digorydoo.goigoi.furigana.FuriganaBuilder
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.withStudyLang

class TopicListItem(val topic: Topic, ctx: Context): AbstrListItem(ItemViewType.DOUBLE_WITH_DRAWABLE) {
    override val primaryText = FuriganaBuilder.buildSpan(topic.name.withStudyLang, false)
    override val secondaryText = topic.name.withSystemLangExcept("ja") // use en if systemLang is ja
    override val drwPadding = DimUtils.dpToPx(8, ctx)

    override val drawable = ContextCompat.getDrawable(ctx, R.drawable.ic_local_library_black_24dp)?.apply {
        setTint(ResUtils.getARGBFromAttr(R.attr.decorativeIconTintColour, ctx))
    }

    // Hidden topics will be visible in BuildConfig.DEBUG, but dimmed.
    override val dimmed = topic.hidden
}
