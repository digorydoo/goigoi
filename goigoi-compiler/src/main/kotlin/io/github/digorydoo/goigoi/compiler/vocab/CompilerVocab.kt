package io.github.digorydoo.goigoi.compiler.vocab

import ch.digorydoo.kutils.cjk.JLPTLevel

class CompilerVocab {
    val topics = mutableListOf<CompilerTopic>()
    val manualKanjiLevels = mutableMapOf<JLPTLevel, MutableSet<Char>>() // manual JLPT level corrections
    val kanjiBySchoolYear = mutableMapOf<Int, MutableSet<Char>>() // first grade has key=1 (not 0)
    var kanjiByFreq = "" // characters at smaller indexes are more frequent
    val visuallySimilar = mutableListOf<String>() // each entry is a string of kanjis

    fun findUnytById(unytId: String): CompilerUnyt? {
        for (t in topics) {
            for (u in t.unyts) {
                if (u.id == unytId) {
                    return u
                }
            }
        }

        return null
    }

    fun forEachUnyt(lambda: (u: CompilerUnyt, t: CompilerTopic) -> Unit) {
        for (t in topics) {
            t.unyts.forEach { u ->
                lambda(u, t)
            }
        }
    }

    fun forEachWord(lambda: (w: CompilerWord, s: CompilerTopicSection, u: CompilerUnyt, t: CompilerTopic) -> Unit) {
        for (t in topics) {
            t.forEachWord { w, s, u ->
                lambda(w, s, u, t)
            }
        }
    }

    fun forEachWord(lambda: (w: CompilerWord) -> Unit) {
        for (t in topics) {
            t.forEachWord { w, _, _ ->
                lambda(w)
            }
        }
    }

    fun forEachVisibleWord(lambda: (w: CompilerWord) -> Unit) {
        for (t in topics) {
            if (!t.hidden) {
                t.forEachVisibleWord { w, _, _ ->
                    lambda(w)
                }
            }
        }
    }

    fun forEachVisibleWord(lambda: (w: CompilerWord, s: CompilerTopicSection, u: CompilerUnyt, t: CompilerTopic) -> Unit) {
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
        lambda: (word: CompilerWord, unyt: CompilerUnyt, topic: CompilerTopic) -> Unit,
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
