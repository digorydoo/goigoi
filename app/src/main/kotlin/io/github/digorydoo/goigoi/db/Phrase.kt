package io.github.digorydoo.goigoi.db

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.IntlString

class Phrase {
    var primaryForm = FuriganaString()
    val kanji get() = primaryForm.kanji
    val kana get() = primaryForm.kana
    var romaji = ""
    val translation = IntlString()
    val explanation = IntlString()

    fun canRemoveWordFromPrimaryForm(word: Word): Boolean {
        val lookFor = if (word.usuallyInKana) word.kana else word.primaryForm.raw
        val idx = primaryForm.raw.indexOf(lookFor)
        if (idx < 0) return false

        // Make sure the word appears only once, otherwise we're not sure the first one is what we're looking for.
        return primaryForm.raw.indexOf(lookFor, idx + 1) < 0
    }

    fun primaryFormWithWordRemoved(word: Word): FuriganaString {
        val lookFor = if (word.usuallyInKana) word.kana else word.primaryForm.raw
        val idx = primaryForm.raw.indexOf(lookFor)
        require(idx >= 0) { "primaryFormWithWordRemoved: Failed to find '$lookFor' in '${primaryForm.raw}'" }

        val raw = primaryForm.raw.replaceFirst(lookFor, "___")
        return FuriganaString(raw)
    }

    override fun toString() =
        "Phrase($kanji, $kana)"
}
