package io.github.digorydoo.goigoi.activity.topic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.db.Topic
import io.github.digorydoo.goigoi.drawable.SheetHead
import io.github.digorydoo.goigoi.furigana.FuriganaBuilder
import io.github.digorydoo.goigoi.list.AbstrListItem
import io.github.digorydoo.goigoi.listviewholder.AbstrViewHolder

class HeaderViewHolder private constructor(
    rootView: View,
    private val topic: Topic,
    private val sheetHead: SheetHead,
): AbstrViewHolder(rootView) {
    private val sheetHeadView: ImageView = rootView.findViewById(R.id.sheet_head)
    private val heading: TextView = rootView.findViewById(R.id.heading)

    override fun bind(item: AbstrListItem) {
        sheetHeadView.setImageDrawable(sheetHead)
        sheetHeadView.updateLayoutParams { height = sheetHead.intrinsicHeight }
        heading.text = FuriganaBuilder.buildSpan(topic.name.ja)
    }

    companion object {
        fun create(parent: ViewGroup, inflater: LayoutInflater, topic: Topic, sheetHead: SheetHead) =
            HeaderViewHolder(
                inflater.inflate(R.layout.topic_activity_header, parent, false),
                topic,
                sheetHead
            )
    }
}
