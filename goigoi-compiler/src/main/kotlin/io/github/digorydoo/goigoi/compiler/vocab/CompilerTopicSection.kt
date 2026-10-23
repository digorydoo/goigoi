package io.github.digorydoo.goigoi.compiler.vocab

import ch.digorydoo.kutils.cjk.IntlString

class CompilerTopicSection {
    var id = ""
    var name = IntlString()
    val words = mutableListOf<CompilerWord>()

    fun forEachWord(lambda: (word: CompilerWord) -> Unit) {
        for (w in words) {
            lambda(w)
        }
    }

    fun forEachVisibleWord(lambda: (word: CompilerWord) -> Unit) {
        for (w in words) {
            if (!w.hidden) {
                lambda(w)
            }
        }
    }

    fun findWordById(wordId: String): CompilerWord? {
        if (wordId.isEmpty()) {
            return null
        }

        var result: CompilerWord? = null

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

    override fun toString() = "Section" + when {
        name.en.isNotEmpty() -> " ${name.en}"
        else -> ""
    }
}
