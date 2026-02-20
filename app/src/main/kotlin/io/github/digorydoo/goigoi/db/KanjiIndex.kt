package io.github.digorydoo.goigoi.db

import android.content.Context
import android.util.Log
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.file.DontConfuseFileReader
import io.github.digorydoo.goigoi.file.KanjiIndexReader
import io.github.digorydoo.goigoi.file.KanjiReadingsReader
import io.github.digorydoo.goigoi.utils.StringUtils
import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.cjk.Unicode.HIRAGANA
import ch.digorydoo.kutils.cjk.Unicode.KATAKANA
import ch.digorydoo.kutils.cjk.isCJK
import ch.digorydoo.kutils.cjk.toHiragana
import java.io.InputStream

class KanjiIndex private constructor() {
    private val n5Kanjis = mutableSetOf<Char>()
    private val n4Kanjis = mutableSetOf<Char>()
    private val n3Kanjis = mutableSetOf<Char>()
    private val n2Kanjis = mutableSetOf<Char>()
    private val n1Kanjis = mutableSetOf<Char>()
    private val otherKanjis = mutableSetOf<Char>()
    private val readings = mutableMapOf<String, Set<String>>()
    private val dontConfuse = mutableMapOf<Char, Set<Char>>()

    fun getRandomKanjisOfLevel(level: JLPTLevel, desiredCount: Int, except: Set<Char>, avoidReadings: List<String>) =
        getKanjisWithReadings(avoidReadings) // e.g. へん, avoid 変 and 辺
            // .also { Log.d(TAG, "Kanjis with reading ${avoidReadings.joinToString(", ")}: ${it.joinToString(", ")}") }
            .filter { it.isNotEmpty() } // just to be safe
            .map { it[0] } // for combined kanjis we just pick the first one
            .let { StringUtils.getRandomSubset(kanjisOfLevel(level), desiredCount, except + it) }

    fun getRandomHiragana(desiredCount: Int, except: Set<Char>) =
        StringUtils.getRandomSubset(HIRAGANA.allCommon, desiredCount, except)

    fun getRandomKatakana(desiredCount: Int, except: Set<Char>) =
        StringUtils.getRandomSubset(KATAKANA.allCommon, desiredCount, except)

    /**
     * Finds all known kanjis with a given reading. The reading may be given in either hiragana or katakana. Since our
     * readings map contains hiragana only (as converted by extractKanjiList), we convert the given kana to hiragana
     * before looking it up.
     */
    private fun getKanjisWithReading(kana: String): Set<String> =
        (readings[kana.toHiragana()] ?: emptySet())

    private fun getKanjisWithReadings(kana: List<String>): Set<String> =
        kana.fold(mutableSetOf()) { result, cur ->
            result.apply { addAll(getKanjisWithReading(cur)) }
        }

    fun getReadingsOfKanji(kanji: Char): Set<String> =
        readings.filter { (_, kanjiSet) -> kanjiSet.contains("$kanji") }
            .map { (kana, _) -> kana }
            .toSet()

    private fun kanjisOfLevel(level: JLPTLevel) = when (level) {
        JLPTLevel.N5 -> n5Kanjis
        JLPTLevel.N4 -> n4Kanjis
        JLPTLevel.N3 -> n3Kanjis
        JLPTLevel.N2 -> n2Kanjis
        JLPTLevel.N1 -> n1Kanjis
        else -> otherKanjis
    }

    private fun isKanjiOfLevel(kanji: Char, level: JLPTLevel) =
        kanjisOfLevel(level).contains(kanji)

    fun levelOfKanji(kanji: Char) = when {
        isKanjiOfLevel(kanji, JLPTLevel.N5) -> JLPTLevel.N5
        isKanjiOfLevel(kanji, JLPTLevel.N4) -> JLPTLevel.N4
        isKanjiOfLevel(kanji, JLPTLevel.N3) -> JLPTLevel.N3
        isKanjiOfLevel(kanji, JLPTLevel.N2) -> JLPTLevel.N2
        isKanjiOfLevel(kanji, JLPTLevel.N1) -> JLPTLevel.N1
        isKanjiOfLevel(kanji, JLPTLevel.Nx) -> JLPTLevel.Nx
        else -> null
    }

    fun levelOfMostDifficultKanji(kanjiStr: String) =
        kanjiStr.filter { it.isCJK() }
            .mapNotNull { levelOfKanji(it) }
            .minOfOrNull { it.toInt() } // will be null if kanjiStr doesn't contain any kanji at all
            ?.let { JLPTLevel.fromInt(it) }

    fun getVisuallySimilarKanjis(kanji: Char) =
        dontConfuse[kanji] ?: emptySet()

    private fun loadFiles(ctx: Context) {
        @Suppress("KotlinConstantConditions", "SimplifyBooleanWithConstants")
        if (BuildConfig.FLAVOR != "japanese_free") {
            Log.d(TAG, "KanjiIndex is empty since build config flavour is ${BuildConfig.FLAVOR}")
            return
        }

        ctx.assets.apply {
            open(KANJI_INDEX_FILE_NAME).use { loadKanjiIndexFile(it) }
            open(READINGS_FILE_NAME).use { loadReadingsFile(it) }
            open(DONT_CONFUSE_FILE_NAME).use { loadDontConfuseFile(it) }
        }
    }

    private fun loadKanjiIndexFile(stream: InputStream) {
        require(n5Kanjis.isEmpty())
        require(n4Kanjis.isEmpty())
        require(n3Kanjis.isEmpty())
        require(n2Kanjis.isEmpty())
        require(n1Kanjis.isEmpty())
        require(otherKanjis.isEmpty())

        KanjiIndexReader(stream).read { kanji, level ->
            kanjisOfLevel(level).add(kanji)
        }

        Log.d(
            TAG,
            "Kanjis: " +
                "${n5Kanjis.size} n5, " +
                "${n4Kanjis.size} n4, " +
                "${n3Kanjis.size} n3, " +
                "${n2Kanjis.size} n2, " +
                "${n1Kanjis.size} n1, " +
                "${otherKanjis.size} nx"
        )
        require(n5Kanjis.isNotEmpty())
        require(n4Kanjis.isNotEmpty())
        require(n3Kanjis.isNotEmpty())
        require(n2Kanjis.isNotEmpty())
        require(n1Kanjis.isNotEmpty())
        require(otherKanjis.isNotEmpty())
    }

    private fun loadReadingsFile(stream: InputStream) {
        require(readings.isEmpty())

        KanjiReadingsReader(stream).read { kana, kanjis ->
            readings[kana] = mutableSetOf<String>().apply { addAll(kanjis) }
        }

        Log.d(TAG, "Readings: ${readings.size}")
        require(readings.isNotEmpty())
    }

    private fun loadDontConfuseFile(stream: InputStream) {
        require(dontConfuse.isEmpty())

        DontConfuseFileReader(stream).read { kanji, similarKanjis ->
            dontConfuse[kanji] = mutableSetOf<Char>().apply { addAll(similarKanjis) }
        }

        Log.d(TAG, "DontConfuse: ${dontConfuse.size}")
        require(dontConfuse.isNotEmpty())
    }

    companion object {
        private const val TAG = "KanjiIndex"
        private const val KANJI_INDEX_FILE_NAME = "voc_ja/all-kanjis.txt"
        private const val READINGS_FILE_NAME = "voc_ja/readings.txt"
        private const val DONT_CONFUSE_FILE_NAME = "voc_ja/dont-confuse.txt"

        private var singleton: KanjiIndex? = null

        fun getSingleton(ctx: Context) =
            singleton ?: KanjiIndex().apply {
                loadFiles(ctx)
                singleton = this
            }
    }
}
