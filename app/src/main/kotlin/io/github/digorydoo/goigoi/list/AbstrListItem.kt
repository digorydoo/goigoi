package io.github.digorydoo.goigoi.list

import android.graphics.drawable.Drawable

enum class ItemViewType {
    NONE,
    HEADER,
    SUBHEADER,
    SINGLE,
    SINGLE_WITH_DRAWABLE,
    DOUBLE,
    DOUBLE_WITH_DRAWABLE,
    DOUBLE_WITH_DRAWABLE_AND_BADGE,
    LARGE,
}

abstract class AbstrListItem(val viewType: ItemViewType) {
    open val primaryText: CharSequence = "" // may contain FuriganaSpans
    open val secondaryText = ""
    open val badge = ""
    open var topMargin = 0
    open val lrMargin = 0
    open val drwPadding = 0
    open val drawable: Drawable? = null
    open val dimmed = false
    open var hasTopDivider = false
}

class HeaderItem: AbstrListItem(ItemViewType.HEADER)

class SubheaderItem(override val secondaryText: String): AbstrListItem(ItemViewType.SUBHEADER) {
    override var hasTopDivider = true
}

class ActionItem(
    override val primaryText: CharSequence,
    override val secondaryText: String,
    override val drawable: Drawable?,
    override val lrMargin: Int,
): AbstrListItem(ItemViewType.LARGE)
