package io.github.digorydoo.goigoi.activity.prog_study

import io.github.digorydoo.goigoi.stats.StatsKey

enum class QAKind(val intValue: Int) {
    SHOW_KANJI_ASK_KANA(1),
    SHOW_KANA_ASK_KANJI(2),
    SHOW_ROMAJI_ASK_KANA(3),
    SHOW_TRANSLATION_ASK_KANA(4),
    SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS(5),
    SHOW_WORD_ASK_NOTHING(6),
    SHOW_PHRASE_ASK_NOTHING(7),
    SHOW_SENTENCE_ASK_NOTHING(8),
    SHOW_PHRASE_ASK_KANJI(9),
    SHOW_SENTENCE_ASK_KANJI(10),
    SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR(11),
    SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA(12);

    val shouldUseWeightBasedOnKanji: Boolean
        get() = when (this) {
            SHOW_KANJI_ASK_KANA -> true
            SHOW_KANA_ASK_KANJI -> true
            SHOW_ROMAJI_ASK_KANA -> false
            SHOW_TRANSLATION_ASK_KANA -> false
            SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS -> true
            SHOW_WORD_ASK_NOTHING -> false
            SHOW_PHRASE_ASK_NOTHING -> false
            SHOW_SENTENCE_ASK_NOTHING -> false
            SHOW_PHRASE_ASK_KANJI -> true
            SHOW_SENTENCE_ASK_KANJI -> true
            SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> true
            SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> false
        }

    val doesNotAskAnything: Boolean
        get() = when (this) {
            SHOW_KANJI_ASK_KANA -> false
            SHOW_KANA_ASK_KANJI -> false
            SHOW_ROMAJI_ASK_KANA -> false
            SHOW_TRANSLATION_ASK_KANA -> false
            SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS -> false
            SHOW_WORD_ASK_NOTHING -> true
            SHOW_PHRASE_ASK_NOTHING -> true
            SHOW_SENTENCE_ASK_NOTHING -> true
            SHOW_PHRASE_ASK_KANJI -> false
            SHOW_SENTENCE_ASK_KANJI -> false
            SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> false
            SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> false
        }

    val asksForKanji: Boolean
        get() = when (this) {
            SHOW_KANJI_ASK_KANA -> false
            SHOW_KANA_ASK_KANJI -> true
            SHOW_ROMAJI_ASK_KANA -> false
            SHOW_TRANSLATION_ASK_KANA -> false
            SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS -> true
            SHOW_WORD_ASK_NOTHING -> false
            SHOW_PHRASE_ASK_NOTHING -> false
            SHOW_SENTENCE_ASK_NOTHING -> false
            SHOW_PHRASE_ASK_KANJI -> true
            SHOW_SENTENCE_ASK_KANJI -> true
            SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> true
            SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> false
        }

    val asksForKana: Boolean
        get() = when (this) {
            SHOW_KANJI_ASK_KANA -> true
            SHOW_KANA_ASK_KANJI -> false
            SHOW_ROMAJI_ASK_KANA -> true
            SHOW_TRANSLATION_ASK_KANA -> true
            SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS -> false
            SHOW_WORD_ASK_NOTHING -> false
            SHOW_PHRASE_ASK_NOTHING -> false
            SHOW_SENTENCE_ASK_NOTHING -> false
            SHOW_PHRASE_ASK_KANJI -> false
            SHOW_SENTENCE_ASK_KANJI -> false
            SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> false
            SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> true
        }

    val involvesPhrases: Boolean
        get() = when (this) {
            SHOW_KANJI_ASK_KANA -> false
            SHOW_KANA_ASK_KANJI -> false
            SHOW_ROMAJI_ASK_KANA -> false
            SHOW_TRANSLATION_ASK_KANA -> false
            SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS -> false
            SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> false
            SHOW_WORD_ASK_NOTHING -> false
            SHOW_PHRASE_ASK_NOTHING -> true
            SHOW_SENTENCE_ASK_NOTHING -> false
            SHOW_PHRASE_ASK_KANJI -> true
            SHOW_SENTENCE_ASK_KANJI -> false
            SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> true
        }

    val involvesSentences: Boolean
        get() = when (this) {
            SHOW_KANJI_ASK_KANA -> false
            SHOW_KANA_ASK_KANJI -> false
            SHOW_ROMAJI_ASK_KANA -> false
            SHOW_TRANSLATION_ASK_KANA -> false
            SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS -> false
            SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> false
            SHOW_WORD_ASK_NOTHING -> false
            SHOW_PHRASE_ASK_NOTHING -> false
            SHOW_SENTENCE_ASK_NOTHING -> true
            SHOW_PHRASE_ASK_KANJI -> false
            SHOW_SENTENCE_ASK_KANJI -> true
            SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> false
        }

    val involvesPhrasesOrSentences: Boolean
        get() = when (this) {
            SHOW_KANJI_ASK_KANA -> false
            SHOW_KANA_ASK_KANJI -> false
            SHOW_ROMAJI_ASK_KANA -> false
            SHOW_TRANSLATION_ASK_KANA -> false
            SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS -> false
            SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> false
            SHOW_WORD_ASK_NOTHING -> false
            SHOW_PHRASE_ASK_NOTHING -> true
            SHOW_SENTENCE_ASK_NOTHING -> true
            SHOW_PHRASE_ASK_KANJI -> true
            SHOW_SENTENCE_ASK_KANJI -> true
            SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> true
        }

    fun toStatsKey() = when (this) {
        SHOW_KANJI_ASK_KANA -> StatsKey.PROGSTUDY_SHOW_KANJI_ASK_KANA
        SHOW_KANA_ASK_KANJI -> StatsKey.PROGSTUDY_SHOW_KANA_ASK_KANJI
        SHOW_ROMAJI_ASK_KANA -> StatsKey.PROGSTUDY_SHOW_ROMAJI_ASK_KANA
        SHOW_TRANSLATION_ASK_KANA -> StatsKey.PROGSTUDY_SHOW_TRANSLATION_ASK_KANA
        SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS -> StatsKey.PROGSTUDY_SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS
        SHOW_WORD_ASK_NOTHING -> StatsKey.PROGSTUDY_SHOW_WORD_ASK_NOTHING
        SHOW_PHRASE_ASK_NOTHING -> StatsKey.PROGSTUDY_SHOW_PHRASE_ASK_NOTHING
        SHOW_SENTENCE_ASK_NOTHING -> StatsKey.PROGSTUDY_SHOW_SENTENCE_ASK_NOTHING
        SHOW_PHRASE_ASK_KANJI -> StatsKey.PROGSTUDY_SHOW_PHRASE_ASK_KANJI
        SHOW_SENTENCE_ASK_KANJI -> StatsKey.PROGSTUDY_SHOW_SENTENCE_ASK_KANJI
        SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> StatsKey.PROGSTUDY_SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR
        SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> StatsKey.PROGSTUDY_SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA
    }

    companion object {
        fun fromIntOrNull(i: Int) =
            entries.firstOrNull { it.intValue == i }
    }
}
