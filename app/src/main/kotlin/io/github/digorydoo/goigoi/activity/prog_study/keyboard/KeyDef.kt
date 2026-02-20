package io.github.digorydoo.goigoi.activity.prog_study.keyboard

import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.KeyLensPart
import ch.digorydoo.kutils.cjk.toSmallKana
import ch.digorydoo.kutils.cjk.toggleDakuten
import ch.digorydoo.kutils.cjk.toggleHandakuten

// KeyDef is immutable
class KeyDef(
    val mainText: String,
    val leftText: String = "",
    val aboveText: String = "",
    val rightText: String = "",
    val belowText: String = "",
    val mainAction: Action = if (mainText.isEmpty()) Action.NONE else Action.LITERAL,
    val leftAction: Action = if (leftText.isEmpty()) Action.NONE else Action.LITERAL,
    val aboveAction: Action = if (aboveText.isEmpty()) Action.NONE else Action.LITERAL,
    val rightAction: Action = if (rightText.isEmpty()) Action.NONE else Action.LITERAL,
    val belowAction: Action = if (belowText.isEmpty()) Action.NONE else Action.LITERAL,
    val mainIconResId: Int? = null,
    val leftIconResId: Int? = null,
    val rightIconResId: Int? = null,
    val contentDescResId: Int? = null,
) {
    enum class Action { NONE, LITERAL, AUTO_TRANSFORM, DAKUTEN, HANDAKUTEN, NORMAL_SIZE, SMALL_SIZE }

    class ActionAndText(val action: Action, val text: String)

    val shouldShowLens =
        leftAction != Action.NONE ||
            aboveAction != Action.NONE ||
            rightAction != Action.NONE ||
            belowAction != Action.NONE

    private val literals = mutableListOf<String>()
        .apply {
            if (mainAction == Action.LITERAL) add(mainText)
            if (leftAction == Action.LITERAL) add(leftText)
            if (aboveAction == Action.LITERAL) add(aboveText)
            if (rightAction == Action.LITERAL) add(rightText)
            if (belowAction == Action.LITERAL) add(belowText)
        }
        .filter { it.length == 1 }
        .map { it[0] }
        .toSet()

    fun getActionAndText(part: KeyLensPart) =
        when (part) {
            KeyLensPart.CENTRE -> ActionAndText(mainAction, mainText)
            KeyLensPart.LEFT -> ActionAndText(leftAction, leftText)
            KeyLensPart.ABOVE -> ActionAndText(aboveAction, aboveText)
            KeyLensPart.RIGHT -> ActionAndText(rightAction, rightText)
            KeyLensPart.BELOW -> ActionAndText(belowAction, belowText)
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
            leftIconResId = R.drawable.ic_tenten_24dp,
            leftAction = Action.DAKUTEN,
            aboveText = "大",
            aboveAction = Action.NORMAL_SIZE,
            rightIconResId = R.drawable.ic_maru_24dp,
            rightAction = Action.HANDAKUTEN,
            belowText = "小",
            belowAction = Action.SMALL_SIZE,
            mainIconResId = R.drawable.ic_tenten_maru_24dp,
            contentDescResId = R.string.transforms_key,
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
