package io.github.digorydoo.goigoi.compiler.check

import io.github.digorydoo.goigoi.compiler.CheckFailed
import io.github.digorydoo.goigoi.compiler.Options
import io.github.digorydoo.goigoi.compiler.check.prechecks.PhraseOrSentenceChecker
import io.github.digorydoo.goigoi.compiler.check.prechecks.TopicChecker
import io.github.digorydoo.goigoi.compiler.check.prechecks.UnytChecker
import io.github.digorydoo.goigoi.compiler.check.prechecks.WordChecker
import io.github.digorydoo.goigoi.compiler.check.prechecks.WordLinkChecker
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab

/**
 * These checks happen after the XML has parsed, but before anything gets written. Note that compileGoigoi's kanjiIndex
 * is not available here; those checks happen in PostChecks.
 */
class PreChecks(private val options: Options) {
    private val topicChecker = TopicChecker()
    private val unytChecker = UnytChecker()
    private val wordChecker = WordChecker()
    private val wordLinkChecker = WordLinkChecker()
    private val phraseOrSentenceChecker = PhraseOrSentenceChecker()

    fun check(vocab: GoigoiVocab) {
        var count = 0

        for (topic in vocab.topics) {
            try {
                topicChecker.check(topic)

                for (unyt in topic.unyts) {
                    try {
                        unytChecker.check(unyt, topic)

                        for (section in unyt.sections) {
                            for (word in section.words) {
                                try {
                                    wordChecker.check(word, unyt)

                                    for (phrase in word.phrases) {
                                        try {
                                            phraseOrSentenceChecker.check(phrase, word, unyt)
                                        } catch (e: Exception) {
                                            throw CheckFailed("$phrase:\n${e.message?.prependIndent("   ")}", e)
                                        }
                                    }

                                    for (sentence in word.sentences) {
                                        try {
                                            phraseOrSentenceChecker.check(sentence, word, unyt)
                                        } catch (e: Exception) {
                                            throw CheckFailed("$sentence:\n${e.message?.prependIndent("   ")}", e)
                                        }
                                    }

                                    for (link in word.links) {
                                        try {
                                            wordLinkChecker.check(link, word, unyt, vocab)
                                        } catch (e: Exception) {
                                            throw CheckFailed("$link:\n${e.message?.prependIndent("   ")}", e)
                                        }
                                    }
                                } catch (e: Exception) {
                                    throw CheckFailed("$word:\n${e.message?.prependIndent("   ")}", e)
                                }

                                if (options.quiet && count++ % 400 == 0) print(".")
                            }
                        }
                    } catch (e: Exception) {
                        throw CheckFailed("$unyt:\n${e.message?.prependIndent("   ")}", e)
                    }
                }
            } catch (e: Exception) {
                throw CheckFailed("$topic:\n${e.message?.prependIndent("   ")}", e)
            }
        }
    }
}
