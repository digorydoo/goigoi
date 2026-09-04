package io.github.digorydoo.goigoi.compiler.vocab

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.IntlString
import ch.digorydoo.kutils.cjk.JLPTLevel

class GoigoiPhraseOrSentence(val kind: Kind) {
    enum class Kind { PHRASE, SENTENCE }

    val isPhrase = kind == Kind.PHRASE

    var primaryForm = FuriganaString()
    val kanji: String get() = primaryForm.kanji
    val kana: String get() = primaryForm.kana
    var romaji = ""
    var translation = IntlString()
    var explanation = IntlString()
    var level: JLPTLevel? = null
    var wordFormToAsk = FuriganaString() // if the word appears in a different form, e.g. kana, verb form, etc.
    var wordFormToAskSuffix = "" // if this is defined, wordFormToAsk is the stem
    var allowSpaces: Boolean? = null // null = determine based on JLPT level
    var origin = ""
    var href = ""
    var remark = ""

    override fun toString() = when (kind) {
        Kind.PHRASE -> "Phrase "
        Kind.SENTENCE -> "Sentence "
    } + romaji.ifEmpty { translation.en }.ifEmpty { kana }.ifEmpty { kanji }
}
