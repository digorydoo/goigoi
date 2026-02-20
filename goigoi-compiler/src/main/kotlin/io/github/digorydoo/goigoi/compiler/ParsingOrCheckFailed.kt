package io.github.digorydoo.goigoi.compiler

import io.github.digorydoo.goigoi.compiler.vocab.GoigoiPhrase
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiTopic
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiUnyt
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import oracle.xml.parser.v2.MyDOMParser.Companion.customLineNumber
import org.w3c.dom.Element

abstract class ParsingOrCheckFailed(
    msg: String,
    val topic: GoigoiTopic? = null,
    val unyt: GoigoiUnyt? = null,
    val word: GoigoiWord? = null,
    val phrase: GoigoiPhrase? = null,
): Exception(msg) {
    fun prettyPrint() {
        println(message)

        // We don't know whether it's a phrase or sentence, so we don't write that information.
        phrase?.prettyPrint(withRomaji = true)

        word?.let {
            print("Word: ")
            it.prettyPrint(withId = true, withKanjiKanaSeparated = true, withLvl = true)
        }

        unyt?.let {
            print("Unyt: ")
            it.prettyPrint()
        }

        topic?.let {
            print("Topic: ")
            it.prettyPrint()
        }
    }
}

class ParsingFailed private constructor(
    msg: String,
    topic: GoigoiTopic?,
    unyt: GoigoiUnyt?,
    word: GoigoiWord?,
    phrase: GoigoiPhrase?,
): ParsingOrCheckFailed(msg, topic, unyt, word, phrase) {
    constructor(msg: String, e: Element): this(makeMessage(msg, e), null, null, null, null)
    constructor(msg: String, e: Element, u: GoigoiUnyt): this(makeMessage(msg, e), null, u, null, null)
    constructor(msg: String, e: Element, u: GoigoiUnyt, w: GoigoiWord): this(makeMessage(msg, e), null, u, w, null)
    constructor(newMsg: String, e: ParsingFailed): this(newMsg, e.topic, e.unyt, e.word, e.phrase)

    companion object {
        private fun makeMessage(providedMsg: String, element: Element?) =
            element?.let { e ->
                (e.customLineNumber.takeIf { it > 0 }?.let { "Line $it: " } ?: "") + "${e.nodeName}: "
            } + providedMsg
    }
}

class CheckFailed private constructor(
    msg: String,
    topic: GoigoiTopic?,
    unyt: GoigoiUnyt?,
    word: GoigoiWord?,
    phrase: GoigoiPhrase?,
): ParsingOrCheckFailed(msg, topic, unyt, word, phrase) {
    constructor(msg: String): this(msg, null, null, null, null)
    constructor(msg: String, t: GoigoiTopic): this(msg, t, null, null, null)
    constructor(msg: String, u: GoigoiUnyt): this(msg, null, u, null, null)
    constructor(msg: String, u: GoigoiUnyt, w: GoigoiWord): this(msg, null, u, w, null)
    constructor(msg: String, u: GoigoiUnyt, w: GoigoiWord, ph: GoigoiPhrase): this(msg, null, u, w, ph)
    constructor(newMsg: String, e: CheckFailed): this(newMsg, e.topic, e.unyt, e.word, e.phrase)
}
