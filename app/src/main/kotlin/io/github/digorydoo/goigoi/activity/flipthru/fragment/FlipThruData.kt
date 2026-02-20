package io.github.digorydoo.goigoi.activity.flipthru.fragment

import io.github.digorydoo.goigoi.db.Word
import ch.digorydoo.kutils.cjk.FuriganaString

class FlipThruData(
    private val word: Word,
    private val phraseIdx: Int?,
    private val sentenceIdx: Int?,
) {
    val primaryForm: FuriganaString
        get() = when {
            phraseIdx != null -> word.phrases[phraseIdx].primaryForm
            sentenceIdx != null -> word.sentences[sentenceIdx].primaryForm
            else -> word.primaryForm
        }

    val kanji: String get() = primaryForm.kanji
    val kana: String get() = primaryForm.kana

    val usuallyInKana: Boolean
        get() = when {
            phraseIdx != null || sentenceIdx != null -> false
            else -> word.usuallyInKana
        }

    val romaji: String
        get() = when {
            phraseIdx != null -> word.phrases[phraseIdx].romaji
            sentenceIdx != null -> word.sentences[sentenceIdx].romaji
            else -> word.romaji
        }

    val translation: String
        get() = when {
            phraseIdx != null -> word.phrases[phraseIdx].translation.withSystemLang
            sentenceIdx != null -> word.sentences[sentenceIdx].translation.withSystemLang
            else -> word.translation.withSystemLang
        }

    // The hint is an extension of the translation and is shown on the front when the translation is on the front.
    val hint: String
        get() = when {
            phraseIdx != null -> ""
            sentenceIdx != null -> ""
            else -> word.hintsWithSystemLang
        }

    // The info is additional information that should never be shown on the front.
    val explanation: String
        get() = when {
            phraseIdx != null -> word.phrases[phraseIdx].explanation.withSystemLang
            sentenceIdx != null -> word.sentences[sentenceIdx].explanation.withSystemLang
            else -> ""
        }
}
