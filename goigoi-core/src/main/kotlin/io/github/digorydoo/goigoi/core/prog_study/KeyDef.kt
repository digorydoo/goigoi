package io.github.digorydoo.goigoi.core.prog_study

import ch.digorydoo.kutils.cjk.toSmallKana
import ch.digorydoo.kutils.cjk.toggleDakuten
import ch.digorydoo.kutils.cjk.toggleHandakuten
import io.github.digorydoo.goigoi.core.prog_study.KeyActionHandler.Action

// KeyDef is immutable
class KeyDef private constructor(
    val mainText: String,
    val leftText: String = "",
    val topText: String = "",
    val rightText: String = "",
    val bottomText: String = "",
    val mainAction: Action = if (mainText.isEmpty()) Action.NONE else Action.LITERAL,
    val leftAction: Action = if (leftText.isEmpty()) Action.NONE else Action.LITERAL,
    val topAction: Action = if (topText.isEmpty()) Action.NONE else Action.LITERAL,
    val rightAction: Action = if (rightText.isEmpty()) Action.NONE else Action.LITERAL,
    val bottomAction: Action = if (bottomText.isEmpty()) Action.NONE else Action.LITERAL,
) {
    enum class KeyLensPart { CENTRE, LEFT, TOP, RIGHT, BOTTOM }

    @Deprecated("Only for legacy Keyboard")
    constructor(mainText: String): this(mainText = mainText, leftText = "")

    @Deprecated("Only for legacy Keyboard")
    val shouldShowLens =
        leftAction != Action.NONE ||
            topAction != Action.NONE ||
            rightAction != Action.NONE ||
            bottomAction != Action.NONE

    private val literals = mutableListOf<String>()
        .apply {
            if (mainAction == Action.LITERAL) add(mainText)
            if (leftAction == Action.LITERAL) add(leftText)
            if (topAction == Action.LITERAL) add(topText)
            if (rightAction == Action.LITERAL) add(rightText)
            if (bottomAction == Action.LITERAL) add(bottomText)
        }
        .filter { it.length == 1 }
        .map { it[0] }
        .toSet()

    fun getAction(part: KeyLensPart) =
        when (part) {
            KeyLensPart.CENTRE -> mainAction
            KeyLensPart.LEFT -> leftAction
            KeyLensPart.TOP -> topAction
            KeyLensPart.RIGHT -> rightAction
            KeyLensPart.BOTTOM -> bottomAction
        }

    fun getText(part: KeyLensPart) =
        when (part) {
            KeyLensPart.CENTRE -> mainText
            KeyLensPart.LEFT -> leftText
            KeyLensPart.TOP -> topText
            KeyLensPart.RIGHT -> rightText
            KeyLensPart.BOTTOM -> bottomText
        }

    companion object {
        val hiraganaVowelKey = KeyDef("あ", "い", "う", "え", "お")
        val hiraganaKxKey = KeyDef("か", "き", "く", "け", "こ")
        val hiraganaSxKey = KeyDef("さ", "し", "す", "せ", "そ")
        val hiraganaTxKey = KeyDef("た", "ち", "つ", "て", "と")
        val hiraganaNxKey = KeyDef("な", "に", "ぬ", "ね", "の")
        val hiraganaHxKey = KeyDef("は", "ひ", "ふ", "へ", "ほ")
        val hiraganaMxKey = KeyDef("ま", "み", "む", "め", "も")
        val hiraganaYxKey = KeyDef("や", "（", "ゆ", "）", "よ")
        val hiraganaRxKey = KeyDef("ら", "り", "る", "れ", "ろ")
        val hiraganaWxKey = KeyDef("わ", "を", "ん", "ー", "〜")

        val katakanaVowelKey = KeyDef("ア", "イ", "ウ", "エ", "オ")
        val katakanaKxKey = KeyDef("カ", "キ", "ク", "ケ", "コ")
        val katakanaSxKey = KeyDef("サ", "シ", "ス", "セ", "ソ")
        val katakanaTxKey = KeyDef("タ", "チ", "ツ", "テ", "ト")
        val katakanaNxKey = KeyDef("ナ", "ニ", "ヌ", "ネ", "ノ")
        val katakanaHxKey = KeyDef("ハ", "ヒ", "フ", "ヘ", "ホ")
        val katakanaMxKey = KeyDef("マ", "ミ", "ム", "メ", "モ")
        val katakanaYxKey = KeyDef("ヤ", "（", "ユ", "）", "ヨ")
        val katakanaRxKey = KeyDef("ラ", "リ", "ル", "レ", "ロ")
        val katakanaWxKey = KeyDef("ワ", "ヲ", "ン", "ー", "〜")

        val transformsKey = KeyDef(
            mainText = "",
            mainAction = Action.AUTO_TRANSFORM,
            leftAction = Action.DAKUTEN,
            topText = "大",
            topAction = Action.NORMAL_SIZE,
            rightAction = Action.HANDAKUTEN,
            bottomText = "小",
            bottomAction = Action.SMALL_SIZE,
        )
        val punctuationsKey = KeyDef("、", "。", "？", "！", "・")

        val supportedHiraganaAndPunctuation = mutableSetOf<Char>()
            .apply {
                addAll(hiraganaVowelKey.literals)
                addAll(hiraganaKxKey.literals)
                addAll(hiraganaSxKey.literals)
                addAll(hiraganaTxKey.literals)
                addAll(hiraganaNxKey.literals)
                addAll(hiraganaHxKey.literals)
                addAll(hiraganaMxKey.literals)
                addAll(hiraganaYxKey.literals)
                addAll(hiraganaRxKey.literals)
                addAll(hiraganaWxKey.literals)
                addAll(punctuationsKey.literals)

                val more = mutableSetOf<Char>()

                forEach {
                    more.add(it.toggleDakuten())
                    more.add(it.toggleHandakuten())
                    more.add(it.toSmallKana())
                }

                addAll(more)
            }
            .toSet()

        val supportedKatakanaAndPunctuation = mutableSetOf<Char>()
            .apply {
                addAll(katakanaVowelKey.literals)
                addAll(katakanaKxKey.literals)
                addAll(katakanaSxKey.literals)
                addAll(katakanaTxKey.literals)
                addAll(katakanaNxKey.literals)
                addAll(katakanaHxKey.literals)
                addAll(katakanaMxKey.literals)
                addAll(katakanaYxKey.literals)
                addAll(katakanaRxKey.literals)
                addAll(katakanaWxKey.literals)
                addAll(punctuationsKey.literals)

                val more = mutableSetOf<Char>()

                forEach {
                    more.add(it.toggleDakuten())
                    more.add(it.toggleHandakuten())
                    more.add(it.toSmallKana())
                }

                addAll(more)
            }
            .toSet()
    }
}
