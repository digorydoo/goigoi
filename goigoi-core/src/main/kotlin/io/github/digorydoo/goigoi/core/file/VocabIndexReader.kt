package io.github.digorydoo.goigoi.core.file

import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.file.KDataInputStream.FileMarker
import io.github.digorydoo.goigoi.core.db.Topic
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.file.GoigoiFileMarker.*
import java.io.InputStream

class VocabIndexReader(
    stream: InputStream,
    private val vocab: Vocabulary,
): BinaryFileReader(stream) {
    private var topic: Topic? = null
    private var unyt: Unyt? = null
    private val wordFilenames = mutableListOf<String>()

    override fun process(marker: FileMarker, value: String) {
        when (marker) {
            TOPIC_ID -> startNewTopic(value)
            TOPIC_NAME_DE -> topic!!.name.de = value
            TOPIC_NAME_EN -> topic!!.name.en = value
            TOPIC_NAME_FR -> topic!!.name.fr = value
            TOPIC_NAME_IT -> topic!!.name.it = value
            TOPIC_NAME_JA -> topic!!.name.ja = value
            TOPIC_IMG_SRC -> Unit // imgSrc is deprecated
            TOPIC_NOTICE_DE -> Unit // notice is deprecated
            TOPIC_NOTICE_EN -> Unit // notice is deprecated
            TOPIC_NOTICE_FR -> Unit // notice is deprecated
            TOPIC_NOTICE_IT -> Unit // notice is deprecated
            TOPIC_NOTICE_JA -> Unit // notice is deprecated
            TOPIC_LINK_TEXT -> Unit // linkText is deprecated
            TOPIC_LINK_HREF -> Unit // linkHref is deprecated
            TOPIC_HIDDEN -> topic!!.hidden = value.toBoolean()
            TOPIC_BG_COLOUR -> Unit // bgColour is deprecated

            UNYT_ID -> startNewUnyt(value)
            UNYT_NAME_DE -> unyt!!.name.de = value
            UNYT_NAME_EN -> unyt!!.name.en = value
            UNYT_NAME_FR -> unyt!!.name.fr = value
            UNYT_NAME_IT -> unyt!!.name.it = value
            UNYT_NAME_JA -> unyt!!.name.ja = value
            UNYT_STUDY_LANG -> unyt!!.studyLang = value
            UNYT_HAS_ROMAJI -> unyt!!.hasRomaji = value.toBoolean()
            UNYT_HAS_FURIGANA -> unyt!!.hasFurigana = value.toBoolean()
            UNYT_SUBHEADER_DE -> unyt!!.subheader.de = value
            UNYT_SUBHEADER_EN -> unyt!!.subheader.en = value
            UNYT_SUBHEADER_FR -> unyt!!.subheader.fr = value
            UNYT_SUBHEADER_IT -> unyt!!.subheader.it = value
            UNYT_SUBHEADER_JA -> unyt!!.subheader.ja = value
            UNYT_LEVELS -> unyt!!.levels = value.takeIf { it.isNotEmpty() }
                ?.split(",")
                ?.mapNotNull { JLPTLevel.fromStringOrNull(it, nxIsNull = false) }
                ?: emptyList()
            WORD_FILE_NAME -> {
                unyt!!.wordFilenames.add(value)
                wordFilenames.add(value)
            }

            else -> throw Exception("Key not understood: $marker")
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
