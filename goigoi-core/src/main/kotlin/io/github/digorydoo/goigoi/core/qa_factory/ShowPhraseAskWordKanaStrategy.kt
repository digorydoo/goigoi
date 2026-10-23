package io.github.digorydoo.goigoi.core.qa_factory

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer.Hint
import io.github.digorydoo.goigoi.core.qa_factory.QAFactory.QuestionData

class ShowPhraseAskWordKanaStrategy: QACreationStrategy() {
    override fun getQuestion(data: QuestionData): OneOf<String, FuriganaString> =
        OneOf.Second(getQuestionWithGap(data.phrasePrimaryFormInParts!!, data.presentWholeWords))

    override fun getQuestionAfterReveal(data: QuestionData): OneOf<String, FuriganaString> =
        OneOf.Second(data.phrase!!.primaryForm)

    override fun getQuestionHint(data: QuestionData): OneOf<String, Hint> =
        OneOf.First(data.phrase?.translation?.withSystemLang ?: "")

    override fun getExpectedAnswer(data: QuestionData) =
        getExpectedKanaAnswerForPhraseOrSentence(data.word, data.phrasePrimaryFormInParts!!, data.presentWholeWords)

    override fun getAcceptableAnswers(data: QuestionData) =
        getAcceptableKanaAnswersForPhraseOrSentence(
            data.word,
            data.phrase!!,
            data.phrasePrimaryFormInParts!!,
            data.presentWholeWords
        )

    override fun getKanjiOrKanaToReveal(data: QuestionData) = ""
    override fun getTranslationToReveal(data: QuestionData) = ""
    override fun getHintToReveal(data: QuestionData) = ""

    override fun getExplanation(data: QuestionData) =
        data.phrase?.explanation?.withSystemLang?.takeIf { it.isNotEmpty() }?.let { FuriganaString(it) }
}
