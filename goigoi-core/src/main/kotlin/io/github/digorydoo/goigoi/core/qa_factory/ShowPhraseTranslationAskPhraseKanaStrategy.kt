package io.github.digorydoo.goigoi.core.qa_factory

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer.Hint
import io.github.digorydoo.goigoi.core.qa_factory.QAFactory.QuestionData

class ShowPhraseTranslationAskPhraseKanaStrategy: QACreationStrategy() {
    override fun getQuestion(data: QuestionData): OneOf<String, FuriganaString> =
        OneOf.First(data.phrase!!.translation.withSystemLang)

    override fun getQuestionAfterReveal(data: QuestionData): OneOf<String, FuriganaString>? = null
    override fun getQuestionHint(data: QuestionData): OneOf<String, Hint> = OneOf.Second(Hint.PHRASE)
    override fun getExpectedAnswer(data: QuestionData) = data.phrase?.kana ?: ""

    override fun getAcceptableAnswers(data: QuestionData) =
        getAcceptableKanaAnswersForFullPhrase(data.word, data.phrase!!, data.phrasePrimaryFormInParts!!)

    override fun getKanjiOrKanaToReveal(data: QuestionData) = ""
    override fun getTranslationToReveal(data: QuestionData) = ""
    override fun getHintToReveal(data: QuestionData) = ""
    override fun getExplanation(data: QuestionData) = null
}
