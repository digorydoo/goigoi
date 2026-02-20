package io.github.digorydoo.goigoi.listviewholder

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.setPadding
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.list.AbstrListItem
import io.github.digorydoo.goigoi.list.ItemViewType
import io.github.digorydoo.goigoi.utils.DimUtils

class ListItemViewHolder private constructor(
    private val rootView: View,
    private val delegate: ClickableItemDelegate,
): AbstrViewHolder(rootView) {
    private val primaryText: TextView? = rootView.findViewById(R.id.primary_text)
    private val secondaryText: TextView? = rootView.findViewById(R.id.secondary_text)
    private val badge: TextView? = rootView.findViewById(R.id.badge)
    private val imageView: ImageView? = rootView.findViewById(R.id.image_view)
    private val compoundDrwSize = DimUtils.dpToPx(48, rootView.context)
    private val compoundDrwPadding = DimUtils.dpToPx(8, rootView.context)

    /**
     * IMPORTANT: This method should be as performant as possible. Avoid creating unnecessary
     * objects, especially FuriganaSpan.
     */
    override fun bind(item: AbstrListItem) {
        rootView.alpha = if (item.dimmed) 0.3f else 1.0f
        rootView.contentDescription = item.primaryText.ifEmpty { item.secondaryText }

        primaryText?.text = item.primaryText
        secondaryText?.text = item.secondaryText
        badge?.text = item.badge

        if (imageView != null) {
            // We have a separate imageView for the drawable. Items with both primary and secondary
            // texts use this, because the compound drawable is always vertically centred to a
            // single TextView.
            imageView.apply {
                setImageDrawable(item.drawable)
                setPadding(item.drwPadding)
            }
        } else {
            // When we don't have an imageView, we use a compound drawable.
            if (item.drawable != null) {
                val size = compoundDrwSize - 2 * item.drwPadding
                item.drawable?.bounds = Rect(0, 0, size, size)

                primaryText?.setCompoundDrawables(item.drawable, null, null, null)
                primaryText?.compoundDrawablePadding = compoundDrwPadding
            } else {
                primaryText?.setCompoundDrawables(null, null, null, null)
            }
        }

        rootView.setOnClickListener { delegate.onClick(item) }
        rootView.setOnLongClickListener { delegate.onLongClick(item) }
    }

    companion object {
        fun create(
            parent: ViewGroup,
            inflater: LayoutInflater,
            itemType: ItemViewType,
            delegate: ClickableItemDelegate,
        ): ListItemViewHolder {
            val resId = when (itemType) {
                ItemViewType.SINGLE -> R.layout.my_list_item_1
                ItemViewType.SINGLE_WITH_DRAWABLE -> R.layout.my_list_item_1
                ItemViewType.DOUBLE -> R.layout.my_list_item_2
                ItemViewType.DOUBLE_WITH_DRAWABLE -> R.layout.my_list_item_2_drw
                ItemViewType.DOUBLE_WITH_DRAWABLE_AND_BADGE -> R.layout.my_list_item_2_drw_badge
                ItemViewType.LARGE -> R.layout.my_list_item_large
                else -> throw RuntimeException("Item type not supported: $itemType")
            }
            val rootView = inflater.inflate(resId, parent, false)
            return ListItemViewHolder(rootView, delegate)
        }
    }
}
