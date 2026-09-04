package io.github.digorydoo.goigoi.core.prog_study

import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.logging.Log
import ch.digorydoo.kutils.string.lpad
import ch.digorydoo.kutils.string.rpad
import ch.digorydoo.kutils.string.toPrecision
import io.github.digorydoo.goigoi.core.db.KanjiIndex
import io.github.digorydoo.goigoi.core.db.StudyInContextKind
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.prog_study.QAPicker.WordInfo
import io.github.digorydoo.goigoi.core.stats.Stats
import kotlin.math.max

class QAWeightAssigner(
    private val kanjiIndex: KanjiIndex,
    private val rounds: RoundsTracker,
    private val stats: Stats,
) {
    fun getWeight(
        kind: QAKind,
        word: Word,
        info: WordInfo,
        avgLevelOfWords: JLPTLevel,
        canUseRomaji: Boolean,
    ): Float {
        val roundOfWordAndKind = rounds.of(word, kind)
        val weightBasedOnRating = getWeightBasedOnRating(kind, word, info)
        val weightBasedOnKanji = getWeightBasedOnKanji(kind, word, avgLevelOfWords)
        val weightBasedOnKind = getWeightBasedOnKind(kind, word, roundOfWordAndKind, info, canUseRomaji)
        val w = max(ALMOST_NEVER, weightBasedOnRating * weightBasedOnKanji * weightBasedOnKind)

        val seen = stats.getWordSeenCount(word, kind.toStatsKey())

        fun Float.percent() = (this * 100).let {
            it.toPrecision(
                when {
                    it >= 100 -> 3
                    it >= 10 -> 4
                    else -> 5
                }
            )
        }

        Log.debug(
            TAG,
            arrayOf(
                rpad(w.percent(), 7),
                rpad("$kind", 40),
                "seen $seen",
                "round ${lpad("$roundOfWordAndKind", 4)}",
                "ratg ${weightBasedOnRating.percent()}",
                "kanji ${weightBasedOnKanji.percent()}",
                "kind ${weightBasedOnKind.percent()}",
            ).joinToString(" ")
        )

        return w
    }

    private fun getWeightBasedOnRating(kind: QAKind, word: Word, info: WordInfo): Float {
        val statsKey = kind.toStatsKey()
        val kindSeenCount = stats.getWordSeenCount(word, statsKey)

        val numPhrasesOrSentences = when {
            kind.involvesPhrases -> when (kind.asksForKanji) {
                true -> info.phrasesUsableForAskGap.size
                false -> word.phrases.size
            }
            kind.involvesSentences -> when (kind.asksForKanji) {
                true -> info.sentencesUsableForAskGap.size
                false -> word.sentences.size
            }
            else -> 0
        }

        if (kind.doesNotAskAnything) {
            // The app can get stuck if a word's rating forces it to choose from kinds that do not ask anything,
            // because then the word can no longer progress. Therefore, make sure these kinds make room for others.
            return (1.0f + numPhrasesOrSentences) / (kindSeenCount + 1.0f + numPhrasesOrSentences)
        }

        if (kindSeenCount < 3) {
            return 1.0f // rating is not significant unless kind was seen a couple of times
        }

        val rating = stats.getWordRating(word, statsKey)

        val weight = when {
            rating >= 0.8f -> when {
                kindSeenCount < 5 -> 0.2f // a bit more often than SOMETIMES, because rating is not significant
                else -> SOMETIMES // seldom, but not RARE
            }
            else -> 1.0f - rating // will be between 0.2f (good rating = seldom) .. 1.0f (worst rating)
        }

        if (!kind.involvesPhrasesOrSentences || numPhrasesOrSentences == 0) {
            return weight
        }

        // Multiple phrases or sentences are involved, so approach the weight based on repeat count
        val repeating = kindSeenCount / numPhrasesOrSentences
        return weight - (weight - 1f) * 1f / (repeating + 1f)
    }

    private fun getWeightBasedOnKanji(kind: QAKind, word: Word, avgLevelOfWords: JLPTLevel): Float {
        if (!kind.shouldUseWeightBasedOnKanji || kind.doesNotAskAnything) {
            return 1.0f
        }

        val kanjiLevel = (kanjiIndex.levelOfMostDifficultKanji(word.kanji) ?: JLPTLevel.N5).toInt()
        val avgLevel = avgLevelOfWords.toInt()
        val diff = avgLevel - kanjiLevel
        val totalCorrectCount = stats.getWordTotalCorrectCount(word)

        return when {
            word.usuallyInKana && diff <= 1 -> when {
                totalCorrectCount >= 10 -> SOMETIMES
                totalCorrectCount >= 3 -> JUST_ABOVE_RARE
                else -> RARE
            }
            diff < 0 -> 1.0f // e.g. an N5 kanji in an N4 unyt
            diff == 0 -> 1.0f // e.g. an N4 kanji in an N4 unyt
            diff == 1 -> when { // e.g. an N3 kanji in an N4 unyt
                totalCorrectCount >= 12 -> 1.0f
                totalCorrectCount >= 8 -> 0.5f
                totalCorrectCount >= 3 -> SOMETIMES
                totalCorrectCount > 0 -> JUST_ABOVE_RARE
                else -> RARE
            }
            diff == 2 -> when { // e.g. an N2 kanji in an N4 unyt
                totalCorrectCount >= 16 -> SOMETIMES
                totalCorrectCount >= 12 -> JUST_ABOVE_RARE
                totalCorrectCount >= 8 -> RARE
                else -> ALMOST_NEVER
            }
            diff == 3 -> when { // e.g. an N1 kanji in an N4 unyt
                totalCorrectCount >= 24 -> SOMETIMES
                totalCorrectCount >= 16 -> JUST_ABOVE_RARE
                totalCorrectCount >= 12 -> RARE
                else -> ALMOST_NEVER
            }
            diff == 4 -> when { // e.g. an N1 kanji in an N5 unyt
                totalCorrectCount >= 32 -> SOMETIMES
                totalCorrectCount >= 24 -> JUST_ABOVE_RARE
                totalCorrectCount >= 16 -> RARE
                else -> ALMOST_NEVER
            }
            else -> ALMOST_NEVER
        }
    }

    private fun getWeightBasedOnKind(
        kind: QAKind,
        word: Word,
        roundOfWordAndKind: Int?,
        info: WordInfo,
        canUseRomaji: Boolean,
    ): Float {
        val totalCorrectCount = stats.getWordTotalCorrectCount(word)
        return when (kind) {
            QAKind.SHOW_KANJI_ASK_KANA -> when {
                canUseRomaji && totalCorrectCount < 1 -> RARE // prefer SHOW_ROMAJI_ASK_KANA
                word.studyInContext == StudyInContextKind.REQUIRED -> RARE
                word.studyInContext == StudyInContextKind.PREFERRED -> SOMETIMES
                totalCorrectCount > 3 && word.phrases.size >= 3 -> SOMETIMES
                totalCorrectCount < 10 -> 1.0f
                else -> 0.5f // prefer SHOW_KANA_ASK_KANJI for higher correct counts
            }
            QAKind.SHOW_KANA_ASK_KANJI -> when {
                word.studyInContext == StudyInContextKind.REQUIRED -> RARE
                canUseRomaji && totalCorrectCount < 1 -> RARE // prefer SHOW_KANA_ASK_ROMAJI
                canUseRomaji && totalCorrectCount < 3 -> SOMETIMES
                !canUseRomaji && totalCorrectCount < 2 -> RARE // prefer SHOW_KANJI_ASK_KANA
                totalCorrectCount < 3 -> 0.5f // prefer SHOW_KANJI_ASK_KANA
                word.phrases.size >= 3 -> 0.5f
                word.studyInContext == StudyInContextKind.PREFERRED -> 0.5f
                else -> 1.0f
            }
            QAKind.SHOW_ROMAJI_ASK_KANA -> when {
                word.studyInContext == StudyInContextKind.REQUIRED -> RARE
                totalCorrectCount < 1 && word.studyInContext == StudyInContextKind.NOT_REQUIRED -> 1.0f
                totalCorrectCount < 2 && word.studyInContext == StudyInContextKind.NOT_REQUIRED -> 0.5f
                totalCorrectCount < 3 -> SOMETIMES
                else -> ALMOST_NEVER
            }
            QAKind.SHOW_TRANSLATION_ASK_KANA -> when {
                word.studyInContext == StudyInContextKind.REQUIRED -> RARE
                word.studyInContext == StudyInContextKind.PREFERRED -> when {
                    word.usuallyInKana || word.kanji == word.kana -> JUST_ABOVE_RARE // PHRASE_ASK_KANJI not available
                    else -> ALMOST_NEVER
                }
                word.usuallyInKana || word.kanji == word.kana -> 0.5f
                totalCorrectCount < 2 -> JUST_ABOVE_RARE
                totalCorrectCount < 5 -> SOMETIMES
                totalCorrectCount < 7 -> 0.5f
                word.phrases.size >= 3 -> 0.5f
                else -> 1.0f
            }
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> {
                val thisKindStatsKey = QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR.toStatsKey()
                val thisKindCorrectCount = stats.getWordCorrectCount(word, thisKindStatsKey)

                when {
                    canUseRomaji && totalCorrectCount < 1 -> ALMOST_NEVER
                    word.studyInContext == StudyInContextKind.REQUIRED -> RARE
                    word.studyInContext == StudyInContextKind.PREFERRED -> JUST_ABOVE_RARE
                    thisKindCorrectCount >= 2 -> JUST_ABOVE_RARE
                    thisKindCorrectCount >= 1 -> SOMETIMES
                    canUseRomaji && totalCorrectCount < 2 -> SOMETIMES
                    else -> 1.0f
                }
            }
            QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
            -> when {
                totalCorrectCount < 3 -> ALMOST_NEVER
                word.studyInContext == StudyInContextKind.REQUIRED -> RARE
                word.studyInContext == StudyInContextKind.PREFERRED -> JUST_ABOVE_RARE
                totalCorrectCount < 5 -> when (canUseRomaji) {
                    true -> RARE
                    false -> SOMETIMES
                }
                totalCorrectCount < 7 -> SOMETIMES
                totalCorrectCount < 9 -> 0.5f
                word.phrases.size >= 3 -> 0.5f
                else -> 1.0f
            }
            QAKind.SHOW_WORD_ASK_NOTHING -> when {
                word.studyInContext != StudyInContextKind.NOT_REQUIRED -> RARE
                else -> 1.0f
            }
            QAKind.SHOW_PHRASE_ASK_NOTHING -> {
                val seenCount = stats.getWordSeenCount(word, kind.toStatsKey())
                val shouldAskKanji = seenCount >= word.phrases.size && info.phrasesUsableForAskGap.isNotEmpty()

                when (word.studyInContext) {
                    StudyInContextKind.PREFERRED, StudyInContextKind.REQUIRED -> when {
                        shouldAskKanji -> ALMOST_NEVER // prefer SHOW_PHRASE_ASK_KANJI
                        rounds.since(rounds.lastTrivial) <= 5 -> 0.5f // try to ask something non-trivial first
                        else -> 1.0f
                    }
                    StudyInContextKind.NOT_REQUIRED -> when {
                        shouldAskKanji -> ALMOST_NEVER // prefer SHOW_PHRASE_ASK_KANJI
                        rounds.since(roundOfWordAndKind) < 20 -> RARE // hold back next phrase
                        rounds.since(rounds.lastTrivial) <= 1 -> RARE // don't ask nothing twice in a row
                        seenCount >= word.phrases.size -> JUST_ABOVE_RARE // SHOW_PHRASE_ASK_KANJI not available
                        rounds.since(rounds.lastTrivial) <= 5 -> SOMETIMES // still a bit early
                        totalCorrectCount < 1 -> SOMETIMES
                        else -> 1.0f
                    }
                }
            }
            QAKind.SHOW_SENTENCE_ASK_NOTHING -> {
                val seenCount = stats.getWordSeenCount(word, kind.toStatsKey())
                val phraseAskNothingCount = stats.getWordSeenCount(word, QAKind.SHOW_PHRASE_ASK_NOTHING.toStatsKey())
                val shouldShowPhraseFirst = phraseAskNothingCount <= seenCount && word.phrases.isNotEmpty()
                val shouldAskKanji = seenCount >= word.sentences.size && info.sentencesUsableForAskGap.isNotEmpty()

                when (word.studyInContext) {
                    StudyInContextKind.PREFERRED, StudyInContextKind.REQUIRED -> when {
                        shouldShowPhraseFirst -> ALMOST_NEVER
                        seenCount >= word.sentences.size -> ALMOST_NEVER // prefer ASK_KANJI after one round
                        rounds.since(rounds.lastTrivial) <= 5 -> 0.5f // try to ask something non-trivial first
                        else -> 1.0f
                    }
                    StudyInContextKind.NOT_REQUIRED -> when {
                        shouldShowPhraseFirst -> ALMOST_NEVER
                        shouldAskKanji -> ALMOST_NEVER // prefer SHOW_SENTENCE_ASK_KANJI
                        rounds.since(roundOfWordAndKind) < 20 -> RARE // hold back next sentence
                        rounds.since(rounds.lastTrivial) <= 1 -> RARE // don't ask nothing twice in a row
                        seenCount >= word.sentences.size -> SOMETIMES // SHOW_SENTENCE_ASK_KANJI not available
                        rounds.since(rounds.lastTrivial) <= 5 -> SOMETIMES // still a bit early
                        totalCorrectCount < 1 -> SOMETIMES
                        else -> 0.5f
                    }
                }
            }
            QAKind.SHOW_PHRASE_ASK_WORD_KANA -> {
                val askNothingCount = stats.getWordSeenCount(word, QAKind.SHOW_PHRASE_ASK_NOTHING.toStatsKey())
                val thisKindCount = stats.getWordSeenCount(word, kind.toStatsKey())
                val shouldAskNothingFirst = askNothingCount < word.phrases.size && askNothingCount <= thisKindCount

                when {
                    shouldAskNothingFirst -> ALMOST_NEVER
                    else -> 1.0f
                }
            }
            QAKind.SHOW_PHRASE_ASK_WORD_KANJI -> {
                val askNothingCount = stats.getWordSeenCount(word, QAKind.SHOW_PHRASE_ASK_NOTHING.toStatsKey())
                val thisKindCount = stats.getWordSeenCount(word, kind.toStatsKey())
                val kanaKindCount = stats.getWordSeenCount(word, QAKind.SHOW_PHRASE_ASK_WORD_KANA.toStatsKey())
                val sentenceKindCount = stats.getWordSeenCount(word, QAKind.SHOW_SENTENCE_ASK_WORD_KANJI.toStatsKey())
                val repeatingPhrases = thisKindCount >= info.phrasesUsableForAskGap.size
                val repeatingSentences = sentenceKindCount >= info.sentencesUsableForAskGap.size
                val shouldAskNothingFirst = askNothingCount < word.phrases.size && askNothingCount <= thisKindCount
                val shouldAskKanaFirst = kanaKindCount < word.phrases.size && kanaKindCount <= thisKindCount
                val hasNonRepeatedSentences = !repeatingSentences && info.sentencesUsableForAskGap.isNotEmpty()

                when {
                    shouldAskNothingFirst -> ALMOST_NEVER
                    shouldAskKanaFirst -> RARE
                    repeatingPhrases && hasNonRepeatedSentences -> SOMETIMES
                    else -> 1.0f
                }
            }
            QAKind.SHOW_SENTENCE_ASK_WORD_KANA -> {
                val askNothingCount = stats.getWordSeenCount(word, QAKind.SHOW_SENTENCE_ASK_NOTHING.toStatsKey())
                val thisKindCount = stats.getWordSeenCount(word, kind.toStatsKey())
                val shouldAskNothingFirst = askNothingCount < word.sentences.size && askNothingCount <= thisKindCount

                when {
                    shouldAskNothingFirst -> ALMOST_NEVER
                    else -> 1.0f
                }
            }
            QAKind.SHOW_SENTENCE_ASK_WORD_KANJI -> {
                val askNothingCount = stats.getWordSeenCount(word, QAKind.SHOW_SENTENCE_ASK_NOTHING.toStatsKey())
                val thisKindCount = stats.getWordSeenCount(word, kind.toStatsKey())
                val kanaKindCount = stats.getWordSeenCount(word, QAKind.SHOW_SENTENCE_ASK_WORD_KANA.toStatsKey())
                val phraseKindCount = stats.getWordSeenCount(word, QAKind.SHOW_PHRASE_ASK_WORD_KANJI.toStatsKey())
                val repeatingSentences = thisKindCount >= info.sentencesUsableForAskGap.size
                val repeatingPhrases = phraseKindCount >= info.phrasesUsableForAskGap.size
                val shouldAskNothingFirst = askNothingCount < word.sentences.size && askNothingCount <= thisKindCount
                val shouldAskKanaFirst = kanaKindCount < word.sentences.size && kanaKindCount <= thisKindCount
                val hasNonRepeatedPhrases = !repeatingPhrases && info.phrasesUsableForAskGap.isNotEmpty()

                when {
                    shouldAskNothingFirst -> ALMOST_NEVER
                    shouldAskKanaFirst -> RARE
                    repeatingSentences && hasNonRepeatedPhrases -> RARE
                    else -> 1.0f
                }
            }
            QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> {
                val askNothingCount = stats.getWordSeenCount(word, QAKind.SHOW_PHRASE_ASK_NOTHING.toStatsKey())
                val askKanjiCount = stats.getWordSeenCount(word, QAKind.SHOW_PHRASE_ASK_WORD_KANJI.toStatsKey())
                val thisKindCount = stats.getWordSeenCount(word, kind.toStatsKey())
                val sentenceKindCount = stats.getWordSeenCount(word, QAKind.SHOW_SENTENCE_ASK_WORD_KANJI.toStatsKey())
                val repeatingPhrases = thisKindCount >= word.phrases.size // word not removed with this kind
                val repeatingSentences = sentenceKindCount >= info.sentencesUsableForAskGap.size
                val shouldAskNothingFirst = askNothingCount < word.phrases.size && askNothingCount <= thisKindCount
                val kanjiLevel = kanjiIndex.levelOfMostDifficultKanji(word.kanji) ?: JLPTLevel.Nx

                val shouldAskKanjiFirst = !word.usuallyInKana && word.kanji != word.kana &&
                    askKanjiCount < word.phrases.size && askKanjiCount <= thisKindCount &&
                    info.phrasesUsableForAskGap.isNotEmpty() &&
                    !kanjiLevel.isMoreDifficultThan(word.level ?: JLPTLevel.Nx)

                when (word.studyInContext) {
                    StudyInContextKind.PREFERRED, StudyInContextKind.REQUIRED -> when {
                        shouldAskNothingFirst -> RARE
                        shouldAskKanjiFirst -> RARE // ask kanji only asks for word, this kind asks entire phrase
                        repeatingPhrases -> when {
                            info.sentencesUsableForAskGap.isEmpty() || repeatingSentences -> 0.5f
                            word.usuallyInKana || word.kanji == word.kana -> SOMETIMES
                            else -> JUST_ABOVE_RARE
                        }
                        else -> 1.0f
                    }
                    StudyInContextKind.NOT_REQUIRED -> when {
                        shouldAskNothingFirst -> RARE
                        shouldAskKanjiFirst -> RARE
                        repeatingPhrases -> when {
                            word.usuallyInKana || word.kanji == word.kana -> 0.5f
                            info.sentencesUsableForAskGap.isEmpty() || repeatingSentences -> 0.3f
                            else -> RARE
                        }
                        else -> 1.0f
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = Log.Tag("QAWeightAssigner")
        private const val SOMETIMES = 0.05f // seldom, but not RARE
        private const val JUST_ABOVE_RARE = 0.005f // between RARE and SOMETIMES
        const val RARE = 0.001f // likelihood when a QAKind is only to be shown if it's the only one available
        private const val ALMOST_NEVER = RARE / 1000 // different from RARE only if all weights are <= RARE
    }
}
