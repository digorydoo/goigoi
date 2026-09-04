package io.github.digorydoo.goigoi.compiler.check.prechecks

import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.cjk.isHiragana
import io.github.digorydoo.goigoi.compiler.*
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiPhraseOrSentence
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiUnyt
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord

class PhraseOrSentenceChecker {
    private val sentenceRomajis = mutableSetOf<String>()
    private val phraseRomajis = mutableSetOf<String>()

    fun check(phors: GoigoiPhraseOrSentence, word: GoigoiWord, unyt: GoigoiUnyt) {
        val kindAsString = if (phors.isPhrase) "Phrase" else "Sentence"

        // Check mandatory members

        if (phors.primaryForm.isEmpty()) {
            throw CheckFailed("$kindAsString with empty primaryText!")
        }

        if (phors.translation.en.isEmpty()) {
            throw CheckFailed("$kindAsString with empty English translation")
        }

        unyt.requiredTranslations.forEach { langId ->
            if (phors.translation.withLanguage(langId).isEmpty()) {
                throw CheckFailed("$kindAsString missing required translation: $langId")
            }
        }

        // Check rōmaji

        if (phors.romaji.isEmpty()) {
            throw CheckFailed("$kindAsString has empty rōmaji")
        }

        checkRomaji(phors.primaryForm.raw, phors.romaji)

        val maxLen = when (phors.isPhrase) {
            true -> 42
            false -> 88
        }

        if (phors.romaji.length > maxLen) {
            throw CheckFailed(
                "$kindAsString with rōmaji of length ${phors.romaji.length} is longer than the allowed max ($maxLen)"
            )
        }

        if (phors.isPhrase) {
            if (phraseRomajis.contains(phors.romaji)) {
                throw CheckFailed("Phrases must be unique, but this phrase appears at least twice!")
            } else {
                phraseRomajis.add(phors.romaji)
            }
        } else {
            if (sentenceRomajis.contains(phors.romaji)) {
                throw CheckFailed("Sentences must be unique, but this sentence appears at least twice!")
            } else {
                sentenceRomajis.add(phors.romaji)
            }
        }

        // Check punctuation

        if (phors.isPhrase) {
            val punctuationChars = arrayOf('、', '。', '？', '!', '！')
            val hasPunctuation = punctuationChars.any { phors.primaryForm.raw.contains(it) }

            if (hasPunctuation) {
                throw CheckFailed(
                    "Phrase must not contain any of these characters: ${punctuationChars.joinToString("")}"
                )
            }
        } else {
            val endings = arrayOf("。", "？", "！", "〜", "···")
            val hasProperEnd = endings.any { phors.primaryForm.raw.endsWith(it) }

            if (!hasProperEnd) {
                throw CheckFailed("Sentence must end with one of these: ${endings.joinToString("")}")
            }
        }

        // Check spaces

        val hasNormalSpace = phors.primaryForm.raw.contains(' ')
        val hasFullWidthSpace = phors.primaryForm.raw.contains('　')

        val allowSpaces = phors.allowSpaces ?: when (phors.level) {
            JLPTLevel.N5 -> true
            JLPTLevel.N4 -> false
            JLPTLevel.N3 -> false
            JLPTLevel.N2 -> false
            JLPTLevel.N1 -> false
            else -> false
        }

        when {
            !allowSpaces && (hasNormalSpace || hasFullWidthSpace) -> {
                throw CheckFailed(
                    "$kindAsString must not contain any spaces since lvl=${phors.level}, allowSpaces=${phors.allowSpaces}"
                )
            }
            hasFullWidthSpace -> {
                throw CheckFailed("$kindAsString contains full-width space, should use normal spaces")
            }
        }

        // Check that the word appears in the sentence or phrase

        if (phors.wordFormToAsk.isEmpty()) {
            if (phors.wordFormToAskSuffix.isNotEmpty()) {
                throw CheckFailed("Stem must be defined when suffix is specified for word form")
            }

            var ck = word.primaryForm.raw

            if (word.usuallyInKana) {
                // The word is required to appear in kana in the sentence.
                // We expect the same kind of kana as the word's furigana (usually hiragana).
                // If the sentence uses a different kind of kana, it needs to declare hasDifferentForm.
                ck = word.primaryForm.kana
            }

            if (ck.isNotEmpty()) {
                val msgTail = "or use <ask> to declare the word form"

                if (phors.primaryForm.contains(ck)) {
                    // The word was found in the sentence. However, if we were looking for the kana,
                    // we want to make sure that we didn't find the kana within the furigana bracket.
                    if (word.usuallyInKana && phors.primaryForm.contains(word.primaryForm.raw)) {
                        throw CheckFailed(
                            "$kindAsString must use the word in kana as stated by usuallyInKana $msgTail"
                        )
                    }
                } else if (word.usuallyInKana) {
                    throw CheckFailed("$kindAsString must contain the word in hiragana only $msgTail")
                } else {
                    throw CheckFailed("$kindAsString must contain the word it is associated with $msgTail")
                }
            }
        } else {
            // The reason why we declare the wordForm is that the app needs to find the word in the phrase or
            // sentence in order to be able to remove it with a placeholder the user is supposed to fill in.

            val wordFormToAsk = phors.wordFormToAsk
            val kanaToAsk = wordFormToAsk.kana
            val kanjiToAsk = wordFormToAsk.kanji
            val suffix = phors.wordFormToAskSuffix

            if ((!word.usuallyInKana && wordFormToAsk.raw == word.primaryForm.raw) ||
                (word.usuallyInKana && wordFormToAsk.raw == word.kana)
            ) {
                throw CheckFailed("Do not specify word form when it's identical to word kanji form")
            }

            if (!phors.primaryForm.raw.contains(wordFormToAsk.raw + suffix)) {
                throw CheckFailed("Word form does not appear like this: ${wordFormToAsk.raw + suffix}")
            }

            if (kanaToAsk.length + suffix.length > MAX_WORD_FORM_LENGTH) {
                throw CheckFailed(
                    "Form is too long: allowed=$MAX_WORD_FORM_LENGTH, actual=${kanaToAsk.length + suffix.length}"
                )
            }

            if (word.usuallyInKana) {
                if (kanaToAsk.contains(word.kana)) {
                    if (word.kanji == word.kana || !wordFormToAsk.raw.contains(word.primaryForm.raw)) {
                        throw CheckFailed("Custom form containing the unchanged word is pointless")
                    }
                }
            } else if (wordFormToAsk.raw.contains(word.primaryForm.raw)) {
                throw CheckFailed("Custom form containing the unchanged word is pointless")
            }

            val kanjiAndSuffix = kanjiToAsk + suffix
            val kanjiFormCount = phors.kanji.windowed(kanjiAndSuffix.length).count { it == kanjiAndSuffix }

            if (kanjiFormCount != 1) {
                throw CheckFailed(
                    "Expected kanji form $kanjiAndSuffix to occur exactly once, but found $kanjiFormCount occurrences"
                )
            }

            val kanaAndSuffix = kanaToAsk + suffix
            val kanaFormCount = phors.kana.windowed(kanaAndSuffix.length).count { it == kanaAndSuffix }

            if (kanaFormCount != 1) {
                throw CheckFailed(
                    "Expected kana form $kanaAndSuffix to occur exactly once, but found $kanaFormCount occurrences"
                )
            }

            val hasTeIru = arrayOf("ている", "ています", "ていた", "ていました").any { kanaToAsk.endsWith(it) }

            if (hasTeIru) {
                throw CheckFailed("Form should stop at the て-form")
            }

            if (kanaToAsk.endsWith("なさい") && word.kana != "なさる") {
                throw CheckFailed("Form should not include なさい")
            }

            // Depending on how the app is going to ask, some forms are ambiguous. For instance, one can often exchange
            // the polite and plain forms to get a correct sentence. But they cannot always be exchanged, because the
            // grammar requires plain form, or other parts of the sentence already indicate polite form. To make the
            // question non-ambiguous, we require that those forms are split into stem + suffix. When the app is asking
            // full words, it can ask the combined stem + suffix like before, but if it is asking the user to enter
            // text, it can restrict it to ask the stem only, which should be non-ambiguous.

            if (suffix.isEmpty()) {
                val needsSplit = arrayOf("ます", "ません", "ました", "ませんでした", "ましょう")
                    .any { kanaToAsk.endsWith(it) }

                if (needsSplit) {
                    throw CheckFailed("Form should be split into stem and suffix")
                }
            } else {
                if (!suffix.isHiragana() || suffix.contains('【')) {
                    throw CheckFailed("Suffix must be hiragana-only (okurigana or part of okurigana)")
                }
            }
        }

        // Check punctuation

        if (!phors.isPhrase) {
            when {
                phors.primaryForm.raw.endsWith("ですか？") ||
                    phors.primaryForm.raw.endsWith("でしたか？") ||
                    phors.primaryForm.raw.endsWith("でしょうか？") ||
                    phors.primaryForm.raw.endsWith("ますか？") ||
                    phors.primaryForm.raw.endsWith("ませんか？") ||
                    phors.primaryForm.raw.endsWith("ましたか？") ||
                    phors.primaryForm.raw.endsWith("ましょうか？") ||
                    phors.primaryForm.raw.endsWith("こうか？") ||
                    phors.primaryForm.raw.endsWith("ようか？") ||
                    phors.primaryForm.raw.endsWith("のか？") ->
                    throw CheckFailed("Questions ending in か should end with 。")

                phors.primaryForm.raw.endsWith("ですか。") ||
                    phors.primaryForm.raw.endsWith("でしたか。") ||
                    phors.primaryForm.raw.endsWith("でしょうか。") ||
                    phors.primaryForm.raw.endsWith("ますか。") ||
                    phors.primaryForm.raw.endsWith("ませんか。") ||
                    phors.primaryForm.raw.endsWith("ましたか。") ||
                    phors.primaryForm.raw.endsWith("ましょうか。") ||
                    phors.primaryForm.raw.endsWith("こうか。") ||
                    phors.primaryForm.raw.endsWith("ようか。") ||
                    phors.primaryForm.raw.endsWith("のか。") ->
                    when {
                        !phors.romaji.endsWith("?") -> throw CheckFailed("Missing question mark in rōmaji")
                    }

                phors.romaji.endsWith("?") && phors.primaryForm.raw.endsWith("。") && !phors.primaryForm.raw.endsWith("か。") ->
                    throw CheckFailed("Questions not ending in か must use a question mark")

                phors.romaji.endsWith(".") && !phors.primaryForm.raw.endsWith("。") ->
                    throw CheckFailed("Missing or wrong punctuation at end of line")
            }
        }

        // The level of sentences and phrases must not be easier than the level of the word it is contained in. We
        // allow sentences and phrases to be harder than the word, though.

        if (word.level != null && word.level != phors.level) {
            val wl = word.level?.toInt() ?: 0
            val sl = phors.level?.toInt() ?: 0

            if (wl < sl) {
                throw CheckFailed("Level of $kindAsString cannot be easier than the word it is associated to")
            }
        }

        // Enforce a consistent use of certain firstnames and surnames.

        val hasSurname = knownSurnames.any { (surname, _) -> phors.romaji.contains(surname) }
        val englishAbbrev = arrayOf("Mr", "Mrs", "Ms", "Miss", "Professor")
        val hasEnglishAbbrev = englishAbbrev.any { phors.translation.en.contains(it) }

        if (hasEnglishAbbrev && !hasSurname) {
            throw CheckFailed(
                "$kindAsString appears to refer to a surname. " +
                    "Please use one of: ${knownSurnames.keys.joinToString(", ")}"
            )
        }

        knownSurnames.forEach { (surname, details) ->
            if (phors.romaji.contains(surname)) {
                val suffixFound = details.suffices.any { suffix ->
                    phors.romaji.contains("$surname-${suffix}")
                }

                var surnameUsage: SurnameUsage? = null

                if (suffixFound) {
                    surnameUsage = SurnameUsage.SUFFIX
                } else {
                    if (details.knownFirstname == null) {
                        if (details.suffixRequired) {
                            throw CheckFailed(
                                "Surname $surname expected to have one of suffices: " +
                                    details.suffices.joinToString(", ")
                            )
                        }
                    } else {
                        if (phors.romaji.contains("$surname ${details.knownFirstname}")) {
                            surnameUsage = SurnameUsage.FIRSTNAME
                        } else if (phors.romaji.contains("${details.knownFirstname} $surname")) {
                            throw CheckFailed(
                                "Firstname ${details.knownFirstname} should come after surname $surname in rōmaji"
                            )
                        } else if (details.suffixRequired) {
                            throw CheckFailed(
                                "Surname $surname should either use a suffix (" +
                                    details.suffices.joinToString(", ") +
                                    ") or the known firstname ${details.knownFirstname}"
                            )
                        }
                    }
                }

                if (surnameUsage == SurnameUsage.SUFFIX) {
                    // A suffix in rōmaji must be accompanied by a prefix in the English translation.
                    val hasPrefix = details.prefixes.any { prefix -> phors.translation.en.contains("$prefix $surname") }

                    if (!hasPrefix) {
                        throw CheckFailed(
                            "Surname $surname is expected to be used with prefix: " +
                                details.prefixes.joinToString(", ")
                        )
                    }
                }
            }
        }

        val allSuffixes = arrayOf("san", "kun", "chan")

        knownFirstnames.forEach { (firstname, details) ->
            if (phors.romaji.contains(firstname)) {
                val hasEnglishAbbrevForThis = englishAbbrev.any { phors.translation.en.contains("$it $firstname") }

                if (hasEnglishAbbrevForThis) {
                    throw CheckFailed(
                        "Firstname $firstname should not be used with: ${englishAbbrev.joinToString(", ")}"
                    )
                }

                val hasSuffix = phors.romaji.contains("$firstname-${details.suffix}")

                if (!hasSuffix) {
                    val hasImproperSuffix = phors.romaji.contains("$firstname ${details.suffix}") ||
                        phors.romaji.contains("$firstname${details.suffix}")

                    if (hasImproperSuffix) {
                        throw CheckFailed("Suffix for firstname used improperly, should use $firstname-(suffix)")
                    }

                    val hasOtherSuffix = allSuffixes.any { suffix ->
                        phors.romaji.contains("$firstname-$suffix") ||
                            phors.romaji.contains("$firstname $suffix") ||
                            phors.romaji.contains("$firstname$suffix")
                    }

                    if (hasOtherSuffix) {
                        throw CheckFailed("Firstname $firstname expected to use suffix ${details.suffix} or no suffix")
                    }
                }
            }
        }

        // Check origin, and that remark does not contain the origin.

        if (phors.origin.isNotEmpty() && !allowedOrigins.any { it.matches(phors.origin) }) {
            val msg = "   " + allowedOrigins.joinToString("\n   ")
            throw CheckFailed("Origin (${phors.origin}) should be one of the allowed origins\n${msg}")
        }

        if (phors.remark.isNotEmpty()) {
            if (phors.remark.contains("local db")) {
                throw CheckFailed("Remark should not contain deprecated origin: ${phors.remark}")
            }

            phors.remark.split(';')
                .map { it.trim() }
                .forEach { part ->
                    if (allowedOrigins.any { it.matches(part) }) {
                        throw CheckFailed(
                            "Remark contains a part that looks like an origin; use origin instead: $part"
                        )
                    }
                }
        }

        // Check href, and that remark does not contain any hrefs.

        if (phors.href.isNotEmpty() && !phors.href.startsWith("https://")) {
            throw CheckFailed("href should start with https:// prefix: ${phors.href}")
        }

        if (phors.remark.contains("http")) {
            throw CheckFailed("Remark should not contain any hrefs, use href instead: ${phors.remark}")
        }
    }

    companion object {
        private const val MAX_WORD_FORM_LENGTH = 10
    }
}
