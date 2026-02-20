package io.github.digorydoo.goigoi.study

import android.content.Context
import android.util.Log
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.stats.Stats
import io.github.digorydoo.goigoi.stats.StatsKey
import kotlin.math.min

class StudyItemIterator private constructor(
    private val list: MutableList<StudyItem>,
    private val listMaintainer: StudyListMaintainer,
    private val myWordsMaintainer: MyWordsMaintainer,
    private val unyt: Unyt?, // null = super progressive mode
    private val vocab: Vocabulary,
    private val howToStudy: HowToStudy,
    private val stats: Stats,
) {
    enum class HowToStudy { WORST_CONTINUOUSLY, EACH_ONCE }

    private var index: Int = 0
    private val curItem get() = list[index]

    val size get() = list.size
    val curWord get() = curItem.word

    private var answer = Answer.NONE
    private var numCorrect = 0
    private var numWrong = 0
    private var streak = 0
    private var currentChanged = false
    private val updateMyWordsUnyt = unyt != vocab.myWordsUnyt

    fun hasNext() = when (howToStudy) {
        HowToStudy.EACH_ONCE -> index < list.size - 1
        HowToStudy.WORST_CONTINUOUSLY -> list.isNotEmpty()
    }

    fun notifyAnswer(statsKey: StatsKey, ans: Answer) {
        require(answer == Answer.NONE) { "Answer already set!" }

        // If unyt is not null, all words we show should come from that unyt.
        // If unyt is null, we need to search for the original unyt, and it may not be loaded.
        val unyt = unyt ?: vocab.findFirstUnytContainingWordWithSameFile(curWord)

        stats.notifyAnswer(curWord, unyt, statsKey, ans)
        answer = ans

        if (BuildConfig.DEBUG) {
            val rating = stats.getWordTotalRating(curWord)
            Log.d(TAG, "Answer $ans for ${curWord.id}, new rating=$rating")
        }

        when (ans) {
            Answer.CORRECT -> {
                numCorrect++
                streak++
                curItem.localNumCorrect++

                if (updateMyWordsUnyt) {
                    myWordsMaintainer.onAnswerCorrect(curWord)
                }

                currentChanged = listMaintainer.onAnswerCorrect(curWord, streak)
            }
            Answer.WRONG -> {
                numWrong++
                streak = 0
                curItem.localNumWrong++

                if (updateMyWordsUnyt) {
                    myWordsMaintainer.onAnswerWrong(curWord)
                }

                listMaintainer.onAnswerWrong()
            }
            Answer.SKIP -> {
                curItem.localNumSkipped++
            }
            Answer.CORRECT_EXCEPT_KANA_SIZE,
            Answer.TRIVIAL,
            Answer.NONE,
            -> Unit
        }
    }

    fun next() {
        when (howToStudy) {
            HowToStudy.EACH_ONCE -> {
                index++
            }
            HowToStudy.WORST_CONTINUOUSLY -> {
                require(index == 0) { "Index is not supposed to change in mode $howToStudy" }

                if (currentChanged) {
                    Log.d(TAG, "Not pushing back the front word, since it has changed")
                    currentChanged = false
                } else {
                    listMaintainer.pushBack(curItem, answer)
                }
            }
        }

        answer = Answer.NONE
        Log.d(TAG, "Streak: $streak")

        if (updateMyWordsUnyt) {
            myWordsMaintainer.onNextWord(curWord)
        }
    }

    fun getSummary(ctx: Context) = when (howToStudy) {
        HowToStudy.EACH_ONCE ->
            ctx.getString(R.string.n_of_m)
                .replace("\${N}", "${index + 1}")
                .replace("\${M}", "$size")
        HowToStudy.WORST_CONTINUOUSLY ->
            ctx.getString(R.string.correct_wrong_counts)
                .replace("\${N}", "$numCorrect")
                .replace("\${M}", "$numWrong")
    }

    fun saveState(outState: StudyItemIteratorState) {
        outState.index = index
        outState.curWordId = list.getOrNull(index)?.word?.id ?: ""
        outState.numCorrect = numCorrect
        outState.numWrong = numWrong
    }

    fun restoreState(state: StudyItemIteratorState) {
        index = state.index.takeIf { it >= 0 && it < list.size } ?: 0

        val curItem = state.curWordId
            .takeIf { it.isNotEmpty() }
            ?.let { wordId -> list.firstOrNull { it.word.id == wordId } }

        if (curItem != null) {
            list.remove(curItem)
            list.add(min(index, list.size), curItem)
        }

        numCorrect = state.numCorrect
        numWrong = state.numWrong
        Log.d(TAG, "Got ${list.size} study items after restore")
    }

    init {
        if (unyt != null) {
            listMaintainer.initForNormalMode(unyt)
        } else {
            listMaintainer.initForSuperProgressiveMode(vocab.myWordsUnyt)
        }

        if (updateMyWordsUnyt) {
            if (unyt == null && vocab.myWordsUnyt.numWordsLoaded < MIN_MY_WORDS_UNYT_SIZE) {
                // We want to ensure a minimal size even if it means filling My Words Unyt with arbitrary new words,
                // because FixedKeysProvider grabs kanji and kana from words in the unyt it is given.

                if (list.size < MIN_MY_WORDS_UNYT_SIZE) {
                    // This shouldn't happen, because listMaintainer should either have filled the list with words from
                    // the past or words from the head.
                    Log.w(TAG, "List has fewer than $MIN_MY_WORDS_UNYT_SIZE words!")
                } else {
                    Log.d(TAG, "My Words Unyt has only ${vocab.myWordsUnyt.numWordsLoaded} words, adding some more")

                    for (studyItem in list.iterator()) {
                        myWordsMaintainer.onNextWord(studyItem.word) // should add word if there's no ambiguity
                        if (vocab.myWordsUnyt.numWordsLoaded >= MIN_MY_WORDS_UNYT_SIZE) break
                    }

                    Log.d(TAG, "My Words Unyt has now ${vocab.myWordsUnyt.numWordsLoaded} words")
                }
            }

            myWordsMaintainer.onNextWord(curWord)
        }
    }

    companion object {
        private const val TAG = "StudyItemIterator"
        private const val MIN_MY_WORDS_UNYT_SIZE = 5

        fun create(
            unyt: Unyt?, // null = super progressive mode
            howToStudy: HowToStudy,
            ctx: Context,
        ): StudyItemIterator {
            val vocab = Vocabulary.getSingleton(ctx)
            val stats = Stats.getSingleton(ctx)
            val list = mutableListOf<StudyItem>()

            val myWordsMaintainer = MyWordsMaintainer(
                delegate = object: MyWordsMaintainer.Delegate {
                    override fun getWordStudyProgress(word: Word) = stats.getWordStudyProgress(word)
                    override fun getWordTotalRating(word: Word) = stats.getWordTotalRating(word)
                },
                myWordsUnyt = vocab.myWordsUnyt,
            )

            val pool = StudyItemPool(
                delegate = object: StudyItemPool.Delegate {
                    override fun loadWordFile(filename: String) = vocab.loadWordFile(filename, ctx)

                    override fun wouldCauseAmbiguityWithMyWordsUnyt(word: Word) =
                        myWordsMaintainer.wouldCauseAmbiguity(word)
                },
                vocab.allWordFilenames,
                stats
            )

            val listMaintainer = StudyListMaintainer(
                delegate = object: StudyListMaintainer.Delegate {
                    override fun loadWordFile(filename: String) =
                        vocab.loadWordFile(filename, ctx)

                    override fun wouldCauseAmbiguityWithMyWordsUnyt(word: Word) =
                        myWordsMaintainer.wouldCauseAmbiguity(word)
                },
                list,
                pool,
                superProgressive = unyt == null,
                stats
            )

            return StudyItemIterator(list, listMaintainer, myWordsMaintainer, unyt, vocab, howToStudy, stats)
        }
    }
}
