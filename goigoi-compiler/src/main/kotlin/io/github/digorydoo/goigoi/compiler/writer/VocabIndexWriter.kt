package io.github.digorydoo.goigoi.compiler.writer

import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import io.github.digorydoo.goigoi.core.file.GoigoiFileMarker.*
import java.io.OutputStream

class VocabIndexWriter(private val vocab: GoigoiVocab, stream: OutputStream): AbstrWriter(stream) {
    override fun write() {
        for (topic in vocab.topics) {
            if (!topic.hidden) {
                beginTopic(topic.id)
                write(TOPIC_NAME_DE, topic.name.de)
                write(TOPIC_NAME_EN, topic.name.en)
                write(TOPIC_NAME_FR, topic.name.fr)
                write(TOPIC_NAME_IT, topic.name.it)
                write(TOPIC_NAME_JA, topic.name.ja)
                write(TOPIC_IMG_SRC, topic.imgSrc)
                write(TOPIC_NOTICE_DE, topic.notice.de)
                write(TOPIC_NOTICE_EN, topic.notice.en)
                write(TOPIC_NOTICE_FR, topic.notice.fr)
                write(TOPIC_NOTICE_IT, topic.notice.it)
                write(TOPIC_NOTICE_JA, topic.notice.ja)
                write(TOPIC_LINK_TEXT, topic.linkText)
                write(TOPIC_LINK_HREF, topic.linkHref)
                write(TOPIC_HIDDEN, topic.hidden)
                write(TOPIC_BG_COLOUR, topic.bgColour)

                for (unyt in topic.unyts) {
                    if (!unyt.hidden) {
                        beginUnyt(unyt.id)
                        write(UNYT_NAME_DE, unyt.name.de)
                        write(UNYT_NAME_EN, unyt.name.en)
                        write(UNYT_NAME_FR, unyt.name.fr)
                        write(UNYT_NAME_IT, unyt.name.it)
                        write(UNYT_NAME_JA, unyt.name.ja)
                        write(UNYT_STUDY_LANG, unyt.studyLang)
                        write(UNYT_HAS_ROMAJI, unyt.hasRomaji)
                        write(UNYT_HAS_FURIGANA, unyt.hasFurigana)
                        write(UNYT_SUBHEADER_DE, unyt.subheader.de)
                        write(UNYT_SUBHEADER_EN, unyt.subheader.en)
                        write(UNYT_SUBHEADER_FR, unyt.subheader.fr)
                        write(UNYT_SUBHEADER_IT, unyt.subheader.it)
                        write(UNYT_SUBHEADER_JA, unyt.subheader.ja)
                        write(UNYT_LEVELS, unyt.levels.joinToString(","))

                        for (section in unyt.sections) {
                            for (word in section.words) {
                                if (!word.hidden) {
                                    write(WORD_FILE_NAME, word.fileName)
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
