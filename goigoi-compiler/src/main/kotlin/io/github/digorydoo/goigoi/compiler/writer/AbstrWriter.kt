package io.github.digorydoo.goigoi.compiler.writer

import java.io.OutputStream

abstract class AbstrWriter(private val stream: OutputStream) {
    private val buf = ByteArray(2)

    abstract fun write()

    protected fun beginTopic(id: String) {
        write(TOPIC_ID_KEY, id)
    }

    protected fun beginUnyt(id: String) {
        write(UNYT_ID_KEY, id)
    }

    protected fun beginPhrase() {
        write(PHRASE_ID_KEY, "") // currently without id
    }

    protected fun beginSentence() {
        write(SENTENCE_ID_KEY, "") // currently without id
    }

    protected fun beginSeeAlso(otherWordId: String) {
        write(WORDLINK_ID_KEY, otherWordId)
    }

    protected fun writeEOFMarker() {
        writeUInt16(EOF_KEY)
    }

    protected fun write(key: Int, value: String) {
        writeUInt16(key)
        writeUTF8(value)
    }

    protected fun writeIfNonEmpty(key: Int, value: String) {
        if (value.isNotEmpty()) {
            write(key, value)
        }
    }

    @Suppress("SameParameterValue")
    protected fun write(key: Int, value: Int) {
        write(key, "$value")
    }

    protected fun write(key: Int, value: Boolean) {
        write(key, "$value")
    }

    private fun writeUInt16(i: Int) {
        require(i in 0 .. 65535) { "Parameter out of range: $i" }
        buf[0] = ((i shr 8) and 0xff).toByte()
        buf[1] = (i and 0xff).toByte()
        stream.write(buf)
    }

    private fun writeUTF8(s: String) {
        val ba = s.toByteArray(Charsets.UTF_8)
        writeUInt16(ba.size)
        stream.write(ba)
    }

    companion object {
        private const val TOPIC_ID_KEY = 1001
        const val TOPIC_NAME_DE_KEY = 1002
        const val TOPIC_NAME_EN_KEY = 1003
        const val TOPIC_NAME_FR_KEY = 1004
        const val TOPIC_NAME_IT_KEY = 1005
        const val TOPIC_NAME_JA_KEY = 1006
        const val TOPIC_IMG_SRC_KEY = 1007
        const val TOPIC_NOTICE_DE_KEY = 1008
        const val TOPIC_NOTICE_EN_KEY = 1009
        const val TOPIC_NOTICE_FR_KEY = 1010
        const val TOPIC_NOTICE_IT_KEY = 1011
        const val TOPIC_NOTICE_JA_KEY = 1012
        const val TOPIC_LINK_TEXT_KEY = 1013
        const val TOPIC_LINK_HREF_KEY = 1014
        const val TOPIC_HIDDEN_KEY = 1015
        const val TOPIC_BG_COLOUR_KEY = 1016

        private const val UNYT_ID_KEY = 2001
        const val UNYT_NAME_DE_KEY = 2002
        const val UNYT_NAME_EN_KEY = 2003
        const val UNYT_NAME_FR_KEY = 2004
        const val UNYT_NAME_IT_KEY = 2005
        const val UNYT_NAME_JA_KEY = 2006
        const val UNYT_STUDY_LANG_KEY = 2007
        const val UNYT_HAS_ROMAJI_KEY = 2008
        const val UNYT_HAS_FURIGANA_KEY = 2009
        const val UNYT_SUBHEADER_DE_KEY = 2010
        const val UNYT_SUBHEADER_EN_KEY = 2011
        const val UNYT_SUBHEADER_FR_KEY = 2012
        const val UNYT_SUBHEADER_IT_KEY = 2013
        const val UNYT_SUBHEADER_JA_KEY = 2014
        const val UNYT_LEVELS_KEY = 2015

        const val WORD_ID_KEY = 4001
        const val WORD_KNOWN_HINT_KEY = 4002
        const val WORD_PRIMARY_FORM_KEY = 4003
        const val WORD_ROMAJI_KEY = 4004
        const val WORD_TRANSLATION_DE_KEY = 4005
        const val WORD_TRANSLATION_EN_KEY = 4006
        const val WORD_TRANSLATION_FR_KEY = 4007
        const val WORD_TRANSLATION_IT_KEY = 4008
        const val WORD_TRANSLATION_JA_KEY = 4009
        const val WORD_DICTIONARY_WORD_KEY = 4010
        const val WORD_HINT_DE_KEY = 4011
        const val WORD_HINT_EN_KEY = 4012
        const val WORD_HINT_FR_KEY = 4013
        const val WORD_HINT_IT_KEY = 4014
        const val WORD_HINT_JA_KEY = 4015
        const val WORD_LEVEL_KEY = 4016
        const val WORD_USUALLY_IN_KANA_KEY = 4017
        const val WORD_STUDY_IN_CONTEXT_KEY = 4018
        const val WORD_FILE_NAME_KEY = 4019
        const val WORD_CATEGORY_KEY = 4020
        const val WORD_SYNONYM_KEY = 4021

        private const val PHRASE_ID_KEY = 5001
        const val PHRASE_PRIMARY_FORM_KEY = 5002
        const val PHRASE_ROMAJI_KEY = 5003
        const val PHRASE_TRANSLATION_DE_KEY = 5004
        const val PHRASE_TRANSLATION_EN_KEY = 5005
        const val PHRASE_TRANSLATION_FR_KEY = 5006
        const val PHRASE_TRANSLATION_IT_KEY = 5007
        const val PHRASE_TRANSLATION_JA_KEY = 5008
        const val PHRASE_EXPLANATION_DE_KEY = 5009
        const val PHRASE_EXPLANATION_EN_KEY = 5010
        const val PHRASE_EXPLANATION_FR_KEY = 5011
        const val PHRASE_EXPLANATION_IT_KEY = 5012
        const val PHRASE_EXPLANATION_JA_KEY = 5013

        private const val SENTENCE_ID_KEY = 6001
        const val SENTENCE_PRIMARY_FORM_KEY = 6002
        const val SENTENCE_ROMAJI_KEY = 6003
        const val SENTENCE_TRANSLATION_DE_KEY = 6004
        const val SENTENCE_TRANSLATION_EN_KEY = 6005
        const val SENTENCE_TRANSLATION_FR_KEY = 6006
        const val SENTENCE_TRANSLATION_IT_KEY = 6007
        const val SENTENCE_TRANSLATION_JA_KEY = 6008
        const val SENTENCE_EXPLANATION_DE_KEY = 6009
        const val SENTENCE_EXPLANATION_EN_KEY = 6010
        const val SENTENCE_EXPLANATION_FR_KEY = 6011
        const val SENTENCE_EXPLANATION_IT_KEY = 6012
        const val SENTENCE_EXPLANATION_JA_KEY = 6013

        private const val WORDLINK_ID_KEY = 7001
        const val WORDLINK_PRIMARY_FORM_KEY = 7002
        const val WORDLINK_TRANSLATION_DE_KEY = 7003
        const val WORDLINK_TRANSLATION_EN_KEY = 7004
        const val WORDLINK_TRANSLATION_FR_KEY = 7005
        const val WORDLINK_TRANSLATION_IT_KEY = 7006
        const val WORDLINK_TRANSLATION_JA_KEY = 7007
        const val WORDLINK_KIND_KEY = 7008

        private const val EOF_KEY = 9999
    }
}
