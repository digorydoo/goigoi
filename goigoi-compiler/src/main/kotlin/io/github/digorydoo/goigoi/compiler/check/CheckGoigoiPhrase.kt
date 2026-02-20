package io.github.digorydoo.goigoi.compiler.check

import io.github.digorydoo.goigoi.compiler.*
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiPhrase
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiUnyt
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import ch.digorydoo.kutils.cjk.JLPTLevel

fun GoigoiPhrase.checkPhrase(requiredTranslations: List<String>, word: GoigoiWord, unyt: GoigoiUnyt) {
    checkSentenceOrPhrase(this, true, requiredTranslations, word, unyt)
}

fun GoigoiPhrase.checkSentence(requiredTranslations: List<String>, word: GoigoiWord, unyt: GoigoiUnyt) {
    checkSentenceOrPhrase(this, false, requiredTranslations, word, unyt)
}

private fun checkSentenceOrPhrase(
    s: GoigoiPhrase,
    isPhrase: Boolean,
    requiredTranslations: List<String>,
    word: GoigoiWord,
    unyt: GoigoiUnyt,
) {
    val kind = if (isPhrase) "Phrase" else "Sentence"

    // Check mandatory members

    if (s.primaryForm.isEmpty()) {
        throw CheckFailed("$kind with empty primaryText!", unyt, word, s)
    }

    if (s.translation.en.isEmpty()) {
        throw CheckFailed("$kind with empty English translation", unyt, word, s)
    }

    requiredTranslations.forEach { langId ->
        if (s.translation.withLanguage(langId).isEmpty()) {
            throw CheckFailed("$kind missing required translation: $langId", unyt, word, s)
        }
    }

    // Check rōmaji

    if (s.romaji.isEmpty()) {
        throw CheckFailed("$kind has empty rōmaji", unyt, word, s)
    }

    checkRomaji(s.primaryForm.raw, s.romaji, unyt, word)

    val maxLen = when (isPhrase) {
        true -> 38
        false -> 88
    }

    if (s.romaji.length > maxLen) {
        throw CheckFailed(
            "$kind with rōmaji of length ${s.romaji.length} is longer than the allowed maximum ($maxLen)",
            unyt,
            word,
            s
        )
    }

    // Check punctuation

    if (isPhrase) {
        val punctuationChars = arrayOf('、', '。', '？', '!', '！')
        val hasPunctuation = punctuationChars.any { s.primaryForm.raw.contains(it) }

        if (hasPunctuation) {
            throw CheckFailed(
                "Phrase must not contain any of these characters: ${punctuationChars.joinToString("")}",
                unyt,
                word,
                s
            )
        }
    } else {
        val endings = arrayOf("。", "？", "！", "〜", "···")
        val hasProperEnd = endings.any { s.primaryForm.raw.endsWith(it) }

        if (!hasProperEnd) {
            throw CheckFailed("Sentence must end with one of these: ${endings.joinToString("")}", unyt, word, s)
        }
    }

    // Check spaces

    val hasNormalSpace = s.primaryForm.raw.contains(' ')
    val hasFullWidthSpace = s.primaryForm.raw.contains('　')

    val allowSpaces = s.allowSpaces ?: when (s.level) {
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
                "$kind must not contain any spaces since lvl=${s.level}, allowSpaces=${s.allowSpaces}",
                unyt,
                word,
                s
            )
        }
        hasFullWidthSpace -> {
            throw CheckFailed("$kind contains full-width space, should use normal spaces", unyt, word, s)
        }
    }

    if (!s.hasDifferentForm) {
        // Check that the word.primaryForm appears in the sentence or phrase.

        var ck = word.primaryForm.raw

        if (word.usuallyInKana) {
            // The word is required to appear in kana in the sentence.
            // We expect the same kind of kana as the word's furigana (usually hiragana).
            // If the sentence uses a different kind of kana, it needs to declare hasDifferentForm.
            ck = word.primaryForm.kana
        }

        if (ck.endsWith("をする")) {
            // A suru verb. Check the stem only.
            ck = ck.slice(0 ..< ck.length - 3)
        } else if (ck.endsWith("する")) {
            // Probably a suru verb.
            ck = ck.slice(0 ..< ck.length - 2)
        } else if (
            ck.endsWith("う") ||
            ck.endsWith("く") ||
            ck.endsWith("ぐ") ||
            ck.endsWith("す") ||
            ck.endsWith("つ") ||
            ck.endsWith("ぬ") ||
            ck.endsWith("ぶ") ||
            ck.endsWith("む") ||
            ck.endsWith("る")
        ) {
            // Probably a verb.
            ck = ck.slice(0 ..< ck.length - 1)
        } else if (ck.endsWith("い")) {
            // Probably an adjective.
            ck = ck.slice(0 ..< ck.length - 1)
        }

        if (ck.isNotEmpty()) {
            if (s.primaryForm.contains(ck)) {
                // The word was found in the sentence. However, if we were looking for the kana,
                // we want to make sure that we didn't find the kana within the furigana bracket.
                if (word.usuallyInKana && s.primaryForm.contains(word.primaryForm.raw)) {
                    throw CheckFailed("$kind should use the word in kana as stated by usuallyInKana", unyt, word, s)
                }
            } else if (word.usuallyInKana) {
                throw CheckFailed(
                    "$kind must contain the word in hiragana only, or declare hasDifferentForm",
                    unyt,
                    word,
                    s
                )
            } else {
                throw CheckFailed(
                    "$kind must contain the word it is associated with, or declare hasDifferentForm",
                    unyt,
                    word,
                    s
                )
            }
        }
    }

    if (!isPhrase) {
        when {
            s.primaryForm.raw.endsWith("ですか？") ||
                s.primaryForm.raw.endsWith("でしたか？") ||
                s.primaryForm.raw.endsWith("でしょうか？") ||
                s.primaryForm.raw.endsWith("ますか？") ||
                s.primaryForm.raw.endsWith("ませんか？") ||
                s.primaryForm.raw.endsWith("ましたか？") ||
                s.primaryForm.raw.endsWith("ましょうか？") ||
                s.primaryForm.raw.endsWith("こうか？") ||
                s.primaryForm.raw.endsWith("ようか？") ||
                s.primaryForm.raw.endsWith("のか？") ->
                throw CheckFailed("Questions ending in か should end with 。", unyt, word, s)

            s.primaryForm.raw.endsWith("ですか。") ||
                s.primaryForm.raw.endsWith("でしたか。") ||
                s.primaryForm.raw.endsWith("でしょうか。") ||
                s.primaryForm.raw.endsWith("ますか。") ||
                s.primaryForm.raw.endsWith("ませんか。") ||
                s.primaryForm.raw.endsWith("ましたか。") ||
                s.primaryForm.raw.endsWith("ましょうか。") ||
                s.primaryForm.raw.endsWith("こうか。") ||
                s.primaryForm.raw.endsWith("ようか。") ||
                s.primaryForm.raw.endsWith("のか。") ->
                when {
                    !s.romaji.endsWith("?") -> throw CheckFailed("Missing question mark in rōmaji", unyt, word, s)
                }

            s.romaji.endsWith("?") && s.primaryForm.raw.endsWith("。") && !s.primaryForm.raw.endsWith("か。") ->
                throw CheckFailed("Questions not ending in か must use a question mark", unyt, word, s)

            s.romaji.endsWith(".") && !s.primaryForm.raw.endsWith("。") ->
                throw CheckFailed("Missing or wrong punctuation at end of line", unyt, word, s)
        }
    }

    // The level of sentences must not be easier than the level of the word it is contained in.

    if (!isPhrase && word.level != null && word.level != s.level) {
        if (s.level == null) {
            throw CheckFailed("Level attribute is missing from $kind", unyt, word, s)
        } else {
            val wl = word.level?.toInt() ?: 0
            val sl = s.level?.toInt() ?: 0

            if (wl < sl) {
                throw CheckFailed(
                    "Level of $kind cannot be easier than the word it is associated to",
                    unyt,
                    word,
                    s
                )
            }
        }
    }

    // The level of the sentence must match the unyt level when the unyt is dedicated to a single level.
    // Exception: n5 unyts may contain n4 sentences (because it's hard to make pure n5 sentences).

    if (unyt.levels.size == 1) {
        val unytLevel = unyt.levels[0]

        if (s.level != unytLevel && !(s.level == JLPTLevel.N4 && unytLevel == JLPTLevel.N5)) {
            throw CheckFailed(
                "Unyt is dedicated to level ${unytLevel}, but sentence wants level ${s.level}",
                unyt,
                word,
                s
            )
        }
    }

    // Enforce a consistent use of certain firstnames and surnames.

    val hasSurname = knownSurnames.any { (surname, _) -> s.romaji.contains(surname) }
    val englishAbbrev = arrayOf("Mr", "Mrs", "Ms", "Miss", "Professor")
    val hasEnglishAbbrev = englishAbbrev.any { s.translation.en.contains(it) }

    if (hasEnglishAbbrev && !hasSurname) {
        throw CheckFailed(
            "$kind appears to refer to a surname. Please use one of: ${knownSurnames.keys.joinToString(", ")}",
            unyt,
            word,
            s
        )
    }

    knownSurnames.forEach { (surname, details) ->
        if (s.romaji.contains(surname)) {
            val suffixFound = details.suffices.any { suffix ->
                s.romaji.contains("$surname-${suffix}")
            }

            var surnameUsage: SurnameUsage? = null

            if (suffixFound) {
                surnameUsage = SurnameUsage.SUFFIX
            } else {
                if (details.knownFirstname == null) {
                    if (details.suffixRequired) {
                        throw CheckFailed(
                            "Surname $surname expected to have one of suffices: ${details.suffices.joinToString(", ")}",
                            unyt,
                            word,
                            s
                        )
                    }
                } else {
                    if (s.romaji.contains("$surname ${details.knownFirstname}")) {
                        surnameUsage = SurnameUsage.FIRSTNAME
                    } else if (s.romaji.contains("${details.knownFirstname} $surname")) {
                        throw CheckFailed(
                            "Firstname ${details.knownFirstname} should come after surname $surname in rōmaji",
                            unyt,
                            word,
                            s
                        )
                    } else if (details.suffixRequired) {
                        throw CheckFailed(
                            "Surname $surname should either use a suffix (" +
                                details.suffices.joinToString(", ") +
                                ") or the known firstname ${details.knownFirstname}",
                            unyt,
                            word,
                            s
                        )
                    }
                }
            }

            if (surnameUsage == SurnameUsage.SUFFIX) {
                // A suffix in rōmaji must be accompanied by a prefix in the English translation.
                val hasPrefix = details.prefixes.any { prefix -> s.translation.en.contains("$prefix $surname") }

                if (!hasPrefix) {
                    throw CheckFailed(
                        "Surname $surname is expected to be used with prefix: ${details.prefixes.joinToString(", ")}",
                        unyt,
                        word,
                        s
                    )
                }
            }
        }
    }

    val allSuffixes = arrayOf("san", "kun", "chan")

    knownFirstnames.forEach { (firstname, details) ->
        if (s.romaji.contains(firstname)) {
            val hasEnglishAbbrevForThis = englishAbbrev.any { s.translation.en.contains("$it $firstname") }

            if (hasEnglishAbbrevForThis) {
                throw CheckFailed(
                    "Firstname $firstname should not be used with: ${englishAbbrev.joinToString(", ")}",
                    unyt,
                    word,
                    s
                )
            }

            val hasSuffix = s.romaji.contains("$firstname-${details.suffix}")

            if (!hasSuffix) {
                val hasImproperSuffix = s.romaji.contains("$firstname ${details.suffix}") ||
                    s.romaji.contains("$firstname${details.suffix}")

                if (hasImproperSuffix) {
                    throw CheckFailed(
                        "Suffix for firstname used improperly, should use $firstname-(suffix)",
                        unyt,
                        word,
                        s
                    )
                }

                val hasOtherSuffix = allSuffixes.any { suffix ->
                    s.romaji.contains("$firstname-$suffix") ||
                        s.romaji.contains("$firstname $suffix") ||
                        s.romaji.contains("$firstname$suffix")
                }

                if (hasOtherSuffix) {
                    throw CheckFailed(
                        "Firstname $firstname expected to use suffix ${details.suffix} or no suffix",
                        unyt,
                        word,
                        s
                    )
                }
            }
        }
    }

    // Check origin, and that remark does not contain the origin.

    if (s.origin.isNotEmpty() && !allowedOrigins.any { it.matches(s.origin) }) {
        val msg = "   " + allowedOrigins.joinToString("\n   ")
        throw CheckFailed("Origin (${s.origin}) should be one of the allowed origins\n${msg}", unyt, word, s)
    }

    if (s.remark.isNotEmpty()) {
        if (s.remark.contains("local db")) {
            throw CheckFailed("Remark should not contain deprecated origin: ${s.remark}", unyt, word, s)
        }

        s.remark.split(';')
            .map { it.trim() }
            .forEach { part ->
                if (allowedOrigins.any { it.matches(part) }) {
                    throw CheckFailed(
                        "Remark contains a part that looks like an origin; use origin instead: $part",
                        unyt,
                        word,
                        s
                    )
                }
            }
    }

    // Check href, and that remark does not contain any hrefs.

    if (s.href.isNotEmpty() && !s.href.startsWith("https://")) {
        throw CheckFailed("href should start with https:// prefix: ${s.href}", unyt, word, s)
    }

    if (s.remark.contains("http")) {
        throw CheckFailed("Remark should not contain any hrefs, use href instead: ${s.remark}", unyt, word, s)
    }
}
