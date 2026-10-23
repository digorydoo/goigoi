package io.github.digorydoo.goigoi.core.db

import ch.digorydoo.kutils.cjk.isKana

// See vocab_schema.xsd for a description of the semantics of these values
// It combines the values available for <word> with those for <phrase> and <sentence>.
enum class AnswerAccuracy(val xmlKey: String, val intValue: Int) {
    ALT_READING_SAME_WORD("alt-reading-same-word", 1),
    ALT_SPELLING_SAME_WORD("alt-spelling-same-word", 2),
    CORRECT_FOR_TRANSLATION("correct-for-translation", 3),
    CORRECT_IN_PHRASES_AND_SENTENCES("correct-in-phrases-and-sentences", 4),
    CORRECT("correct", 5),
    INACCURATE("inaccurate", 6);

    val intendedForWord
        get() = when (this) {
            ALT_READING_SAME_WORD -> true
            ALT_SPELLING_SAME_WORD -> true
            CORRECT_FOR_TRANSLATION -> true
            CORRECT_IN_PHRASES_AND_SENTENCES -> true
            CORRECT -> false // word must use either correct-for-translation or correct-in-phrases-and-sentences
            INACCURATE -> true
        }

    val intendedForPhraseOrSentence
        get() = when (this) {
            ALT_READING_SAME_WORD -> false
            ALT_SPELLING_SAME_WORD -> false
            CORRECT_FOR_TRANSLATION -> false
            CORRECT_IN_PHRASES_AND_SENTENCES -> false
            CORRECT -> true
            INACCURATE -> true
        }

    val canPropagateToPhrasesAndSentences
        get() = when (this) {
            ALT_READING_SAME_WORD -> true
            ALT_SPELLING_SAME_WORD -> true
            CORRECT_FOR_TRANSLATION -> false
            CORRECT_IN_PHRASES_AND_SENTENCES -> true
            CORRECT -> false // irrelevant, since a word's acceptable answers should never have this as the reason
            INACCURATE -> false
        }

    companion object {
        fun fromXmlKey(xmlKey: String) =
            entries.find { it.xmlKey == xmlKey }

        fun fromInt(intValue: Int) =
            entries.find { it.intValue == intValue }
    }
}

class AcceptableAnswer(
    val answer: String, // furigana not expected here; answer is only stem in case phrase/sentence defines a suffix
    val accuracy: AnswerAccuracy,
) {
    val isKana = answer.isKana()
    val inaccurate = accuracy == AnswerAccuracy.INACCURATE
    override fun toString() = "AcceptableAnswer($answer, ${accuracy.xmlKey}"
}
