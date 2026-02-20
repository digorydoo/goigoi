package io.github.digorydoo.goigoi.file

import io.github.digorydoo.goigoi.db.Topic
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import ch.digorydoo.kutils.cjk.JLPTLevel
import java.io.InputStream

class VocabIndexReader(
    stream: InputStream,
    private val vocab: Vocabulary,
): BinaryFileReader(stream) {
    private var topic: Topic? = null
    private var unyt: Unyt? = null
    private val wordFilenames = mutableListOf<String>()

    override fun process(key: Int, value: String) {
        when (key) {
            TOPIC_ID_KEY -> startNewTopic(value)
            TOPIC_NAME_DE_KEY -> topic!!.name.de = value
            TOPIC_NAME_EN_KEY -> topic!!.name.en = value
            TOPIC_NAME_FR_KEY -> topic!!.name.fr = value
            TOPIC_NAME_IT_KEY -> topic!!.name.it = value
            TOPIC_NAME_JA_KEY -> topic!!.name.ja = value
            TOPIC_IMG_SRC_KEY -> Unit // imgSrc is deprecated
            TOPIC_NOTICE_DE_KEY -> Unit // notice is deprecated
            TOPIC_NOTICE_EN_KEY -> Unit // notice is deprecated
            TOPIC_NOTICE_FR_KEY -> Unit // notice is deprecated
            TOPIC_NOTICE_IT_KEY -> Unit // notice is deprecated
            TOPIC_NOTICE_JA_KEY -> Unit // notice is deprecated
            TOPIC_LINK_TEXT_KEY -> Unit // linkText is deprecated
            TOPIC_LINK_HREF_KEY -> Unit // linkHref is deprecated
            TOPIC_HIDDEN_KEY -> topic!!.hidden = value.toBoolean()
            TOPIC_BG_COLOUR_KEY -> Unit // bgColour is deprecated

            UNYT_ID_KEY -> startNewUnyt(value)
            UNYT_NAME_DE_KEY -> unyt!!.name.de = value
            UNYT_NAME_EN_KEY -> unyt!!.name.en = value
            UNYT_NAME_FR_KEY -> unyt!!.name.fr = value
            UNYT_NAME_IT_KEY -> unyt!!.name.it = value
            UNYT_NAME_JA_KEY -> unyt!!.name.ja = value
            UNYT_STUDY_LANG_KEY -> unyt!!.studyLang = value
            UNYT_HAS_ROMAJI_KEY -> unyt!!.hasRomaji = value.toBoolean()
            UNYT_HAS_FURIGANA_KEY -> unyt!!.hasFurigana = value.toBoolean()
            UNYT_SUBHEADER_DE_KEY -> unyt!!.subheader.de = value
            UNYT_SUBHEADER_EN_KEY -> unyt!!.subheader.en = value
            UNYT_SUBHEADER_FR_KEY -> unyt!!.subheader.fr = value
            UNYT_SUBHEADER_IT_KEY -> unyt!!.subheader.it = value
            UNYT_SUBHEADER_JA_KEY -> unyt!!.subheader.ja = value
            UNYT_LEVELS_KEY -> unyt!!.levels = value.takeIf { it.isNotEmpty() }
                ?.split(",")
                ?.mapNotNull { JLPTLevel.fromString(it) }
                ?: emptyList()
            WORD_FILE_NAME_KEY -> {
                unyt!!.wordFilenames.add(value)
                wordFilenames.add(value)
            }

            else -> throw Exception("Key not understood: $key")
        }
    }

    override fun done() {
        vocab.setWordFilenames(wordFilenames)
    }

    private fun startNewTopic(id: String) {
        topic = vocab.createNewTopic(id)
        unyt = null
    }

    private fun startNewUnyt(id: String) {
        unyt = vocab.createNewUnyt(topic!!, id)
    }
}
