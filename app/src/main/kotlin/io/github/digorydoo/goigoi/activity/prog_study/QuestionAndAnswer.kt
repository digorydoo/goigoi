package io.github.digorydoo.goigoi.activity.prog_study

import io.github.digorydoo.goigoi.db.Word
import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.utils.OneOf

// This class is immutable
class QuestionAndAnswer(val word: Word, val kind: QAKind, val index: Int, val questionHasFurigana: Boolean) {
    enum class Hint { PHRASE }
    enum class FontType { DEFAULT, BOLD_HIRAGANA, BOLD_KATAKANA, PENCIL, CALLIGRAPHY }

    var fontType = FontType.DEFAULT // will be set by Choreographer
    var furiganaRelVOffset = 0.0f // dito

    private val phraseOrNull = word.phrases.getOrNull(index)
    private val sentenceOrNull = word.sentences.getOrNull(index)

    val question: OneOf<String, FuriganaString> = when (kind) {
        QAKind.SHOW_KANJI_ASK_KANA -> OneOf.First(word.kanji)
        QAKind.SHOW_KANA_ASK_KANJI -> OneOf.First(word.kana)
        QAKind.SHOW_ROMAJI_ASK_KANA -> OneOf.First(word.romaji)
        QAKind.SHOW_TRANSLATION_ASK_KANA -> OneOf.First(word.translation.withSystemLang)

        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
        -> OneOf.First(word.translation.withSystemLang)

        QAKind.SHOW_WORD_ASK_NOTHING -> when {
            word.usuallyInKana -> OneOf.First(word.kana)
            questionHasFurigana -> OneOf.Second(word.primaryForm)
            else -> OneOf.First(word.primaryForm.kanji)
        }

        QAKind.SHOW_PHRASE_ASK_NOTHING -> when {
            questionHasFurigana -> OneOf.Second(phraseOrNull!!.primaryForm)
            else -> OneOf.First(phraseOrNull!!.primaryForm.kanji)
        }
        QAKind.SHOW_SENTENCE_ASK_NOTHING -> when {
            questionHasFurigana -> OneOf.Second(sentenceOrNull!!.primaryForm)
            else -> OneOf.First(sentenceOrNull!!.primaryForm.kanji)
        }
        QAKind.SHOW_PHRASE_ASK_KANJI -> OneOf.Second(phraseOrNull!!.primaryFormWithWordRemoved(word))
        QAKind.SHOW_SENTENCE_ASK_KANJI -> OneOf.Second(sentenceOrNull!!.primaryFormWithWordRemoved(word))
        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> OneOf.First(phraseOrNull!!.translation.withSystemLang)
    }

    val questionWithoutFurigana: String = question.let {
        when (it) {
            is OneOf.First -> it.first
            is OneOf.Second -> it.second.kanji
        }
    }

    val questionWithGapFilled: OneOf<String, FuriganaString> = when (kind) {
        QAKind.SHOW_WORD_ASK_NOTHING -> when {
            word.usuallyInKana -> OneOf.First(word.kana)
            else -> OneOf.Second(word.primaryForm)
        }
        QAKind.SHOW_PHRASE_ASK_NOTHING -> OneOf.Second(phraseOrNull!!.primaryForm)
        QAKind.SHOW_SENTENCE_ASK_NOTHING -> OneOf.Second(sentenceOrNull!!.primaryForm)
        QAKind.SHOW_PHRASE_ASK_KANJI -> OneOf.Second(phraseOrNull!!.primaryForm)
        QAKind.SHOW_SENTENCE_ASK_KANJI -> OneOf.Second(sentenceOrNull!!.primaryForm)
        else -> question
    }

    val questionHint: OneOf<String, Hint> = when (kind) {
        QAKind.SHOW_KANJI_ASK_KANA,
        QAKind.SHOW_KANA_ASK_KANJI,
        QAKind.SHOW_ROMAJI_ASK_KANA,
        QAKind.SHOW_WORD_ASK_NOTHING,
        QAKind.SHOW_PHRASE_ASK_NOTHING,
        QAKind.SHOW_SENTENCE_ASK_NOTHING,
        -> OneOf.First("")

        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
        -> OneOf.Second(Hint.PHRASE)

        QAKind.SHOW_TRANSLATION_ASK_KANA,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
        -> OneOf.First(word.hintsWithSystemLang)

        QAKind.SHOW_PHRASE_ASK_KANJI -> OneOf.First(phraseOrNull?.translation?.withSystemLang ?: "")
        QAKind.SHOW_SENTENCE_ASK_KANJI -> OneOf.First(sentenceOrNull?.translation?.withSystemLang ?: "")
    }

    val answers: List<String> = when (kind) {
        QAKind.SHOW_KANJI_ASK_KANA,
        QAKind.SHOW_ROMAJI_ASK_KANA,
        QAKind.SHOW_TRANSLATION_ASK_KANA,
        -> mutableListOf(word.kana).apply { addAll(word.synonyms.map { it.kana }) }

        QAKind.SHOW_KANA_ASK_KANJI,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
        -> mutableListOf(word.kanji).apply { addAll(word.synonyms.map { it.kanji }) }

        QAKind.SHOW_WORD_ASK_NOTHING,
        QAKind.SHOW_PHRASE_ASK_NOTHING,
        QAKind.SHOW_SENTENCE_ASK_NOTHING,
        -> listOf()

        QAKind.SHOW_PHRASE_ASK_KANJI,
        QAKind.SHOW_SENTENCE_ASK_KANJI,
        -> when {
            word.usuallyInKana -> mutableListOf(word.kana).apply { addAll(word.synonyms.map { it.kana }) }
            else -> mutableListOf(word.kanji).apply { addAll(word.synonyms.map { it.kanji }) }
        }

        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> listOf(phraseOrNull?.kana ?: "")
    }

    val kanjiOrKanaToReveal = when (kind) {
        QAKind.SHOW_ROMAJI_ASK_KANA,
        QAKind.SHOW_TRANSLATION_ASK_KANA,
        -> when {
            word.kanji != word.kana && !word.usuallyInKana -> word.kanji
            else -> ""
        }

        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
        -> when {
            word.kana != word.kanji -> word.kana
            else -> ""
        }

        QAKind.SHOW_KANJI_ASK_KANA,
        QAKind.SHOW_KANA_ASK_KANJI,
        QAKind.SHOW_WORD_ASK_NOTHING,
        QAKind.SHOW_PHRASE_ASK_NOTHING,
        QAKind.SHOW_SENTENCE_ASK_NOTHING,
        QAKind.SHOW_PHRASE_ASK_KANJI,
        QAKind.SHOW_SENTENCE_ASK_KANJI,
        -> ""

        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> when {
            phraseOrNull?.kana != phraseOrNull?.kanji -> phraseOrNull?.kanji ?: ""
            else -> ""
        }
    }

    val translationToReveal = when (kind) {
        QAKind.SHOW_KANJI_ASK_KANA,
        QAKind.SHOW_KANA_ASK_KANJI,
        QAKind.SHOW_ROMAJI_ASK_KANA,
        QAKind.SHOW_WORD_ASK_NOTHING,
        -> word.translation.withSystemLang

        QAKind.SHOW_PHRASE_ASK_NOTHING -> phraseOrNull?.translation?.withSystemLang ?: ""
        QAKind.SHOW_SENTENCE_ASK_NOTHING -> sentenceOrNull?.translation?.withSystemLang ?: ""

        QAKind.SHOW_TRANSLATION_ASK_KANA,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
        QAKind.SHOW_PHRASE_ASK_KANJI,
        QAKind.SHOW_SENTENCE_ASK_KANJI,
        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
        -> ""
    }

    val hintToReveal = when (kind) {
        QAKind.SHOW_KANJI_ASK_KANA,
        QAKind.SHOW_KANA_ASK_KANJI,
        QAKind.SHOW_ROMAJI_ASK_KANA,
        QAKind.SHOW_WORD_ASK_NOTHING,
        -> word.hintsWithSystemLang

        QAKind.SHOW_TRANSLATION_ASK_KANA,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
        QAKind.SHOW_PHRASE_ASK_NOTHING,
        QAKind.SHOW_SENTENCE_ASK_NOTHING,
        QAKind.SHOW_PHRASE_ASK_KANJI,
        QAKind.SHOW_SENTENCE_ASK_KANJI,
        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
        -> ""
    }

    val explanation = when (kind) {
        QAKind.SHOW_WORD_ASK_NOTHING,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
        -> ""

        QAKind.SHOW_KANJI_ASK_KANA,
        QAKind.SHOW_KANA_ASK_KANJI,
        QAKind.SHOW_ROMAJI_ASK_KANA,
        QAKind.SHOW_TRANSLATION_ASK_KANA,
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
        -> ""

        QAKind.SHOW_PHRASE_ASK_NOTHING,
        QAKind.SHOW_PHRASE_ASK_KANJI,
        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
        -> phraseOrNull?.explanation?.withSystemLang ?: ""

        QAKind.SHOW_SENTENCE_ASK_NOTHING,
        QAKind.SHOW_SENTENCE_ASK_KANJI,
        -> sentenceOrNull?.explanation?.withSystemLang ?: ""
    }

    val presentWholeWords = when (kind) {
        QAKind.SHOW_KANJI_ASK_KANA -> false
        QAKind.SHOW_KANA_ASK_KANJI -> false
        QAKind.SHOW_ROMAJI_ASK_KANA -> false
        QAKind.SHOW_TRANSLATION_ASK_KANA -> false
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> true
        QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS -> false
        QAKind.SHOW_WORD_ASK_NOTHING -> false
        QAKind.SHOW_PHRASE_ASK_NOTHING -> false
        QAKind.SHOW_SENTENCE_ASK_NOTHING -> false
        QAKind.SHOW_PHRASE_ASK_KANJI -> true
        QAKind.SHOW_SENTENCE_ASK_KANJI -> true
        QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> false
    }
}
