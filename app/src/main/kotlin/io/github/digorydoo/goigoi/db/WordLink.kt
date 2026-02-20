package io.github.digorydoo.goigoi.db

import ch.digorydoo.kutils.cjk.IntlString

class WordLink(val wordId: String) {
    enum class Kind(val value: Int) {
        SAME_READING(1),
        SAME_KANJI(2),
        SAME_EN_TRANSLATION(10),
        SAME_DE_TRANSLATION(11),
        CLOSELY_RELATED(40),
        KEEP_APART(41),

        TRANSITIVE_VERB(50),
        INTRANSITIVE_VERB(51),
        VERB(52),
        NOUN(53),
        ADJECTIVE(56),
        ANTONYM(57);

        companion object {
            fun fromInt(value: Int) =
                entries.find { it.value == value }
        }
    }

    var primaryForm = ""
    val translation = IntlString()
    var kind: Kind? = null

    val canCauseAmbiguity: Boolean
        get() = when (kind) {
            Kind.SAME_READING -> true
            Kind.SAME_KANJI -> true
            Kind.SAME_EN_TRANSLATION -> true
            Kind.SAME_DE_TRANSLATION -> true
            Kind.KEEP_APART -> true
            else -> false
        }
}
