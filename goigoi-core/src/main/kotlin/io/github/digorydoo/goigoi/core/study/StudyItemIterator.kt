package io.github.digorydoo.goigoi.core.study

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.core.stats.Stats
import io.github.digorydoo.goigoi.core.stats.StatsKey

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

    var index: Int = 0; private set
    private val curItem get() = list[index]

    val size get() = list.size
    val curWord get() = curItem.word
    val curWordOrNull get() = list.getOrNull(index)?.word

    private var answer = Answer.NONE
    var numCorrect = 0; private set
    var numWrong = 0; private set
    private var currentChanged = false
    private val updateMyWordsUnyt = unyt != vocab.myWordsUnyt

    fun set(newIndex: Int, curWordId: String, newNumCorrect: Int, newNumWrong: Int) {
        index = if (newIndex in 0 ..< list.size) newIndex else 0

        val curItem = curWordId
            .takeIf { it.isNotEmpty() }
            ?.let { wordId -> list.firstOrNull { it.word.id == wordId } }

        if (curItem != null) {
            list.remove(curItem)
            list.add(index, curItem)
        }

        numCorrect = newNumCorrect
        numWrong = newNumWrong
        Log.debug(TAG, "StudyItemIterator was set to index=$newIndex, size=$size")
    }

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

        // FIXME Should only be logged in BuildConfig.DEBUG, but BuildConfig is currently unavailable
        val rating = stats.getWordTotalRating(curWord)
        Log.debug(TAG, "Answer $ans for ${curWord.id}, new rating=$rating")

        when (ans) {
            Answer.CORRECT -> {
                numCorrect++
                curItem.localNumCorrect++

                if (updateMyWordsUnyt) {
                    myWordsMaintainer.onAnswerCorrect(curWord)
                }

                currentChanged = listMaintainer.onAnswerCorrect(curWord)
            }
            Answer.WRONG -> {
                numWrong++
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
                    Log.debug(TAG, "Not pushing back the front word, since it has changed")
                    currentChanged = false
                } else {
                    listMaintainer.pushBack(curItem, answer)
                }
            }
        }

        answer = Answer.NONE

        if (updateMyWordsUnyt) {
            myWordsMaintainer.onNextWord(curWord)
        }
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
                    Log.warn(TAG, "List has fewer than $MIN_MY_WORDS_UNYT_SIZE words!")
                } else {
                    Log.debug(TAG, "My Words has only ${vocab.myWordsUnyt.numWordsLoaded} words, adding some more")

                    for (studyItem in list.iterator()) {
                        myWordsMaintainer.onNextWord(studyItem.word) // should add word if there's no ambiguity
                        if (vocab.myWordsUnyt.numWordsLoaded >= MIN_MY_WORDS_UNYT_SIZE) break
                    }

                    Log.debug(TAG, "My Words has now ${vocab.myWordsUnyt.numWordsLoaded} words")
                }
            }

            myWordsMaintainer.onNextWord(curWord)
        }
    }

    companion object {
        private val TAG = Log.Tag("StudyItemIterator")
        private const val MIN_MY_WORDS_UNYT_SIZE = 5

        fun create(
            vocab: Vocabulary,
            stats: Stats,
            unyt: Unyt?, // null = super progressive mode
            howToStudy: HowToStudy,
        ): StudyItemIterator {
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
                    override fun loadWordFile(filename: String) = vocab.loadWordFile(filename)

                    override fun wouldCauseAmbiguityWithMyWordsUnyt(word: Word) =
                        myWordsMaintainer.wouldCauseAmbiguity(word)
                },
                vocab.allWordFilenames,
                stats
            )

            val listMaintainer = StudyListMaintainer(
                delegate = object: StudyListMaintainer.Delegate {
                    override fun loadWordFile(filename: String) =
                        vocab.loadWordFile(filename)

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
