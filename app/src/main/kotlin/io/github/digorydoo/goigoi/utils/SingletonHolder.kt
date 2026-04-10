package io.github.digorydoo.goigoi.utils

import android.content.Context
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.core.db.KanjiIndex
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.stats.Stats
import io.github.digorydoo.goigoi.core.utils.Flavour

object SingletonHolder {
    private var _kanjiIndex: KanjiIndex? = null
    private var _prefs: UserPrefs? = null
    private var _stats: Stats? = null
    private var _vocab: Vocabulary? = null

    val kanjiIndex: KanjiIndex get() = _kanjiIndex!!
    val prefs: UserPrefs get() = _prefs!!
    val stats: Stats get() = _stats!!
    val vocab: Vocabulary get() = _vocab!!

    var singletonsExist = false; private set

    fun createSingletons(ctx: Context) {
        // FIXME We store the Context here, which may be considered a memory leak.
        // If it's safe to keep a reference to Context, then get rid of getContext and pass ctx directly!
        val getContext = { ctx }
        val assets = AndroidAssetsAccessor(getContext)
        val stats = Stats(assets).also { _stats = it }

        val flavour = when (BuildConfig.FLAVOR) {
            "japanese_free" -> Flavour.JAPANESE
            else -> Flavour.FRENCH
        }

        _kanjiIndex = KanjiIndex(flavour, assets).apply { loadFiles() }
        _vocab = Vocabulary(flavour, assets, stats).apply { loadVocab() }
        _prefs = UserPrefs(ctx)
        singletonsExist = true
    }
}
