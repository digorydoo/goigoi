package io.github.digorydoo.goigoi.list

import android.content.Context
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.drawable.AnimatedDrawable
import io.github.digorydoo.goigoi.drawable.IconBuilder
import io.github.digorydoo.goigoi.helper.UserPrefs.WordListItemMode

class WordListItem(
    val word: Word,
    mode: WordListItemMode,
    hasBadge: Boolean,
    private val ctx: Context,
): AbstrListItem(
    if (hasBadge) {
        ItemViewType.DOUBLE_WITH_DRAWABLE_AND_BADGE
    } else {
        ItemViewType.DOUBLE_WITH_DRAWABLE
    }
) {
    override val primaryText = when {
        word.usuallyInKana -> word.kana
        else -> word.kanji
    }

    override val secondaryText = when (mode) {
        WordListItemMode.SHOW_KANA -> if (word.usuallyInKana) word.kanji else word.kana
        WordListItemMode.SHOW_ROMAJI -> word.romaji
        WordListItemMode.SHOW_TRANSLATION -> word.translation.withSystemLang
    }

    override val badge =
        if (hasBadge) word.level?.toPrettyString() ?: "" else ""

    private var cachedDrawable: AnimatedDrawable? = null

    override val drawable: AnimatedDrawable
        get() {
            // We use a getter to avoid creating all the drawables when the list is created.
            cachedDrawable?.let { return it }
            return IconBuilder.makeWordIcon(ctx, word).also {
                cachedDrawable = it
            }
        }
}
