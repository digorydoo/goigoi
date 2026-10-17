package io.github.digorydoo.goigoi.utils

import android.app.Activity
import android.util.Log
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.core.db.KanjiIndex
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.list.ListItemDataFactory
import io.github.digorydoo.goigoi.core.stats.IStats
import io.github.digorydoo.goigoi.core.stats.Stats
import io.github.digorydoo.goigoi.core.utils.Flavour

private const val TAG = "SingletonsHolder"

class SingletonsHolder(
    val kanjiIndex: KanjiIndex,
    val prefs: UserPrefs,
    val stats: IStats,
    val vocab: Vocabulary,
    val listItemDataFactory: ListItemDataFactory,
)

private var singletons: SingletonsHolder? = null
val singletonsExist get() = singletons != null

/**
 * Only Activity is allowed to get or create the singletons. Everything else should use dependency injection!
 */
fun Activity.getOrCreateSingletons(): SingletonsHolder {
    var s = singletons
    if (s != null) return s

    Log.d(TAG, "Creating singletons")

    val flavour = when (BuildConfig.FLAVOR) {
        "japanese_free" -> Flavour.JAPANESE
        else -> Flavour.FRENCH
    }

    val ctx = applicationContext
    val assets = AndroidAssetsAccessor(getContext = { ctx }) // FIXME replace getContext arg with a delegate
    val stats = Stats(assets)
    val kanjiIndex = KanjiIndex(flavour, assets).apply { loadFiles() }
    val prefs = UserPrefs(ctx) // FIXME replace ctx arg with a delegate
    val vocab = Vocabulary(flavour, assets, stats).apply { loadVocab() }

    val listItemDataFactory = ListItemDataFactory(
        object: ListItemDataFactory.Delegate {
            override fun formatRelativeTime(m: Moment) =
                m.formatRelativeTime(ctx) // FIXME replace ctx arg with a delegate
        }
    )

    s = SingletonsHolder(
        kanjiIndex = kanjiIndex,
        prefs = prefs,
        stats = stats,
        vocab = vocab,
        listItemDataFactory = listItemDataFactory,
    )
    singletons = s
    return s
}
