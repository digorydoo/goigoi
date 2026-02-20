package io.github.digorydoo.goigoi.activity.unyt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.drawable.IconBuilder
import io.github.digorydoo.goigoi.drawable.SheetHead
import io.github.digorydoo.goigoi.list.AbstrListItem
import io.github.digorydoo.goigoi.listviewholder.AbstrViewHolder

class HeaderViewHolder private constructor(
    private val rootView: View,
    private val unyt: Unyt,
    private val sheetHead: SheetHead,
    private val delegate: Delegate,
): AbstrViewHolder(rootView) {
    interface Delegate {
        fun overflowBtnClicked(anchor: View)
    }

    private val sheetHeadView: ImageView = rootView.findViewById(R.id.sheet_head)
    private val unytChartView = rootView.findViewById<ImageView>(R.id.unyt_chart)!!
    private val numWordsView = rootView.findViewById<TextView>(R.id.num_words)!!
    private val overflowBtn = rootView.findViewById<View>(R.id.overflow_btn)!!
    private val overflowBtnAnchor = rootView.findViewById<View>(R.id.overflow_btn_anchor)!!

    override fun bind(item: AbstrListItem) {
        // NOTE: Even though there is only one item with a HeaderViewHolder, we can't bind stuff
        // from the init function, because the drawable needs to be recreated when coming back
        // from one of the study activities!

        val ctx = rootView.context
        val vocab = Vocabulary.getSingleton(ctx)

        sheetHeadView.setImageDrawable(sheetHead)
        sheetHeadView.updateLayoutParams { height = sheetHead.intrinsicHeight }

        if (unyt == vocab.myWordsUnyt) {
            // Showing the progress doesn't make sense for My Words Unyt
            unytChartView.visibility = View.GONE
        } else {
            unytChartView.visibility = View.VISIBLE
            unytChartView.setImageDrawable(IconBuilder.makeUnytIcon(ctx, unyt))
        }

        numWordsView.text = ctx.getString(R.string.n_words).replace("\${N}", "" + unyt.numWordsLoaded)

        val canSeeOverflowBtn = unyt.hasRomaji || unyt.hasFurigana

        if (canSeeOverflowBtn) {
            overflowBtn.setOnClickListener { delegate.overflowBtnClicked(overflowBtnAnchor) }
        } else {
            overflowBtn.visibility = View.GONE
        }
    }

    companion object {
        fun create(parent: ViewGroup, inflater: LayoutInflater, unyt: Unyt, sheetHead: SheetHead, delegate: Delegate) =
            HeaderViewHolder(
                inflater.inflate(R.layout.words_list_header, parent, false),
                unyt,
                sheetHead,
                delegate
            )
    }
}
