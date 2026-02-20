package io.github.digorydoo.goigoi.compiler

import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.cjk.hasDigitOfAnyForm
import ch.digorydoo.kutils.cjk.isCJK
import ch.digorydoo.kutils.cjk.isHiragana
import ch.digorydoo.kutils.cjk.isKatakana
import ch.digorydoo.kutils.collections.ValueAndWeight
import ch.digorydoo.kutils.collections.weightedAverageOrNull
import ch.digorydoo.kutils.math.lerp
import io.github.digorydoo.goigoi.compiler.check.check
import io.github.digorydoo.goigoi.compiler.stats.Stats
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWordLink
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiXmlParser
import io.github.digorydoo.goigoi.compiler.writer.VocabIndexWriter
import io.github.digorydoo.goigoi.compiler.writer.WordFileWriter
import io.github.digorydoo.goigoi.core.StudyInContextKind
import io.github.digorydoo.goigoi.core.WordCategory
import io.github.digorydoo.goigoi.core.WordHint
import io.github.digorydoo.kokuban.ShellCommandError
import java.io.File
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow

class GoigoiVocabBuilder(private val vocab: GoigoiVocab, private val options: Options) {
    fun build(srcDir: File) {
        if (!options.quiet) {
            println("Parsing XML...")
        }

        readGoigoiXmls(srcDir.listFiles()!!)

        buildFileNames() // creates file names for *.voc files, ignoring original XML number prefixes

        if (!options.quiet) {
            println("Checking vocab...")
        }

        vocab.check() // checks constraints that cannot be checked at XML reading time

        if (!options.quiet) {
            Stats.printStats(vocab) // stats about see-also links will be emitted by prepare below
            println("Preparing see-also links...")
        }

        val prep = PrepWordLinks(vocab, options)
        prep.prepare() // prepares see-also links and prints statistics about those

        if (!options.quiet) {
            println("Writing vocab index...")
        }

        writeVocabIndex()

        if (!options.quiet) {
            println("Writing word files...")
        }

        val wordDir = File("${options.dstDir}/$WORD_DIR")

        if (!wordDir.exists()) {
            if (!wordDir.mkdir()) {
                throw ShellCommandError("Failed to create directory: ${wordDir.path}")
            }
        }

        for (topic in vocab.topics) {
            for (unyt in topic.unyts) {
                if (!topic.hidden && !unyt.hidden) {
                    if (options.quiet) {
                        print(".")
                    }

                    unyt.forEachVisibleWord { word, _ ->
                        writeWord(word)
                    }
                }
            }
        }

        if (options.quiet) {
            println() // dots are printed in quiet mode, so terminate the line here
        }
    }

    private fun buildFileNames() {
        class WrappedWord(val word: GoigoiWord, val unytIdx: Int, val wordWithinUnytIdx: Int) {
            var sortIndex = 0.0f
        }

        val words = mutableListOf<WrappedWord>()
        var unytIdx = 0

        for (topic in vocab.topics) {
            if (!topic.hidden) {
                for (unyt in topic.unyts) {
                    if (!unyt.hidden) {
                        var wordWithinUnytIdx = 0
                        unyt.forEachVisibleWord { word, _ ->
                            words.add(WrappedWord(word, unytIdx, wordWithinUnytIdx++))
                        }
                        unytIdx++
                    }
                }
            }
        }

        val totalNumVisibleUnyts = unytIdx

        words.forEach {
            it.sortIndex = getSortIndex(
                it.word,
                it.unytIdx,
                it.wordWithinUnytIdx,
                totalNumVisibleUnyts,
                vocab.kanjiByFreq
            ).toFloat()
        }

        // Filenames should be short and unique, to make the VocabIndex occupy less disk space.
        // Enable the extended filename for debugging only.
        val extendedFileNames = false

        if (extendedFileNames) {
            println("Warning: Extended filenames are enabled!")
        }

        // Sort the list of words. This affects the prefix of the filename, and because Goigoi sorts the list of words
        // by filename, it affects the index of a word within super progressive mode. Do NOT randomize the list, because
        // the list needs to be stable across builds!

        words.sortWith { w1, w2 ->
            val s1 = w1.sortIndex
            val s2 = w2.sortIndex
            when {
                s1 < s2 -> -1
                s1 > s2 -> 1
                else -> w1.word.id.compareTo(w2.word.id)
            }
        }

        val numDigits = floor(log10(words.size.toDouble())).toInt() + 1

        words.forEachIndexed { wordIdx, wrapped ->
            val word = wrapped.word
            val prefix = "w${wordIdx.toString().padStart(numDigits, '0')}"
            val name = word.romaji
                .ifEmpty { word.primaryForm.raw }
                .ifEmpty { word.id }
                .trim()
                .lowercase()
                .replace(", ", "-")
                .replace(" ", "-")
                .replace("/", "")
                .replace(".", "")

            if (extendedFileNames) {
                word.fileName = "$prefix-${word.level?.toString() ?: "nx"}-${name}.voc"
            } else {
                word.fileName = "${prefix}${name}".replace("-", "").take(10) + ".voc"
            }
        }
    }

    private fun readGoigoiXmls(srcFiles: Array<File>) {
        srcFiles.sortedBy { it.name }
            .forEach { fileOrDir ->
                if (fileOrDir.isDirectory) {
                    readGoigoiXmls(fileOrDir.listFiles()!!)
                } else if (fileOrDir.isFile) {
                    if (fileOrDir.name.startsWith(".")) {
                        // Files starting with a dot are silently ignored, e.g. ".DS_Store"
                    } else if (fileOrDir.extension.lowercase() != "xml") {
                        System.err.println("Warning: Ignoring non-XML file: ${fileOrDir.path}")
                    } else {
                        readGoigoiXml(fileOrDir)
                    }
                } else {
                    System.err.println("Warning: Inaccessible, or neither directory nor regular file: ${fileOrDir.path}")
                }
            }
    }

    private fun readGoigoiXml(xmlFile: File) {
        if (options.quiet) {
            print(".")
        }

        try {
            val stream = xmlFile.inputStream()
            val parser = GoigoiXmlParser()
            parser.parse(stream, vocab)
        } catch (e: ParsingFailed) {
            throw ParsingFailed("${xmlFile.path}\n   ${e.message}", e)
        } catch (e: CheckFailed) {
            throw CheckFailed("${xmlFile.path}\n   ${e.message}", e)
        } catch (e: Exception) {
            throw RuntimeException("${xmlFile.path}\n   ${e.message}")
        }
    }

    private fun writeVocabIndex() {
        val indexFname = "${options.dstDir}/index.voc"

        if (options.quiet) {
            print(".")
        }

        val indexFile = File(indexFname)

        if (indexFile.exists()) {
            if (options.overwrite) {
                indexFile.delete()
            } else {
                throw ShellCommandError("\nFile already exists: $indexFname")
            }
        }

        if (!indexFile.createNewFile()) {
            throw ShellCommandError("\nFailed to create file: ${indexFile.path}")
        }

        val indexStream = indexFile.outputStream()
        VocabIndexWriter(vocab, indexStream).write()
    }

    private fun writeWord(word: GoigoiWord) {
        val fname = "${options.dstDir}/$WORD_DIR/${word.fileName}"

        val file = File(fname)

        if (file.exists()) {
            if (options.overwrite) {
                file.delete()
            } else {
                throw ShellCommandError("\nFile already exists: ${file.path}")
            }
        }

        if (!file.createNewFile()) {
            throw ShellCommandError("\nFailed to create file: ${file.path}")
        }

        file.outputStream().use {
            WordFileWriter(word, it).write()
        }
    }

    companion object {
        private const val WORD_DIR = "word"

        private fun getSortIndex(
            w: GoigoiWord,
            unytIdx: Int,
            wordWithinUnytIdx: Int,
            totalNumUnyts: Int,
            kanjiByFreq: String,
        ) =
            arrayOf(
                42088.0 * getJLPTLevelScore(w),
                4805.0 * getCatAvgIndex(w) / WordCategory.entries.size,
                4707.0 * wordWithinUnytIdx * getWordWithinUnytScore(w) / 50.0,
                2405.0 * unytIdx * getUnytScore(w) / totalNumUnyts,
                42.0 * getKanjiDifficulty(w, kanjiByFreq.also { require(it.isNotEmpty()) }),
                8.0 * getHintScore(w),
                5.0 * getFlagsScore(w),
                1.0 * getCharTypeDifficulty(w),
                0.00001 * min(20, w.translation.en.length) / 20.0,
            ).sum()

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

        private fun getKanjiDifficulty(w: GoigoiWord, kanjiByFreq: String) = when {
            w.usuallyInKana -> 0.0
            w.primaryForm.raw.isKatakana() -> 0.11
            w.primaryForm.raw.isHiragana() -> 0.12
            else -> w.kanji.maxOf { c ->
                when {
                    !c.isCJK() || c.isHiragana() || c.isKatakana() -> 0.0
                    else -> {
                        val idx = kanjiByFreq.indexOf(c)
                        when {
                            idx < 0 -> 0.0
                            else -> 0.1 + (idx.toDouble() / kanjiByFreq.length).pow(4.2)
                        }
                    }
                }
            }
        }

        private fun getCharTypeDifficulty(w: GoigoiWord) = when {
            w.usuallyInKana -> 1.0
            w.primaryForm.raw.isHiragana() -> 1.0
            w.primaryForm.raw.isKatakana() -> 1.1
            w.primaryForm.raw.hasDigitOfAnyForm() -> 5.0
            else -> 2.0 + min(8, w.kanji.filter { !it.isHiragana() && !it.isKatakana() }.length) / 8.0
        }

        private fun getWordWithinUnytScore(w: GoigoiWord) = when {
            w.isDifficult() -> 1.0
            w.cats.contains(WordCategory.TIME) -> 0.3 // there are TIME words that are not difficult
            w.hint2 == WordHint.LOANWORD -> 0.2
            w.cats.size == 1 && w.cats.contains(WordCategory.GENERAL) -> 0.2
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
                        value = catRank.indexOf(cat)
                            .also { require(it >= 0) { "Missing rank for category $cat" } }
                            .toFloat(),
                        weight = 1.0f / (1.0f + index) // move words towards the first category
                    )
                }
                ?: catRank.indexOf(null).toFloat()

            if (w.isDifficult()) {
                // Move difficult words like counters towards the middle, because they will be spread out.
                val middle = catRank.indexOf(null).toDouble()
                return lerp(weightedAvg.toDouble(), middle, 0.9)
            } else {
                return weightedAvg.toDouble()
            }
        }

        private val catRank = arrayOf(
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
    }
}
