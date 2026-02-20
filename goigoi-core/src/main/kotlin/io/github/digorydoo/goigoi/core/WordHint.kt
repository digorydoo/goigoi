package io.github.digorydoo.goigoi.core

enum class WordHint(val id: Int, val en: String, val de: String) {
    ABBREV(1, "abbrev.", "Abkürz."),
    ADJECTIVE(2, "adjective", "Adjektiv"),
    ADVERB(3, "adverb", "Adverb"),
    ADVERB_SURU(4, "adverb; suru verb", "Adverb; Suru-Verb"),
    COLLOQUIAL(5, "colloquial", "umgangssprachlich"),
    CONJUNCTION(6, "conjunction", "Konjunktion"),
    COUNTER(7, "counter", "Zählwort"),
    EXPRESSION(8, "expression", "Ausdruck"),
    EXTRA_MODEST(9, "extra-modest", "bescheiden"),
    FORMAL(10, "formal", "formell"),
    HONORIFIC(11, "honorific", "ehrend"),
    HUMBLE(12, "humble", "bescheiden"),
    I_ADJECTIVE(13, "i-adjective", "I-Adjektiv"),
    LOANWORD(14, "loanword", "Lehnwort"),
    MODEST(15, "modest", "bescheiden"),
    NA_ADJECTIVE(16, "na-adjective", "Na-Adjektiv"),
    NOUN(17, "noun", "Nomen"),
    NOUN_ADVERB(18, "noun; adverb", "Nomen; Adverb"),
    NOUN_NA_ADJECTIVE(19, "noun; na-adjective", "Nomen; Na-Adjektiv"),
    NOUN_SUFFIX(20, "noun; suffix", "Nomen; Suffix"),
    NOUN_SURU(21, "noun; suru verb", "Nomen; Suru-Verb"),
    NUMBER(22, "number", "Zahl"),
    PARTICLE(23, "particle", "Partikel"),
    POLITE(24, "polite", "höflich"),
    PREFIX(25, "prefix", "Präfix"),
    PRONOUN(26, "pronoun", "Pronomen"),
    RESPECTFUL(27, "respectful", "respektvoll"),
    SUFFIX(28, "suffix", "Suffix"),
    VERB(29, "verb", "Verb"),
    V_I(30, "v.i.", ""), // hint_de is expected to be empty
    V_T(31, "v.t.", ""), // hint_de is expected to be empty
    V_T_AND_I(32, "v.t. & i.", ""), // hint_de is expected to be empty
    ;

    companion object {
        fun fromInt(id: Int): WordHint? =
            entries.find { it.id == id }

        fun fromENString(en: String): WordHint? =
            entries.find { it.en == en }
    }
}
