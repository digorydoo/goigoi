package io.github.digorydoo.goigoi.db

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.IntlString
import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.cjk.isHiragana
import io.github.digorydoo.goigoi.core.StudyInContextKind
import io.github.digorydoo.goigoi.core.WordCategory
import io.github.digorydoo.goigoi.core.WordHint
import kotlin.math.min

class Word {
    var id = ""
    var primaryForm = FuriganaString()
    val kanji get() = primaryForm.kanji
    val kana get() = primaryForm.kana
    var romaji = ""
    val translation = IntlString()
    val hint = IntlString()
    var hint2: WordHint? = null
    var dictionaryWord = ""
    var level: JLPTLevel? = null
    var usuallyInKana = false
    var studyInContext = StudyInContextKind.NOT_REQUIRED
    val synonyms = mutableListOf<FuriganaString>()
    val phrases = mutableListOf<Phrase>()
    val sentences = mutableListOf<Phrase>()
    val links = mutableListOf<WordLink>()
    val cats = mutableListOf<WordCategory>()
    var filename = ""

    val hintsWithSystemLang: String
        get() = arrayOf(
            hint.withSystemLang,
            hint2?.let { IntlString().apply { en = it.en; de = it.de }.withSystemLang },
        )
            .filter { it != "" }
            .filterNotNull()
            .joinToString("; ")

    fun hintsWithLanguage(lang: String): String =
        arrayOf(
            hint.withLanguage(lang),
            hint2?.let { IntlString().apply { en = it.en; de = it.de }.withLanguage(lang) },
        )
            .filter { it != "" }
            .filterNotNull()
            .joinToString("; ")

    val honorificPrefix: String
        get() {
            val kanji = kanji
            val kana = kana
            if (kanji == kana) return "" // we can't tell if pure kana has an honorific prefix

            var prefix = ""

            for (i in 0 ..< min(kanji.length, kana.length)) {
                val c = kanji[i]
                if (c != kana[i] || !c.isHiragana()) break
                prefix += c
            }

            return prefix
        }

    override fun toString() =
        "Word($id, $kanji, $kana)"
}
