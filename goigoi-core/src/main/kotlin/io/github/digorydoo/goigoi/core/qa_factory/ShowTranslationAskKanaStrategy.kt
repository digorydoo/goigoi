package io.github.digorydoo.goigoi.core.qa_factory

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer.Hint
import io.github.digorydoo.goigoi.core.qa_factory.QAFactory.QuestionData

class ShowTranslationAskKanaStrategy: QACreationStrategy() {
    override fun getQuestion(data: QuestionData): OneOf<String, FuriganaString> =
        OneOf.First(data.word.translation.withSystemLang)

    override fun getQuestionAfterReveal(data: QuestionData): OneOf<String, FuriganaString>? = null
    override fun getQuestionHint(data: QuestionData): OneOf<String, Hint> = OneOf.First(data.word.hintsWithSystemLang)
    override fun getExpectedAnswer(data: QuestionData) = data.word.kana
    override fun getAcceptableAnswers(data: QuestionData) = data.word.acceptableAnswers.filter { it.isKana }

    override fun getKanjiOrKanaToReveal(data: QuestionData): String {
        val word = data.word
        return when {
            word.kanji != word.kana && !word.usuallyInKana -> word.kanji
            else -> ""
        }
    }

    override fun getTranslationToReveal(data: QuestionData) = ""
    override fun getHintToReveal(data: QuestionData) = ""
    override fun getExplanation(data: QuestionData) = null
}
