package io.github.digorydoo.goigoi.bottom_sheet

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.helper.MyLayoutInflater
import ch.digorydoo.kutils.cjk.hasBracket
import ch.digorydoo.kutils.cjk.hasPunctuation

class DictionaryLinksBuilder(private val word: Word, private val studyLang: String) {
    private val dictionaries = arrayOf(
        Pair("jisho.org", ::getJishoUri),
        Pair("wiktionary.org", ::getWiktionaryUri),
        Pair("dict.leo.org", ::getLeoUri)
    )

    fun build(ctx: Context, inflater: MyLayoutInflater, onClick: (uri: Uri) -> Unit) {
        val dw = getDictionaryWord() ?: return

        val links = dictionaries
            .map { pair ->
                val (label, getUri) = pair
                Pair(label, getUri(dw))
            }
            .filter { pair -> pair.second != null }

        if (links.isEmpty()) {
            return
        }

        ctx.getString(R.string.look_it_up)
            .replace("\${word}", dw)
            .let { inflater.insertSubheader(it) }

        links.forEach { (label, uri) ->
            inflater.insertItem(R.drawable.ic_local_library_black_24dp, label) {
                uri?.let { onClick(it) }
            }
        }
    }

    private fun getDictionaryWord(): String? {
        var dw = word.dictionaryWord

        if (dw.isNotEmpty()) {
            return if (dw == "-") null else dw
        }

        dw = word.kanji

        if (dw.hasPunctuation() || dw.hasBracket()) {
            return null
        }

        if (studyLang == "ja") {
            // Remove 〜 prefix.

            if (dw.startsWith('〜')) {
                dw = dw.slice(1 ..< dw.length)
            }

            // Remove 〜 suffix.

            if (dw.endsWith('〜')) {
                dw = dw.slice(0 ..< dw.length - 1)
            }

            // Remove をする suffix.

            val osuru = "をする"

            if (dw.length > osuru.length && dw.endsWith(osuru)) {
                dw = dw.slice(0 ..< dw.length - osuru.length)
            }

            // Remove する suffix.

            val suru = "する"

            if (dw.length > suru.length && dw.endsWith(suru)) {
                dw = dw.slice(0 ..< dw.length - suru.length)
            }

            // NOTE: It is NOT generally better to remove お and ご from the dw!
        }

        return dw
    }

    private fun getJishoUri(word: String): Uri? {
        return if (studyLang == "ja") {
            (JISHO_URL + word).toUri()
        } else {
            null
        }
    }

    private fun getWiktionaryUri(word: String): Uri {
        return (WIKTIONARY_URL + word).toUri()
    }

    private fun getLeoUri(word: String): Uri? {
        return when (studyLang) {
            "en", "de" -> (LEO_EN_DE_URL + word).toUri()
            "fr" -> (LEO_FR_DE_URL + word).toUri()
            "es" -> (LEO_ES_DE_URL + word).toUri()
            "it" -> (LEO_IT_DE_URL + word).toUri()
            "zh" -> (LEO_ZH_DE_URL + word).toUri()
            "ru" -> (LEO_RU_DE_URL + word).toUri()
            "po" -> (LEO_PO_DE_URL + word).toUri()
            "pl" -> (LEO_PL_DE_URL + word).toUri()
            else -> null
        }
    }

    companion object {
        private const val JISHO_URL = "http://www.jisho.org/search/"
        private const val WIKTIONARY_URL = "https://en.wiktionary.org/wiki/"
        private const val LEO_EN_DE_URL = "http://dict.leo.org/englisch-deutsch/"
        private const val LEO_FR_DE_URL = "http://dict.leo.org/französisch-deutsch/"
        private const val LEO_ES_DE_URL = "http://dict.leo.org/spanisch-deutsch/"
        private const val LEO_IT_DE_URL = "http://dict.leo.org/italienisch-deutsch/"
        private const val LEO_ZH_DE_URL = "http://dict.leo.org/chinesisch-deutsch/"
        private const val LEO_RU_DE_URL = "http://dict.leo.org/russisch-deutsch/"
        private const val LEO_PO_DE_URL = "http://dict.leo.org/portugiesisch-deutsch/"
        private const val LEO_PL_DE_URL = "http://dict.leo.org/polnisch-deutsch/"
    }
}
