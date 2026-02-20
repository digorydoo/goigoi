package io.github.digorydoo.goigoi.activity.unyt

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.NavUtils
import androidx.core.view.updateLayoutParams
import ch.digorydoo.kutils.cjk.Unicode
import com.google.android.material.snackbar.Snackbar
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.flipthru.FlipThruActivityParams
import io.github.digorydoo.goigoi.activity.flipthru.startFlipThruActivity
import io.github.digorydoo.goigoi.bottom_sheet.WordInfoBottomSheet
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.dialog.WordCtxDlgFragment
import io.github.digorydoo.goigoi.dialog.WordCtxMenu
import io.github.digorydoo.goigoi.drawable.SheetHead
import io.github.digorydoo.goigoi.helper.UserPrefs
import io.github.digorydoo.goigoi.helper.UserPrefs.WordListItemMode
import io.github.digorydoo.goigoi.helper.addMyListOnScrollHandler
import io.github.digorydoo.goigoi.list.*
import io.github.digorydoo.goigoi.listviewholder.ClickableItemDelegate
import io.github.digorydoo.goigoi.stats.Stats
import io.github.digorydoo.goigoi.stats.StatsKey
import io.github.digorydoo.goigoi.utils.DeviceUtils
import io.github.digorydoo.goigoi.utils.ResUtils

class UnytActivity: AppCompatActivity(), WordCtxMenu.Callback {
    private lateinit var params: UnytActivityParams
    private lateinit var bindings: Bindings
    private lateinit var values: Values
    private lateinit var vocab: Vocabulary
    private lateinit var unyt: Unyt
    private lateinit var prefs: UserPrefs
    private lateinit var adapter: MyListAdapter

    private var listItemMode = WordListItemMode.SHOW_ROMAJI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        setContentView(R.layout.unyt_activity)

        val ctx = applicationContext
        params = UnytActivityParams.fromIntent(intent)
        bindings = Bindings(this)
        values = Values(this)
        vocab = Vocabulary.getSingleton(ctx)
        unyt = vocab.findUnytById(params.unytId)!!
        prefs = UserPrefs.getSingleton(ctx)

        val stats = Stats.getSingleton(ctx)
        stats.notifyUnytActivityLaunched(unyt)

        setSupportActionBar(bindings.toolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            title = unyt.name.withSystemLang
        }

        setupStartBtn()
        setupRecyclerView(ctx)
    }

    private fun setupStartBtn() {
        bindings.startBtn.setOnClickListener {
            val ctx = applicationContext

            if (!unyt.hasEnoughWordsForStudy()) {
                val msg = ctx.getString(R.string.too_few_words)
                Snackbar.make(bindings.startBtn, msg, Snackbar.LENGTH_SHORT).show()
            } else {
                startFlipThruActivity(FlipThruActivityParams(unyt.id))
            }
        }

        val screenSize = DeviceUtils.getScreenSize(this)
        val orient = DeviceUtils.getOrientation(this)
        val rm = values.startBtnRightMargin(screenSize, orient)
        val bm = values.startBtnBottomMargin(screenSize, orient)

        bindings.startBtn.updateLayoutParams {
            (this as? RelativeLayout.LayoutParams)?.apply {
                bottomMargin = bm
                rightMargin = rm
                removeRule(RelativeLayout.CENTER_HORIZONTAL)
                addRule(RelativeLayout.ALIGN_PARENT_END)
            }
        }

        bindings.startBtn.isEnabled = false // prevent unwanted user interaction until initPhase is READY
    }

    private fun setupRecyclerView(ctx: Context) {
        listItemMode = when {
            canSeeMode(prefs.wordListItemMode) -> prefs.wordListItemMode
            canSeeMode(WordListItemMode.SHOW_ROMAJI) -> WordListItemMode.SHOW_ROMAJI
            else -> WordListItemMode.SHOW_TRANSLATION
        }

        val itemDelegate = object: ClickableItemDelegate {
            override fun onClick(item: AbstrListItem) {
                val witem = item as? WordListItem ?: return
                showBottomSheet(witem.word)
            }

            override fun onLongClick(item: AbstrListItem): Boolean {
                val witem = item as? WordListItem ?: return false
                val dlg = WordCtxDlgFragment.createNewFragment(witem.word, unyt)
                dlg.show(supportFragmentManager, WordCtxDlgFragment.TAG)
                return true
            }
        }

        val headerDelegate = object: HeaderViewHolder.Delegate {
            override fun overflowBtnClicked(anchor: View) {
                val popup = PopupMenu(ctx, anchor, Gravity.END)
                val menu = popup.menu
                popup.setOnMenuItemClickListener {
                    overflowItemSelected(it, menu)
                    true
                }
                popup.menuInflater.inflate(R.menu.words_list_menu, menu)
                adjustMenuItems(menu)
                popup.show()
            }
        }

        adapter = MyListAdapter(itemDelegate, layoutInflater) { parent, type ->
            when (type) {
                ItemViewType.HEADER -> HeaderViewHolder.create(
                    parent,
                    layoutInflater,
                    unyt,
                    SheetHead(ctx),
                    headerDelegate
                )
                else -> null
            }
        }

        val recyclerView = bindings.recyclerView
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(false)
        recyclerView.addItemDecoration(MyItemDecoration(ctx))
        recyclerView.addMyListOnScrollHandler(bindings.toolbar)

        // Add the header to the items already to make the transition less jerky when the list is replaced after
        // onResume has rebuilt the list via loadUnytIfNecessaryAndRebuildWordsListAsync.
        adapter.items = arrayOf(HeaderItem())
    }

    override fun onResume() {
        super.onResume()

        val ctx = applicationContext
        val vocab = Vocabulary.getSingleton(ctx)
        vocab.writeMyWordsUnytIfNecessary(ctx)

        loadUnytIfNecessaryAndRebuildWordsListAsync()
    }

    override fun onPause() {
        super.onPause()

        val ctx = applicationContext
        val vocab = Vocabulary.getSingleton(ctx)
        vocab.writeMyWordsUnytIfNecessary(ctx)
    }

    private fun loadUnytIfNecessaryAndRebuildWordsListAsync(done: (() -> Unit)? = null) {
        val ctx = applicationContext

        if (ctx == null) {
            done?.invoke()
            return
        }

        val mode = listItemMode
        val firstItemTopMargin = values.firstItemTopMargin

        Thread {
            vocab.loadUnytIfNecessary(unyt, ctx)
            val newItems = ListBuilder.buildWordsList(unyt, mode, firstItemTopMargin, ctx)

            Handler(Looper.getMainLooper()).post {
                adapter.items = newItems
                bindings.startBtn.isEnabled = true
                bindings.startBtn.show()
                done?.invoke()
            }
        }.start()
    }

    private fun canSeeMode(mode: WordListItemMode): Boolean {
        return when (mode) {
            WordListItemMode.SHOW_KANA -> unyt.hasFurigana
            WordListItemMode.SHOW_ROMAJI -> unyt.hasRomaji
            WordListItemMode.SHOW_TRANSLATION -> true
        }
    }

    private fun overflowItemSelected(item: MenuItem, menu: Menu) {
        listItemMode = when (item.itemId) {
            R.id.action_show_kana -> WordListItemMode.SHOW_KANA
            R.id.action_show_romaji -> WordListItemMode.SHOW_ROMAJI
            else -> WordListItemMode.SHOW_TRANSLATION
        }
        prefs.wordListItemMode = listItemMode
        adjustMenuItems(menu)
        loadUnytIfNecessaryAndRebuildWordsListAsync()
    }

    private fun adjustMenuItems(menu: Menu) {
        adjustMenuItem(menu.findItem(R.id.action_show_kana), WordListItemMode.SHOW_KANA)
        adjustMenuItem(menu.findItem(R.id.action_show_romaji), WordListItemMode.SHOW_ROMAJI)
        adjustMenuItem(menu.findItem(R.id.action_show_translation), WordListItemMode.SHOW_TRANSLATION)
    }

    private fun adjustMenuItem(item: MenuItem, representedMode: WordListItemMode) {
        // The following does not work:
        //    item.setChecked(checked);
        //    item.setIcon(if (checked) R.drawable.ic_check_black_24dp else null);
        // As a workaround, we show the checkmark by adding a Unicode checkmark to the item text.
        // We should probably use a custom menu item instead so the above would work.

        val curMark = (item.title ?: "")[0]
        val hasMark = curMark == Unicode.EN_SPACE || curMark == Unicode.CHECK_MARK

        val newMark = if (listItemMode == representedMode) {
            Unicode.CHECK_MARK
        } else {
            Unicode.EN_SPACE
        }

        item.title = if (!hasMark) {
            "${newMark}${Unicode.EN_SPACE}${item.title}"
        } else {
            "${newMark}${item.title?.substring(1) ?: ""}"
        }

        item.isVisible = canSeeMode(representedMode)
    }

    fun hiliteItem(word: Word) {
        adapter.items
            .indexOfFirst { (it as? WordListItem)?.word == word }
            .takeIf { it >= 0 }
            ?.let { adapter.hiliteItemAfterUpdates(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onWordCtxMenuAction(action: WordCtxMenu.Action) {
        loadUnytIfNecessaryAndRebuildWordsListAsync()
    }

    // Called by WordsListFragment
    fun showBottomSheet(word: Word) {
        val ctx = applicationContext
        val stats = Stats.getSingleton(ctx)
        stats.incUserStudyCountOfToday(StatsKey.BOTTOM_SHEET)
        bindings.startBtn.hide()

        WordInfoBottomSheet.show(unyt, word, supportFragmentManager) {
            bindings.startBtn.show()
            hiliteItem(word)
        }
    }
}
