package io.github.digorydoo.goigoi.core.prog_study

import ch.digorydoo.kutils.cjk.*
import ch.digorydoo.kutils.math.clamp

class KeyActionHandler {
    enum class Action {
        NONE, LITERAL, AUTO_TRANSFORM, DAKUTEN, HANDAKUTEN, NORMAL_SIZE, SMALL_SIZE, MOVE_CARET_LEFT, MOVE_CARET_RIGHT
    }

    interface TextAndCaret {
        var text: CharSequence
        var caretPos: Int
    }

    fun handle(textAndCaret: TextAndCaret, action: Action, key: String) {
        when (action) {
            Action.NONE -> Unit
            Action.LITERAL -> insert(key, textAndCaret)
            Action.AUTO_TRANSFORM -> transformPrevChar(textAndCaret, ::autoTransform)
            Action.DAKUTEN -> transformPrevChar(textAndCaret, ::toggleDakuten)
            Action.HANDAKUTEN -> transformPrevChar(textAndCaret, ::toggleHandakuten)
            Action.NORMAL_SIZE -> transformPrevChar(textAndCaret) { it.toNormalSizedKana() }
            Action.SMALL_SIZE -> transformPrevChar(textAndCaret, ::makeSmallKana)
            Action.MOVE_CARET_LEFT -> moveCaret(-1, textAndCaret)
            Action.MOVE_CARET_RIGHT -> moveCaret(1, textAndCaret)
        }
    }

    fun handleBackspace(textAndCaret: TextAndCaret) {
        transformPrevChar(textAndCaret) { Char(0) }
    }

    private fun moveCaret(distance: Int, textAndCaret: TextAndCaret) {
        textAndCaret.caretPos = clamp(textAndCaret.caretPos + distance, 0, textAndCaret.text.length)
    }

    private fun insert(key: String, textAndCaret: TextAndCaret) {
        val text = textAndCaret.text
        val caretPos = textAndCaret.caretPos
        val lpart = text.slice(0 ..< caretPos)
        val rpart = text.substring(caretPos)

        if (rpart.startsWith(key)) {
            // Overwrite the characters to the right, i.e. simply move the caret.
            textAndCaret.caretPos += key.length
        } else {
            textAndCaret.text = "$lpart$key$rpart"
            textAndCaret.caretPos += key.length // key can be an entire word
        }
    }

    private fun transformPrevChar(textAndCaret: TextAndCaret, trf: (c: Char) -> Char) {
        val text = textAndCaret.text
        val caretPos = textAndCaret.caretPos

        if (text.isEmpty() || caretPos <= 0) {
            textAndCaret.caretPos = 0
        } else {
            val lpart = text.slice(0 ..< caretPos - 1)
            val rpart = text.substring(caretPos)
            val transformed = trf(text[caretPos - 1])

            if (transformed == Char(0)) {
                textAndCaret.text = "$lpart$rpart"
                textAndCaret.caretPos--
            } else {
                textAndCaret.text = "$lpart$transformed$rpart"
            }
        }
    }

    private fun autoTransform(c: Char) = when {
        c.isDakuten() -> c.toHandakuten().takeIf { it != c } ?: c.removeDakutenHandakuten().toSmallKana()
        c.isHandakuten() -> c.removeDakutenHandakuten().toSmallKana()
        c.isSmallKana() -> c.toNormalSizedKana()
        else -> c.toDakuten().takeIf { it != c } ?: c.toSmallKana()
    }

    private fun toggleDakuten(c: Char) = when {
        c.isDakuten() -> c.removeDakutenHandakuten()
        c.isSmallKana() -> c.toNormalSizedKana().toDakuten()
        else -> c.toDakuten()
    }

    private fun toggleHandakuten(c: Char) = when {
        c.isHandakuten() -> c.removeDakutenHandakuten()
        c.isSmallKana() -> c.toNormalSizedKana().toHandakuten()
        else -> c.toHandakuten()
    }

    // Small kana cannot have dakuten or handakuten
    private fun makeSmallKana(c: Char) = c.removeDakutenHandakuten().toSmallKana()
}
