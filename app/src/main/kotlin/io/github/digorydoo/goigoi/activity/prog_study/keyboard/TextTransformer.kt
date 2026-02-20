package io.github.digorydoo.goigoi.activity.prog_study.keyboard

import ch.digorydoo.kutils.cjk.*

class TextTransformer {
    fun transform(text: String, action: KeyDef.Action, key: String) =
        when (action) {
            KeyDef.Action.NONE -> text
            KeyDef.Action.LITERAL -> text + key
            KeyDef.Action.AUTO_TRANSFORM -> transformLastChar(text, ::autoTransform)
            KeyDef.Action.DAKUTEN -> transformLastChar(text, ::toggleDakuten)
            KeyDef.Action.HANDAKUTEN -> transformLastChar(text, ::toggleHandakuten)
            KeyDef.Action.NORMAL_SIZE -> transformLastChar(text) { it.toNormalSizedKana() }
            KeyDef.Action.SMALL_SIZE -> transformLastChar(text, ::makeSmallKana)
        }

    private fun transformLastChar(text: String, trf: (c: Char) -> Char): String {
        if (text.isEmpty()) return text
        val lpart = text.slice(0 ..< text.length - 1)
        return lpart + trf(text.last())
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
