package io.github.digorydoo.goigoi.core.db

import ch.digorydoo.kutils.cjk.*
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
    val phrases = mutableListOf<PhraseOrSentence>()
    val sentences = mutableListOf<PhraseOrSentence>()
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

    val kanaPrefix: String
        get() {
            val kanji = kanji
            val kana = kana
            var prefix = ""

            for (i in 0 ..< min(kanji.length, kana.length)) {
                val c = kanji[i]
                if (c != kana[i] || !c.isKana()) break
                prefix += c
            }

            return prefix
        }

    val kanaSuffix: String
        get() {
            val kanji = kanji
            val kana = kana
            var suffix = ""

            for (i in 0 ..< min(kanji.length, kana.length)) {
                val c = kanji[kanji.length - i - 1]
                if (c != kana[kana.length - i - 1] || !c.isKana()) break
                suffix = c + suffix
            }

            return suffix
        }

    val dictionaryWordWithHeuristic: String
        get() {
            var dw = dictionaryWord

            if (dw.isNotEmpty()) {
                return if (dw == "-") "" else dw
            }

            dw = kanji

            if (dw.hasPunctuation() || dw.hasBracket()) {
                return ""
            }

            for (prefix in cutDictionaryWordPrefixes) {
                if (dw.length > prefix.length && dw.startsWith(prefix)) {
                    dw = dw.slice(prefix.length ..< dw.length)
                }
            }

            for (suffix in cutDictionaryWordSuffixes) {
                if (dw.length > suffix.length && dw.endsWith(suffix)) {
                    dw = dw.slice(0 ..< dw.length - suffix.length)
                }
            }

            return dw
        }

    override fun toString() =
        "Word($id, $kanji, $kana)"

    companion object {
        private val cutDictionaryWordPrefixes = arrayOf("〜")
        private val cutDictionaryWordSuffixes = arrayOf("〜", "をする", "する")
    }
}
