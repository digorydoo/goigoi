package io.github.digorydoo.goigoi.core.welcome

import ch.digorydoo.kutils.cjk.IntlString
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.core.stats.Stats
import kotlin.math.min
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DailyProgressTracker(private val stats: Stats) {
    val daily = Array(NUM_DAYS_TO_TRACK) { 0.0f }
    var today = 0f; private set
    var message = IntlString(); private set

    fun update() {
        var m = Moment.now()
        var numPastNonZero = 0

        for (i in daily.indices) {
            val studyCount = stats.getUserStudyCountOfDay(m)
            daily[i] = min(1.0f, studyCount / STUDY_COUNT_OF_FULL_MARK)

            if (i < 6 && daily[i] > 0.0f) {
                numPastNonZero++
            }

            m -= 1.toDuration(DurationUnit.DAYS) // 1.days
        }

        today = daily[0]

        message = when {
            today <= 0.00f -> message1 // さあ、始めよう！
            today <= 0.10f -> message2 // まだ先は長いけど、頑張ろう。
            today <= 0.49f -> message3 // この調子で、どんどん覚えよう！
            today <= 0.60f -> message4 // いいところまで来た。もう少し！
            today <= 0.80f -> message5 // 順調に進んでる。その調子！
            today < CHECKMARK_THRESHOLD -> message6 //　もうすぐ目標達成。頑張ろう！
            else -> message7 // おめでとう！今日の目標達成！
        }
    }

    companion object {
        const val CHECKMARK_THRESHOLD = 0.97f // minimum daily progress to draw the checkmark
        private const val NUM_DAYS_TO_TRACK = 7
        const val STUDY_COUNT_OF_FULL_MARK = 180.0f

        // 0%
        private val message1 = IntlString().apply {
            ja = "さあ、【始：はじ】めよう！"
            en = "Let's get started!"
            de = "Los geht's!"
            fr = "C'est parti !"
            it = "Cominciamo!"
        }

        // 1..10%
        private val message2 = IntlString().apply {
            ja = "まだ【先：さき】は【長：なが】いけど、【頑：がん】【張：ば】ろう。"
            en = "There's still a long way to go, but keep going!"
            de = "Es ist noch ein langer Weg, aber bleib dran!"
            fr = "Le chemin est encore long, mais accroche-toi !"
            it = "La strada è ancora lunga, ma continua così!"
        }

        // 10%..49%
        private val message3 = IntlString().apply {
            ja = "この【調：ちょう】【子：し】で、どんどん【覚：おぼ】えよう！"
            en = "Keep it up! Let's learn even more!"
            de = "Weiter so! Lernen wir noch mehr!"
            fr = "Continue comme ça, apprends-en encore plus !"
            it = "Continua così, impariamo sempre di più!"
        }

        // 49%..60%
        private val message4 = IntlString().apply {
            ja = "いいところまで【来：き】た。もう【少：すこ】し！"
            en = "You've come a long way. Just a bit more!"
            de = "Du hast schon viel geschafft. Nur noch ein bisschen!"
            fr = "Tu es sur la bonne voie. Encore un petit effort !"
            it = "Sei a buon punto. Ancora un po'!"
        }

        // 60%..80%
        private val message5 = IntlString().apply {
            ja = "【順：じゅん】【調：ちょう】に【進：すす】んでる。その【調：ちょう】【子：し】！"
            en = "Making great progress. Keep it up!"
            de = "Gute Fortschritte. Weiter so!"
            fr = "Tu progresses bien. Continue comme ça !"
            it = "Stai andando alla grande. Continua così!"
        }

        // 80%..99%
        private val message6 = IntlString().apply {
            ja = "もうすぐ【目：もく】【標：ひょう】【達：たっ】【成：せい】。【頑：がん】【張：ば】ろう！"
            en = "Almost at your goal. Keep going!"
            de = "Fast am Ziel. Weiter geht's!"
            fr = "Presque au but. Courage !"
            it = "Quasi al traguardo. Dai!"
        }

        // 100%
        private val message7 = IntlString().apply {
            ja = "おめでとう！【今日：きょう】の【目：もく】【標：ひょう】【達：たっ】【成：せい】！"
            en = "Congratulations! You've reached today's goal!"
            de = "Glückwunsch! Tagesziel erreicht!"
            fr = "Félicitations ! Objectif du jour atteint !"
            it = "Congratulazioni! Obiettivo del giorno raggiunto!"
        }
    }
}
