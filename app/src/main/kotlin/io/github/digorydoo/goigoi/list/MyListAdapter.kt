package io.github.digorydoo.goigoi.list

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.RecyclerView
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.listviewholder.AbstrViewHolder
import io.github.digorydoo.goigoi.listviewholder.ClickableItemDelegate
import io.github.digorydoo.goigoi.listviewholder.ListItemViewHolder
import io.github.digorydoo.goigoi.listviewholder.SubheaderViewHolder

class MyListAdapter(
    private val itemDelegate: ClickableItemDelegate,
    private val inflater: LayoutInflater,
    private val createViewHolder: ((parent: ViewGroup, type: ItemViewType) -> AbstrViewHolder?)? = null,
): RecyclerView.Adapter<AbstrViewHolder>() {
    private var theItems: Array<AbstrListItem> = emptyArray()
    private var theRecyclerView: RecyclerView? = null

    var items: Array<AbstrListItem>
        get() = theItems
        @SuppressLint("NotifyDataSetChanged")
        set(newItems) {
            theItems = newItems
            notifyDataSetChanged()
        }

    fun itemAtPos(pos: Int): AbstrListItem? {
        return if (pos in theItems.indices) {
            theItems[pos]
        } else {
            null
        }
    }

    override fun getItemCount() = theItems.size

    private fun getMyItemViewType(pos: Int) = itemAtPos(pos)?.viewType ?: ItemViewType.NONE

    override fun getItemViewType(pos: Int) = getMyItemViewType(pos).ordinal

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstrViewHolder {
        val itemType = ItemViewType.entries[viewType]
        val holder = createViewHolder?.let { it(parent, itemType) }

        return when {
            holder != null -> holder
            itemType == ItemViewType.SUBHEADER -> SubheaderViewHolder.create(parent, inflater)
            else -> ListItemViewHolder.create(parent, inflater, itemType, itemDelegate)
        }
    }

    override fun onBindViewHolder(holder: AbstrViewHolder, pos: Int) {
        val item = itemAtPos(pos) ?: throw RuntimeException("There is no item at $pos")
        holder.bind(item)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        theRecyclerView = recyclerView
    }

    /**
     * This method will return the pixel-accurate value near the top of the list as long as the first
     * item's still visible.
     * @param farValue: value to be returned when the first item is no longer in view; null=still try to
     *    find the offset, although it won't be guaranteed to be pixel-accurate
     * @return 0 when the list is at the top; positive values further down
     */
    fun computeScrollOffsetWithHeuristic(farValue: Int? = null): Int {
        val recyclerView = theRecyclerView ?: return 0

        if (itemCount == 0) {
            return 0
        }

        val vh = recyclerView.findViewHolderForAdapterPosition(0)

        return when {
            vh != null -> recyclerView.paddingTop - vh.itemView.top
            farValue != null -> farValue
            else -> recyclerView.computeVerticalScrollOffset()
        }
    }

    fun hiliteItemAfterUpdates(pos: Int) {
        doAfterUpdates {
            hiliteItem(pos)
        }
    }

    private fun hiliteItem(pos: Int) {
        val recyclerView = theRecyclerView ?: return

        recyclerView.findViewHolderForAdapterPosition(pos)?.itemView?.let { view ->
            // Most my_list_item_*.xml apply the ripple to their root view.
            // When they don't, they should assign R.id.ripple to some View instead.

            (view.background ?: view.findViewById<View>(R.id.ripple)?.background)?.let { drw ->
                val arr = ArrayList(drw.state.asList())
                arr.add(android.R.attr.state_pressed)
                drw.state = arr.toIntArray()
                view.invalidate()

                Handler(Looper.getMainLooper()).postDelayed({
                    arr.remove(android.R.attr.state_pressed)
                    drw.state = arr.toIntArray()
                    view.invalidate()
                }, 200)
            }
        }
    }

    private fun doAfterUpdates(lambda: () -> Unit) {
        val recyclerView = theRecyclerView ?: return

        if (recyclerView.hasPendingAdapterUpdates()) {
            // Add a temporary global layout listener.
            recyclerView.viewTreeObserver.addOnGlobalLayoutListener(
                object: ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        try {
                            lambda()
                        } finally {
                            recyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        }
                    }
                }
            )
        } else {
            // Layout has already finished.
            lambda()
        }
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "MyListAdapter"
    }
}
