package io.github.digorydoo.goigoi.compiler.check.prechecks

import ch.digorydoo.kutils.cjk.JLPTLevel
import io.github.digorydoo.goigoi.compiler.CheckFailed
import io.github.digorydoo.goigoi.compiler.supportedLanguages
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiTopic
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiUnyt
import io.github.digorydoo.goigoi.core.db.StudyInContextKind

class UnytChecker {
    fun check(unyt: GoigoiUnyt, topic: GoigoiTopic) {
        // Check that the unyt's levels match those required by the topic.

        if (topic.levels.isNotEmpty()) {
            if (unyt.levels.isEmpty()) {
                throw CheckFailed(
                    "Unyt lvl cannot be empty as topic requires levels: ${topic.levels.joinToString(", ")}"
                )
            }
            unyt.levels.forEach { level ->
                if (!topic.levels.contains(level)) {
                    throw CheckFailed(
                        "Unyt declares level $level, but topic is constrainted to: ${topic.levels.joinToString(", ")}"
                    )
                }
            }
        }

        // Check that the unyt's levels match those of its nested words, phrases and sentences.

        if (unyt.levels.isNotEmpty()) {
            val declaredLevelsNotUsedByAnyWord = unyt.levels.toMutableList()

            for (section in unyt.sections) {
                for (word in section.words) {
                    if (word.level == null) {
                        throw CheckFailed(
                            "Word level is required and must be one of the unyt levels: " +
                                unyt.levels.joinToString(",")
                        )
                    }

                    if (!word.hidden) {
                        if (!unyt.levels.contains(word.level)) {
                            throw CheckFailed(
                                "Level of word (${word.level}) does not match unyt levels: " +
                                    unyt.levels.joinToString(",")
                            )
                        }

                        if (declaredLevelsNotUsedByAnyWord.contains(word.level)) {
                            declaredLevelsNotUsedByAnyWord.remove(word.level)
                        }


                        if (word.studyInContext == StudyInContextKind.NOT_REQUIRED) {
                            if (unyt.requiresPhrases) {
                                // Phrase levels may differ, but at least one of them must match the unyt's.
                                // Relaxed for n5 words, which are allowed to contain only n4 phrases.
                                if (word.phrases.isNotEmpty()) {
                                    var anyMatchingUnyt = false

                                    for (phrase in word.phrases) {
                                        if (unyt.levels.contains(phrase.level)) {
                                            anyMatchingUnyt = true
                                        } else if (word.level == JLPTLevel.N5 && phrase.level == JLPTLevel.N4) {
                                            anyMatchingUnyt = true
                                        }
                                    }

                                    if (!anyMatchingUnyt) {
                                        throw CheckFailed(
                                            "At least one of the ${word.phrases.size} phrase(s) must match what's " +
                                                "declared by the unyt (${unyt.levels.joinToString(", ")})"
                                        )
                                    }
                                }
                            }

                            if (unyt.requiresSentences) {
                                // Sentences levels may differ, but at least one of them must match the unyt's.
                                // Relaxed for n5 words, which are allowed to contain only n4 sentences.
                                if (word.sentences.isNotEmpty()) {
                                    var anyMatchingUnyt = false

                                    for (sentence in word.sentences) {
                                        if (unyt.levels.contains(sentence.level)) {
                                            anyMatchingUnyt = true
                                        } else if (word.level == JLPTLevel.N5 && sentence.level == JLPTLevel.N4) {
                                            anyMatchingUnyt = true
                                        }
                                    }

                                    if (!anyMatchingUnyt) {
                                        throw CheckFailed(
                                            "At least one of the ${word.sentences.size} sentences(s) must match " +
                                                "what's declared by the unyt (${unyt.levels.joinToString(", ")})"
                                        )
                                    }
                                }
                            }
                        } else {
                            // All phrases must have the same level as their containing word.
                            // Relaxed for n5 words, which are allowed to contain only n4 phrases.
                            for (phrase in word.phrases) {
                                if (
                                    word.level != phrase.level &&
                                    !(word.level == JLPTLevel.N5 && phrase.level == JLPTLevel.N4)
                                ) {
                                    throw CheckFailed(
                                        "Word is marked with studyInContext, which requires that phrase levels " +
                                            "match their containing word."
                                    )
                                }
                            }

                            // All sentences must have the same level as their containing word.
                            // Relaxed for n5 words, which are allowed to contain only n4 sentences.
                            for (sentence in word.sentences) {
                                if (
                                    word.level != sentence.level &&
                                    !(word.level == JLPTLevel.N5 && sentence.level == JLPTLevel.N4)
                                ) {
                                    throw CheckFailed(
                                        "Word is marked with studyInContext, which requires that sentences levels " +
                                            "match their containing word."
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (declaredLevelsNotUsedByAnyWord.isNotEmpty()) {
                throw CheckFailed(
                    "Unyt specifies levels that aren't used by any word: $declaredLevelsNotUsedByAnyWord"
                )
            }
        }

        // Check that all sections have the same number of translations of their name attribute.

        val languages = arrayOf("en", "de", "fr", "it")
        var langDefined: String? = null

        for (section in unyt.sections) {
            val l = languages
                .filter { langId -> section.name.withLanguage(langId).isNotEmpty() }
                .joinToString(", ")

            if (langDefined == null) {
                langDefined = l
            } else if (langDefined != l) {
                throw CheckFailed("Section name translations are inconsistent within unyt: $l")
            }
        }

        // Check that translations + hints are unique among visible words in this unyt.

        languages.forEach { langId ->
            val found = mutableSetOf<String>()

            for (section in unyt.sections) {
                for (word in section.words) {
                    if (!word.hidden) {
                        val tr = word.translation.withLanguage(langId)
                            .takeIf { it.isNotEmpty() }
                            ?: word.translation.en

                        val hint = word.hint.withLanguage(langId)
                            .takeIf { it.isNotEmpty() }
                            ?: word.hint.en

                        val hint2 = when (langId) {
                            "de" -> word.hint2?.de
                            else -> word.hint2?.en
                        } ?: ""

                        val trAndHint = "${tr}/${hint}/${hint2}"

                        if (found.contains(trAndHint)) {
                            throw CheckFailed("Translation/hint ($langId) not unique in unyt: $trAndHint")
                        } else {
                            found.add(trAndHint)
                        }
                    }
                }
            }
        }

        // Check that rōmaji are unique among visible words in this unyt.

        if (unyt.hasRomaji && !unyt.hidden) {
            val found = mutableSetOf<String>()

            for (section in unyt.sections) {
                for (word in section.words) {
                    if (!word.hidden) {
                        if (found.contains(word.romaji)) {
                            throw CheckFailed("Rōmaji not unique in unyt!")
                        } else {
                            found.add(word.romaji)
                        }
                    }
                }
            }
        }

        // Check if unyt has German translations

        unyt.requiredTranslations.forEach {
            if (!supportedLanguages.contains(it)) {
                throw CheckFailed("Not a valid language identifier: $it")
            }
        }

        if (!unyt.hidden) {
            if (!unyt.requiredTranslations.contains("en")) {
                throw CheckFailed("Unyt has no English translation or does not say so")
            }

            if (!unyt.requiredTranslations.contains("de")) {
                when {
                    unyt.levels.size == 1 -> Unit // allow missing de when unyt is dedicated to a single JLPT-level
                    unyt.name.en.startsWith("Numbers") -> Unit // numbers need not be translated
                    else -> throw CheckFailed("Unyt must be translated to German as exceptions do not apply!")
                }
            }
        }

        // Check that we can rely solely on unyt.hidden when topic is hidden

        if (!unyt.hidden && topic.hidden) {
            throw CheckFailed("Unyt is required to be marked as hidden, because the topic is hidden!")
        }
    }
}
