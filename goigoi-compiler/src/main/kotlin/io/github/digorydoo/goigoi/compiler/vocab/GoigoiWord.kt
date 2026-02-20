package io.github.digorydoo.goigoi.compiler.vocab

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.IntlString
import ch.digorydoo.kutils.cjk.JLPTLevel
import io.github.digorydoo.goigoi.core.StudyInContextKind
import io.github.digorydoo.goigoi.core.WordCategory
import io.github.digorydoo.goigoi.core.WordHint
import io.github.digorydoo.kokuban.ttyBlue
import io.github.digorydoo.kokuban.ttyFaint
import io.github.digorydoo.kokuban.ttyGreen
import io.github.digorydoo.kokuban.ttyPlain
import io.github.digorydoo.kokuban.ttyYellow

class GoigoiWord {
    var id = ""
    var primaryForm = FuriganaString()
    val kanji: String get() = primaryForm.kanji
    val kana: String get() = primaryForm.kana
    var romaji = ""
    var translation = IntlString()
    var dictionaryWord = ""
    var hint = IntlString()
    var hint2: WordHint? = null
    var level: JLPTLevel? = null
    var deLangenscheidt = ""
    var href = ""
    var remark = ""
    var hasCustomId = false
    var studyInContext = StudyInContextKind.NOT_REQUIRED
    var usuallyInKana = false
    var hidden = false
    var common: Boolean? = null // corresponds to jisho's common word flag (true=green, null=our XML lacks the flag)
    var fileName = ""
    val synonyms = mutableListOf<FuriganaString>()
    val phrases = mutableListOf<GoigoiPhrase>()
    val sentences = mutableListOf<GoigoiPhrase>()
    val links = mutableListOf<GoigoiWordLink>()
    val cats = mutableSetOf<WordCategory>()

    override fun toString() =
        "GoigoiWord(w=${primaryForm}, rom=${romaji}, lvl=$level, id=${id})"

    fun prettyPrint(
        withRomaji: Boolean = false,
        withTranslation: Boolean = false,
        withHint: Boolean = false,
        withLvl: Boolean = false,
        withFileName: Boolean = false,
        withKanjiKanaSeparated: Boolean = false,
        withColour: Boolean = true,
        withId: Boolean = false,
    ) {
        println(
            toPrettyString(
                withRomaji = withRomaji,
                withTranslation = withTranslation,
                withHint = withHint,
                withLvl = withLvl,
                withFileName = withFileName,
                withKanjiKanaSeparated = withKanjiKanaSeparated,
                withColour = withColour,
                withId = withId,
            )
        )
    }

    fun toPrettyString(
        withRomaji: Boolean = false,
        withTranslation: Boolean = false,
        withHint: Boolean = false,
        withLvl: Boolean = false,
        withFileName: Boolean = false,
        withKanjiKanaSeparated: Boolean = false,
        withColour: Boolean = true,
        withId: Boolean = false,
        withCats: Boolean = false,
    ): String {
        fun StringBuilder.separator() {
            if (withColour) {
                append(ttyFaint("・"))
            } else {
                append("・")
            }
        }

        return buildString {
            if (withKanjiKanaSeparated) {
                append(kanji)
                separator()
                append(kana)
            } else {
                append(primaryForm)
            }

            if (withRomaji) {
                separator()

                if (withColour) {
                    append(ttyGreen(romaji))
                } else {
                    append(romaji)
                }
            }

            if (withId) {
                separator()

                if (withColour) {
                    append(ttyGreen(id))
                } else {
                    append(id)
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

            if (withHint) {
                separator()
                val enHints = arrayOf(hint.en, hint2?.en).filterNotNull().joinToString("; ")

                if (withColour) {
                    append(ttyBlue(enHints))
                } else {
                    append(enHints)
                }
            }

            if (withFileName) {
                separator()

                if (withColour) {
                    append(ttyFaint(fileName))
                } else {
                    append(fileName)
                }
            }

            if (withCats) {
                separator()

                if (withColour) {
                    append(ttyFaint(cats.joinToString(", ") { it.text }))
                } else {
                    append(cats.joinToString(", ") { it.text })
                }
            }

            if (withColour) {
                append(ttyPlain())
            }
        }
    }

    companion object {
        const val TRANSLATION_MAX_LEN = 82
    }
}
