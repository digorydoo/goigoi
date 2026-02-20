package io.github.digorydoo.goigoi.bottom_sheet

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import ch.digorydoo.kutils.cjk.isCJK
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.db.KanjiIndex
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.helper.MyLayoutInflater

class KanjiDetailsLinksBuilder(private val word: Word) {
    fun build(ctx: Context, inflater: MyLayoutInflater, onClick: (uri: Uri) -> Unit) {
        val kanjis = word.kanji.filter { it.isCJK() }.toSet()

        if (kanjis.isEmpty()) {
            return
        }

        inflater.insertSubheader(ctx.getString(R.string.kanji_details))

        val kanjiIndex = KanjiIndex.getSingleton(ctx)

        kanjis.forEach { kanji ->
            val readings = word.primaryForm.readings
                .filter { it.kanji == "$kanji" }
                .map { it.kana }
                .toMutableSet() // Kotlin Sets are ordered, so the reading appearing in the word will appear first
                .apply { addAll(kanjiIndex.getReadingsOfKanji(kanji).sorted()) }

            val primaryText = readings.joinToString("・")
            val secondaryText = kanjiIndex.levelOfKanji(kanji)?.toPrettyString()
                ?: ctx.getString(R.string.jlpt_level_unknown)

            inflater.insertItem(kanji, primaryText, secondaryText) {
                val uri = getJishoUri(kanji)
                onClick(uri)
            }
        }
    }

    private fun getJishoUri(kanji: Char): Uri {
        return JISHO_URL.replace("{KANJI}", "$kanji").toUri()
    }

    companion object {
        private const val JISHO_URL = "https://jisho.org/search/{KANJI}%23kanji"
    }
}
