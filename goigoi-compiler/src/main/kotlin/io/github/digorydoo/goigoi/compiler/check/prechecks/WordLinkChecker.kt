package io.github.digorydoo.goigoi.compiler.check.prechecks

import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.cjk.isHiragana
import ch.digorydoo.kutils.cjk.isKatakana
import io.github.digorydoo.goigoi.compiler.CheckFailed
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiUnyt
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWordLink

class WordLinkChecker {
    private data class UnytAndWord(val unyt: GoigoiUnyt, val word: GoigoiWord)

    fun check(link: GoigoiWordLink, word: GoigoiWord, unyt: GoigoiUnyt, vocab: GoigoiVocab) {
        // Check that the link doesn't point to its own word

        if (link.wordId == word.id) {
            throw CheckFailed("Link must not refer to its own word!")
        }

        if (link.kind == GoigoiWordLink.Kind.XML_SEE_ALSO) {
            // Check see-also link's remark.

            val allowedRem = arrayOf(
                "v.i.",
                "v.t.",
                "noun",
                "verb",
                "adjective",
                "closely related",
                "antonym",
                "not auto-generated due to honorific prefix"
            )

            if (link.remark.isEmpty()) {
                throw CheckFailed(
                    "See-also link has no remark!\n  But we expected one of: ${allowedRem.joinToString(", ")}",
                )
            } else if (!allowedRem.contains(link.remark)) {
                throw CheckFailed(
                    "See-also link has remark: ${link.remark}\n  But we expected one of: ${allowedRem.joinToString(", ")}"
                )
            }

            val checkHint = when (link.remark) {
                "v.i." -> "v.t."
                "v.t." -> "v.i."
                else -> ""
            }

            if (checkHint.isNotEmpty() && !word.hint.en.contains(checkHint) && word.hint2?.en != checkHint) {
                throw CheckFailed(
                    "See-also link (rem=${link.remark}) requires that its own word be marked as $checkHint in hint_en"
                )
            }
        }

        when (link.kind) {
            GoigoiWordLink.Kind.XML_SEE_ALSO -> checkSeeAlso(link, word, vocab)
            GoigoiWordLink.Kind.XML_KEEP_APART -> checkKeepApart(
                link,
                word,
                unyt,
                vocab
            )
            GoigoiWordLink.Kind.XML_KEEP_TOGETHER -> checkKeepTogether(
                link,
                word,
                unyt,
                vocab
            )
            else -> throw CheckFailed("Unexpected kind of link: ${link.kind}")
        }
    }

    private fun checkSeeAlso(see: GoigoiWordLink, fromWord: GoigoiWord, vocab: GoigoiVocab) {
        var foundLinkedWord = false
        val counterparts = mutableListOf<UnytAndWord>()

        vocab.forEachWordWithId(see.wordId) { toWord, toUnyt, _ ->
            if (fromWord == toWord) {
                throw CheckFailed("See-also link must not refer to its own word!")
            } else if (see.wordId != toWord.id) {
                throw CheckFailed("Bug here: See-also link id=${see.wordId} should be equal to toWord.id=${toWord.id}!")
            } else {
                foundLinkedWord = true

                // Check that see-also link is mentioned by both sides.

                val backLink = toWord.links.firstOrNull {
                    it.wordId == fromWord.id && it.kind == GoigoiWordLink.Kind.XML_SEE_ALSO
                }

                if (backLink == null) {
                    throw CheckFailed(
                        "See-also link must be mentioned by both words!\n" +
                            "   word with link:    ${fromWord.id}\n" +
                            "   word without link: ${see.wordId}"
                    )
                }

                // Check that pairs of see-also links are properly marked in rem.

                val expectedBackRem = when (see.remark) {
                    "v.t." -> arrayOf("v.i.")
                    "v.i." -> arrayOf("v.t.")
                    "noun" -> arrayOf("verb", "adjective")
                    "verb" -> arrayOf("noun", "adjective")
                    "adjective" -> arrayOf("noun", "verb")
                    else -> arrayOf(see.remark) // i.e. remarks must be same
                }

                if (!expectedBackRem.contains(backLink.remark)) {
                    if (expectedBackRem.size == 1) {
                        throw CheckFailed(
                            "See-also linked with rem=${see.remark}\n" +
                                "   requires that the back-link have rem=${expectedBackRem[0]},\n" +
                                "   but found: ${backLink.remark}"
                        )
                    } else {
                        throw CheckFailed(
                            "See-also linked with rem=${see.remark}\n" +
                                "   requires that back-link's rem be one of: ${expectedBackRem.joinToString(", ")},\n" +
                                "   but found: ${backLink.remark}"
                        )
                    }
                }

                // If this see-also link is marked as v.t., then fromWord must be toWord's counterpart.
                val isCounterPart = arrayOf("v.t.", "v.i").contains(see.remark)

                if (isCounterPart) {
                    counterparts.add(UnytAndWord(toUnyt, toWord))
                }

                // Some see-also links require that the toWord be marked in its hint.

                if (see.remark == "v.t." || see.remark == "v.i.") {
                    if (!toWord.hint.en.contains(see.remark) && toWord.hint2?.en != see.remark) {
                        throw CheckFailed(
                            "See-also link has rem=${see.remark}, but the linked word does not mention this in " +
                                "its hint_en!"
                        )
                    }
                }

                // Some see-also links require that both words use the same kanjis.

                if (
                    see.remark == "v.t." ||
                    see.remark == "v.i." ||
                    see.remark == "verb" ||
                    see.remark == "noun" ||
                    see.remark == "adjective"
                ) {
                    val kanji1 = fromWord.kanji.filter { c -> !c.isHiragana() && !c.isKatakana() }
                    val kanji2 = toWord.kanji.filter { c -> !c.isHiragana() && !c.isKatakana() }

                    if (kanji1 != kanji2) {
                        throw CheckFailed(
                            "See-also link is marked as ${see.remark}, but the kanjis differ!\n" +
                                "   fromWord has kanji $kanji1\n   toWord has kanji $kanji2"
                        )
                    }
                }
            }
        }

        if (!foundLinkedWord) {
            throw CheckFailed("See-also link refers to id=${see.wordId}, but there is no word with that id!")
        }

        // Check that counterparts be kept in the same unyt if possible.

        if (counterparts.isNotEmpty()) {
            // The counterpart of a v.t. is v.i., but there may be multiple instances in different unyts.
            // Those instances are expected to share the same id, and their levels should be the same.

            var otherId = ""
            var otherLevel: JLPTLevel? = null

            counterparts.forEach { other ->
                if (otherId == "") {
                    otherId = other.word.id
                } else if (otherId != other.word.id) {
                    throw CheckFailed("The counterparts of see-also link ${see.remark} do not share a common id!")
                }

                if (otherLevel == null) {
                    otherLevel = other.word.level ?: JLPTLevel.Nx
                } else if (otherLevel != other.word.level) {
                    throw CheckFailed("The counterparts of see-also link ${see.remark} do not have a common JLPT level!")
                }
            }

            require(otherId != "" && otherLevel != null) // otherwise something's odd
            require(otherId != fromWord.id) // otherwise we have a see-also link that links to its own word

            if (otherLevel != JLPTLevel.Nx) {
                if (fromWord.links.none { it.wordId == otherId && it.kind == GoigoiWordLink.Kind.XML_KEEP_APART }) {
                    val fromCtx = arrayOf(fromWord.hint.en, fromWord.hint2?.en)
                        .filter { it != "" }
                        .filterNotNull()
                        .joinToString("; ")
                    checkWordsInSameUnyt(vocab, fromWord, fromCtx, otherId, otherLevel, see.remark)
                }
            }
        }
    }

    private fun checkKeepApart(
        see: GoigoiWordLink,
        fromWord: GoigoiWord,
        fromUnyt: GoigoiUnyt,
        vocab: GoigoiVocab,
    ) {
        var otherFound = false
        var missingBackLink: GoigoiWord? = null
        var unytOfWordMissingBackLink: GoigoiUnyt? = null

        vocab.forEachWordWithId(see.wordId) { otherWord, otherUnyt, _ ->
            otherFound = true

            if (otherUnyt == fromUnyt && !fromUnyt.hidden && !fromWord.hidden && !otherWord.hidden) {
                throw CheckFailed(
                    "Word with ids ${fromWord.id} and ${see.wordId} are not supposed to appear in the same unyt!"
                )
            }

            if (otherWord.links.none { it.wordId == fromWord.id && it.kind == GoigoiWordLink.Kind.XML_KEEP_APART }) {
                missingBackLink = otherWord
                unytOfWordMissingBackLink = otherUnyt
            }
        }

        if (!otherFound) {
            throw CheckFailed("Word links to id=${see.wordId} via keep_apart_from, but there is no such word!")
        }

        if (missingBackLink != null) {
            throw CheckFailed(
                "keep_apart_from link must be mentioned by both words!\n" +
                    "   has link: ${fromWord.id}\n" +
                    "   hasn't:   ${missingBackLink.id} (" +
                    unytOfWordMissingBackLink?.filename?.ifEmpty { unytOfWordMissingBackLink.id } +
                    ")"
            )
        }
    }

    private fun checkKeepTogether(
        see: GoigoiWordLink,
        fromWord: GoigoiWord,
        fromUnyt: GoigoiUnyt,
        vocab: GoigoiVocab,
    ) {
        var otherFound = false
        var missingBackLink: GoigoiWord? = null
        var unytOfWordMissingBackLink: GoigoiUnyt? = null

        vocab.forEachWordWithId(see.wordId) { otherWord, otherUnyt, _ ->
            otherFound = true

            if (otherUnyt != fromUnyt) {
                throw CheckFailed("Word with ids ${fromWord.id} and ${see.wordId} should be kept in the same unyt!")
            }

            var backLinkFound = false

            otherWord.links.forEach {
                if (it.wordId == fromWord.id && it.kind == GoigoiWordLink.Kind.XML_KEEP_TOGETHER) {
                    if (backLinkFound) {
                        throw CheckFailed("Multiple back-links from word ${fromWord.id} to ${see.wordId} found")
                    }

                    backLinkFound = true

                    if (!fromWord.hidden && !otherWord.hidden) {
                        if (fromWord.cats.firstOrNull() != otherWord.cats.firstOrNull()) {
                            throw CheckFailed(
                                "keep_together between ${fromWord.id} and ${otherWord.id} requires that their first " +
                                    "category be the same!\n" +
                                    "   ${fromWord.id} has categories ${fromWord.cats.joinToString(", ")}\n" +
                                    "   ${otherWord.id} has categories ${otherWord.cats.joinToString(", ")}"
                            )
                        }
                    }
                }
            }

            if (!backLinkFound) {
                missingBackLink = otherWord
                unytOfWordMissingBackLink = otherUnyt
            }
        }

        if (!otherFound) {
            throw CheckFailed("Word links to id=${see.wordId} via keep_together, but there is no such word!")
        }

        if (missingBackLink != null) {
            throw CheckFailed(
                "keep_together link must be mentioned by both words!\n" +
                    "   has link: ${fromWord.id}\n" +
                    "   hasn't:   ${missingBackLink.id} (" +
                    unytOfWordMissingBackLink?.filename?.ifEmpty { unytOfWordMissingBackLink.id } +
                    ")"
            )
        }
    }

    private fun checkWordsInSameUnyt(
        vocab: GoigoiVocab,
        fromWord: GoigoiWord,
        fromCtx: String,
        otherId: String, // passing a wordId here, because there may be multiple instances
        otherLevel: JLPTLevel,
        otherCtx: String,
    ) {
        // Look for any unyt that has both ids.
        var unytThatHasFromWord: GoigoiUnyt? = null
        var unytThatHasOtherWord: GoigoiUnyt? = null
        var foundPair = false

        vocab.forEachUnyt { u, _ ->
            if (!foundPair) {
                var foundFromWord = false
                var foundOtherWord = false

                u.forEachWord { w, _ ->
                    if (w.id == fromWord.id) {
                        foundFromWord = true

                        if (unytThatHasFromWord == null) {
                            unytThatHasFromWord = u
                        } else if (u.levels.size == 1) {
                            unytThatHasFromWord = u // prefer this unyt in error message
                        }
                    }
                    if (w.id == otherId) {
                        foundOtherWord = true

                        if (unytThatHasOtherWord == null) {
                            unytThatHasOtherWord = u
                        } else if (u.levels.size == 1) {
                            unytThatHasOtherWord = u // prefer this unyt in error message
                        }
                    }
                }

                if (foundFromWord && foundOtherWord) {
                    foundPair = true
                }
            }
        }

        if (!foundPair) {
            val msg = arrayOf(
                "   $fromCtx: ${fromWord.level} ${fromWord.id} (${unytThatHasFromWord?.name?.en})",
                "   $otherCtx: $otherLevel $otherId (${unytThatHasOtherWord?.name?.en})",
            ).joinToString("\n")

            if (fromWord.level == otherLevel) {
                throw CheckFailed(
                    "Counterpart of see-also link should be kept in the same unyt" +
                        " when their JLPT levels allow it!\n$msg"
                )
            } else {
                vocab.warnings.add(
                    "Hint: Counterpart of see-also link could be kept in the same unyt" +
                        " if their JLPT levels allowed it\n$msg"
                )
            }
        }
    }
}
