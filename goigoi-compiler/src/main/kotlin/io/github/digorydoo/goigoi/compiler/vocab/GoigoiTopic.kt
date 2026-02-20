package io.github.digorydoo.goigoi.compiler.vocab

import ch.digorydoo.kutils.cjk.IntlString
import ch.digorydoo.kutils.cjk.JLPTLevel

class GoigoiTopic {
    var id = ""
    val name = IntlString()
    var imgSrc = ""
    var notice = IntlString()
    var linkText = ""
    var linkHref = ""
    var levels = listOf<JLPTLevel>()
    val unyts = mutableListOf<GoigoiUnyt>()
    var hidden = false
    var bgColour = ""

    fun forEachUnyt(lambda: (u: GoigoiUnyt) -> Unit) {
        for (u in unyts) {
            lambda(u)
        }
    }

    fun forEachVisibleUnyt(lambda: (u: GoigoiUnyt) -> Unit) {
        for (u in unyts) {
            if (!u.hidden) {
                lambda(u)
            }
        }
    }

    fun forEachWord(lambda: (w: GoigoiWord, s: GoigoiSection, u: GoigoiUnyt) -> Unit) {
        for (u in unyts) {
            u.forEachWord { w, s ->
                lambda(w, s, u)
            }
        }
    }

    fun forEachVisibleWord(lambda: (w: GoigoiWord, s: GoigoiSection, u: GoigoiUnyt) -> Unit) {
        for (u in unyts) {
            if (!u.hidden) {
                u.forEachVisibleWord { w, s ->
                    lambda(w, s, u)
                }
            }
        }
    }

    /**
     * Note: ids are now unique since sharedId is no longer supported
     */
    fun forEachWordWithId(wordId: String, lambda: (word: GoigoiWord, unyt: GoigoiUnyt) -> Unit) {
        if (wordId.isEmpty()) {
            return
        }

        for (u in unyts) {
            val w = u.findWordById(wordId)

            if (w != null) {
                lambda(w, u)
            }
        }
    }

    override fun toString() =
        "GoigoiTopic(name_en=${name.en})"

    fun prettyPrint() {
        // Output is meant for console, so we could use Kokuban here
        println(name.en)
    }
}
