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
            daily[i] = min(1.0f, studyCount / REL_COUNT_DIVISOR)

            if (i < 6 && daily[i] > 0.0f) {
                numPastNonZero++
            }

            m -= 1.toDuration(DurationUnit.DAYS) // 1.days
        }

        today = daily[0]

        message = when {
            today <= 0.00f -> message1 // 始めましょう！
            today <= 0.10f -> message2 // まだ先は長い。
            today <= 0.49f -> message3 // 頑張って！
            today <= 0.60f -> message4 // 途中です。
            today <= 0.80f -> message5 // あなたはそれをうまくやります！
            today < CHECKMARK_THRESHOLD -> message6 //　もうすぐです！
            else -> message7 // おめでとう！
        }
    }

    companion object {
        const val CHECKMARK_THRESHOLD = 0.98f // minimum daily progress to draw the checkmark
        private const val NUM_DAYS_TO_TRACK = 7
        private const val REL_COUNT_DIVISOR = 180.0f // number of study count units until day gets full mark

        // 0%
        private val message1 = IntlString().apply {
            ja = "【始：はじ】めましょう！"
            en = "Let's get started!"
            de = "Los geht's!"
            fr = "Commençons !"
            it = "Cominciamo!"
        }

        // 1..10%
        private val message2 = IntlString().apply {
            ja = "まだ【先：さき】は【長：なが】い。"
            en = "We have a long way to go."
            de = "Wir haben noch einen langen Weg vor uns."
            fr = "Nous avons encore un long chemin à parcourir."
            it = "Abbiamo ancora molta strada da fare."
        }

        // 10%..49%
        private val message3 = IntlString().apply {
            ja = "【頑張：がんば】って！"
            en = "Do your best!"
            de = "Gib alles!"
            fr = "Fais de ton mieux !"
            it = "Fai del tuo meglio!"
        }

        // 49%..60%
        private val message4 = IntlString().apply {
            ja = "【途：と】【中：ちゅう】です。"
            de = "Die Hälfte hätten wir schon mal."
            en = "We're halfway through."
            fr = "Nous sommes à mi-chemin."
            it = "Siamo a metà."
        }

        // 60%..80%
        private val message5 = IntlString().apply {
            ja = "あなたはそれを\nうまくやります！"
            de = "Du machst das grossartig!"
            en = "You're doing great!"
            fr = "Tu le fais bien !"
            it = "Stai andando alla grande!"
        }

        // 80%..99%
        private val message6 = IntlString().apply {
            ja = "もうすぐです！"
            en = "Almost there!"
            de = "Fast geschafft!"
            fr = "On y est presque !"
            it = "Ci siamo quasi!"
        }

        // 100%
        private val message7 = IntlString().apply {
            ja = "おめでとう！やりましたね。"
            en = "Congratulations! You did it!"
            de = "Gratuliere! Du hast es geschafft!"
            fr = "Félicitations ! Vous avez réussi !"
            it = "Congratulazioni! Ce l'hai fatta!"
        }
    }
}
