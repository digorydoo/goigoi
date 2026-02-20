package io.github.digorydoo.goigoi.activity.prog_study

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ch.digorydoo.kutils.cjk.JLPTLevel
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.QAProvider.NoQAAvailableError
import io.github.digorydoo.goigoi.activity.prog_study.choreo.Choreographer
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.KeyDef
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.KeyLensDrawable
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.Mode
import io.github.digorydoo.goigoi.bottom_sheet.WordInfoBottomSheet
import io.github.digorydoo.goigoi.db.KanjiIndex
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.dialog.HintDialogManager
import io.github.digorydoo.goigoi.drawable.CheckmarkIcon
import io.github.digorydoo.goigoi.drawable.FlashIcon
import io.github.digorydoo.goigoi.stats.Stats
import io.github.digorydoo.goigoi.study.Answer
import io.github.digorydoo.goigoi.study.StudyItemIterator
import io.github.digorydoo.goigoi.study.StudyItemIterator.HowToStudy
import io.github.digorydoo.goigoi.utils.ActivityUtils
import io.github.digorydoo.goigoi.utils.DeviceUtils
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.ScreenSize

class ProgStudyActivity: AppCompatActivity() {
    private lateinit var bindings: Bindings
    private lateinit var choreo: Choreographer
    private lateinit var controller: Controller
    private lateinit var hintDialogMgr: HintDialogManager
    private lateinit var kanjiIndex: KanjiIndex
    private lateinit var keyboard: Keyboard
    private lateinit var params: ProgStudyActivityParams
    private lateinit var qaProvider: QAProvider
    private lateinit var vocab: Vocabulary
    private lateinit var stats: Stats
    private lateinit var values: Values
    private var unyt: Unyt? = null // super-progressive mode when null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        setContentView(R.layout.prog_study_activity)

        val ctx = applicationContext
        vocab = Vocabulary.getSingleton(ctx)

        stats = Stats.getSingleton(ctx)
        hintDialogMgr = HintDialogManager(stats)
        kanjiIndex = KanjiIndex.getSingleton(ctx)
        params = ProgStudyActivityParams.fromIntent(intent)
        bindings = Bindings(this)
        values = Values(this)
        unyt = if (params.unytId.isEmpty()) null else vocab.findUnytById(params.unytId)!!

        val qaDelegate = object: QAProvider.Delegate {
            override val isInTestLab = DeviceUtils.isInTestLab(ctx)

            override val canUseRomaji: Boolean
                get() {
                    val unyt = unyt
                    if (unyt != null) {
                        // When studying a unyt solo, use rōmaji if the unyt is dedicated to N5.
                        return unyt.hasRomaji && unyt.levels.size == 1 && unyt.levels[0] == JLPTLevel.N5
                    } else {
                        // In super progressive mode, use rōmaji only for the first few words.
                        return stats.superProgressiveIdx < MAX_SUPER_PROGRESSIVE_IDX_FOR_ROMAJI
                    }
                }

            override val averageLevelOfWords: JLPTLevel
                get() = (unyt ?: vocab.myWordsUnyt).averageLevelOfWords() ?: JLPTLevel.N5

            override fun createIterator() =
                StudyItemIterator.create(unyt, HowToStudy.WORST_CONTINUOUSLY, ctx) // unyt may be null

            override fun ranOutOfWords() {
                Snackbar.make(bindings.nextBtn, R.string.study_set_restarted, Snackbar.LENGTH_SHORT).show()
            }
        }

        qaProvider = QAProvider(qaDelegate, stats, kanjiIndex)
        val state = savedInstanceState?.let { ProgStudyState.from(it) }

        if (!qaProvider.start(state)) {
            Log.e(TAG, "QAProvider failed to find first word")
            finish()
            return
        }

        bindings.toolbar.let {
            ActivityUtils.adjustSubtitleTextColour(it, ctx)
            setSupportActionBar(it)
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            title = unyt?.name?.withSystemLang ?: ""
            subtitle = qaProvider.getSummary(ctx)
        }

        val screenSize = DeviceUtils.getScreenSize(this)
        val orient = DeviceUtils.getOrientation(this)

        if (screenSize != ScreenSize.LARGE && orient != Orientation.PORTRAIT) {
            // There is too little vertical space in landscape on SMALL and NORMAL sized phones for the AppBar!
            bindings.toolbar.visibility = View.GONE
        }

        if (screenSize == ScreenSize.SMALL || (screenSize == ScreenSize.NORMAL && orient != Orientation.PORTRAIT)) {
            // There is too little vertical space to show the "what to do" hint.
            bindings.whatToDoTextView.visibility = View.GONE
        }

        val choreoDelegate = ChoreoDelegate()
        choreo = Choreographer(choreoDelegate, bindings, values)
        keyboard = Keyboard(KeyboardDelegate(), bindings, values)

        bindings.infoBtn.apply {
            setOnClickListener { infoBtnClicked() }
        }

        var roundsUntilSave = ROUNDS_UNTIL_SAVE

        bindings.nextBtn.apply {
            setOnClickListener {
                try {
                    controller.nextBtnClicked()
                } catch (_: NoQAAvailableError) {
                    Log.e(TAG, "No QA available!")
                    finish()
                }

                if (--roundsUntilSave <= 0) {
                    vocab.writeMyWordsUnytIfNecessary(ctx)
                    roundsUntilSave = ROUNDS_UNTIL_SAVE
                }

                supportActionBar?.subtitle = qaProvider.getSummary(ctx)
            }
        }

        val ctrlDelegate = object: Controller.Delegate {
            override fun isInTestLab() =
                DeviceUtils.isInTestLab(this@ProgStudyActivity)

            override fun showKeyboardHintIfAppropriate(qa: QuestionAndAnswer, mode: Mode) {
                hintDialogMgr.showKeyboardHintIfAppropriate(qa, mode, this@ProgStudyActivity)
            }
        }

        controller = Controller(ctrlDelegate, bindings, choreo, kanjiIndex, keyboard, qaProvider, stats, unyt, vocab)
        var didInit = false

        bindings.rootView.viewTreeObserver.addOnGlobalLayoutListener {
            // We come here whenever choreo requests a layout reformatting.
            choreo.updateLayoutIfNecessary()

            if (!didInit) {
                // We come here only at the first formatting.
                didInit = true
                state?.answer
                    ?.takeIf { it != Answer.NONE }
                    ?.let {
                        controller.answer = it
                        choreo.revealAnswer(it, null)
                        bindings.nextBtn.show()
                    }
            }
        }

        controller.updateContent()
    }

    override fun onPause() {
        super.onPause()
        hintDialogMgr.cancel()
        vocab.writeMyWordsUnytIfNecessary(applicationContext)
    }

    private fun infoBtnClicked() {
        bindings.nextBtn.hide()

        val word = qaProvider.qa.word

        // If unyt is not null, all words we show should come from that unyt.
        // If unyt is null, we need to search for the original unyt, and it may not be loaded.
        val unyt = unyt ?: vocab.findFirstUnytContainingWordWithSameFile(word)

        WordInfoBottomSheet.show(unyt, word, supportFragmentManager) {
            bindings.nextBtn.show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        ProgStudyState()
            .also {
                it.answer = controller.answer
                qaProvider.saveState(it)
            }
            .writeTo(outState)
    }

    private inner class ChoreoDelegate: Choreographer.Delegate {
        override val iconWhenCorrect = CheckmarkIcon(this@ProgStudyActivity)
        override val iconWhenWrong = FlashIcon(this@ProgStudyActivity)
        override val iconWhenAlmostCorrect =
            ContextCompat.getDrawable(this@ProgStudyActivity, R.drawable.ic_attention_24dp)

        override val screenSize = DeviceUtils.getScreenSize(this@ProgStudyActivity)
        override val screenOrientation = DeviceUtils.getOrientation(this@ProgStudyActivity)
        override val minTop = values.getMinTop(screenSize, screenOrientation)

        override fun getWhatToDoHint(qa: QuestionAndAnswer): String =
            ContextCompat.getString(
                this@ProgStudyActivity,
                when (qa.kind) {
                    QAKind.SHOW_KANJI_ASK_KANA -> R.string.hint_when_show_kanji_ask_kana
                    QAKind.SHOW_KANA_ASK_KANJI -> when (qa.answers.firstOrNull()?.length == 1) {
                        true -> R.string.hint_when_show_kana_ask_kanji_singular
                        false -> R.string.hint_when_show_kana_ask_kanji_plural
                    }
                    QAKind.SHOW_ROMAJI_ASK_KANA -> R.string.hint_when_show_romaji_ask_kana
                    QAKind.SHOW_TRANSLATION_ASK_KANA -> R.string.hint_when_show_translation_ask_kana

                    QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
                    -> R.string.hint_when_show_translation_ask_kanji_among_similar

                    QAKind.SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
                    -> R.string.hint_when_show_translation_ask_kanji_among_words

                    QAKind.SHOW_WORD_ASK_NOTHING -> R.string.hint_when_new_word
                    QAKind.SHOW_PHRASE_ASK_NOTHING -> R.string.hint_when_asking_nothing
                    QAKind.SHOW_SENTENCE_ASK_NOTHING -> R.string.hint_when_asking_nothing
                    QAKind.SHOW_PHRASE_ASK_KANJI -> R.string.hint_when_show_s_or_ph_ask_kanji
                    QAKind.SHOW_SENTENCE_ASK_KANJI -> R.string.hint_when_show_s_or_ph_ask_kanji

                    QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
                    -> R.string.hint_when_show_ph_translation_ask_ph_kana
                }
            )

        override fun getHint(hint: QuestionAndAnswer.Hint): String =
            ContextCompat.getString(
                this@ProgStudyActivity,
                when (hint) {
                    QuestionAndAnswer.Hint.PHRASE -> R.string.asking_phrase
                }
            )

        override fun getAnswerComment(answer: Answer): String {
            val textId = when (answer) {
                Answer.CORRECT -> R.string.answer_correct
                Answer.WRONG -> R.string.answer_wrong
                Answer.CORRECT_EXCEPT_KANA_SIZE -> R.string.answer_correct_except_kana_size
                Answer.TRIVIAL -> null
                Answer.SKIP -> null
                Answer.NONE -> null
            }
            return if (textId == null) "" else ContextCompat.getString(this@ProgStudyActivity, textId)
        }
    }

    private inner class KeyboardDelegate: Keyboard.Delegate() {
        private val screenSize = DeviceUtils.getScreenSize(this@ProgStudyActivity)
        private val orient = DeviceUtils.getOrientation(this@ProgStudyActivity)

        override val chipFontSize = values.getChipFontSize(screenSize, orient)
        override val chipFontSizeForSmallKana = values.getChipFontSizeForSmallKana(screenSize, orient)

        override fun createNewChip(iconResId: Int?, contentDescResId: Int?) =
            Chip(applicationContext).apply {
                minWidth = values.getChipMinWidth(screenSize, orient).toInt()
                chipMinHeight = values.getChipMinHeight(screenSize, orient)
                if (contentDescResId != null) {
                    contentDescription = ContextCompat.getString(applicationContext, contentDescResId)
                }
                if (iconResId != null) {
                    chipIcon = ContextCompat.getDrawable(applicationContext, iconResId)
                    isChipIconVisible = true
                    chipIconSize = values.getChipIconSize(screenSize, orient)
                    chipStartPadding = values.getChipIconXShift(screenSize, orient)
                }
            }

        override fun createNewChipGroup() =
            ChipGroup(applicationContext).apply {
                chipSpacingVertical = values.getChipSpacing(screenSize).toInt()
                chipSpacingHorizontal = chipSpacingVertical
                layoutParams = ChipGroup.LayoutParams(
                    ChipGroup.LayoutParams.WRAP_CONTENT,
                    ChipGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    topMargin = chipSpacingVertical
                }
            }

        override fun createNewButton(strResId: Int) =
            Button(applicationContext).apply {
                text = ContextCompat.getString(applicationContext, strResId)
            }

        override fun createNewKeyLensDrawable(keyDef: KeyDef) =
            KeyLensDrawable(applicationContext, keyDef)

        override fun applyInputTextTransform(trf: (text: String) -> String) {
            choreo.applyInputTextTransform(trf)
        }

        override fun okBtnClicked() {
            if (choreo.canAcceptInput()) {
                controller.checkAndRevealAnswer()
            }
        }
    }

    companion object {
        private const val TAG = "ProgStudyActv"
        private const val ROUNDS_UNTIL_SAVE = 10
        private const val MAX_SUPER_PROGRESSIVE_IDX_FOR_ROMAJI = 100 // still well into N5
    }
}
