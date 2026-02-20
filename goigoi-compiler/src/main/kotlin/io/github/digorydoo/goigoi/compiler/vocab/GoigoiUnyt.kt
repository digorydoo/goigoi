package io.github.digorydoo.goigoi.compiler.vocab

import ch.digorydoo.kutils.cjk.IntlString
import ch.digorydoo.kutils.cjk.JLPTLevel

class GoigoiUnyt {
    var id = ""
    var fileName = ""
    val name = IntlString()
    val defaultHint = IntlString()
    var studyLang = ""
    var hidden = false
    var hasRomaji = false
    var hasFurigana = false
    var ignoresCombinedReadings = false
    var requiresIds = false
    var requiresSentences = false
    var requiresPhrases = false
    var requiredTranslations = listOf<String>()
    val subheader = IntlString()
    var levels = listOf<JLPTLevel>()
    val sections = mutableListOf<GoigoiSection>()

    /**
     * @return the number of visible words with at least one sentence
     */
    fun countVisibleWordsWithSentences(): Int {
        var count = 0

        forEachVisibleWord { w, _ ->
            if (w.sentences.isNotEmpty()) {
                count++
            }
        }

        return count
    }

    /**
     * @return the number of visible words with at least one phrase
     */
    fun countVisibleWordsWithPhrases(): Int {
        var count = 0

        forEachVisibleWord { w, _ ->
            if (w.phrases.isNotEmpty()) {
                count++
            }
        }

        return count
    }

    /**
     * @return The English name of the unyt with Japanese characters replaced for use in println()
     */
    val nameForStdout: String
        get() = name.en
            .replace("て", "Te")
            .replace("①", "(1)")
            .replace("②", "(2)")
            .replace("③", "(3)")
            .replace("④", "(4)")
            .replace("⑤", "(5)")
            .replace("⑥", "(6)")
            .replace("⑦", "(7)")
            .replace("⑧", "(8)")
            .replace("⑨", "(9)")
            .replace("⑩", "(10)")

    fun forEachWord(lambda: (w: GoigoiWord, s: GoigoiSection) -> Unit) {
        for (s in sections) {
            s.forEachWord { w ->
                lambda(w, s)
            }
        }
    }

    fun forEachVisibleWord(lambda: (w: GoigoiWord, s: GoigoiSection) -> Unit) {
        for (s in sections) {
            s.forEachVisibleWord { w ->
                lambda(w, s)
            }
        }
    }

    fun findWordById(wordId: String): GoigoiWord? {
        if (wordId.isEmpty()) {
            return null
        }

        var result: GoigoiWord? = null

        for (s in sections) {
            val w = s.findWordById(wordId)

            if (w != null) {
                if (result != null) {
                    throw Exception("Unyt contains multiple words with same id=$wordId")
                } else {
                    result = w
                }
            }
        }

        return result
    }

    fun countWords(predicate: ((w: GoigoiWord) -> Boolean)? = null): Int {
        var count = 0

        for (s in sections) {
            if (predicate == null) {
                count += s.words.size
            } else {
                s.words.forEach { word ->
                    if (predicate(word)) {
                        count++
                    }
                }
            }
        }

        return count
    }

    override fun toString() =
        "GoigoiUnyt(name=${nameForStdout})"

    fun prettyPrint() {
        // Output is meant for console, so we could use Kokuban here
        print(nameForStdout)

        if (fileName.isNotEmpty()) {
            print(" (${fileName})")
        }

        println()
    }
}
