package io.github.digorydoo.goigoi.file

import java.io.InputStream

abstract class BinaryFileReader(private val stream: InputStream) {
    private val buf = ByteArray(2)

    fun read() {
        while (true) {
            val key = readUInt16()

            if (key == EOF_KEY) {
                done()
                break
            } else {
                val value = readUTF8()
                process(key, value)
            }
        }
    }

    abstract fun process(key: Int, value: String)
    open fun done() {}

    private fun readUInt16(): Int {
        val count = stream.read(buf)
        require(count == 2) { "Unexpected end of file" }
        val u = buf[0].toInt() and 0xff
        val v = buf[1].toInt() and 0xff
        return (u shl 8) or v
    }

    private fun readUTF8(): String {
        val size = readUInt16()
        return if (size == 0) {
            ""
        } else {
            val tmp = ByteArray(size)
            val count = stream.read(tmp)
            require(count == size) { "Unexpected end of file" }
            tmp.toString(Charsets.UTF_8)
        }
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "BinaryFileReader"

        const val TOPIC_ID_KEY = 1001
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

        const val UNYT_ID_KEY = 2001
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

        const val PHRASE_ID_KEY = 5001
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

        const val SENTENCE_ID_KEY = 6001
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

        const val WORDLINK_ID_KEY = 7001
        const val WORDLINK_PRIMARY_FORM_KEY = 7002
        const val WORDLINK_TRANSLATION_DE_KEY = 7003
        const val WORDLINK_TRANSLATION_EN_KEY = 7004
        const val WORDLINK_TRANSLATION_FR_KEY = 7005
        const val WORDLINK_TRANSLATION_IT_KEY = 7006
        const val WORDLINK_TRANSLATION_JA_KEY = 7007
        const val WORDLINK_KIND_KEY = 7008

        const val EOF_KEY = 9999
    }
}
