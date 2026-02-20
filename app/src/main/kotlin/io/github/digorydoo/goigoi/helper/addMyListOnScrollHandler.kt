package io.github.digorydoo.goigoi.helper

import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import io.github.digorydoo.goigoi.list.MyListAdapter
import io.github.digorydoo.goigoi.utils.DimUtils

fun RecyclerView.addMyListOnScrollHandler(appBar: Toolbar) {
    addOnScrollListener(object: RecyclerView.OnScrollListener() {
        override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(view, dx, dy)

            val adapter = adapter as? MyListAdapter
            require(adapter != null) { "adapter is not MyListAdapter" }

            val pos = adapter.computeScrollOffsetWithHeuristic(height)

            val elevation = when {
                pos <= 0 -> 0.0f
                else -> DimUtils.dpToPx(4.0f, context)
            }

            if (appBar.elevation != elevation) {
                appBar.elevation = elevation
                appBar.translationZ = elevation
            }
        }
    })
}
