package io.github.digorydoo.goigoi.compiler.vocab

class GoigoiWordLink(
    val kind: Kind,
    var wordId: String,
    val remark: String,
    var word: GoigoiWord? = null, // when multiple words share the same id, this is the one we've picked
) {
    enum class Kind {
        XML_SEE_ALSO, // manual <see> link from goigoi-xml
        XML_KEEP_APART, // manual <keep_apart_from> link from goigoi-xml
        XML_KEEP_TOGETHER, // manual <keep_together> link from goigoi-xml
        AUTO_SAME_READING, // same kana when converted to hiragana
        AUTO_SAME_KANJI,
        AUTO_SAME_EN_TRANSLATION, // at least one translation (separated by semicolon) are shared
        AUTO_SAME_DE_TRANSLATION,
    }

    // This will be used by WordFileWriter. Values need to be unique. Keep this list in sync with Goigoi!
    enum class ExtendedKind(val value: Int) {
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

        // I_ADJECTIVE(54),
        // NA_ADJECTIVE(55),
        ADJECTIVE(56),
        ANTONYM(57),
    }

    val extendedKind: ExtendedKind?
        get() = when (kind) {
            Kind.AUTO_SAME_KANJI -> ExtendedKind.SAME_KANJI
            Kind.AUTO_SAME_READING -> ExtendedKind.SAME_READING
            Kind.AUTO_SAME_EN_TRANSLATION -> ExtendedKind.SAME_EN_TRANSLATION
            Kind.AUTO_SAME_DE_TRANSLATION -> ExtendedKind.SAME_DE_TRANSLATION
            Kind.XML_KEEP_APART -> ExtendedKind.KEEP_APART
            Kind.XML_KEEP_TOGETHER -> null // not relevant for Goigoi
            Kind.XML_SEE_ALSO -> when (remark) {
                "closely related" -> ExtendedKind.CLOSELY_RELATED
                "v.t." -> ExtendedKind.TRANSITIVE_VERB
                "v.i." -> ExtendedKind.INTRANSITIVE_VERB
                "verb" -> ExtendedKind.VERB
                "noun" -> ExtendedKind.NOUN
                "adjective" -> ExtendedKind.ADJECTIVE
                "antonym" -> ExtendedKind.ANTONYM
                else -> null
            }
        }

    override fun toString() =
        "GoigoiWordLink(id=$wordId, word=$word)"

    init {
        require(word == null || word?.id == wordId) { "Mismatch between word and id" }
    }
}
