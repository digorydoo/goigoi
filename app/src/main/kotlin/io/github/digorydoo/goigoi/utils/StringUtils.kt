package io.github.digorydoo.goigoi.utils

import ch.digorydoo.kutils.cjk.IntlString
import io.github.digorydoo.goigoi.BuildConfig

@Suppress("KotlinConstantConditions")
val IntlString.withStudyLang
    get() = when (BuildConfig.FLAVOR) {
        "japanese_free" -> ja
        "french_free" -> fr
        else -> throw RuntimeException("Unhandled build flavor: ${BuildConfig.FLAVOR}")
    }
