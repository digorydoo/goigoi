package io.github.digorydoo.goigoi.compiler.vocab

import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord.Companion.TRANSLATION_MAX_LEN
import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.IntlString
import ch.digorydoo.kutils.cjk.JLPTLevel
import io.github.digorydoo.kokuban.ttyBlue
import io.github.digorydoo.kokuban.ttyFaint
import io.github.digorydoo.kokuban.ttyGreen
import io.github.digorydoo.kokuban.ttyPlain
import io.github.digorydoo.kokuban.ttyYellow

class GoigoiPhrase {
    var primaryForm = FuriganaString()
    val kanji: String get() = primaryForm.kanji
    val kana: String get() = primaryForm.kana
    var romaji = ""
    var translation = IntlString()
    var explanation = IntlString()
    var level: JLPTLevel? = null
    var hasDifferentForm = false
    var allowSpaces: Boolean? = null // null = determine based on JLPT level
    var origin = ""
    var href = ""
    var remark = ""

    override fun toString() =
        "GoigoiPhrase($romaji, ${primaryForm.kanji})"

    fun prettyPrint(
        withRomaji: Boolean = false,
        withTranslation: Boolean = false,
        withInfo: Boolean = false,
        withLvl: Boolean = false,
        withColour: Boolean = true,
    ) {
        println(
            toPrettyString(
                withRomaji = withRomaji,
                withTranslation = withTranslation,
                withInfo = withInfo,
                withLvl = withLvl,
                withColour = withColour,
            )
        )
    }

    private fun toPrettyString(
        withRomaji: Boolean = false,
        withTranslation: Boolean = false,
        withInfo: Boolean = false,
        withLvl: Boolean = false,
        withColour: Boolean = true,
    ): String {
        fun StringBuilder.separator() {
            if (withColour) {
                append(ttyFaint("・"))
            } else {
                append("・")
            }
        }

        return buildString {
            append(primaryForm)

            if (withRomaji) {
                separator()

                if (withColour) {
                    append(ttyGreen(romaji))
                } else {
                    append(romaji)
                }
            }

            if (withLvl) {
                separator()

                if (withColour) {
                    append(ttyYellow("$level"))
                } else {
                    append(level)
                }
            }

            if (withTranslation) {
                separator()

                if (withColour) {
                    append(ttyFaint())
                }

                append(translation.en.let {
                    if (it.length <= TRANSLATION_MAX_LEN) {
                        it
                    } else {
                        it.slice(0 .. TRANSLATION_MAX_LEN - 2) + "…"
                    }
                })
            }

            if (withInfo) {
                separator()

                if (withColour) {
                    append(ttyBlue(explanation.en))
                } else {
                    append(explanation.en)
                }
            }

            if (withColour) {
                append(ttyPlain())
            }
        }
    }
}
