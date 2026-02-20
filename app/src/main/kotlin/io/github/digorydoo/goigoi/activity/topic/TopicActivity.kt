package io.github.digorydoo.goigoi.activity.topic

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.unyt.startUnytActivityAsync
import io.github.digorydoo.goigoi.db.Topic
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.dialog.UnytCtxDlgFragment
import io.github.digorydoo.goigoi.dialog.UnytCtxMenu
import io.github.digorydoo.goigoi.drawable.SheetHead
import io.github.digorydoo.goigoi.helper.addMyListOnScrollHandler
import io.github.digorydoo.goigoi.list.*
import io.github.digorydoo.goigoi.listviewholder.ClickableItemDelegate
import io.github.digorydoo.goigoi.utils.DeviceUtils
import io.github.digorydoo.goigoi.utils.ResUtils

class TopicActivity: AppCompatActivity(), UnytCtxMenu.Callback {
    private lateinit var params: TopicActivityParams
    private lateinit var bindings: Bindings
    private lateinit var values: Values
    private lateinit var vocab: Vocabulary
    private lateinit var adapter: MyListAdapter
    private lateinit var topic: Topic
    private var unytLaunched: Unyt? = null
    private var uiDisabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        setContentView(R.layout.topic_activity)

        val ctx = applicationContext
        params = TopicActivityParams.fromIntent(intent)
        bindings = Bindings(this)
        values = Values(this)
        vocab = Vocabulary.getSingleton(ctx)
        topic = vocab.findTopicById(params.topicId)!!

        val itemDelegate = object: ClickableItemDelegate {
            override fun onClick(item: AbstrListItem) {
                when (item) {
                    is UnytListItem -> unytListItemClicked(item)
                    else -> Unit
                }
            }

            override fun onLongClick(item: AbstrListItem): Boolean {
                if (uiDisabled) return false
                val uitem = item as? UnytListItem ?: return false
                val dlg = UnytCtxDlgFragment.createNewFragment(uitem.unyt)
                dlg.show(supportFragmentManager, UnytCtxDlgFragment.TAG)
                return true
            }
        }

        val screenSize = DeviceUtils.getScreenSize(this)
        val orient = DeviceUtils.getOrientation(this)

        val gapSize = values.gapSize(screenSize, orient).toInt()

        if (gapSize > 0) {
            bindings.leftGap.updateLayoutParams { width = gapSize }
            bindings.rightGap.updateLayoutParams { width = gapSize }

            bindings.unytsList.apply {
                setPadding(
                    gapSize,
                    paddingTop,
                    gapSize,
                    paddingBottom
                )
            }
        } else {
            bindings.leftGap.visibility = View.GONE
            bindings.rightGap.visibility = View.GONE
        }

        setSupportActionBar(bindings.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            title = topic.name.withSystemLang
        }

        adapter = MyListAdapter(itemDelegate, layoutInflater) { parent, type ->
            when (type) {
                ItemViewType.HEADER -> HeaderViewHolder.create(
                    parent,
                    layoutInflater,
                    topic,
                    SheetHead(ctx)
                )
                else -> null
            }
        }

        val unytsList = bindings.unytsList
        unytsList.adapter = adapter
        unytsList.setHasFixedSize(false)
        unytsList.addItemDecoration(MyItemDecoration(ctx))
        unytsList.addMyListOnScrollHandler(bindings.toolbar)
    }

    override fun onResume() {
        super.onResume()

        rebuildList()

        unytLaunched?.let { unyt ->
            hiliteItem(unyt)
            unyt.unload()
        }

        unytLaunched = null
    }

    private fun hiliteItem(unyt: Unyt) {
        adapter.items
            .indexOfFirst { item -> (item as? UnytListItem)?.unyt == unyt }
            .takeIf { it >= 0 }
            ?.let { adapter.hiliteItemAfterUpdates(it) }
    }

    private fun unytListItemClicked(item: UnytListItem) {
        if (uiDisabled) return
        uiDisabled = true // make sure the item cannot be clicked again while the activity launches
        unytLaunched = item.unyt // remember which unyt was launched when we come back

        startUnytActivityAsync(item.unyt) {
            uiDisabled = false
        }
    }

    override fun onUnytCtxMenuAction(action: UnytCtxMenu.Action) {
        when (action) {
            UnytCtxMenu.Action.FAKE_AVG_STATS -> rebuildList()
            UnytCtxMenu.Action.FAKE_GOOD_STATS -> rebuildList()
            UnytCtxMenu.Action.FAKE_POOR_STATS -> rebuildList()
            UnytCtxMenu.Action.RESET_STATS -> rebuildList()
            UnytCtxMenu.Action.SET_SUPER_PROGRESSIVE_IDX -> Unit
        }
    }

    private fun rebuildList() {
        adapter.items = ListBuilder.buildUnytsList(
            topic,
            values.listItemTopMargin,
            values.listItemTopMarginWhenSubheader,
            applicationContext
        )
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "TopicActivity"
    }
}
