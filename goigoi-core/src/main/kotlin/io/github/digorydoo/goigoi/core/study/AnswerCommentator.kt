package io.github.digorydoo.goigoi.core.study

import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.IntlString
import kotlin.random.Random

class AnswerCommentator {
    class Comment(val ja: FuriganaString, val translation: String)

    fun getComment(answer: Answer, streak: Int): Comment? {
        val comment: IntlString

        if (answer == Answer.CORRECT && streak >= MIN_STREAK_FOR_DISPLAY) {
            comment = getStreakText(streak)
        } else {
            val comments = when (answer) {
                Answer.CORRECT -> responsesIfCorrect
                Answer.CORRECT_EXCEPT_KANA_SIZE -> responseIfAlmostCorrect
                Answer.WRONG -> responsesIfWrong
                else -> return null
            }
            comment = comments[Random.nextInt(comments.size)]
        }

        return Comment(ja = FuriganaString(comment.ja), translation = comment.withSystemLangExcept("ja"))
    }

    private fun getStreakText(streak: Int) =
        IntlString(
            de = "$streak in Folge!",
            en = "$streak in a row!",
            fr = "$streak d'affilée !",
            it = "$streak di fila!",

            ja = when (streak) {
                1 -> "【１：いち】【連：れん】！"
                2 -> "【２：に】【連：れん】！"
                3 -> "【３：さん】【連：れん】！"
                4 -> "【４：よん】【連：れん】！"
                5 -> "【５：ご】【連：れん】！"
                6 -> "【６：ろく】【連：れん】！"
                7 -> "【７：なな】【連：れん】！"
                8 -> "【８：はち】【連：れん】！"
                9 -> "【９：きゅう】【連：れん】！"

                // Starting with 10, we use normal width digits and omit the furigana over the digit.
                else -> "${streak}【連：れん】！"
            }
        )

    companion object {
        private const val MIN_STREAK_FOR_DISPLAY = 3

        private val responsesIfCorrect = arrayOf(
            IntlString(
                de = "Richtig!",
                en = "Correct!",
                fr = "Correct !",
                it = "Corretto!",
                ja = "【正：せい】【解：かい】！"
            ),
            IntlString(
                de = "Genau!",
                en = "That's right!",
                fr = "Exactement !",
                it = "Esatto!",
                ja = "その【通：とお】り！"
            ),
            IntlString(
                de = "Stimmt!",
                en = "You got it!",
                fr = "C'est ça !",
                it = "Giusto!",
                ja = "【合：あ】ってる！"
            ),
            IntlString(
                de = "Ja!",
                en = "Yes!",
                fr = "Oui !",
                it = "Sì!",
                ja = "そう！"
            )
        )
        private val responsesIfWrong = arrayOf(
            IntlString(
                de = "Falsch!",
                en = "Incorrect!",
                fr = "Incorrect !",
                it = "Sbagliato!",
                ja = "【不：ふ】【正：せい】【解：かい】！"
            ),
            IntlString(
                de = "Nicht ganz!",
                en = "Not quite!",
                fr = "Ce n'est pas ça !",
                it = "Non è corretto!",
                ja = "【違：ちが】う！"
            ),
            IntlString(
                de = "Schade!",
                en = "Too bad!",
                fr = "Dommage !",
                it = "Peccato!",
                ja = "【残：ざん】【念：ねん】！"
            ),
            IntlString(
                de = "Nicht getroffen!",
                en = "Missed!",
                fr = "Loupé !",
                it = "Mancato!",
                ja = "ハズレ！"
            )
        )
        private val responseIfAlmostCorrect = arrayOf(
            IntlString(
                de = "Knapp!",
                en = "So close!",
                fr = "Presque !",
                it = "Quasi!",
                ja = "【惜：お】しい！"
            ),
            IntlString(
                de = "Hoppla!",
                en = "Oops!",
                fr = "Oups !",
                it = "Ops!",
                ja = "おっと！"
            )
        )
    }
}
