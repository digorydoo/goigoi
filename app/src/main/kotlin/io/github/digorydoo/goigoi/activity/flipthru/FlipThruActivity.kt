package io.github.digorydoo.goigoi.activity.flipthru

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.app.AppCompatActivity
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.flipthru.Choreographer.State
import io.github.digorydoo.goigoi.activity.flipthru.fragment.FlipThruFragment
import io.github.digorydoo.goigoi.activity.flipthru.fragment.FlipThruFragmentParams
import io.github.digorydoo.goigoi.bottom_sheet.WordInfoBottomSheet
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.drawable.CheckmarkIcon
import io.github.digorydoo.goigoi.drawable.FlashIcon
import io.github.digorydoo.goigoi.helper.MyGestureDetector
import io.github.digorydoo.goigoi.stats.Stats
import io.github.digorydoo.goigoi.stats.StatsKey
import io.github.digorydoo.goigoi.study.Answer
import io.github.digorydoo.goigoi.study.StudyItemIterator
import io.github.digorydoo.goigoi.study.StudyItemIterator.HowToStudy
import io.github.digorydoo.goigoi.study.StudyItemIteratorState
import io.github.digorydoo.goigoi.utils.ActivityUtils
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import ch.digorydoo.kutils.cjk.JLPTLevel
import com.google.android.material.snackbar.Snackbar
import kotlin.random.Random

class FlipThruActivity: AppCompatActivity() {
    private lateinit var params: FlipThruActivityParams
    private lateinit var bindings: Bindings
    private lateinit var stats: Stats
    private lateinit var unyt: Unyt
    private lateinit var vocab: Vocabulary
    private lateinit var studyItemIterator: StudyItemIterator
    private lateinit var gesture: MyGestureDetector
    private lateinit var choreo: Choreographer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        setContentView(R.layout.flip_thru_words_activity)

        val ctx = applicationContext
        params = FlipThruActivityParams.fromIntent(intent)
        bindings = Bindings(this)

        vocab = Vocabulary.getSingleton(ctx)
        stats = Stats.getSingleton(ctx)

        unyt = vocab.findUnytById(params.unytId)!!
        studyItemIterator = StudyItemIterator.create(unyt, HowToStudy.EACH_ONCE, ctx)

        savedInstanceState
            ?.let { StudyItemIteratorState.from(it) }
            ?.let { studyItemIterator.restoreState(it) }

        // Set up the toolbar/actionbar

        bindings.toolbar.let {
            ActivityUtils.adjustSubtitleTextColour(it, ctx)
            setSupportActionBar(it)
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            title = unyt.name.withSystemLang
            subtitle = studyItemIterator.getSummary(ctx)
        }

        // Set up gesture detector

        gesture = MyGestureDetector(ctx).apply {
            onTouch = { _ -> choreo.state != State.IDLE }
            onFlingLeft = { choreo.startFlingLeft() }
            onFlingRight = { choreo.startFlingRight() }
            onFlingUp = { choreo.startFlingUp() }
            onFlingDown = { choreo.startFlingDown() }
            onScroll = { dx, dy -> choreo.translateCardTo(dx, dy) }
            onSingleTap = ::cardTapped
            onLetGo = { choreo.animatedTranslateCardTo(0.0f, 0.0f, false) }
            attachTo(bindings.scrollContainer)
        }

        // Adjust card sizes when the layouting has completed

        bindings.scrollContainer.apply {
            viewTreeObserver.addOnGlobalLayoutListener(
                object: OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        choreo.startWordAppearing()
                        // NOTE: We need to call viewTreeObserver again, the old one's gone!
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            )
        }

        bindings.resultImg.apply {
            setImageDrawable(null)
            visibility = View.INVISIBLE
        }

        bindings.actionMsg.visibility = View.INVISIBLE

        choreo = Choreographer(ChoreoDelegate(ctx), bindings)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        StudyItemIteratorState()
            .also { studyItemIterator.saveState(it) }
            .writeTo(outState)
    }

    override fun onPause() {
        super.onPause()
        choreo.pause()
        vocab.writeMyWordsUnytIfNecessary(applicationContext)
    }

    override fun onResume() {
        super.onResume()
        choreo.resume()
    }

    private fun cardTapped() {
        if (choreo.state != State.IDLE) {
            return
        }

        val card = choreo.card ?: return

        if (card.state == FlipThruFragment.State.FRONT) {
            card.state = FlipThruFragment.State.BACK
            gesture.canFlingLeft = true
            gesture.canFlingRight = true
        } else {
            choreo.startShaking()
        }
    }

    private fun showBottomSheet() {
        if (choreo.state == State.PAUSED) {
            return
        }

        WordInfoBottomSheet.show(unyt, studyItemIterator.curWord, supportFragmentManager) {
            choreo.continueFlingDown()
        }

        val ctx = applicationContext
        val stats = Stats.getSingleton(ctx)
        stats.incUserStudyCountOfToday(StatsKey.BOTTOM_SHEET)
    }

    private inner class ChoreoDelegate(ctx: Context): Choreographer.Delegate() {
        override val oneDip = DimUtils.dpToPx(1.0f, ctx)
        override val correctIcon = CheckmarkIcon(ctx)
        override val wrongIcon = FlashIcon(ctx)
        override val textForSkipAction = ctx.getString(R.string.skip_action_text)
        override val textForInfoAction = ctx.getString(R.string.info_action_text)

        override fun createNewCard(): FlipThruFragment {
            val word = studyItemIterator.curWord
            val studyPrimaryForm = Random.nextFloat() > 0.5f
            val isWordKnown = stats.getWordTotalCorrectCount(word) > 5
            val canStudyRomaji = unyt.levelOfMostDifficultWord == JLPTLevel.N5
            val studyRomaji = !isWordKnown && canStudyRomaji

            val studyFurigana = studyPrimaryForm && when (canStudyRomaji) {
                true -> isWordKnown
                false -> !isWordKnown
            }

            val card = FlipThruFragment.create(
                FlipThruFragmentParams(
                    unytId = unyt.id,
                    wordId = word.id,
                    phraseIdx = -1, // phrases and sentences are no longer supported in flip-thru
                    sentenceIdx = -1,
                    studyPrimaryForm = studyPrimaryForm,
                    studyRomaji = studyRomaji,
                    studyFurigana = studyFurigana,
                    studyTranslation = !studyPrimaryForm,
                )
            )

            val mgr = supportFragmentManager
            val fta = mgr.beginTransaction()
            fta.replace(R.id.card_area, card) // replaces the fragment nested inside the card_area
            fta.commit()
            return card
        }

        override fun aboutToStartWordAppearing(newWord: Boolean) {
            val ctx = applicationContext

            if (choreo.state != State.INITIAL && newWord) {
                if (studyItemIterator.hasNext()) {
                    studyItemIterator.next()
                } else {
                    studyItemIterator = StudyItemIterator.create(unyt, HowToStudy.EACH_ONCE, ctx)
                    Snackbar.make(bindings.scrollContainer, R.string.study_set_restarted, Snackbar.LENGTH_SHORT).show()
                }

                supportActionBar?.subtitle = studyItemIterator.getSummary(ctx)
            }

            gesture.canFlingLeft = false
            gesture.canFlingRight = false
            gesture.canFlingUp = true
            gesture.canFlingDown = true
        }

        override fun didFlingFarLeft() {
            studyItemIterator.notifyAnswer(StatsKey.FLIPTHRU, Answer.CORRECT)
        }

        override fun didFlingFarRight() {
            studyItemIterator.notifyAnswer(StatsKey.FLIPTHRU, Answer.WRONG)
        }

        override fun didFlingFarUp() {
            studyItemIterator.notifyAnswer(StatsKey.FLIPTHRU, Answer.SKIP)
        }

        override fun didFlingDown() {
            showBottomSheet()
        }

        override fun didFlingFarDown() {
            studyItemIterator.notifyAnswer(StatsKey.FLIPTHRU, Answer.SKIP)
        }
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "FlipThruActivity"
    }
}
