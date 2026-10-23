package io.github.digorydoo.goigoi.core.qa_factory

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.db.AcceptableAnswer
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer.Hint
import io.github.digorydoo.goigoi.core.qa_factory.QAFactory.QuestionData

class ShowWordAskNothingStrategy: QACreationStrategy() {
    override fun getQuestion(data: QuestionData): OneOf<String, FuriganaString> =
        when {
            data.word.usuallyInKana -> OneOf.First(data.word.kana)
            else -> OneOf.Second(data.word.primaryForm)
        }

    override fun getQuestionAfterReveal(data: QuestionData) = null
    override fun getQuestionHint(data: QuestionData): OneOf<String, Hint> = OneOf.First("")
    override fun getExpectedAnswer(data: QuestionData) = ""
    override fun getAcceptableAnswers(data: QuestionData) = listOf<AcceptableAnswer>()
    override fun getKanjiOrKanaToReveal(data: QuestionData) = ""
    override fun getTranslationToReveal(data: QuestionData) = data.word.translation.withSystemLang
    override fun getHintToReveal(data: QuestionData) = ""
    override fun getExplanation(data: QuestionData) = null
}
