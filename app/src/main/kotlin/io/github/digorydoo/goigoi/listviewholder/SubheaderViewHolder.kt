package io.github.digorydoo.goigoi.listviewholder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.list.AbstrListItem

class SubheaderViewHolder private constructor(rootView: View): AbstrViewHolder(rootView) {
    private val secondaryText = rootView.findViewById<TextView>(R.id.secondary_text)!!

    override fun bind(item: AbstrListItem) {
        secondaryText.visibility = if (item.secondaryText.isEmpty()) View.GONE else View.VISIBLE
        secondaryText.text = item.secondaryText
    }

    companion object {
        fun create(parent: ViewGroup, inflater: LayoutInflater) =
            SubheaderViewHolder(inflater.inflate(R.layout.my_list_subheader, parent, false))
    }
}
