package io.github.digorydoo.goigoi.activity.prog_study.keyboard

import android.util.Log
import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.cjk.Unicode
import ch.digorydoo.kutils.cjk.isHiragana
import ch.digorydoo.kutils.cjk.isKatakana
import io.github.digorydoo.goigoi.activity.prog_study.QAKind
import io.github.digorydoo.goigoi.activity.prog_study.QuestionAndAnswer
import io.github.digorydoo.goigoi.db.KanjiIndex
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.utils.StringUtils.getRandomSubset
import kotlin.math.abs

class FixedKeysProvider(
    private val qa: QuestionAndAnswer,
    private val unytToTakeWordsFrom: Unyt, // will be set to myWordsUnyt in super progressive mode
    private val kanjiIndex: KanjiIndex,
) {
    fun get() = when {
        qa.kind == QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR -> getWordWithIncorrectKanjis()
        qa.presentWholeWords -> getWords()
        else -> getChars()
    }

    private fun getWordWithIncorrectKanjis(): List<String> {
        var permutations = 1
        val primaryAnswer = qa.answers.firstOrNull() ?: throw Exception("qa.answers is empty")

        val variants: List<Set<Char>> = primaryAnswer.map { char ->
            val dontConfuse = kanjiIndex.getVisuallySimilarKanjis(char)

            if (dontConfuse.isEmpty()) {
                setOf(char) // a kanji for which we don't know any visually similar kanjis, or okurigana
            } else {
                require(!dontConfuse.contains(char)) { "Kanji $char appears in set as visually similar to itself" }
                val chooseFrom = dontConfuse.toMutableSet().also { it.add(char) }
                permutations *= chooseFrom.size
                chooseFrom
            }
        }

        require(permutations >= NUM_CHIPS_WHEN_SIMILAR_KANJIS) {
            "Kind ${qa.kind} was set, but only $permutations permutation(s) are available"
        }

        val allPermutations = getAllPermutations(variants)
        Log.d(TAG, "allPermutations: ${allPermutations.joinToString(", ")}")

        require(allPermutations.size == permutations) { "Unexpected permutation count: Expected $permutations" }

        return allPermutations
            .filter { permutation -> qa.answers.none { it == permutation } }
            .shuffled()
            .take(NUM_CHIPS_WHEN_SIMILAR_KANJIS - 1)
            .toMutableList()
            .also { it.add(primaryAnswer) }
            .shuffled()
    }

    private fun getAllPermutations(variants: List<Set<Char>>, startIdx: Int = 0, prefix: String = ""): List<String> {
        require(startIdx < variants.size) { "startIdx out of bounds: $startIdx >= ${variants.size}" }
        val result = mutableListOf<String>()

        variants[startIdx].forEach { char ->
            if (startIdx + 1 >= variants.size) {
                result.add("$prefix$char")
            } else {
                result.addAll(getAllPermutations(variants, startIdx + 1, "$prefix$char"))
            }
        }

        return result
    }

    private fun getWords(): List<String> {
        val result = mutableSetOf<String>()
        val enough = { result.size >= MIN_NUM_CHIPS_WHEN_WORDS * 2 }
        val primaryAnswer = qa.answers.firstOrNull() ?: throw Exception("qa.answers is empty")
        val addKana = primaryAnswer.isHiragana() || primaryAnswer.isKatakana()

        val checkAndAdd = { w: Word, requireSameLength: Boolean ->
            if (w == qa.word) {
                false
            } else {
                val toBeAdded = if (addKana) w.kana else w.kanji

                if (requireSameLength && toBeAdded.length != primaryAnswer.length) {
                    false
                } else {
                    result.add(toBeAdded)
                }
            }
        }

        for (i in 0 .. 1) {
            val requireSameLength = i == 0

            // Try adding words with the same hint
            qa.word.hintsWithSystemLang
                .takeIf { it.isNotEmpty() }
                .let { hintOfQ ->
                    var numAdded = 0
                    unytToTakeWordsFrom.forEachWord { w ->
                        if (w.hintsWithSystemLang == hintOfQ && checkAndAdd(w, requireSameLength)) {
                            numAdded++
                        }
                    }
                    if (numAdded > 0) {
                        Log.d(TAG, "Added $numAdded words with hint $hintOfQ, sameLen=$requireSameLength")
                    }
                }

            if (enough()) break

            // Try adding words with the same categories
            qa.word.cats
                .takeIf { it.isNotEmpty() }
                ?.joinToString(",")
                ?.let { catsOfQ ->
                    var numAdded = 0
                    unytToTakeWordsFrom.forEachWord { w ->
                        if (w.cats.joinToString(",") == catsOfQ && checkAndAdd(w, requireSameLength)) {
                            numAdded++
                        }
                    }
                    if (numAdded > 0) {
                        Log.d(TAG, "Added $numAdded words with categories $catsOfQ, sameLen=$requireSameLength")
                    }
                }

            if (enough()) break
        }

        if (!enough()) {
            // When there are too few specific words, we completely replace the list with arbitrary words, because
            // just filling the gaps would show the few specific ones too often, making it too easy.
            result.clear()
            Log.d(TAG, "Got too few specific words, starting over again")

            for (i in 0 .. 1) {
                val requireSameLength = i == 0
                var numAdded = 0

                unytToTakeWordsFrom.forEachWord { w ->
                    if (checkAndAdd(w, requireSameLength)) {
                        numAdded++
                    }
                }

                if (numAdded > 0) {
                    Log.d(TAG, "Added $numAdded arbitrary words, sameLen=$requireSameLength")
                }

                if (enough()) break
            }
        }

        return result
            .shuffled()
            .take(MIN_NUM_CHIPS_WHEN_WORDS - 1)
            .toMutableList()
            .also { it.add(primaryAnswer) }
            .shuffled()
    }

    private fun getChars(): List<String> {
        val primaryAnswer = qa.answers.firstOrNull() ?: throw Exception("qa.answers is empty")

        val chars = primaryAnswer.map { it }.toMutableSet()

        // Answers can get too ambiguous if we add more chars when asking full phrase
        if (qa.kind != QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA) {
            addRandomChars(chars)
        }

        // Shuffle the keys in a way that avoids revealing parts of the answer
        return chars.map { it.toString() }.shuffledWhileAvoiding(qa.answers)
    }

    private fun addRandomChars(to: MutableSet<Char>) {
        val numToAdd = MIN_NUM_CHIPS_WHEN_CHARS - to.size

        if (numToAdd <= 0) {
            return
        }

        to.addAll(
            when {
                qa.kind.asksForKanji -> getRandomKanjis(numToAdd, except = to)
                qa.kind.asksForKana -> getRandomKana(numToAdd, except = to)
                else -> getRandomRomaji(numToAdd, except = to)
            }
        )
    }

    private fun getRandomKanjis(desiredCount: Int, except: Set<Char>): Set<Char> {
        // We want to avoid adding any kanjis that have the same readings as the ones from our question!
        val avoidReadings = qa.word.primaryForm.readings
            .map { it.kana }
            .toMutableList()
            .also { it.add(qa.word.kana) } // avoid kanji 湖 having reading みずうみ should we ask for 水、海
            .toList()

        return kanjiIndex.getRandomKanjisOfLevel(
            qa.word.level ?: JLPTLevel.N3,
            desiredCount,
            except,
            avoidReadings,
        )
    }

    private fun getRandomKana(desiredCount: Int, except: Set<Char>) =
        if (qa.word.kana.isHiragana()) {
            kanjiIndex.getRandomHiragana(desiredCount, except)
        } else {
            kanjiIndex.getRandomKatakana(desiredCount, except)
        }

    private fun getRandomRomaji(desiredCount: Int, except: Set<Char>) =
        getRandomSubset(
            Unicode.ANSI_LOWERCASE_LETTERS,
            desiredCount,
            except,
            ignoreCase = true
        )

    companion object {
        private const val TAG = "FixedKeysProvider"
        private const val MIN_NUM_CHIPS_WHEN_CHARS = 10
        private const val MIN_NUM_CHIPS_WHEN_WORDS = 5
        const val NUM_CHIPS_WHEN_SIMILAR_KANJIS = 4

        private fun List<String>.shuffledWhileAvoiding(avoid: List<String>): List<String> {
            if (isEmpty()) return listOf()
            if (size == 1) return listOf(first())

            var result: List<String>? = null
            var score = 0.0f // higher score means adjacent characters in `avoid` are further apart in `result`
            var attempt = 1

            do {
                val newResult = shuffled()
                var newScore = 0.0f

                avoid.forEach {
                    (0 ..< newResult.size - 1).forEach { idx ->
                        val c1 = newResult[idx]
                        val c2 = newResult[idx + 1]

                        val foundC1At = it.indexOf(c1)
                        val foundC2At = it.indexOf(c2)

                        // To prevent zigzag patterns from appearing too frequently, we treat a distance larger than
                        // two the same as if one of the chars was unique.
                        if (foundC1At >= 0 && foundC2At >= 0 && abs(foundC1At - foundC2At) <= 2) {
                            if (foundC1At < foundC2At) {
                                newScore += foundC2At - foundC1At
                            } else {
                                newScore += 1.1f * (foundC1At - foundC2At)
                            }
                        } else {
                            newScore += it.length * 4.2f
                        }
                    }
                }

                if (result == null || newScore > score) {
                    result = newResult
                    score = newScore
                }
            } while (++attempt <= 6)

            return result
        }
    }
}
