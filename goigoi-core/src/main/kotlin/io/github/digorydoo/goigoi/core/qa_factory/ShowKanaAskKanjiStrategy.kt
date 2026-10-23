package io.github.digorydoo.goigoi.core.qa_factory

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.db.AnswerAccuracy
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer.Hint
import io.github.digorydoo.goigoi.core.qa_factory.QAFactory.QuestionData

class ShowKanaAskKanjiStrategy: QACreationStrategy() {
    override fun getQuestion(data: QuestionData): OneOf<String, FuriganaString> = OneOf.First(data.word.kana)
    override fun getQuestionAfterReveal(data: QuestionData): OneOf<String, FuriganaString>? = null
    override fun getQuestionHint(data: QuestionData): OneOf<String, Hint> = OneOf.First("")
    override fun getExpectedAnswer(data: QuestionData) = data.word.kanji

    override fun getAcceptableAnswers(data: QuestionData) = data.word.acceptableAnswers.filter {
        it.accuracy == AnswerAccuracy.ALT_SPELLING_SAME_WORD
    }

    override fun getKanjiOrKanaToReveal(data: QuestionData) = ""
    override fun getTranslationToReveal(data: QuestionData) = data.word.translation.withSystemLang
    override fun getHintToReveal(data: QuestionData) = data.word.hintsWithSystemLang
    override fun getExplanation(data: QuestionData) = null
}
