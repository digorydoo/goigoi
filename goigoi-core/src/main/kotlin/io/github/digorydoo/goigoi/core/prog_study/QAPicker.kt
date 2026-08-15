package io.github.digorydoo.goigoi.core.prog_study

import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.goigoi.core.db.KanjiIndex
import io.github.digorydoo.goigoi.core.db.StudyInContextKind
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.prog_study.FixedKeysProvider.Companion.NUM_CHIPS_WHEN_SIMILAR_KANJIS
import io.github.digorydoo.goigoi.core.prog_study.QAWeightAssigner.Companion.RARE
import io.github.digorydoo.goigoi.core.stats.Stats
import kotlin.random.Random

class QAPicker(
    private val isInTestLab: Boolean,
    private val kanjiIndex: KanjiIndex,
    private val rounds: RoundsTracker,
    private val stats: Stats,
) {
    private class KindAndWeight(val kind: QAKind, val weight: Float) {
        override fun toString() = "{ kind = $kind, weight = $weight }"
    }

    class WordInfo(word: Word) {
        val phrasesUsableForAskKanji = word.phrases.filter { it.canRemoveWordFromPrimaryForm(word) }
        val sentencesUsableForAskKanji = word.sentences.filter { it.canRemoveWordFromPrimaryForm(word) }

        val maxNumCharsInAnswer = when (word.studyInContext) {
            StudyInContextKind.NOT_REQUIRED -> MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_NOT_REQUIRED
            StudyInContextKind.PREFERRED -> MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_PREFERRED
            StudyInContextKind.REQUIRED -> MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_REQUIRED
        }

        val shortPhrases = word.phrases.filter { it.kana.length <= maxNumCharsInAnswer }
    }

    private val weightAssigner = QAWeightAssigner(kanjiIndex, rounds, stats)

    fun getQA(word: Word, avgLevelOfWords: JLPTLevel, canUseRomaji: Boolean): QuestionAndAnswer? {
        Log.debug(TAG, "Round ${rounds.round}, last trivial @${rounds.lastTrivial}")
        Log.debug(TAG, "Total seen: ${stats.getWordTotalSeenCount(word)}")
        Log.debug(TAG, "Total correct: ${stats.getWordTotalCorrectCount(word)}")
        Log.debug(TAG, "studyInContext=${word.studyInContext}")

        val info = WordInfo(word)

        val kinds = getAvailableKinds(word, info, canUseRomaji).map { kind ->
            KindAndWeight(kind, weightAssigner.getWeight(kind, word, info, avgLevelOfWords, canUseRomaji))
        }

        val notRare = kinds.filter { it.weight > RARE }
        val pickFrom = notRare.ifEmpty { kinds } // we have to pick from RARE kinds when there are no other
        val picked = pickFrom.pickOne()
        val index = picked?.let { getIndexOfNextPhraseOrSentence(word, info, it) } ?: -1

        Log.debug(TAG, "Picked: $picked, index=$index")
        val kindSeenCount = picked?.let { stats.getWordSeenCount(word, it.toStatsKey()) } ?: 0
        val questionHasFurigana = picked?.doesNotAskAnything != true && kindSeenCount < 1
        return picked?.let { QuestionAndAnswer(word, it, index, questionHasFurigana) }
    }

    private fun getIndexOfNextPhraseOrSentence(word: Word, info: WordInfo, kind: QAKind): Int {
        val kindSeenCount = stats.getWordSeenCount(word, kind.toStatsKey())

        // Don't use random values here, because this will be called multiple times
        return when (kind) {
            QAKind.SHOW_PHRASE_ASK_NOTHING -> when {
                word.phrases.isEmpty() -> -1
                else -> kindSeenCount % word.phrases.size
            }
            QAKind.SHOW_SENTENCE_ASK_NOTHING -> when {
                word.sentences.isEmpty() -> -1
                else -> kindSeenCount % word.sentences.size
            }
            QAKind.SHOW_PHRASE_ASK_KANJI -> when {
                info.phrasesUsableForAskKanji.isEmpty() -> -1
                else -> word.phrases.indexOf(
                    info.phrasesUsableForAskKanji[kindSeenCount % info.phrasesUsableForAskKanji.size]
                )
            }
            QAKind.SHOW_SENTENCE_ASK_KANJI -> when {
                info.sentencesUsableForAskKanji.isEmpty() -> -1
                else -> word.sentences.indexOf(
                    info.sentencesUsableForAskKanji[kindSeenCount % info.sentencesUsableForAskKanji.size]
                )
            }
            QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> when {
                info.shortPhrases.isEmpty() -> -1
                else -> word.phrases.indexOf(info.shortPhrases[kindSeenCount % info.shortPhrases.size])
            }
            else -> -1
        }
    }

    private fun getAvailableKinds(word: Word, info: WordInfo, canUseRomaji: Boolean): Set<QAKind> {
        val canUseKanji = word.kanji != word.kana
        val maxNumCharsInAnswer = info.maxNumCharsInAnswer
        val kanjiTooLongForAnswer = word.kanji.length > maxNumCharsInAnswer
        val kanaTooLongForAnswer = word.kana.length > maxNumCharsInAnswer

        val anyPhraseSeen = stats.getWordSeenCount(word, QAKind.SHOW_PHRASE_ASK_NOTHING.toStatsKey()) > 0
        val anySentenceSeen = stats.getWordSeenCount(word, QAKind.SHOW_SENTENCE_ASK_NOTHING.toStatsKey()) > 0

        val hasSeenWord = when {
            isInTestLab -> true // otherwise most kinds would never show up in test lab
            stats.getWordTotalSeenCount(word) > 0 -> true
            else -> false
        }

        return QAKind.entries
            .filter { kind ->
                when (kind) {
                    QAKind.SHOW_KANJI_ASK_KANA -> hasSeenWord && canUseKanji && !kanaTooLongForAnswer
                    QAKind.SHOW_KANA_ASK_KANJI -> hasSeenWord && canUseKanji && !kanjiTooLongForAnswer
                    QAKind.SHOW_ROMAJI_ASK_KANA -> hasSeenWord && canUseRomaji && !kanaTooLongForAnswer
                    QAKind.SHOW_TRANSLATION_ASK_KANA -> hasSeenWord && !kanaTooLongForAnswer
                    QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> {
                        if (!canUseKanji || kanjiTooLongForAnswer) {
                            false
                        } else {
                            val permutations = word.kanji.fold(1) { result, char ->
                                // the kanji itself plus the similar kanjis
                                result * (1 + kanjiIndex.getVisuallySimilarKanjis(char).size)
                            }
                            val canFillChips = permutations >= NUM_CHIPS_WHEN_SIMILAR_KANJIS
                            if (permutations > 1) {
                                Log.debug(
                                    TAG,
                                    "Visually similar permutations: $permutations" +
                                        (if (canFillChips) "" else " (not enough)")
                                )
                            }
                            canFillChips
                        }
                    }
                    QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS -> hasSeenWord && canUseKanji &&
                        !kanjiTooLongForAnswer
                    QAKind.SHOW_WORD_ASK_NOTHING -> !hasSeenWord
                    QAKind.SHOW_PHRASE_ASK_NOTHING -> word.phrases.isNotEmpty()
                    QAKind.SHOW_PHRASE_ASK_KANJI -> anyPhraseSeen && info.phrasesUsableForAskKanji.isNotEmpty()
                    QAKind.SHOW_SENTENCE_ASK_NOTHING -> (anyPhraseSeen || word.phrases.isEmpty()) &&
                        word.sentences.isNotEmpty()
                    QAKind.SHOW_SENTENCE_ASK_KANJI -> anySentenceSeen && info.sentencesUsableForAskKanji.isNotEmpty()
                    QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> anyPhraseSeen && info.shortPhrases.isNotEmpty()
                }
            }
            .toSet()
    }

    companion object {
        private val TAG = Log.Tag("QAPicker")

        // Long answers are tedious, so apply a maximum length. Phrases exceeding this maximum will not be asked as
        // a whole. However, if studyInContext is set, we must make sure enough phrases can actually be used, therefore
        // we use a different maximum.
        const val MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_NOT_REQUIRED = 10
        const val MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_PREFERRED = 13
        const val MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_REQUIRED = 20

        private fun Collection<KindAndWeight>.pickOne(): QAKind? {
            val sum = sumOf { it.weight.toDouble() }
            val r = Random.nextFloat() * sum
            var partialSum = 0.0
            var pick: KindAndWeight? = null

            forEach { kaw ->
                if (pick == null) {
                    if (partialSum + kaw.weight > r) {
                        pick = kaw
                    } else {
                        partialSum += kaw.weight
                    }
                }
            }

            return pick?.kind ?: lastOrNull()?.kind
        }
    }
}
