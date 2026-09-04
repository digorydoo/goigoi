package io.github.digorydoo.goigoi.core.db

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.IntlString

class PhraseOrSentence {
    class SplitPrimaryForm(
        val begin: FuriganaString,
        val wordStem: FuriganaString,
        val wordSuffix: String,
        val end: FuriganaString,
    )

    var primaryForm = FuriganaString()
    val kanji get() = primaryForm.kanji
    val kana get() = primaryForm.kana
    var romaji = ""
    val translation = IntlString()
    val explanation = IntlString()

    var wordFormToAsk = FuriganaString() // if the word appears in a different form, e.g. kana, verb form, etc.
    var wordFormToAskSuffix = "" // if this is defined, wordFormToAsk is the stem

    private fun wordFormToLookFor(word: Word): String =
        when {
            wordFormToAsk.isNotEmpty() -> wordFormToAsk.raw + wordFormToAskSuffix
            word.usuallyInKana -> word.kana
            else -> word.primaryForm.raw
        }

    private fun rangeOfFormToAsk(word: Word): IntRange? {
        val lookFor = wordFormToLookFor(word)
        if (lookFor.isEmpty()) return null

        val idx = primaryForm.raw.indexOf(lookFor)
        if (idx < 0) return null

        // Make sure the word appears only once, otherwise we're not sure the first one is what we're looking for.
        if (primaryForm.raw.indexOf(lookFor, idx + 1) >= 0) return null
        else return idx ..< idx + lookFor.length
    }

    fun canRemoveWordFromPrimaryForm(word: Word): Boolean =
        rangeOfFormToAsk(word) != null

    fun primaryFormInParts(word: Word): SplitPrimaryForm {
        val range = rangeOfFormToAsk(word)

        return when {
            range == null -> SplitPrimaryForm(primaryForm, FuriganaString(), "", FuriganaString())
            wordFormToAsk.isEmpty() -> SplitPrimaryForm(
                begin = FuriganaString(primaryForm.raw.slice(0 ..< range.first)),
                wordStem = FuriganaString(primaryForm.raw.slice(range.first .. range.last)),
                wordSuffix = "",
                end = FuriganaString(
                    primaryForm.raw.let { if (range.last >= it.length) "" else it.substring(range.last + 1) }
                ),
            )
            else -> SplitPrimaryForm(
                begin = FuriganaString(primaryForm.raw.slice(0 ..< range.first)),
                wordStem = wordFormToAsk,
                wordSuffix = wordFormToAskSuffix,
                end = FuriganaString(
                    primaryForm.raw.let { if (range.last >= it.length) "" else it.substring(range.last + 1) }
                )
            )
        }
    }

    override fun toString() =
        "PhraseOrSentence($kanji, $kana)"
}
