package io.github.digorydoo.goigoi.compiler

import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.cjk.hasDigitOfAnyForm
import ch.digorydoo.kutils.cjk.isCJKNotKana
import ch.digorydoo.kutils.cjk.isHiragana
import ch.digorydoo.kutils.cjk.isKatakana
import ch.digorydoo.kutils.collections.ValueAndWeight
import ch.digorydoo.kutils.collections.weightedAverageOrNull
import ch.digorydoo.kutils.math.lerp
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiUnyt
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWordLink
import io.github.digorydoo.goigoi.core.db.StudyInContextKind
import io.github.digorydoo.goigoi.core.db.WordCategory
import io.github.digorydoo.goigoi.core.db.WordHint
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class WordSorter(private val vocab: GoigoiVocab) {
    private class WordWithSortIndex(
        val word: GoigoiWord,
        val unyt: GoigoiUnyt,
        val unytIdx: Int,
        val wordWithinUnytIdx: Int,
    ) {
        var sortIndex = 0.0f
    }

    private class UnytWithPrefixSuffix(val unyt: GoigoiUnyt, val prefix: String, val suffixIdx: Int)

    fun getSortedWords(): List<GoigoiWord> {
        val words = mutableListOf<WordWithSortIndex>()
        val unyts = mutableListOf<UnytWithPrefixSuffix>()
        var totalNumVisibleUnyts = 0
        val mapUnytNamePrefixToCount = mutableMapOf<String, Int>()

        for (topic in vocab.topics) {
            if (!topic.hidden) {
                for (unyt in topic.unyts) {
                    if (!unyt.hidden) {
                        var wordWithinUnytIdx = 0
                        unyt.forEachVisibleWord { word, _ ->
                            words.add(WordWithSortIndex(word, unyt, totalNumVisibleUnyts, wordWithinUnytIdx++))
                        }
                        totalNumVisibleUnyts++

                        val suffixIdx = UNYT_NUMBER_SUFFIXES.indexOfFirst { suffix ->
                            unyt.name.en.endsWith(suffix)
                        }

                        if (suffixIdx >= 0) {
                            val suffix = UNYT_NUMBER_SUFFIXES[suffixIdx]
                            val prefix = unyt.name.en.take(unyt.name.en.length - suffix.length).trim()
                            mapUnytNamePrefixToCount[prefix] = 1 + (mapUnytNamePrefixToCount[prefix] ?: 0)
                            unyts.add(UnytWithPrefixSuffix(unyt, prefix, suffixIdx))
                        }
                    }
                }
            }
        }

        val mapUnytToSuffixWeight = mutableMapOf<GoigoiUnyt, Double>()

        unyts.forEach { wrapper ->
            val count = mapUnytNamePrefixToCount[wrapper.prefix]!! // entry must exist, otherwise something's odd
            val weight = wrapper.suffixIdx.toDouble() / max(1, count - 1)
            require(weight in 0.0f .. 1.0f)
            mapUnytToSuffixWeight[wrapper.unyt] = weight
        }

        words.forEach { wrapper ->
            val w = wrapper.word
            val unytSuffixWeight = mapUnytToSuffixWeight[wrapper.unyt] ?: 0.5 // push solo unyts towards the middle
            wrapper.sortIndex = arrayOf(
                42088.0 * getJLPTLevelScore(w),
                5101.0 * getCatAvgIndex(w) / WordCategory.entries.size,
                4707.0 * wrapper.wordWithinUnytIdx * getWordWithinUnytScore(w) / 50.0,
                2405.0 * wrapper.unytIdx * getUnytScore(w) / totalNumVisibleUnyts,
                1999.0 * unytSuffixWeight,
                999.0 * getKanjiDifficulty(w),
                333.0 * getAmbiguityScore(w),
                8.0 * getHintScore(w),
                5.0 * getFlagsScore(w),
                1.0 * getCharTypeDifficulty(w),
                0.00001 * min(20, w.translation.en.length) / 20.0,
            ).sum().toFloat()
        }

        words.sortWith { w1, w2 ->
            val s1 = w1.sortIndex
            val s2 = w2.sortIndex
            when {
                s1 < s2 -> -1
                s1 > s2 -> 1
                else -> w1.word.id.compareTo(w2.word.id)
            }
        }

        return words.map { it.word }
    }

    private fun GoigoiWord.isDifficult(): Boolean {
        // DAYS_OF_THE_WEEK and MONTHS are rated difficult, because they shouldn't come all grouped together.
        fun hasDifficultCat() = cats.let {
            it.contains(WordCategory.DAYS_OF_THE_WEEK) ||
                it.contains(WordCategory.MONTHS) ||
                it.contains(WordCategory.ONOMATOPOEIA)
        }

        fun hasDifficultHint() = when (hint2) {
            WordHint.COUNTER -> true
            WordHint.NUMBER -> true
            WordHint.PREFIX -> true
            WordHint.SUFFIX -> true
            else -> false
        }

        return common == false || hasDifficultCat() || hasDifficultHint() || primaryForm.raw.hasDigitOfAnyForm()
    }

    private fun getJLPTLevelScore(w: GoigoiWord) = when (w.level) {
        JLPTLevel.N5 -> 0
        JLPTLevel.N4 -> 1
        JLPTLevel.N3 -> 2
        JLPTLevel.N2 -> 3
        JLPTLevel.N1 -> 4
        JLPTLevel.Nx, null -> 5
    }.toFloat() / 5.0

    private fun getUnytScore(w: GoigoiWord) = when {
        w.isDifficult() -> 1.0 // separate these by unyt
        else -> 0.0001
    }

    private fun getAmbiguityScore(w: GoigoiWord) = min(
        10,
        w.links.count {
            it.kind == GoigoiWordLink.Kind.XML_KEEP_APART || it.kind == GoigoiWordLink.Kind.AUTO_SAME_READING
        }
    ) / 10.0f

    private fun getKanjiDifficulty(w: GoigoiWord) =
        (if (w.usuallyInKana) 0.42f else 1.0f) * w.kanji.maxOf { c ->
            when {
                !c.isCJKNotKana() || c.isHiragana() || c.isKatakana() -> 0.0
                else -> {
                    val kanjiByFreq = vocab.kanjiByFreq.also { require(it.isNotEmpty()) }
                    val idx = kanjiByFreq.indexOf(c)
                    when {
                        idx < 0 -> 0.0
                        else -> 0.1 + (idx.toDouble() / kanjiByFreq.length).pow(4.2)
                    }
                }
            }
        }

    private fun getCharTypeDifficulty(w: GoigoiWord) = when {
        w.primaryForm.raw.isHiragana() -> 1.0
        w.primaryForm.raw.isKatakana() -> 1.1
        w.usuallyInKana -> 1.2 // Goigoi may still ask about the kanji sometimes
        w.primaryForm.raw.hasDigitOfAnyForm() -> 5.0
        else -> 2.0 + min(8, w.kanji.filter { !it.isHiragana() && !it.isKatakana() }.length) / 8.0
    }

    private fun getWordWithinUnytScore(w: GoigoiWord) = when {
        w.isDifficult() -> 1.0
        w.cats.contains(WordCategory.TIME) -> 0.3 // there are TIME words that are not difficult
        w.hint2 == WordHint.LOANWORD || w.primaryForm.raw.isKatakana() -> 0.21 // don't cluster loanwords together
        w.cats.size == 1 && w.cats.contains(WordCategory.GENERAL) -> 0.2 // general words should be spread out
        w.hint2 == WordHint.ADVERB -> 0.15 // adverbs are often difficult, esp. without context
        w.cats.contains(WordCategory.FAMILY) -> 0.1
        w.cats.contains(WordCategory.DIRECTIONS) -> 0.1
        else -> 0.000001
    }

    private fun getFlagsScore(w: GoigoiWord) = when {
        w.common == false -> 10
        w.romaji.let { it.startsWith("go-") && it != "go-gatsu" } -> 9 // go-ryōshin, go-chisō, go-zonji
        w.hint.en.contains("with neg") -> 8 // usually adverbs 'with neg. verb'
        w.romaji.startsWith("o-") -> 7 // o-kane, o-yomi
        w.links.any { it.extendedKind == GoigoiWordLink.ExtendedKind.ADJECTIVE } -> 6 // push away from NOUN
        w.links.any { it.extendedKind == GoigoiWordLink.ExtendedKind.VERB } -> 5 // push away from NOUN
        w.studyInContext == StudyInContextKind.REQUIRED -> 4
        w.studyInContext == StudyInContextKind.PREFERRED -> 3
        w.sentences.isEmpty() -> 2
        w.phrases.isEmpty() -> 1
        else -> 0
    } / 10.0

    private fun getHintScore(w: GoigoiWord) = when (w.hint2) {
        null -> 0
        WordHint.LOANWORD -> 1
        WordHint.NOUN -> 2
        WordHint.NOUN_SURU -> 3
        WordHint.I_ADJECTIVE -> 4
        WordHint.NA_ADJECTIVE -> 5
        WordHint.NOUN_NA_ADJECTIVE -> 6
        WordHint.VERB -> 7
        WordHint.V_I -> 8
        WordHint.V_T -> 9
        WordHint.V_T_AND_I -> 10
        WordHint.ADJECTIVE -> 11
        WordHint.COLLOQUIAL -> 12
        WordHint.EXPRESSION -> 13
        WordHint.NOUN_ADVERB -> 14
        WordHint.ADVERB -> 15
        WordHint.ADVERB_SURU -> 16
        WordHint.PRONOUN -> 17
        WordHint.CONJUNCTION -> 18
        WordHint.NOUN_SUFFIX -> 19
        WordHint.ABBREV -> 20
        WordHint.PARTICLE -> 21
        WordHint.MODEST -> 22
        WordHint.HUMBLE -> 23
        WordHint.EXTRA_MODEST -> 24
        WordHint.POLITE -> 25
        WordHint.HONORIFIC -> 26
        WordHint.RESPECTFUL -> 27
        WordHint.FORMAL -> 28
        WordHint.NUMBER -> 29
        WordHint.COUNTER -> 30
        WordHint.PREFIX -> 31
        WordHint.SUFFIX -> 32
    } / 32.0f

    private fun getCatAvgIndex(w: GoigoiWord): Double {
        val weightedAvg = w.cats
            .weightedAverageOrNull { index, cat ->
                ValueAndWeight(
                    value = CAT_RANKS.indexOf(cat)
                        .also { require(it >= 0) { "Missing rank for category $cat" } }
                        .toFloat(),
                    weight = 1.0f / (1.0f + index) // move words towards the first category
                )
            }
            ?: CAT_RANKS.indexOf(null).toFloat()

        if (w.isDifficult()) {
            // Move difficult words like counters towards the middle, because they will be spread out.
            val middle = CAT_RANKS.indexOf(null).toDouble()
            return lerp(weightedAvg.toDouble(), middle, 0.9)
        } else {
            return weightedAvg.toDouble()
        }
    }

    companion object {
        private val CAT_RANKS = arrayOf(
            WordCategory.TOP_WORDS,
            WordCategory.TRAVELLING,
            WordCategory.FOOD,
            WordCategory.HOLIDAYS,
            WordCategory.SHOPPING,
            WordCategory.BEACH,
            WordCategory.BEVERAGE,
            WordCategory.TALKING,
            WordCategory.EDUCATION,
            WordCategory.HEARING,
            WordCategory.HAPPINESS,
            WordCategory.LOOKING,
            WordCategory.DIRECTIONS,
            WordCategory.GASTRONOMY,
            WordCategory.PEOPLE,
            WordCategory.SPORTS,
            WordCategory.HANDS,
            WordCategory.WRITING,
            WordCategory.FEET,
            WordCategory.COOKING,
            WordCategory.GENERAL,
            WordCategory.WEATHER,
            WordCategory.COLOUR,
            WordCategory.SEASONS,
            WordCategory.TEMPERATURE,
            WordCategory.FAMILY,
            WordCategory.FIRE,
            WordCategory.AQUATIC,
            WordCategory.CULTURE,
            WordCategory.PLANT,
            WordCategory.OBJECTS,
            WordCategory.ANIMAL,
            WordCategory.THINKING,
            WordCategory.NATURE,
            WordCategory.BIRD,
            WordCategory.FASHION,
            WordCategory.MONTHS,
            WordCategory.HOME,
            WordCategory.CLEANING,
            WordCategory.GARDEN,
            WordCategory.DAYS_OF_THE_WEEK,
            WordCategory.RELATIONSHIPS,
            WordCategory.MANGA_ANIME,
            WordCategory.NOT_GOOD,
            WordCategory.TIME,
            WordCategory.GAMES,
            WordCategory.SCENERY,
            WordCategory.HEALTH,
            WordCategory.ARTS,
            WordCategory.MOOD,
            WordCategory.QUARREL,
            WordCategory.BODY,
            WordCategory.COUNTRYSIDE,
            WordCategory.WORK,
            WordCategory.GEOGRAPHY,
            WordCategory.TRANSPORTATION,
            WordCategory.CITY,
            WordCategory.MOTION,
            WordCategory.INSECT,
            WordCategory.OCCUPATIONS,
            WordCategory.GOING_OUT,
            WordCategory.EMPLOYMENT,
            WordCategory.LIFE,
            WordCategory.ENTERTAINMENT,
            WordCategory.MEETINGS,
            WordCategory.HOBBY,
            WordCategory.BUSINESS,
            WordCategory.DIY,
            WordCategory.MATHS,
            WordCategory.TECHNICAL,
            WordCategory.NEWS,
            WordCategory.LIGHT,
            null, // rank for words with no category
            WordCategory.POLITICS,
            WordCategory.TRADE,
            WordCategory.CRIME,
            WordCategory.FINANCES,
            WordCategory.SCIENCE,
            WordCategory.PHYSICS,
            WordCategory.GOVERNMENT,
            WordCategory.FACTORY,
            WordCategory.MILITARY,
            WordCategory.HISTORICAL,
            WordCategory.ASTRONOMY,
            WordCategory.LINGUISTICS,
            WordCategory.LITERATURE,
            WordCategory.OLD_FASHIONED,
            WordCategory.ONOMATOPOEIA,
        )
        private val UNYT_NUMBER_SUFFIXES = arrayOf(
            "①",
            "②",
            "③",
            "④",
            "⑤",
            "⑥",
            "⑦",
            "⑧",
            "⑨",
            "(10)",
            "(11)",
            "(12)",
            "(13)",
            "(14)",
            "(15)",
            "(16)",
            "(17)",
            "(18)",
            "(19)",
            "(20)",
        )
    }
}
