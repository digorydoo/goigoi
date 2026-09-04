package io.github.digorydoo.goigoi.compiler.check.prechecks

import ch.digorydoo.kutils.cjk.*
import io.github.digorydoo.goigoi.compiler.CheckFailed
import io.github.digorydoo.goigoi.compiler.checkRomaji
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiUnyt
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import io.github.digorydoo.goigoi.core.db.StudyInContextKind
import io.github.digorydoo.goigoi.core.db.WordCategory
import io.github.digorydoo.goigoi.core.db.WordHint
import io.github.digorydoo.goigoi.core.prog_study.QAPicker.Companion.MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_NOT_REQUIRED
import io.github.digorydoo.goigoi.core.prog_study.QAPicker.Companion.MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_PREFERRED
import io.github.digorydoo.goigoi.core.prog_study.QAPicker.Companion.MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_REQUIRED

class WordChecker {
    private val romajisSet = mutableSetOf<String>()

    fun check(word: GoigoiWord, unyt: GoigoiUnyt) {
        @Suppress("SimplifyBooleanWithConstants")
        require(KANJI_MAX_LENGTH <= MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_NOT_REQUIRED)

        @Suppress("SimplifyBooleanWithConstants")
        require(KANJI_MAX_LENGTH <= MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_PREFERRED)

        @Suppress("SimplifyBooleanWithConstants")
        require(KANJI_MAX_LENGTH <= MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_REQUIRED)

        @Suppress("SimplifyBooleanWithConstants")
        require(KANA_MAX_LENGTH <= MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_NOT_REQUIRED)

        @Suppress("SimplifyBooleanWithConstants")
        require(KANA_MAX_LENGTH <= MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_PREFERRED)

        @Suppress("SimplifyBooleanWithConstants")
        require(KANA_MAX_LENGTH <= MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_REQUIRED)

        // Restrictions for custom word ids

        if (word.hasCustomId) {
            if (word.id.length < 6) {
                throw CheckFailed("Custom word id too short: ${word.id}")
            }

            if (word.id.length > 30) {
                throw CheckFailed("Custom word id too long: ${word.id}")
            }

            // Custom ids have a dash at the start (automatically added); but they also need one in between

            if (!word.id.slice(1 ..< word.id.length).contains("-")) {
                throw CheckFailed("Custom word id does not contain any dash: ${word.id}")
            }

            if (
                word.id.contains(" ") ||
                word.id.contains("　") ||
                word.id.contains(",") ||
                word.id.contains(".") ||
                word.id.contains("?") ||
                word.id.contains("!") ||
                word.id.contains(":") ||
                word.id.contains("、") ||
                word.id.contains("。") ||
                word.id.contains("？") ||
                word.id.contains("！")
            ) {
                throw CheckFailed("Unexpected character in custom word id: ${word.id}")
            }

            if (word.romaji.isNotEmpty()) {
                var ck1 = word.romaji
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

                val ck2 = word.id
                    .replace("ī", "ii")

                if (!ck2.contains(ck1)) {
                    throw CheckFailed("Rōmaji (${ck1}) not contained in custom word id: ${word.id}")
                }
            }
        }

        // Check that hidden words must be additionally marked in rem

        if (word.remark.contains("HIDDEN")) {
            if (!word.hidden) {
                throw CheckFailed("Remark contains HIDDEN, but word's hidden flag is not set!")
            }
        }

        // Check that remark and hints are not confused

        word.hint.availableLanguages().forEach { langId ->
            val hint = word.hint.withLanguage(langId)

            if (hint.contains("HIDDEN")) {
                throw CheckFailed("Hint contains HIDDEN, should be in remark!")
            } else if (hint.contains("Langenscheidt")) {
                throw CheckFailed("Hint contains Langenscheidt, should be in origin!")
            }
        }

        // Check that furigana are single-character except when unyt or word allow combined readings

        if (unyt.hasFurigana && !unyt.ignoresCombinedReadings) {
            val iter = FuriganaIterator(word.primaryForm.raw)
            var foundMultiChar = false

            for (range in iter) {
                if (range.primaryText.isEmpty()) {
                    throw CheckFailed("Furigana primaryText is empty!")
                } else if (range.primaryText.length > 1) {
                    foundMultiChar = true
                }
            }

            if (foundMultiChar && !word.hasCombinedReading) {
                throw CheckFailed("Furigana with combined reading found, but word misses hasCombinedReading flag")
            } else if (!foundMultiChar && word.hasCombinedReading) {
                throw CheckFailed("Word has hasCombinedReading flag set, but no combined reading found in furigana")
            }
        }

        // Check that furigana's primaryText does not contain any hiragana or katakana

        if (unyt.hasFurigana) {
            val iter = FuriganaIterator(word.primaryForm.raw)

            for (range in iter) {
                val ck = range.primaryText.replace("ヶ".toRegex(), "") // small ke is not checked

                if (ck.hasHiragana()) {
                    throw CheckFailed(
                        "Primary part of furigana is not allowed to contain any hiragana: ${range.primaryText}"
                    )
                } else if (ck.hasKatakana()) {
                    throw CheckFailed(
                        "Primary part of furigana is not allowed to contain any katakana: ${range.primaryText}",
                    )
                }
            }
        }

        // Check that furigana's secondaryText is non-empty and contains nothing but kana

        if (unyt.hasFurigana) {
            val iter = FuriganaIterator(word.primaryForm.raw)

            for (range in iter) {
                if (range.secondaryText.isEmpty()) {
                    throw CheckFailed("Furigana secondaryText is empty!")
                } else if (!range.secondaryText.isHiragana()) {
                    // We allow katakana only if usuallyInKana is true, otherwise we require hiragana.
                    if (!word.usuallyInKana) {
                        throw CheckFailed(
                            "Furigana needs to be in hiragana since usuallyInKana is not set: ${range.secondaryText}",
                        )
                    } else if (!range.secondaryText.isKatakana()) {
                        throw CheckFailed(
                            "Furigana secondaryText is not pure hiragana or katakana: ${range.secondaryText}",
                        )
                    }
                }
            }
        }

        // Rōmaji must be specified if and only if unyt requires it

        if (unyt.hasRomaji) {
            if (word.romaji.isEmpty()) {
                throw CheckFailed("Unyt was flagged for romanized forms, but form is missing here!")
            }
        } else {
            if (word.romaji.isNotEmpty()) {
                throw CheckFailed("Unyt was not flagged for romanized forms: ${word.romaji}")
            }
        }

        // If hasCustomId is not set, rōmaji must be unique

        if (unyt.hasRomaji && !word.hasCustomId && romajisSet.contains(word.romaji)) {
            throw CheckFailed("Rōmaji used by at least one other word, custom id required!")
        }

        if (word.romaji.isNotEmpty()) {
            romajisSet.add(word.romaji)
        }

        // Require rōmaji to contain at least one dash when primaryForm contains 々

        if (unyt.hasRomaji && word.primaryForm.contains("々") && !word.romaji.contains("-")) {
            throw CheckFailed("Rōmaji should contain a dash when primaryForm contains 々: ${word.romaji}")
        }

        // Brackets may only be specified if unyt supports furigana

        if (!unyt.hasFurigana && word.primaryForm.contains("【")) {
            throw CheckFailed("Unyt was not flagged for furigana, but contains bracket: ${word.primaryForm}")
        }

        // If rōmaji is specified, it must match the primary form including furigana

        if (word.romaji.isNotEmpty()) {
            checkRomaji(word.primaryForm.raw, word.romaji)
        }

        // Check maximum length

        if (!word.hidden && !unyt.hidden) {
            if (word.kanji.length > KANJI_MAX_LENGTH) {
                throw CheckFailed("Word kanji too long: max allowed $KANJI_MAX_LENGTH, actual ${word.kanji.length}")
            }

            if (word.kana.length > KANA_MAX_LENGTH) {
                throw CheckFailed("Word kana too long: max allowed $KANA_MAX_LENGTH, actual ${word.kana.length}")
            }

            if (word.romaji.length > ROMAJI_MAX_LENGTH) {
                throw CheckFailed("Rōmaji too long: max allowed $ROMAJI_MAX_LENGTH, actual ${word.romaji.length}")
            }

            if (word.translation.en.length > TRANSLATION_MAX_LENGTH) {
                throw CheckFailed(
                    "tr_en too long: max allowed $TRANSLATION_MAX_LENGTH, actual ${word.translation.en.length}"
                )
            }

            if (word.translation.de.length > TRANSLATION_MAX_LENGTH) {
                throw CheckFailed(
                    "tr_de too long: max allowed $TRANSLATION_MAX_LENGTH, actual ${word.translation.de.length}"
                )
            }

            if (word.hint.en.length > HINT_MAX_LENGTH) {
                throw CheckFailed("hint_en too long: max allowed $HINT_MAX_LENGTH, actual ${word.hint.en.length}")
            }

            if (word.hint.de.length > HINT_MAX_LENGTH) {
                throw CheckFailed("hint_de too long: max allowed $HINT_MAX_LENGTH, actual ${word.hint.de.length}")
            }
        }

        // Check required translations

        if (!word.hidden) {
            unyt.requiredTranslations.forEach { langId ->
                if (word.translation.withLanguage(langId).isEmpty()) {
                    throw CheckFailed("Missing required translation: $langId")
                }
            }
        }

        // Check if German translation of known hint was used, but hint_en was different

        if (!unyt.hidden && !word.hidden) {
            WordHint.entries.forEach { knownHint ->
                if (word.hint.en.isNotEmpty() && word.hint.en == knownHint.en) {
                    // Known hints should be moved to hint2 by GoigoiXmlParser
                    throw CheckFailed("Internal error: hint_en (${word.hint.en}) should have been replaced by $knownHint")
                }
                if (word.hint.de.isNotEmpty() && word.hint.de == knownHint.de) {
                    // We already know hint_en differs from the known hint
                    throw CheckFailed(
                        "hint_de is: ${word.hint.de}\n" +
                            "   expected hint_en to be: ${knownHint.en}\n" +
                            "   actual: ${word.hint.en}"
                    )
                }
            }
        }

        // Check categories

        if (word.cats.size > 2) {
            throw CheckFailed("Word should not have more than two categories")
        }

        // Check that words are properly categorized when their translation matches a category

        if (!word.hidden && !unyt.hidden) {
            val translations = word.translation.en.split(';').map { it.trim() }

            val catsOfTranslations = translations.mapNotNull { tr ->
                tr.lowercase()
                    // complain about verbs only if the list of categories is empty (e.g. "to plant" is no plant)
                    .let { if (word.cats.isEmpty() && it.startsWith("to ")) it.substring(3) else it }
                    .replace(" ", "-")
                    .let { WordCategory.fromString(it) }
            }

            val missingCats = catsOfTranslations.filter { !word.cats.contains(it) }
            val numCatsOfTranslationsAlreadyMentioned = catsOfTranslations.size - missingCats.size

            if (numCatsOfTranslationsAlreadyMentioned < 2 && missingCats.isNotEmpty()) {
                throw CheckFailed(
                    "Word should utilise category that matches translation: " +
                        missingCats.joinToString(", ") { it.text }
                )
            }
        }

        // Check against miscategorised nouns and verbs

        if (!word.hidden && unyt.studyLang == "ja") {
            unyt.name.en.lowercase().let { uname ->
                if (uname.contains("verb") && !uname.contains("adverb")) {
                    val exceptions = arrayOf("いる", "ある", "【居：い】る", "【有：あ】る")

                    if (!word.translation.en.startsWith("to ") && !exceptions.contains(word.primaryForm.raw)) {
                        throw CheckFailed(
                            "Unyt (${unyt.name.en}) seems to be about verbs; " +
                                "English translations of words are required to start with 'to '.",
                        )
                    }
                } else if (uname.contains("noun")) {
                    if (word.translation.en.startsWith("to ")) {
                        throw CheckFailed(
                            "Unyt (${unyt.name.en}) seems to be about nouns; " +
                                "English translations of words must not start with 'to '.",
                        )
                    }
                }
            }
        }

        // The flag crossDict must be set if and only if dict is not contained in primary form

        if (word.dictionaryWord.isEmpty() || word.dictionaryWord == "-") {
            if (word.crossDict) {
                throw CheckFailed("crossDict must not be set when dict is not specified")
            }
        } else {
            val kanji = FuriganaString(word.primaryForm.raw).kanji

            if (kanji.contains(word.dictionaryWord)) {
                if (word.crossDict) {
                    throw CheckFailed(
                        "crossDict is set even though dict (${word.dictionaryWord}) IS contained in kanji ($kanji)",
                    )
                }
            } else {
                if (!word.crossDict) {
                    throw CheckFailed(
                        "crossDict is not set while dict (${word.dictionaryWord}) is NOT contained in kanji ($kanji)",
                    )
                }
            }
        }

        // The flag usuallyInKana makes no sense if primaryForm is kana-only

        if (word.usuallyInKana && (word.primaryForm.raw.isHiragana() || word.primaryForm.raw.isKatakana())) {
            throw CheckFailed("The flag usuallyInKana makes no sense if primaryForm is kana-only")
        }

        // Checks that enforce studyInContext in some cases

        val enHints =
            arrayOf(word.hint.en, word.hint2?.en).filterNotNull().joinToString(";").split(";").map { it.trim() }

        if (!unyt.hidden && !word.hidden && word.studyInContext == StudyInContextKind.NOT_REQUIRED) {
            if (enHints.contains("prefix")) {
                throw CheckFailed("Word having hint 'prefix' should be marked with studyInContext")
            }
            if (enHints.contains("suffix")) {
                throw CheckFailed("Word having hint 'suffix' should be marked with studyInContext")
            }
            if (word.translation.en.contains("~") || word.translation.de.contains("~")) {
                throw CheckFailed("Word having tilde in translation should be marked with studyInContext")
            }
            if (word.romaji.contains("~")) {
                throw CheckFailed("Word having tilde in rōmaji should be marked with studyInContext")
            }
        }

        // The flag studyInContext requires phrases and sentences

        val studyInContext = word.studyInContext

        val maxNumCharsInAnswer = when (studyInContext) {
            StudyInContextKind.NOT_REQUIRED -> MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_NOT_REQUIRED
            StudyInContextKind.PREFERRED -> MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_PREFERRED
            StudyInContextKind.REQUIRED -> MAX_NUM_CHARS_IN_ANSWER_WHEN_STUDY_IN_CONTEXT_REQUIRED
        }

        if (!word.hidden) {
            val kanaOnly = word.usuallyInKana || word.kanji == word.kana

            val numPhrasesRequired = when (studyInContext) {
                StudyInContextKind.REQUIRED -> 2
                StudyInContextKind.PREFERRED -> 2
                StudyInContextKind.NOT_REQUIRED -> when (unyt.requiresPhrases) {
                    true -> when {
                        // Phrases with intransitive verbs often better use 〜ている instead of the dictionary form.
                        word.hint2 == WordHint.V_I || word.hint.en.contains("v.i.") -> 0
                        else -> 1
                    }
                    false -> 0
                }
            }

            if (numPhrasesRequired > 0 && word.phrases.isEmpty()) {
                throw CheckFailed("Word requires at least one phrase")
            }

            val phrasesThatCanBeUsed = word.phrases.filter { phrase ->
                // Unlike sentences, phrases can be asked in full (but kana only)
                val canAskKana = phrase.kana.length <= maxNumCharsInAnswer ||
                    when (phrase.wordFormToAsk.isEmpty()) {
                        true -> word.kana.length <= maxNumCharsInAnswer
                        false -> phrase.wordFormToAsk.kana.length <= maxNumCharsInAnswer
                    }
                // FIXME we should check max level of kanji, but KanjiIndex is not available during pre-checks
                val canAskKanji = when (phrase.wordFormToAsk.isEmpty()) {
                    true -> word.kanji.length <= maxNumCharsInAnswer
                    false -> phrase.wordFormToAsk.kanji.length <= maxNumCharsInAnswer
                }
                return@filter canAskKana || canAskKanji
            }

            if (phrasesThatCanBeUsed.size < numPhrasesRequired) {
                val tail = when {
                    phrasesThatCanBeUsed.size == word.phrases.size -> ""
                    else -> " (only ${phrasesThatCanBeUsed.size} of the ${word.phrases.size} phrases " +
                        "can be used in study due to form and length)"
                }

                if (studyInContext === StudyInContextKind.NOT_REQUIRED) {
                    throw CheckFailed("Word requires at least $numPhrasesRequired phrases$tail")
                } else if (kanaOnly) {
                    throw CheckFailed(
                        "Kana-only word marked as studyInContext requires at least $numPhrasesRequired phrases$tail"
                    )
                } else if (studyInContext == StudyInContextKind.REQUIRED) {
                    throw CheckFailed(
                        "Word that has studyInContext=\"required\" requires at least $numPhrasesRequired phrases$tail"
                    )
                }
            }

            val numSentencesRequired = when (studyInContext) {
                StudyInContextKind.REQUIRED -> 1
                StudyInContextKind.PREFERRED -> 1
                StudyInContextKind.NOT_REQUIRED -> when (unyt.requiresSentences) {
                    true -> 1
                    false -> 0
                }
            }

            if (numSentencesRequired > 0 && word.sentences.isEmpty()) {
                throw CheckFailed("Word requires at least one sentence")
            }

            val sentencesThatCanBeUsed = word.sentences.filter { sentence ->
                val canAskKana = when (sentence.wordFormToAsk.isEmpty()) {
                    true -> word.kana.length <= maxNumCharsInAnswer
                    false -> sentence.wordFormToAsk.kana.length <= maxNumCharsInAnswer
                }
                // FIXME we should check max level of kanji, but KanjiIndex is not available during pre-checks
                val canAskKanji = when (sentence.wordFormToAsk.isEmpty()) {
                    true -> word.kanji.length <= maxNumCharsInAnswer
                    false -> sentence.wordFormToAsk.kanji.length <= maxNumCharsInAnswer
                }
                return@filter canAskKana || canAskKanji
            }

            if (sentencesThatCanBeUsed.size < numSentencesRequired) {
                val tail = when {
                    word.sentences.isEmpty() -> ""
                    else -> " (only ${sentencesThatCanBeUsed.size} of the ${word.sentences.size} sentences " +
                        "can be used in study due to form and length)"
                }

                if (studyInContext === StudyInContextKind.NOT_REQUIRED) {
                    throw CheckFailed("Word requires at least $numSentencesRequired sentence(s)$tail")
                } else if (kanaOnly) {
                    throw CheckFailed(
                        "Kana-only word marked as studyInContext requires at least " +
                            "$numSentencesRequired sentence(s)$tail"
                    )
                } else if (studyInContext == StudyInContextKind.REQUIRED) {
                    throw CheckFailed(
                        "Word that has studyInContext=\"required\" requires at least " +
                            "$numSentencesRequired sentence(s)$tail"
                    )
                }
            }

            if (studyInContext !== StudyInContextKind.NOT_REQUIRED && word.phrases.size + word.sentences.size < 3) {
                throw CheckFailed("Word marked as studyInContext requires #phrases + #sentences >= 3")
            }
        }

        // Check href, and that remark does not contain any hrefs

        if (word.href.isNotEmpty() && !word.href.startsWith("https://")) {
            throw CheckFailed("href should start with https:// prefix: ${word.href}")
        }

        if (word.remark.contains("http")) {
            throw CheckFailed("Remark should not contain any hrefs, use href instead: ${word.remark}")
        }
    }

    companion object {
        private const val KANJI_MAX_LENGTH = 10 // also applies to kana of words with no kanji
        private const val KANA_MAX_LENGTH = 10
        private const val ROMAJI_MAX_LENGTH = 29
        private const val TRANSLATION_MAX_LENGTH = 70
        private const val HINT_MAX_LENGTH = 70
    }
}
