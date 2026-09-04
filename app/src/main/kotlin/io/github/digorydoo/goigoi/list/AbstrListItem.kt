package io.github.digorydoo.goigoi.list

import android.graphics.drawable.Drawable

enum class ItemViewType {
    NONE,
    HEADER,
    SUBHEADER,
    SINGLE_WITH_DRAWABLE,
    DOUBLE,
    DOUBLE_WITH_DRAWABLE,
    DOUBLE_WITH_DRAWABLE_AND_BADGE,
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
