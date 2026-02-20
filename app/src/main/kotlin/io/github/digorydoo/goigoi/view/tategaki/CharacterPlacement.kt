package io.github.digorydoo.goigoi.view.tategaki

class CharacterPlacement private constructor(val relXOffset: Float, val relYOffset: Float, val rotateCW: Boolean) {
    companion object {
        fun get(c: Char) = map[c] ?: defaultPlacement

        val justRotate = CharacterPlacement(0.0f, 0.0f, true)
        private val defaultPlacement = CharacterPlacement(0.0f, 0.0f, false)
        private val halfWidthCharPlacement = CharacterPlacement(0.25f, 0.0f, false)

        private val map = mapOf(
            '。' to CharacterPlacement(0.5f, -0.5f, false),
            '、' to CharacterPlacement(0.5f, -0.5f, false),
            '「' to justRotate,
            '」' to justRotate,
            '『' to justRotate,
            '』' to justRotate,
            '（' to justRotate,
            '）' to justRotate,
            '〔' to justRotate,
            '〕' to justRotate,
            '【' to justRotate,
            '】' to justRotate,
            '〜' to justRotate,
            'っ' to CharacterPlacement(0.1f, -0.15f, false),
            'ぁ' to CharacterPlacement(0.1f, -0.09f, false),
            'ぃ' to CharacterPlacement(0.08f, -0.09f, false),
            'ぅ' to CharacterPlacement(0.05f, -0.15f, false),
            'ぇ' to CharacterPlacement(0.05f, -0.15f, false),
            'ぉ' to CharacterPlacement(0.06f, -0.12f, false),
            'ゃ' to CharacterPlacement(0.07f, -0.12f, false),
            'ゅ' to CharacterPlacement(0.1f, -0.12f, false),
            'ょ' to CharacterPlacement(0.05f, -0.12f, false),
            'ッ' to CharacterPlacement(0.08f, -0.12f, false),
            'ァ' to CharacterPlacement(0.1f, -0.12f, false),
            'ィ' to CharacterPlacement(0.05f, -0.12f, false),
            'ゥ' to CharacterPlacement(0.08f, -0.13f, false),
            'ェ' to CharacterPlacement(0.07f, -0.12f, false),
            'ォ' to CharacterPlacement(0.07f, -0.12f, false),
            'ャ' to CharacterPlacement(0.1f, -0.12f, false),
            'ュ' to CharacterPlacement(0.09f, -0.15f, false),
            'ョ' to CharacterPlacement(0.09f, -0.13f, false),
            'ヵ' to CharacterPlacement(0.09f, -0.12f, false),
            'ヶ' to CharacterPlacement(0.09f, -0.12f, false),
            '0' to CharacterPlacement(0.26f, 0.0f, false),
            '1' to halfWidthCharPlacement,
            '2' to halfWidthCharPlacement,
            '3' to halfWidthCharPlacement,
            '4' to halfWidthCharPlacement,
            '5' to halfWidthCharPlacement,
            '6' to halfWidthCharPlacement,
            '7' to CharacterPlacement(0.27f, 0.0f, false),
            '8' to halfWidthCharPlacement,
            '9' to CharacterPlacement(0.28f, 0.0f, false),
        )
    }
}

