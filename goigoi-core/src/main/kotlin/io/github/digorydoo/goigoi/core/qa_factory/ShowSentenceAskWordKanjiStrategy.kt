package io.github.digorydoo.goigoi.core.qa_factory

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer.Hint
import io.github.digorydoo.goigoi.core.qa_factory.QAFactory.QuestionData

class ShowSentenceAskWordKanjiStrategy: QACreationStrategy() {
    override fun getQuestion(data: QuestionData): OneOf<String, FuriganaString> =
        OneOf.Second(getQuestionWithGap(data.sentencePrimaryFormInParts!!, data.presentWholeWords))

    override fun getQuestionAfterReveal(data: QuestionData): OneOf<String, FuriganaString> =
        OneOf.Second(data.sentence!!.primaryForm)

    override fun getQuestionHint(data: QuestionData): OneOf<String, Hint> =
        OneOf.First(data.sentence?.translation?.withSystemLang ?: "")

    override fun getExpectedAnswer(data: QuestionData) =
        getExpectedKanjiAnswerForPhraseOrSentence(data.word, data.sentencePrimaryFormInParts!!, data.presentWholeWords)

    override fun getAcceptableAnswers(data: QuestionData) =
        getAcceptableKanjiAnswersForPhraseOrSentence(
            data.word,
            data.sentence!!,
            data.sentencePrimaryFormInParts!!,
            data.presentWholeWords
        )

    override fun getKanjiOrKanaToReveal(data: QuestionData) = ""
    override fun getTranslationToReveal(data: QuestionData) = ""
    override fun getHintToReveal(data: QuestionData): String = ""

    override fun getExplanation(data: QuestionData) =
        data.sentence?.explanation?.withSystemLang?.takeIf { it.isNotEmpty() }?.let { FuriganaString(it) }
}
