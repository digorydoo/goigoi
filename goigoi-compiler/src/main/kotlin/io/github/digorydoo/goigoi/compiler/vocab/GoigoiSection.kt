package io.github.digorydoo.goigoi.compiler.vocab

import ch.digorydoo.kutils.cjk.IntlString

class GoigoiSection {
    var id = ""
    var name = IntlString()
    val words = mutableListOf<GoigoiWord>()

    fun forEachWord(lambda: (word: GoigoiWord) -> Unit) {
        for (w in words) {
            lambda(w)
        }
    }

    fun forEachVisibleWord(lambda: (word: GoigoiWord) -> Unit) {
        for (w in words) {
            if (!w.hidden) {
                lambda(w)
            }
        }
    }

    fun findWordById(wordId: String): GoigoiWord? {
        if (wordId.isEmpty()) {
            return null
        }

        var result: GoigoiWord? = null

        for (w in words) {
            if (w.id == wordId) {
                if (result != null) {
                    throw Exception("Section contains multiple words with same id=$wordId")
                } else {
                    result = w
                }
            }
        }

        return result
    }
}
