package io.github.digorydoo.goigoi.activity.welcome

import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import ch.digorydoo.kutils.cjk.FuriganaString
import ch.digorydoo.kutils.cjk.IntlString
import ch.digorydoo.kutils.cjk.dateToIntlStringLong
import ch.digorydoo.kutils.cjk.japaneseDayOfWeek
import ch.digorydoo.kutils.cjk.japaneseDayOfWeekAbbrev
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.welcome.BigRing.Companion.CHECKMARK_THRESHOLD
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.drawable.AnimatedDrawable
import io.github.digorydoo.goigoi.drawable.CheckmarkIcon
import io.github.digorydoo.goigoi.drawable.RingIcon
import io.github.digorydoo.goigoi.list.AbstrListItem
import io.github.digorydoo.goigoi.listviewholder.AbstrViewHolder
import io.github.digorydoo.goigoi.stats.Stats
import kotlin.math.min
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class HeaderViewHolder private constructor(
    private val rootView: View,
    private val delegate: Delegate,
): AbstrViewHolder(rootView) {
    interface Delegate {
        fun prefsBtnClicked()
        fun showHintBalloon(text: IntlString, anchor: View, anchor2: View? = null)
    }

    private val prefsBtn = rootView.findViewById<View>(R.id.prefs_btn)
    private val bottomMessage = rootView.findViewById<TextView>(R.id.bottom_message)

    private val charts = arrayOf<ImageView>(
        rootView.findViewById(R.id.chart_day1),
        rootView.findViewById(R.id.chart_day2),
        rootView.findViewById(R.id.chart_day3),
        rootView.findViewById(R.id.chart_day4),
        rootView.findViewById(R.id.chart_day5),
        rootView.findViewById(R.id.chart_day6),
        rootView.findViewById(R.id.chart_day7)
    )

    private val labels = arrayOf<TextView>(
        rootView.findViewById(R.id.label_day1),
        rootView.findViewById(R.id.label_day2),
        rootView.findViewById(R.id.label_day3),
        rootView.findViewById(R.id.label_day4),
        rootView.findViewById(R.id.label_day5),
        rootView.findViewById(R.id.label_day6),
        rootView.findViewById(R.id.label_day7),
    )

    override fun bind(item: AbstrListItem) {
        // NOTE: Even though there is only one item with a HeaderViewHolder, we can't bind stuff
        // from the init function, because animations need to be retriggered and even weekday
        // drawables need to be recreated when coming back from sleep!

        val ctx = rootView.context
        val vocab = Vocabulary.getSingleton(ctx)
        val stats = Stats.getSingleton(ctx)
        val relCounts = Array(charts.size) { 0.0f }
        var m = Moment.now()
        var numPastNonZero = 0

        for (i in 6 downTo 0) {
            val studyCount = stats.getUserStudyCountOfDay(m)
            relCounts[i] = min(1.0f, studyCount / REL_COUNT_DIVISOR)

            if (i < 6 && relCounts[i] > 0.0f) {
                numPastNonZero++
            }

            m -= 1.toDuration(DurationUnit.DAYS) // 1.days not yet available inside AGP
        }

        val todaysRelCount = relCounts[6]

        val animDuration =
            if (todaysRelCount == cachedTodaysRelCount) {
                0L
            } else {
                (500.0f + 1200.0f * todaysRelCount).toLong()
            }

        cachedTodaysRelCount = todaysRelCount

        makeTodaysIcon(todaysRelCount, animDuration)
        makeDayIcons(relCounts, animDuration, numPastNonZero == 0)
        updateBottomMessage(vocab, stats, ctx)

        prefsBtn.setOnClickListener { delegate.prefsBtnClicked() }
    }

    private fun getCentreText(relCount: Float) = when {
        relCount <= 0.00f -> centreText1 // 始めましょう！
        relCount <= 0.10f -> centreText2 // まだ先は長い。
        relCount <= 0.49f -> centreText3 // 頑張って！
        relCount <= 0.60f -> centreText4 // 途中です。
        relCount <= 0.80f -> centreText5 // あなたはそれをうまくやります！
        relCount < CHECKMARK_THRESHOLD -> centreText6 //　もうすぐです！
        else -> centreText7 // おめでとう！
    }

    private fun makeTodaysIcon(relCount: Float, animDuration: Long) {
        val centreText = getCentreText(relCount)
        val drawable = BigRing(rootView.context, relCount, FuriganaString(centreText.ja).kanji)
        val chart = charts[6]
        chart.setImageDrawable(drawable)
        makeAnimation(drawable, chart, 6, animDuration, relCount)
        chart.setOnClickListener { delegate.showHintBalloon(centreText, chart) }

        val m = Moment.now()
        labels[6].text = FuriganaString(m.japaneseDayOfWeek).kanji
        labels[6].setOnClickListener { delegate.showHintBalloon(m.dateToIntlStringLong(true), it) }
    }

    private fun makeDayIcons(relCounts: Array<Float>, animDuration: Long, hide: Boolean) {
        val todaysRelCount = relCounts[6]
        var m = Moment.now()

        for (i in 5 downTo 0) {
            m -= 1.toDuration(DurationUnit.DAYS) // 1.days not yet available inside AGP

            val chart = charts[i]
            val label = labels[i]

            if (hide) {
                chart.visibility = View.GONE
                label.visibility = View.GONE
            } else {
                chart.visibility = View.VISIBLE
                label.visibility = View.VISIBLE

                val drawable = when {
                    relCounts[i] >= 0.98f -> CheckmarkIcon(rootView.context)
                    else -> RingIcon(rootView.context, RingIcon.Variant.CIRCULAR, relCounts[i])
                }

                chart.setImageDrawable(drawable)
                makeAnimation(drawable, chart, i, animDuration, todaysRelCount)

                label.text = m.japaneseDayOfWeekAbbrev.toString()
                chart.contentDescription = label.text // for screen readers

                val m2 = m // because lambdas capture vars by reference, i.e. m changes
                val lambda = { _: View -> delegate.showHintBalloon(m2.dateToIntlStringLong(true), chart, label) }
                chart.setOnClickListener(lambda)
                label.setOnClickListener(lambda)
            }
        }
    }

    private fun makeAnimation(
        chart: AnimatedDrawable,
        chartView: ImageView,
        chartIdx: Int,
        todaysAnimDuration: Long,
        todaysRelCount: Float,
    ) {
        val isToday = chartIdx == 6

        if (todaysAnimDuration <= 0) {
            chart.animValue = 1.0f
        } else {
            chart.animValue = 0.0f

            ValueAnimator.ofFloat(0.0f, 1.0f).apply {
                addUpdateListener { a: ValueAnimator ->
                    chart.animValue = a.animatedValue as Float
                    chartView.invalidate()
                }

                if (isToday) {
                    startDelay = 42
                    duration = todaysAnimDuration
                } else {
                    startDelay = (6 - chartIdx) * 80 + 100 + (todaysRelCount * 900.0f).toLong()
                    duration = 200
                }

                interpolator = DecelerateInterpolator(1.5f)
                start()
            }
        }
    }

    private fun updateBottomMessage(vocab: Vocabulary, stats: Stats, ctx: Context) {
        // We can't reliably determine the number of words learnt except for the super progressive mode, because if we
        // added a stats value for this, the value could get out of hand when words are added or removed from the
        // vocab.
        val total = vocab.allWordFilenames.size
        val learnt = min(total, stats.superProgressiveIdx + vocab.myWordsUnyt.numWordsAvailable)
        bottomMessage.text = ctx.getString(R.string.learnt_n_of_m_words)
            .replace("\${N}", "$learnt")
            .replace("\${M}", "$total")
    }

    companion object {
        private const val REL_COUNT_DIVISOR = 180.0f // number of study count units until day gets full mark

        private var cachedTodaysRelCount = -1.0f

        // 0%
        private val centreText1 = IntlString().apply {
            ja = "【始：はじ】めましょう！"
            en = "Let's get started!"
            de = "Los geht's!"
            fr = "Commençons !"
            it = "Cominciamo!"
        }

        // 1..10%
        private val centreText2 = IntlString().apply {
            ja = "まだ【先：さき】は【長：なが】い。"
            en = "We have a long way to go."
            de = "Wir haben noch einen langen Weg vor uns."
            fr = "Nous avons encore un long chemin à parcourir."
            it = "Abbiamo ancora molta strada da fare."
        }

        // 10%..49%
        private val centreText3 = IntlString().apply {
            ja = "【頑張：がんば】って！"
            en = "Do your best!"
            de = "Gib alles!"
            fr = "Fais de ton mieux !"
            it = "Fai del tuo meglio!"
        }

        // 49%..60%
        private val centreText4 = IntlString().apply {
            ja = "【途：と】【中：ちゅう】です。"
            de = "Die Hälfte hätten wir schon mal."
            en = "We're halfway through."
            fr = "Nous sommes à mi-chemin."
            it = "Siamo a metà."
        }

        // 60%..80%
        private val centreText5 = IntlString().apply {
            ja = "あなたはそれを\nうまくやります！"
            de = "Du machst das grossartig!"
            en = "You're doing great!"
            fr = "Tu le fais bien !"
            it = "Stai andando alla grande!"
        }

        // 80%..99%
        private val centreText6 = IntlString().apply {
            ja = "もうすぐです！"
            en = "Almost there!"
            de = "Fast geschafft!"
            fr = "On y est presque !"
            it = "Ci siamo quasi!"
        }

        // 100%
        private val centreText7 = IntlString().apply {
            ja = "おめでとう！やりましたね。"
            en = "Congratulations! You did it!"
            de = "Gratuliere! Du hast es geschafft!"
            fr = "Félicitations ! Vous avez réussi !"
            it = "Congratulazioni! Ce l'hai fatta!"
        }

        fun create(parent: ViewGroup, inflater: LayoutInflater, delegate: Delegate) =
            HeaderViewHolder(
                inflater.inflate(R.layout.welcome_activity_header, parent, false),
                delegate
            )
    }
}
