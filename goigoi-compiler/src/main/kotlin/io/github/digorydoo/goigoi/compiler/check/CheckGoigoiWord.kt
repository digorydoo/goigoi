package io.github.digorydoo.goigoi.compiler.check

import ch.digorydoo.kutils.cjk.*
import io.github.digorydoo.goigoi.compiler.CheckFailed
import io.github.digorydoo.goigoi.compiler.checkRomaji
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiUnyt
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import io.github.digorydoo.goigoi.core.StudyInContextKind
import io.github.digorydoo.goigoi.core.WordCategory
import io.github.digorydoo.goigoi.core.WordHint

private const val KANJI_MAX_LENGTH = 10 // also applies to kana of words with no kanji
private const val KANA_MAX_LENGTH = 10
private const val ROMAJI_MAX_LENGTH = 29
private const val TRANSLATION_MAX_LENGTH = 70
private const val HINT_MAX_LENGTH = 70
private const val MAX_NUM_CHARS_IN_ANSWER = 10 // keep this in sync with QAProvider

fun GoigoiWord.check(crossDict: Boolean, hasCombinedReading: Boolean, unyt: GoigoiUnyt) {
    @Suppress("SimplifyBooleanWithConstants")
    require(KANJI_MAX_LENGTH <= MAX_NUM_CHARS_IN_ANSWER)

    @Suppress("SimplifyBooleanWithConstants")
    require(KANA_MAX_LENGTH <= MAX_NUM_CHARS_IN_ANSWER)

    // Restrictions for custom word ids

    if (hasCustomId) {
        if (id.length < 6) {
            throw CheckFailed("Custom word id too short: $id", unyt, this)
        }

        if (id.length > 30) {
            throw CheckFailed("Custom word id too long: $id", unyt, this)
        }

        // Custom ids have a dash at the start (automatically added); but they also need one in between

        if (!id.slice(1 ..< id.length).contains("-")) {
            throw CheckFailed("Custom word id does not contain any dash: $id", unyt, this)
        }

        if (
            id.contains(" ") ||
            id.contains("　") ||
            id.contains(",") ||
            id.contains(".") ||
            id.contains("?") ||
            id.contains("!") ||
            id.contains(":") ||
            id.contains("、") ||
            id.contains("。") ||
            id.contains("？") ||
            id.contains("！")
        ) {
            throw CheckFailed("Unexpected character in custom word id: $id", unyt, this)
        }

        if (romaji.isNotEmpty()) {
            var ck1 = romaji
                .lowercase()
                .replace(" ", "")
                .replace("-", "")
                .replace("~", "")
                .replace("'", "")
                .replace("!", "")
                .replace(".", "")
                .replace("ī", "ii")

            if (ck1.length > 5 && ck1.endsWith("osuru")) {
                ck1 = ck1.slice(0 ..< ck1.length - 5)
            } else if (ck1.length > 5 && ck1.endsWith("suru")) {
                ck1 = ck1.slice(0 ..< ck1.length - 4)
            }

            val ck2 = id
                .replace("ī", "ii")

            if (!ck2.contains(ck1)) {
                throw CheckFailed("Rōmaji (${ck1}) not contained in custom word id: $id", unyt, this)
            }
        }
    }

    // Check that hidden words must be additionally marked in rem

    if (remark.contains("HIDDEN")) {
        if (!hidden) {
            throw CheckFailed("Remark contains HIDDEN, but word's hidden flag is not set!", unyt, this)
        }
    }

    // Check that remark and hints are not confused

    hint.availableLanguages().forEach { langId ->
        val hint = hint.withLanguage(langId)

        if (hint.contains("HIDDEN")) {
            throw CheckFailed("Hint contains HIDDEN, should be in remark!", unyt, this)
        } else if (hint.contains("Langenscheidt")) {
            throw CheckFailed("Hint contains Langenscheidt, should be in origin!", unyt, this)
        }
    }

    // Check that furigana are single-character except when unyt or word allow combined readings

    if (unyt.hasFurigana && !unyt.ignoresCombinedReadings) {
        val iter = FuriganaIterator(primaryForm.raw)
        var foundMultiChar = false

        for (range in iter) {
            if (range.primaryText.isEmpty()) {
                throw CheckFailed("Furigana primaryText is empty!", unyt, this)
            } else if (range.primaryText.length > 1) {
                foundMultiChar = true
            }
        }

        if (foundMultiChar && !hasCombinedReading) {
            throw CheckFailed(
                "Furigana with combined reading found, but word misses hasCombinedReading flag",
                unyt,
                this
            )
        } else if (!foundMultiChar && hasCombinedReading) {
            throw CheckFailed(
                "Word has hasCombinedReading flag set, but no combined reading found in furigana",
                unyt,
                this
            )
        }
    }

    // Check that furigana's primaryText does not contain any hiragana or katakana

    if (unyt.hasFurigana) {
        val iter = FuriganaIterator(primaryForm.raw)

        for (range in iter) {
            val ck = range.primaryText.replace("ヶ".toRegex(), "") // small ke is not checked

            if (ck.hasHiragana()) {
                throw CheckFailed(
                    "Primary part of furigana is not allowed to contain any hiragana: ${range.primaryText}",
                    unyt,
                    this
                )
            } else if (ck.hasKatakana()) {
                throw CheckFailed(
                    "Primary part of furigana is not allowed to contain any katakana: ${range.primaryText}",
                    unyt,
                    this
                )
            }
        }
    }

    // Check that furigana's secondaryText is non-empty and contains nothing but kana

    if (unyt.hasFurigana) {
        val iter = FuriganaIterator(primaryForm.raw)

        for (range in iter) {
            if (range.secondaryText.isEmpty()) {
                throw CheckFailed("Furigana secondaryText is empty!", unyt, this)
            } else if (!range.secondaryText.isHiragana()) {
                // We allow katakana only if usuallyInKana is true, otherwise we require hiragana.
                if (!usuallyInKana) {
                    throw CheckFailed(
                        "Furigana needs to be in hiragana since usuallyInKana is not set: ${range.secondaryText}",
                        unyt,
                        this
                    )
                } else if (!range.secondaryText.isKatakana()) {
                    throw CheckFailed(
                        "Furigana secondaryText is not pure hiragana or katakana: ${range.secondaryText}",
                        unyt,
                        this
                    )
                }
            }
        }
    }

    // Rōmaji must be specified if and only if unyt requires it

    if (unyt.hasRomaji) {
        if (romaji.isEmpty()) {
            throw CheckFailed("Unyt was flagged for romanized forms, but form is missing here!", unyt, this)
        }
    } else {
        if (romaji.isNotEmpty()) {
            throw CheckFailed("Unyt was not flagged for romanized forms: $romaji", unyt, this)
        }
    }

    // Require rōmaji to contain at least one dash when primaryForm contains 々

    if (unyt.hasRomaji && primaryForm.contains("々") && !romaji.contains("-")) {
        throw CheckFailed("Rōmaji should contain a dash when primaryForm contains 々: $romaji", unyt, this)
    }

    // Brackets may only be specified if unyt supports furigana

    if (!unyt.hasFurigana && primaryForm.contains("【")) {
        throw CheckFailed(
            "Unyt was not flagged for furigana, but contains bracket: $primaryForm",
            unyt,
            this
        )
    }

    // If rōmaji is specified, it must match the primary form including furigana

    if (romaji.isNotEmpty()) {
        checkRomaji(primaryForm.raw, romaji, unyt, this)
    }

    // Check maximum length

    if (!hidden && !unyt.hidden) {
        if (kanji.length > KANJI_MAX_LENGTH) {
            throw CheckFailed("Word kanji too long: max allowed $KANJI_MAX_LENGTH, actual ${kanji.length}", unyt, this)
        }

        if (kana.length > KANA_MAX_LENGTH) {
            throw CheckFailed("Word kana too long: max allowed $KANA_MAX_LENGTH, actual ${kana.length}", unyt, this)
        }

        if (romaji.length > ROMAJI_MAX_LENGTH) {
            throw CheckFailed("Rōmaji too long: max allowed $ROMAJI_MAX_LENGTH, actual ${romaji.length}", unyt, this)
        }

        if (translation.en.length > TRANSLATION_MAX_LENGTH) {
            throw CheckFailed(
                "tr_en too long: max allowed $TRANSLATION_MAX_LENGTH, actual ${translation.en.length}",
                unyt,
                this
            )
        }

        if (translation.de.length > TRANSLATION_MAX_LENGTH) {
            throw CheckFailed(
                "tr_de too long: max allowed $TRANSLATION_MAX_LENGTH, actual ${translation.de.length}",
                unyt,
                this
            )
        }

        if (hint.en.length > HINT_MAX_LENGTH) {
            throw CheckFailed("hint_en too long: max allowed $HINT_MAX_LENGTH, actual ${hint.en.length}", unyt, this)
        }

        if (hint.de.length > HINT_MAX_LENGTH) {
            throw CheckFailed("hint_de too long: max allowed $HINT_MAX_LENGTH, actual ${hint.de.length}", unyt, this)
        }
    }

    // Check required translations

    if (!hidden) {
        unyt.requiredTranslations.forEach { langId ->
            if (translation.withLanguage(langId).isEmpty()) {
                throw CheckFailed("Missing required translation: $langId", unyt, this)
            }
        }
    }

    // Check if German translation of known hint was used, but hint_en was different

    if (!unyt.hidden && !hidden) {
        WordHint.entries.forEach { knownHint ->
            if (hint.en.isNotEmpty() && hint.en == knownHint.en) {
                // Known hints should be moved to hint2 by GoigoiXmlParser
                throw CheckFailed(
                    "Internal error: hint_en (${hint.en}) should have been replaced by $knownHint",
                    unyt,
                    this
                )
            }
            if (hint.de.isNotEmpty() && hint.de == knownHint.de) {
                // We already know hint_en differs from the known hint
                throw CheckFailed(
                    "hint_de is: ${hint.de}\n" +
                        "   expected hint_en to be: ${knownHint.en}\n" +
                        "   actual: ${hint.en}",
                    unyt,
                    this
                )
            }
        }
    }

    // Checks that enforce studyInContext in some cases

    val enHints = arrayOf(hint.en, hint2?.en).filterNotNull().joinToString(";").split(";").map { it.trim() }

    if (!unyt.hidden && !hidden && studyInContext == StudyInContextKind.NOT_REQUIRED) {
        if (enHints.contains("prefix") || enHints.contains("suffix")) {
            throw CheckFailed("Word having hint '${hint.en}' should be marked with studyInContext", unyt, this)
        }
        if (translation.en.contains("~") || translation.de.contains("~")) {
            throw CheckFailed("Word having tilde in translation should be marked with studyInContext", unyt, this)
        }
        if (romaji.contains("~")) {
            throw CheckFailed("Word having tilde in rōmaji should be marked with studyInContext", unyt, this)
        }
    }

    // Check categories

    if (cats.size > 2) {
        throw CheckFailed("Word should not have more than two categories", unyt, this)
    }

    // Check that words are properly categorized when their translation matches a category

    if (!hidden && !unyt.hidden) {
        val translations = translation.en.split(';').map { it.trim() }

        val catsOfTranslations = translations.mapNotNull { tr ->
            tr.lowercase()
                // complain about verbs only if the list of categories is empty, because e.g. "to plant" is no plant
                .let { if (cats.isEmpty() && it.startsWith("to ")) it.substring(3) else it }
                .replace(" ", "-")
                .let { WordCategory.fromString(it) }
        }

        val missingCats = catsOfTranslations.filter { !cats.contains(it) }
        val numCatsOfTranslationsAlreadyMentioned = catsOfTranslations.size - missingCats.size

        if (numCatsOfTranslationsAlreadyMentioned < 2 && missingCats.isNotEmpty()) {
            throw CheckFailed(
                "Word should utilise category that matches translation: ${missingCats.joinToString(", ") { it.text }}",
                unyt,
                this
            )
        }
    }

    // Check against miscategorised nouns and verbs

    if (unyt.studyLang == "ja") {
        unyt.name.en.lowercase().let { uname ->
            if (uname.contains("verb") && !uname.contains("adverb")) {
                val exceptions = arrayOf("いる", "ある", "【居：い】る", "【有：あ】る")

                if (!translation.en.startsWith("to ") && !exceptions.contains(primaryForm.raw)) {
                    throw CheckFailed(
                        "Unyt (${unyt.name.en}) seems to be about verbs; " +
                            "English translations of words are required to start with 'to '.",
                        unyt,
                        this
                    )
                }
            } else if (uname.contains("noun")) {
                if (translation.en.startsWith("to ")) {
                    throw CheckFailed(
                        "Unyt (${unyt.name.en}) seems to be about nouns; " +
                            "English translations of words must not start with 'to '.",
                        unyt,
                        this
                    )
                }
            }
        }
    }

    // The flag crossDict must be set if and only if dict is not contained in primary form

    if (dictionaryWord.isEmpty() || dictionaryWord == "-") {
        if (crossDict) {
            throw CheckFailed("crossDict must not be set when dict is not specified", unyt, this)
        }
    } else {
        val kanji = FuriganaString(primaryForm.raw).kanji

        if (kanji.contains(dictionaryWord)) {
            if (crossDict) {
                throw CheckFailed(
                    "crossDict is set even though dict (${dictionaryWord}) IS contained in kanji ($kanji)",
                    unyt,
                    this
                )
            }
        } else {
            if (!crossDict) {
                throw CheckFailed(
                    "crossDict is not set while dict (${dictionaryWord}) is NOT contained in kanji ($kanji)",
                    unyt,
                    this
                )
            }
        }
    }

    // The flag usuallyInKana makes no sense if primaryForm is kana-only

    if (usuallyInKana && (primaryForm.raw.isHiragana() || primaryForm.raw.isKatakana())) {
        throw CheckFailed("The flag usuallyInKana makes no sense if primaryForm is kana-only", unyt, this)
    }

    // The flag studyInContext requires phrases and sentences

    if (studyInContext != StudyInContextKind.NOT_REQUIRED && !hidden) {
        val kanaOnly = usuallyInKana || kanji == kana

        if (phrases.isEmpty()) {
            throw CheckFailed("Word marked as studyInContext requires at least one phrase", unyt, this)
        } else {
            val phrasesThatCanBeUsed = phrases.filter {
                // verbs may appear in different form even though hasDifferentForm is not set
                !it.hasDifferentForm && it.kana.length <= MAX_NUM_CHARS_IN_ANSWER && when {
                    usuallyInKana -> it.kana.contains(kana)
                    else -> it.primaryForm.contains(primaryForm.raw)
                }
            }

            if (phrasesThatCanBeUsed.size < 2) {
                val tail = when {
                    phrasesThatCanBeUsed.size == phrases.size -> ""
                    else -> " (only ${phrasesThatCanBeUsed.size} of the ${phrases.size} phrases can be used in " +
                        "study due to hasDifferentForm and/or length)"
                }
                if (kanaOnly) {
                    throw CheckFailed(
                        "Kana-only word marked as studyInContext requires at least 2 phrases$tail",
                        unyt,
                        this
                    )
                } else if (studyInContext == StudyInContextKind.REQUIRED) {
                    throw CheckFailed(
                        "Word that has studyInContext=\"required\" requires at least 2 phrases$tail",
                        unyt,
                        this
                    )
                }
            }
        }

        if (sentences.isEmpty()) {
            throw CheckFailed("Word marked as studyInContext requires at least one sentence", unyt, this)
        } else {
            val sentencesThatCanBeUsed = sentences.filter {
                !it.hasDifferentForm && when {
                    usuallyInKana -> it.kana.contains(kana)
                    else -> it.primaryForm.contains(primaryForm.raw)
                }
            }

            if (sentencesThatCanBeUsed.isEmpty()) {
                val tail = when {
                    sentences.isEmpty() -> ""
                    else -> " (none of the ${sentences.size} sentences can be used in study due to hasDifferentForm)"
                }
                if (kanaOnly) {
                    throw CheckFailed(
                        "Kana-only word marked as studyInContext requires at least 1 sentence$tail",
                        unyt,
                        this
                    )
                } else if (studyInContext == StudyInContextKind.REQUIRED) {
                    throw CheckFailed(
                        "Word that has studyInContext=\"required\" requires at least 1 sentence$tail",
                        unyt,
                        this
                    )
                }
            }
        }

        if (phrases.size + sentences.size < 3) {
            throw CheckFailed("Word marked as studyInContext requires #phrases + #sentences >= 3", unyt, this)
        }
    }

    // Check href, and that remark does not contain any hrefs

    if (href.isNotEmpty() && !href.startsWith("https://")) {
        throw CheckFailed("href should start with https:// prefix: $href", unyt, this)
    }

    if (remark.contains("http")) {
        throw CheckFailed("Remark should not contain any hrefs, use href instead: $remark", unyt, this)
    }
}
