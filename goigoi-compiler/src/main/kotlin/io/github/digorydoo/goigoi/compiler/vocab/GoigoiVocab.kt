package io.github.digorydoo.goigoi.compiler.vocab

import ch.digorydoo.kutils.cjk.JLPTLevel

class GoigoiVocab {
    val topics = mutableListOf<GoigoiTopic>()
    val manualKanjiLevels = mutableMapOf<JLPTLevel, MutableSet<Char>>() // manual JLPT level corrections
    val kanjiBySchoolYear = mutableMapOf<Int, MutableSet<Char>>() // first grade has key=1 (not 0)
    var kanjiByFreq = "" // characters at smaller indexes are more frequent
    val dontConfuseKanjis = mutableListOf<String>() // each entry is a string of kanjis
    val warnings = mutableListOf<String>() // warnings and hints found during parsing

    fun findUnytById(unytId: String): GoigoiUnyt? {
        for (t in topics) {
            for (u in t.unyts) {
                if (u.id == unytId) {
                    return u
                }
            }
        }

        return null
    }

    fun forEachUnyt(lambda: (u: GoigoiUnyt, t: GoigoiTopic) -> Unit) {
        for (t in topics) {
            t.forEachUnyt { u ->
                lambda(u, t)
            }
        }
    }

    fun forEachVisibleUnyt(lambda: (u: GoigoiUnyt, t: GoigoiTopic) -> Unit) {
        for (t in topics) {
            if (!t.hidden) {
                t.forEachVisibleUnyt { u ->
                    lambda(u, t)
                }
            }
        }
    }

    fun forEachWord(lambda: (w: GoigoiWord, s: GoigoiSection, u: GoigoiUnyt, t: GoigoiTopic) -> Unit) {
        for (t in topics) {
            t.forEachWord { w, s, u ->
                lambda(w, s, u, t)
            }
        }
    }

    fun forEachWord(lambda: (w: GoigoiWord) -> Unit) {
        for (t in topics) {
            t.forEachWord { w, _, _ ->
                lambda(w)
            }
        }
    }

    fun forEachVisibleWord(lambda: (w: GoigoiWord) -> Unit) {
        for (t in topics) {
            if (!t.hidden) {
                t.forEachVisibleWord { w, _, _ ->
                    lambda(w)
                }
            }
        }
    }

    fun forEachVisibleWord(lambda: (w: GoigoiWord, s: GoigoiSection, u: GoigoiUnyt, t: GoigoiTopic) -> Unit) {
        for (t in topics) {
            if (!t.hidden) {
                t.forEachVisibleWord { w, s, u ->
                    lambda(w, s, u, t)
                }
            }
        }
    }

    /**
     * TODO: Since sharedId is no longer supported, this function should be replaced with a findWordById
     */
    fun forEachWordWithId(
        wordId: String,
        lambda: (word: GoigoiWord, unyt: GoigoiUnyt, topic: GoigoiTopic) -> Unit,
    ) {
        if (wordId.isEmpty()) {
            return
        }

        for (t in topics) {
            t.forEachWordWithId(wordId) { w, u ->
                lambda(w, u, t)
            }
        }
    }
}
