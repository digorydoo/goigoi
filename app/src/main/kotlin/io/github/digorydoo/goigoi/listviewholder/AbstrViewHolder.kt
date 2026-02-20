package io.github.digorydoo.goigoi.listviewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import io.github.digorydoo.goigoi.list.AbstrListItem

interface ClickableItemDelegate {
    fun onClick(item: AbstrListItem)
    fun onLongClick(item: AbstrListItem): Boolean = false
}

abstract class AbstrViewHolder(view: View): RecyclerView.ViewHolder(view) {
    abstract fun bind(item: AbstrListItem)
}
