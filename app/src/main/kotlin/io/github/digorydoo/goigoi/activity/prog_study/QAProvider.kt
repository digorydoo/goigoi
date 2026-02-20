package io.github.digorydoo.goigoi.activity.prog_study

import android.content.Context
import android.util.Log
import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.string.lpad
import ch.digorydoo.kutils.string.rpad
import ch.digorydoo.kutils.string.toPrecision
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.FixedKeysProvider.Companion.NUM_CHIPS_WHEN_SIMILAR_KANJIS
import io.github.digorydoo.goigoi.core.StudyInContextKind
import io.github.digorydoo.goigoi.db.KanjiIndex
import io.github.digorydoo.goigoi.db.Phrase
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.stats.Stats
import io.github.digorydoo.goigoi.study.Answer
import io.github.digorydoo.goigoi.study.StudyItemIterator
import kotlin.math.max
import kotlin.random.Random

class QAProvider(
    private val delegate: Delegate,
    private val stats: Stats,
    private val kanjiIndex: KanjiIndex,
) {
    interface Delegate {
        val isInTestLab: Boolean
        val canUseRomaji: Boolean
        val averageLevelOfWords: JLPTLevel
        fun createIterator(): StudyItemIterator
        fun ranOutOfWords()
    }

    class NoQAAvailableError: Exception()

    private class KindAndWeight(val kind: QAKind, val weight: Float) {
        override fun toString() =
            "{ kind = $kind, weight = $weight }"
    }

    private var iterator: StudyItemIterator? = null
    private val rounds = RoundsTracker()

    private var _qa: QuestionAndAnswer? = null
    val qa get() = _qa!!

    fun start(state: ProgStudyState?): Boolean {
        require(iterator == null) { "iterator has already been created!" }

        val iterator = delegate.createIterator()
        this.iterator = iterator

        if (state == null) {
            return pickKindOrChangeWord()
        }

        iterator.restoreState(state.studyItemIteratorState)
        rounds.restoreState(state)
        _qa = QuestionAndAnswer(iterator.curWord, state.qaKind, state.qaIndex, state.questionHasFurigana)
        return true
    }

    fun saveState(outState: ProgStudyState) {
        iterator?.saveState(outState.studyItemIteratorState)
        rounds.saveState(outState)
        outState.qaKind = _qa?.kind ?: QAKind.SHOW_KANJI_ASK_KANA
        outState.qaIndex = _qa?.index ?: 0
        outState.questionHasFurigana = _qa?.questionHasFurigana ?: false
    }

    fun next() {
        val iterator = iterator ?: throw NoQAAvailableError()

        if (!iterator.hasNext()) {
            this.iterator = delegate.createIterator()
            delegate.ranOutOfWords() // shows a snack-bar
        } else {
            iterator.next()
        }

        if (!pickKindOrChangeWord()) {
            throw NoQAAvailableError()
        }
    }

    fun getSummary(ctx: Context) =
        iterator?.getSummary(ctx) ?: ""

    fun notifyAnswer(answer: Answer) {
        iterator?.notifyAnswer(qa.kind.toStatsKey(), answer)
    }

    private fun pickKindOrChangeWord(): Boolean {
        val iterator = iterator ?: return false

        @Suppress("unused")
        for (i in 0 ..< 10) {
            val word = iterator.curWord

            _qa = pickAvailableKind(
                word,
                phrasesWithWordRemoved = word.phrases.filter { it.canRemoveWordFromPrimaryForm(word) },
                sentencesWithWordRemoved = word.sentences.filter { it.canRemoveWordFromPrimaryForm(word) },
                shortPhrases = word.phrases.filter { it.kana.length <= MAX_NUM_CHARS_IN_ANSWER },
            )

            if (_qa != null) break

            Log.w(TAG, "QAProvider found no available kind for word ${iterator.curWord.id}")
            if (!iterator.hasNext()) break
            iterator.next()
        }

        _qa?.let { rounds.aboutToShow(it.word, it.kind) }
        return _qa != null
    }

    private fun pickAvailableKind(
        word: Word,
        phrasesWithWordRemoved: List<Phrase>,
        sentencesWithWordRemoved: List<Phrase>,
        shortPhrases: List<Phrase>,
    ): QuestionAndAnswer? {
        Log.d(TAG, "Round ${rounds.round}, last trivial @${rounds.lastTrivial}")
        Log.d(TAG, "Total seen: ${stats.getWordTotalSeenCount(word)}")
        Log.d(TAG, "Total correct: ${stats.getWordTotalCorrectCount(word)}")
        Log.d(TAG, "studyInContext=${word.studyInContext}")

        val available = getAvailableKinds(word, phrasesWithWordRemoved, sentencesWithWordRemoved, shortPhrases)
            .map { KindAndWeight(it, getWeight(it, word, phrasesWithWordRemoved, sentencesWithWordRemoved)) }

        val notRare = available.filter { it.weight > RARE }
        val pickFrom = notRare.ifEmpty { available } // we have to pick from RARE kinds when there are no other
        val picked = pickFrom.pickOne()

        val index = picked?.let {
            getIndexOfNextPhraseOrSentence(word, phrasesWithWordRemoved, sentencesWithWordRemoved, shortPhrases, it)
        } ?: -1

        Log.d(TAG, "Picked: $picked, index=$index")
        val kindSeenCount = picked?.let { stats.getWordSeenCount(word, it.toStatsKey()) } ?: 0
        val questionHasFurigana = picked?.doesNotAskAnything != true && kindSeenCount < 1
        return picked?.let { QuestionAndAnswer(word, it, index, questionHasFurigana) }
    }

    private fun getIndexOfNextPhraseOrSentence(
        word: Word,
        phrasesWithWordRemoved: List<Phrase>,
        sentencesWithWordRemoved: List<Phrase>,
        shortPhrases: List<Phrase>,
        kind: QAKind,
    ): Int {
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
                phrasesWithWordRemoved.isEmpty() -> -1
                else -> word.phrases.indexOf(phrasesWithWordRemoved[kindSeenCount % phrasesWithWordRemoved.size])
            }
            QAKind.SHOW_SENTENCE_ASK_KANJI -> when {
                sentencesWithWordRemoved.isEmpty() -> -1
                else -> word.sentences.indexOf(sentencesWithWordRemoved[kindSeenCount % sentencesWithWordRemoved.size])
            }
            QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> when {
                shortPhrases.isEmpty() -> -1
                else -> word.phrases.indexOf(shortPhrases[kindSeenCount % shortPhrases.size])
            }
            else -> -1
        }
    }

    private fun getAvailableKinds(
        word: Word,
        phrasesWithWordRemoved: List<Phrase>,
        sentencesWithWordRemoved: List<Phrase>,
        shortPhrases: List<Phrase>,
    ): Set<QAKind> {
        val canUseKanji = word.kanji != word.kana
        val kanjiTooLongForAnswer = word.kanji.length > MAX_NUM_CHARS_IN_ANSWER
        val kanaTooLongForAnswer = word.kana.length > MAX_NUM_CHARS_IN_ANSWER
        val anyPhraseSeen = stats.getWordSeenCount(word, QAKind.SHOW_PHRASE_ASK_NOTHING.toStatsKey()) > 0
        val anySentenceSeen = stats.getWordSeenCount(word, QAKind.SHOW_SENTENCE_ASK_NOTHING.toStatsKey()) > 0

        val hasSeenWord = when {
            delegate.isInTestLab -> true // otherwise most kinds would never show up in Google Play Console
            stats.getWordTotalSeenCount(word) > 0 -> true
            else -> false
        }

        return QAKind.entries
            .filter { kind ->
                when (kind) {
                    QAKind.SHOW_KANJI_ASK_KANA -> hasSeenWord && canUseKanji && !kanaTooLongForAnswer
                    QAKind.SHOW_KANA_ASK_KANJI -> hasSeenWord && canUseKanji && !kanjiTooLongForAnswer
                    QAKind.SHOW_ROMAJI_ASK_KANA -> hasSeenWord && delegate.canUseRomaji && !kanaTooLongForAnswer
                    QAKind.SHOW_TRANSLATION_ASK_KANA -> hasSeenWord && !kanaTooLongForAnswer
                    QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> {
                        if (!canUseKanji || kanjiTooLongForAnswer) {
                            false
                        } else {
                            val permutations = word.kanji.fold(1) { result, char ->
                                result * (1 + kanjiIndex.getVisuallySimilarKanjis(char).size) // the kanji itself plus the similar kanjis
                            }
                            val canFillChips = permutations >= NUM_CHIPS_WHEN_SIMILAR_KANJIS
                            if (permutations > 1) {
                                Log.d(
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
                    QAKind.SHOW_PHRASE_ASK_KANJI -> anyPhraseSeen && phrasesWithWordRemoved.isNotEmpty()
                    QAKind.SHOW_SENTENCE_ASK_NOTHING -> (anyPhraseSeen || word.phrases.isEmpty()) &&
                        word.sentences.isNotEmpty()
                    QAKind.SHOW_SENTENCE_ASK_KANJI -> anySentenceSeen && sentencesWithWordRemoved.isNotEmpty()
                    QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> anyPhraseSeen && shortPhrases.isNotEmpty()
                }
            }
            .toSet()
    }

    private fun getWeightBasedOnRating(kind: QAKind, word: Word): Float {
        val statsKey = kind.toStatsKey()
        val kindSeenCount = stats.getWordSeenCount(word, statsKey)

        if (kind.doesNotAskAnything) {
            // The app can get stuck if a word's rating forces it to choose from kinds that do not ask anything,
            // because then the word can no longer progress. Therefore, make sure these kinds make room for others.
            val n = when {
                kind.involvesPhrases -> word.phrases.size
                kind.involvesSentences -> word.sentences.size
                else -> 0
            }
            return (1.0f + n) / (kindSeenCount + 1.0f + n)
        }

        val rating = when {
            kindSeenCount < 3 -> 1.0f // rating is not significant unless kind was seen a couple of times
            else -> {
                val kindRating = stats.getWordRating(word, statsKey)
                when {
                    kindRating >= 0.8f -> when {
                        kindSeenCount < 5 -> 0.2f // a bit more often than SOMETIMES, because rating is not significant
                        else -> SOMETIMES // seldom, but not RARE
                    }
                    else -> 1.0f - kindRating // will be between 0.2f (good rating = seldom) .. 1.0f (worst rating)
                }
            }
        }
        return when {
            kind.involvesPhrasesOrSentences -> 1.0f - 0.5f * (1.0f - rating) // rating affects several phrases
            else -> rating
        }
    }

    private fun getWeightBasedOnKanji(kind: QAKind, word: Word): Float {
        if (!kind.shouldUseWeightBasedOnKanji || kind.doesNotAskAnything) {
            return 1.0f
        }

        val kanjiLevel = (kanjiIndex.levelOfMostDifficultKanji(word.kanji) ?: JLPTLevel.N5).toInt()
        val avgLevel = delegate.averageLevelOfWords.toInt()
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
        phrasesWithWordRemoved: List<Phrase>,
        sentencesWithWordRemoved: List<Phrase>,
    ): Float {
        val totalCorrectCount = stats.getWordTotalCorrectCount(word)
        val canUseRomaji = delegate.canUseRomaji
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
                when (word.studyInContext) {
                    StudyInContextKind.PREFERRED, StudyInContextKind.REQUIRED -> when {
                        seenCount >= word.phrases.size -> ALMOST_NEVER // prefer SHOW_PHRASE_ASK_KANJI after one round
                        rounds.since(rounds.lastTrivial) <= 5 -> 0.5f // try to ask something non-trivial first
                        else -> 1.0f
                    }
                    StudyInContextKind.NOT_REQUIRED -> when {
                        totalCorrectCount < 1 -> ALMOST_NEVER
                        seenCount >= word.phrases.size && !word.usuallyInKana -> ALMOST_NEVER // prefer ASK_KANJI
                        seenCount >= word.phrases.size && word.usuallyInKana -> SOMETIMES // ASK_KANJI not available
                        rounds.since(roundOfWordAndKind) < 20 -> RARE // hold back next phrase
                        rounds.since(rounds.lastTrivial) <= 1 -> RARE // don't ask nothing twice in a row
                        rounds.since(rounds.lastTrivial) <= 5 -> SOMETIMES // still a bit early
                        seenCount > 0 -> 0.5f // some phrases seen
                        else -> 1.0f // the first phrase should be shown early
                    }
                }
            }
            QAKind.SHOW_SENTENCE_ASK_NOTHING -> {
                val seenCount = stats.getWordSeenCount(word, kind.toStatsKey())
                when (word.studyInContext) {
                    StudyInContextKind.PREFERRED, StudyInContextKind.REQUIRED -> when {
                        seenCount >= word.sentences.size -> ALMOST_NEVER // prefer ASK_KANJI after one round
                        rounds.since(rounds.lastTrivial) <= 5 -> 0.5f // try to ask something non-trivial first
                        else -> 1.0f
                    }
                    StudyInContextKind.NOT_REQUIRED -> when {
                        seenCount >= word.sentences.size && !word.usuallyInKana -> ALMOST_NEVER // prefer ASK_KANJI
                        seenCount >= word.sentences.size && word.usuallyInKana -> SOMETIMES // ASK_KANJI not available
                        rounds.since(roundOfWordAndKind) < 20 -> RARE // hold back next sentence
                        rounds.since(rounds.lastTrivial) <= 1 -> RARE // don't ask nothing twice in a row
                        rounds.since(rounds.lastTrivial) <= 5 -> SOMETIMES // still a bit early
                        totalCorrectCount < 1 -> SOMETIMES
                        else -> 0.5f
                    }
                }
            }
            QAKind.SHOW_PHRASE_ASK_KANJI -> {
                val askNothingCount = stats.getWordSeenCount(word, QAKind.SHOW_PHRASE_ASK_NOTHING.toStatsKey())
                val thisKindCount = stats.getWordSeenCount(word, kind.toStatsKey())
                val sentenceKindCount = stats.getWordSeenCount(word, QAKind.SHOW_SENTENCE_ASK_KANJI.toStatsKey())
                val repeatingPhrases = thisKindCount >= phrasesWithWordRemoved.size
                val repeatingSentences = sentenceKindCount >= sentencesWithWordRemoved.size
                val shouldAskNothingFirst = askNothingCount < word.phrases.size && askNothingCount <= thisKindCount
                when (word.studyInContext) {
                    StudyInContextKind.PREFERRED, StudyInContextKind.REQUIRED -> when {
                        shouldAskNothingFirst -> RARE
                        repeatingPhrases -> when {
                            sentencesWithWordRemoved.isEmpty() || repeatingSentences -> 0.5f
                            else -> RARE
                        }
                        else -> 1.0f
                    }
                    StudyInContextKind.NOT_REQUIRED -> when {
                        shouldAskNothingFirst -> RARE
                        repeatingPhrases -> when {
                            sentencesWithWordRemoved.isEmpty() || repeatingSentences -> 0.3f
                            else -> RARE
                        }
                        else -> 1.0f
                    }
                }
            }
            QAKind.SHOW_SENTENCE_ASK_KANJI -> {
                val askNothingCount = stats.getWordSeenCount(word, QAKind.SHOW_SENTENCE_ASK_NOTHING.toStatsKey())
                val thisKindCount = stats.getWordSeenCount(word, kind.toStatsKey())
                val phraseKindCount = stats.getWordSeenCount(word, QAKind.SHOW_PHRASE_ASK_KANJI.toStatsKey())
                val repeatingSentences = thisKindCount >= sentencesWithWordRemoved.size
                val repeatingPhrases = phraseKindCount >= phrasesWithWordRemoved.size
                val shouldAskNothingFirst = askNothingCount < word.sentences.size && askNothingCount <= thisKindCount
                when (word.studyInContext) {
                    StudyInContextKind.PREFERRED, StudyInContextKind.REQUIRED -> when {
                        shouldAskNothingFirst -> RARE
                        repeatingSentences -> when {
                            phrasesWithWordRemoved.isEmpty() || repeatingPhrases -> 0.5f
                            else -> JUST_ABOVE_RARE
                        }
                        else -> 1.0f
                    }
                    StudyInContextKind.NOT_REQUIRED -> when {
                        shouldAskNothingFirst -> RARE
                        repeatingSentences -> when {
                            phrasesWithWordRemoved.isEmpty() || repeatingPhrases -> 0.3f
                            else -> RARE
                        }
                        else -> 1.0f
                    }
                }
            }
            QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA -> {
                val askNothingCount = stats.getWordSeenCount(word, QAKind.SHOW_PHRASE_ASK_NOTHING.toStatsKey())
                val askKanjiCount = stats.getWordSeenCount(word, QAKind.SHOW_PHRASE_ASK_KANJI.toStatsKey())
                val thisKindCount = stats.getWordSeenCount(word, kind.toStatsKey())
                val sentenceKindCount = stats.getWordSeenCount(word, QAKind.SHOW_SENTENCE_ASK_KANJI.toStatsKey())
                val repeatingPhrases = thisKindCount >= word.phrases.size // word not removed with this kind
                val repeatingSentences = sentenceKindCount >= sentencesWithWordRemoved.size
                val shouldAskNothingFirst = askNothingCount < word.phrases.size && askNothingCount <= thisKindCount
                val kanjiLevel = kanjiIndex.levelOfMostDifficultKanji(word.kanji) ?: JLPTLevel.Nx

                val shouldAskKanjiFirst = !word.usuallyInKana && word.kanji != word.kana &&
                    askKanjiCount < word.phrases.size && askKanjiCount <= thisKindCount &&
                    phrasesWithWordRemoved.isNotEmpty() && // ask kanji is based on phrasesWithWordRemoved
                    !kanjiLevel.isMoreDifficultThan(word.level ?: JLPTLevel.Nx)

                when (word.studyInContext) {
                    StudyInContextKind.PREFERRED, StudyInContextKind.REQUIRED -> when {
                        shouldAskNothingFirst -> RARE
                        shouldAskKanjiFirst -> RARE // ask kanji only asks for word, this kind asks entire phrase
                        repeatingPhrases -> when {
                            sentencesWithWordRemoved.isEmpty() || repeatingSentences -> 0.5f
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
                            sentencesWithWordRemoved.isEmpty() || repeatingSentences -> 0.3f
                            else -> RARE
                        }
                        else -> 1.0f
                    }
                }
            }
        }
    }

    private fun getWeight(
        kind: QAKind,
        word: Word,
        phrasesAvailableForAskKanji: List<Phrase>,
        sentencesAvailableForAskKanji: List<Phrase>,
    ): Float {
        val roundOfWordAndKind = rounds.of(word, kind)
        val wrating = getWeightBasedOnRating(kind, word)
        val wkanji = getWeightBasedOnKanji(kind, word)

        val wkind = getWeightBasedOnKind(
            kind,
            word,
            roundOfWordAndKind,
            phrasesAvailableForAskKanji,
            sentencesAvailableForAskKanji
        )

        val w = max(ALMOST_NEVER, wrating * wkanji * wkind)
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

        Log.d(
            TAG,
            arrayOf(
                rpad(w.percent(), 7),
                rpad("$kind", 40),
                "seen $seen",
                "round ${lpad("$roundOfWordAndKind", 4)}",
                "ratg ${wrating.percent()}",
                "kanji ${wkanji.percent()}",
                "kind ${wkind.percent()}",
            ).joinToString(" ")
        )
        return w
    }

    companion object {
        private const val TAG = "QAProvider"
        private const val MAX_NUM_CHARS_IN_ANSWER = 10 // keep this in sync with CheckGoigoiWord
        private const val SOMETIMES = 0.05f // seldom, but not RARE
        private const val JUST_ABOVE_RARE = 0.005f // between RARE and SOMETIMES
        private const val RARE = 0.001f // likelihood when a QAKind is only to be shown if it's the only one available
        private const val ALMOST_NEVER = RARE / 1000 // different from RARE only if all weights are <= RARE

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
