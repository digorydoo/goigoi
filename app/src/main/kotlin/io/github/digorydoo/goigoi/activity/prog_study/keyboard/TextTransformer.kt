package io.github.digorydoo.goigoi.activity.prog_study.keyboard

import ch.digorydoo.kutils.cjk.*

class TextTransformer {
    fun transform(textAndCaret: Keyboard.TextAndCaret, action: KeyDef.Action, key: String) {
        when (action) {
            KeyDef.Action.NONE -> Unit
            KeyDef.Action.LITERAL -> insert(key, textAndCaret)
            KeyDef.Action.AUTO_TRANSFORM -> transformPrevChar(textAndCaret, ::autoTransform)
            KeyDef.Action.DAKUTEN -> transformPrevChar(textAndCaret, ::toggleDakuten)
            KeyDef.Action.HANDAKUTEN -> transformPrevChar(textAndCaret, ::toggleHandakuten)
            KeyDef.Action.NORMAL_SIZE -> transformPrevChar(textAndCaret) { it.toNormalSizedKana() }
            KeyDef.Action.SMALL_SIZE -> transformPrevChar(textAndCaret, ::makeSmallKana)
        }
    }

    private fun insert(key: String, textAndCaret: Keyboard.TextAndCaret) {
        val text = textAndCaret.text
        val caretPos = textAndCaret.caretPos
        val lpart = text.slice(0 ..< caretPos)
        val rpart = text.substring(caretPos)
        textAndCaret.text = "$lpart$key$rpart"
        textAndCaret.caretPos += key.length // key can be an entire word
    }

    fun transformPrevChar(textAndCaret: Keyboard.TextAndCaret, trf: (c: Char) -> Char) {
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
        c.isDakuten() -> c.toggleHandakuten().takeIf { it != c } ?: c.toggleDakuten() // dakuten -> handakuten/none
        c.isHandakuten() -> c.toggleHandakuten() // handakuten -> none
        c.toggleDakuten() != c -> c.toggleDakuten() // add dakuten if we can
        c.isSmallKana() -> c.toNormalSizedKana() // small kana -> normal sized kana
        else -> c.toSmallKana() // normal sized kana -> small kana
    }

    private fun toggleDakuten(c: Char) =
        c.toggleDakuten().takeIf { it != c } ?: when {
            c.isSmallKana() -> c.toNormalSizedKana().toggleDakuten() // ヶ -> ケ -> ゲ
            else -> c
        }

    private fun toggleHandakuten(c: Char) =
        c.toggleHandakuten().takeIf { it != c } ?: when {
            c.isSmallKana() -> c.toNormalSizedKana().toggleHandakuten() // there is no such case, is there?
            else -> c
        }

    private fun makeSmallKana(c: Char) =
        c.toSmallKana().takeIf { it != c } ?: when {
            c.isDakuten() -> c.toggleDakuten().toSmallKana() // ガ -> カ -> ヵ
            c.isHandakuten() -> c.toggleHandakuten().toSmallKana() // there is no such case, is there?
            else -> c
        }
}
