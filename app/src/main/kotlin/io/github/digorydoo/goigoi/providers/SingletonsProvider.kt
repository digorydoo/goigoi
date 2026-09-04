package io.github.digorydoo.goigoi.providers

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import io.github.digorydoo.goigoi.core.db.KanjiIndex
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.stats.Stats
import io.github.digorydoo.goigoi.utils.SingletonHolder
import io.github.digorydoo.goigoi.utils.UserPrefs

@Immutable
private data class SingletonsData(
    val kanjiIndex: KanjiIndex? = null,
    val prefs: UserPrefs? = null,
    val stats: Stats? = null,
    val vocab: Vocabulary? = null,
)

private val LocalSingletonsData = staticCompositionLocalOf { SingletonsData() }

object Singletons {
    val kanjiIndex
        @Composable
        @ReadOnlyComposable
        get() = LocalSingletonsData.current.kanjiIndex!!

    val prefs
        @Composable
        @ReadOnlyComposable
        get() = LocalSingletonsData.current.prefs!!

    val stats
        @Composable
        @ReadOnlyComposable
        get() = LocalSingletonsData.current.stats!!

    val vocab
        @Composable
        @ReadOnlyComposable
        get() = LocalSingletonsData.current.vocab!!
}

@Composable
fun SingletonsProvider(activity: Activity, content: @Composable () -> Unit) {
    if (!SingletonHolder.singletonsExist) {
        SingletonHolder.createSingletons(activity.applicationContext)
    }

    val data = SingletonsData(
        SingletonHolder.kanjiIndex,
        SingletonHolder.prefs,
        SingletonHolder.stats,
        SingletonHolder.vocab,
    )

    CompositionLocalProvider(LocalSingletonsData provides data) {
        content()
    }
}
