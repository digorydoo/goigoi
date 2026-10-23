package io.github.digorydoo.goigoi.core.qa_factory

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.core.db.AcceptableAnswer
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer.Hint
import io.github.digorydoo.goigoi.core.qa_factory.QAFactory.QuestionData

class ShowRomajiAskKanaStrategy: QACreationStrategy() {
    override fun getQuestion(data: QuestionData): OneOf<String, FuriganaString> = OneOf.First(data.word.romaji)
    override fun getQuestionAfterReveal(data: QuestionData): OneOf<String, FuriganaString>? = null
    override fun getQuestionHint(data: QuestionData): OneOf<String, Hint> = OneOf.First("")
    override fun getExpectedAnswer(data: QuestionData) = data.word.kana
    override fun getAcceptableAnswers(data: QuestionData) = listOf<AcceptableAnswer>()

    override fun getKanjiOrKanaToReveal(data: QuestionData): String {
        val word = data.word
        return when {
            word.kanji != word.kana && !word.usuallyInKana -> word.kanji
            else -> ""
        }
    }

    override fun getTranslationToReveal(data: QuestionData) = data.word.translation.withSystemLang
    override fun getHintToReveal(data: QuestionData) = data.word.hintsWithSystemLang
    override fun getExplanation(data: QuestionData) = null
}
