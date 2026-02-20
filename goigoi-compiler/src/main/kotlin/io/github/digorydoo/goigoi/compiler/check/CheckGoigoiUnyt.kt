package io.github.digorydoo.goigoi.compiler.check

import io.github.digorydoo.goigoi.compiler.CheckFailed
import io.github.digorydoo.goigoi.compiler.supportedLanguages
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiTopic
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiUnyt

fun GoigoiUnyt.check(topic: GoigoiTopic) {
    // Check levels

    if (topic.levels.isNotEmpty()) {
        if (levels.isEmpty()) {
            throw CheckFailed(
                "Unyt lvl cannot be empty as topic requires levels: ${topic.levels.joinToString(", ")}",
                this
            )
        }
        levels.forEach { level ->
            if (!topic.levels.contains(level)) {
                throw CheckFailed(
                    "Unyt declares level $level, but topic is constrainted to: ${topic.levels.joinToString(", ")}",
                    this
                )
            }
        }
    }

    if (levels.isNotEmpty()) {
        val toGo = levels.toMutableList()

        for (section in sections) {
            for (word in section.words) {
                if (word.level == null) {
                    throw CheckFailed(
                        "Word level is required and must be one of the unyt levels: " + levels.joinToString(","),
                        this,
                        word
                    )
                }

                if (!word.hidden) {
                    if (!levels.contains(word.level)) {
                        throw CheckFailed(
                            "Level of word (${word.level}) does not match unyt levels: " + levels.joinToString(","),
                            this,
                            word,
                        )
                    }

                    if (toGo.contains(word.level)) {
                        toGo.remove(word.level)
                    }
                }
            }
        }

        if (toGo.isNotEmpty()) {
            throw CheckFailed("Unyt specifies levels that aren't used by any word: $toGo", this)
        }
    }

    // Check that all sections have the same number of translations of their name attribute.

    val languages = arrayOf("en", "de", "fr", "it")
    var langDefined: String? = null

    for (section in sections) {
        val l = languages
            .filter { langId -> section.name.withLanguage(langId).isNotEmpty() }
            .joinToString(", ")

        if (langDefined == null) {
            langDefined = l
        } else if (langDefined != l) {
            throw CheckFailed("Section name translations are inconsistent within unyt: $l", this)
        }
    }

    // Check that translations + hints are unique among visible words in this unyt.

    languages.forEach { langId ->
        val found = mutableSetOf<String>()

        for (section in sections) {
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
                        throw CheckFailed("Translation/hint ($langId) not unique in unyt: $trAndHint", this, word)
                    } else {
                        found.add(trAndHint)
                    }
                }
            }
        }
    }

    // Check that rōmaji are unique among visible words in this unyt.

    if (hasRomaji && !hidden) {
        val found = mutableSetOf<String>()

        for (section in sections) {
            for (word in section.words) {
                if (!word.hidden) {
                    if (found.contains(word.romaji)) {
                        throw CheckFailed("Rōmaji not unique in unyt!", this, word)
                    } else {
                        found.add(word.romaji)
                    }
                }
            }
        }
    }

    // Check if unyt has German translations

    requiredTranslations.forEach {
        if (!supportedLanguages.contains(it)) {
            throw CheckFailed("Not a valid language identifier: $it", this)
        }
    }

    if (!hidden) {
        if (!requiredTranslations.contains("en")) {
            throw CheckFailed("Unyt has no English translation or does not say so", this)
        }

        if (!requiredTranslations.contains("de")) {
            when {
                levels.size == 1 -> Unit // allow missing de when unyt is dedicated to a single JLPT-level
                name.en.startsWith("Numbers") -> Unit // numbers need not be translated
                else -> throw CheckFailed("Unyt must be translated to German as exceptions do not apply!", this)
            }
        }
    }

    // Check if unyt requires ids

    if (!requiresIds) {
        when {
            levels.size == 1 -> Unit // allow missing ids when unyt is dedicated to a single JLPT-level
            name.en.startsWith("Numbers") -> Unit // numbers don't need any id
            else -> throw CheckFailed("Unyt needs to set requiresIds=true as exceptions do not apply!", this)
        }
    }

    // Check that we can rely solely on unyt.hidden when topic is hidden

    if (!hidden && topic.hidden) {
        throw CheckFailed("Unyt is required to be marked as hidden, because the topic is hidden!", this)
    }
}
