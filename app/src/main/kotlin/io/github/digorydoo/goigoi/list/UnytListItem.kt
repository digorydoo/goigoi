package io.github.digorydoo.goigoi.list

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.drawable.IconBuilder
import io.github.digorydoo.goigoi.furigana.FuriganaBuilder
import io.github.digorydoo.goigoi.helper.formatRelativeTime
import io.github.digorydoo.goigoi.stats.Stats
import io.github.digorydoo.goigoi.utils.ResUtils
import ch.digorydoo.kutils.cjk.Unicode
import kotlin.math.max

class UnytListItem(
    val unyt: Unyt,
    override val badge: String, // "" = none
    private val iconResId: Int, // -1 = use dynamic icon
    private val ctx: Context,
): AbstrListItem(
    if (badge.isNotEmpty()) {
        ItemViewType.DOUBLE_WITH_DRAWABLE_AND_BADGE
    } else {
        ItemViewType.DOUBLE_WITH_DRAWABLE
    }
) {
    constructor(unyt: Unyt, badge: String, ctx: Context): this(unyt, badge, -1, ctx)
    constructor(unyt: Unyt, iconResId: Int, ctx: Context): this(unyt, "", iconResId, ctx)

    override val primaryText = FuriganaBuilder.buildSpan(unyt.name.withSystemLang, false)

    override val secondaryText = run {
        val stats = Stats.getSingleton(ctx)
        val studyDate = stats.getUnytStudyMoment(unyt)

        val numWordsText = "${max(unyt.numWordsLoaded, unyt.numWordsAvailable)}"
        var text = ctx.getString(R.string.n_words).replace("\${N}", numWordsText)

        if (studyDate != null) {
            if (text.isNotEmpty()) {
                text += "${Unicode.EN_SPACE}${Unicode.TRIANG_RIGHT}${Unicode.EN_SPACE}"
            }

            text += studyDate.formatRelativeTime(ctx)
        }

        text
    }

    private var _drawable: Drawable? = null

    // We use a getter to avoid creating all the drawables when the list is created.
    override val drawable: Drawable
        get() = _drawable
            ?: when (iconResId) {
                -1 -> IconBuilder.makeUnytIcon(ctx, unyt)
                else -> ContextCompat.getDrawable(ctx, iconResId)
                    ?.apply { setTint(ResUtils.getARGBFromAttr(R.attr.decorativeIconTintColour, ctx)) }
                    ?: throw Exception("Failed to load drawable with resId=$iconResId")
            }.also { _drawable = it }
}
