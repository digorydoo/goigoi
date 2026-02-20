package io.github.digorydoo.goigoi.compiler.writer

import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import java.io.OutputStream

class VocabIndexWriter(private val vocab: GoigoiVocab, stream: OutputStream): AbstrWriter(stream) {
    override fun write() {
        for (topic in vocab.topics) {
            if (!topic.hidden) {
                beginTopic(topic.id)
                write(TOPIC_NAME_DE_KEY, topic.name.de)
                write(TOPIC_NAME_EN_KEY, topic.name.en)
                write(TOPIC_NAME_FR_KEY, topic.name.fr)
                write(TOPIC_NAME_IT_KEY, topic.name.it)
                write(TOPIC_NAME_JA_KEY, topic.name.ja)
                write(TOPIC_IMG_SRC_KEY, topic.imgSrc)
                write(TOPIC_NOTICE_DE_KEY, topic.notice.de)
                write(TOPIC_NOTICE_EN_KEY, topic.notice.en)
                write(TOPIC_NOTICE_FR_KEY, topic.notice.fr)
                write(TOPIC_NOTICE_IT_KEY, topic.notice.it)
                write(TOPIC_NOTICE_JA_KEY, topic.notice.ja)
                write(TOPIC_LINK_TEXT_KEY, topic.linkText)
                write(TOPIC_LINK_HREF_KEY, topic.linkHref)
                write(TOPIC_HIDDEN_KEY, topic.hidden)
                write(TOPIC_BG_COLOUR_KEY, topic.bgColour)

                for (unyt in topic.unyts) {
                    if (!unyt.hidden) {
                        beginUnyt(unyt.id)
                        write(UNYT_NAME_DE_KEY, unyt.name.de)
                        write(UNYT_NAME_EN_KEY, unyt.name.en)
                        write(UNYT_NAME_FR_KEY, unyt.name.fr)
                        write(UNYT_NAME_IT_KEY, unyt.name.it)
                        write(UNYT_NAME_JA_KEY, unyt.name.ja)
                        write(UNYT_STUDY_LANG_KEY, unyt.studyLang)
                        write(UNYT_HAS_ROMAJI_KEY, unyt.hasRomaji)
                        write(UNYT_HAS_FURIGANA_KEY, unyt.hasFurigana)
                        write(UNYT_SUBHEADER_DE_KEY, unyt.subheader.de)
                        write(UNYT_SUBHEADER_EN_KEY, unyt.subheader.en)
                        write(UNYT_SUBHEADER_FR_KEY, unyt.subheader.fr)
                        write(UNYT_SUBHEADER_IT_KEY, unyt.subheader.it)
                        write(UNYT_SUBHEADER_JA_KEY, unyt.subheader.ja)
                        write(UNYT_LEVELS_KEY, unyt.levels.joinToString(","))

                        for (section in unyt.sections) {
                            for (word in section.words) {
                                if (!word.hidden) {
                                    write(WORD_FILE_NAME_KEY, word.fileName)
                                }
                            }
                        }
                    }
                }
            }
        }

        writeEOFMarker()
    }
}
