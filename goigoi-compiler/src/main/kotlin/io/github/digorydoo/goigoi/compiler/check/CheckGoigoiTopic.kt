package io.github.digorydoo.goigoi.compiler.check

import io.github.digorydoo.goigoi.compiler.CheckFailed
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiTopic

fun GoigoiTopic.check() {
    if (bgColour.isNotEmpty()) {
        if (!bgColour.startsWith("#") || bgColour.length != 7) {
            throw CheckFailed("Topic bgColour not valid: $bgColour", this)
        }
    }

    if (imgSrc.isNotEmpty() && bgColour.isEmpty()) {
        throw CheckFailed("Topic bgColour is missing while imgSrc is set!", this)
    }
}
