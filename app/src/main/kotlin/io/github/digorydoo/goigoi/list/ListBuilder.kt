package io.github.digorydoo.goigoi.list

import android.content.Context
import ch.digorydoo.kutils.cjk.JLPTLevel
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.utils.UserPrefs.WordListItemMode

/**
 * Note: The methods of this class may be called from a thread!
 */
@Deprecated("get rid of this")
object ListBuilder {
    fun buildWordsList(unyt: Unyt, mode: WordListItemMode, topMargin: Int, ctx: Context): Array<AbstrListItem> {
        val items = mutableListOf<AbstrListItem>(HeaderItem())
        var isFirst = true

        unyt.forEachWord { word ->
            val hasBadge = unyt.levels.size > 1 && word.level != null && word.level != JLPTLevel.Nx

            WordListItem(word, mode, hasBadge, ctx).let {
                items.add(it)

                if (isFirst) {
                    it.topMargin = topMargin
                }
            }

            isFirst = false
        }

        return items.toTypedArray()
    }
}
