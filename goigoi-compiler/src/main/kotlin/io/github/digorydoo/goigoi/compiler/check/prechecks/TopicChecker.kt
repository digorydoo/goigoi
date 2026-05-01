package io.github.digorydoo.goigoi.compiler.check.prechecks

import io.github.digorydoo.goigoi.compiler.CheckFailed
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiTopic

class TopicChecker {
    fun check(topic: GoigoiTopic) {
        if (topic.bgColour.isNotEmpty()) {
            if (!topic.bgColour.startsWith("#") || topic.bgColour.length != 7) {
                throw CheckFailed("Topic bgColour not valid: ${topic.bgColour}")
            }
        }

        if (topic.imgSrc.isNotEmpty() && topic.bgColour.isEmpty()) {
            throw CheckFailed("Topic bgColour is missing while imgSrc is set!")
        }
    }
}
