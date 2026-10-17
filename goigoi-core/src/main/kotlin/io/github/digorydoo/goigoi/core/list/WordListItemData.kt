package io.github.digorydoo.goigoi.core.list

import ch.digorydoo.kutils.cjk.JLPTLevel

class WordListItemData(
    val kanji: String,
    val kana: String,
    val usuallyInKana: Boolean,
    val romaji: String,
    val translation: String,
    val level: JLPTLevel,
    val progress: Float, // null = do not display progress
    val rating: Float, // null = do not display rating
    val asleep: Boolean, // true = not studied for a long time
    val isInMyWords: Boolean,
)
