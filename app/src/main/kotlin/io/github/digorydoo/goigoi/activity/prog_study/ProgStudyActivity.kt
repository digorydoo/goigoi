package io.github.digorydoo.goigoi.activity.prog_study

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.cjk.isSmallKana
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.QAProvider.NoQAAvailableError
import io.github.digorydoo.goigoi.activity.prog_study.choreo.Choreographer
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.KeyDef
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.KeyLensDrawable
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.ChipSize
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.Mode
import io.github.digorydoo.goigoi.bottom_sheet.WordInfoBottomSheet
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.prog_study.QAKind
import io.github.digorydoo.goigoi.core.prog_study.QuestionAndAnswer
import io.github.digorydoo.goigoi.core.study.Answer
import io.github.digorydoo.goigoi.core.study.StudyItemIterator
import io.github.digorydoo.goigoi.core.study.StudyItemIterator.HowToStudy
import io.github.digorydoo.goigoi.dialog.HintDialogManager
import io.github.digorydoo.goigoi.drawable.IconBuilder
import io.github.digorydoo.goigoi.utils.DeviceUtils
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.ScreenSize
import io.github.digorydoo.goigoi.utils.SingletonHolder

class ProgStudyActivity: AppCompatActivity() {
    private lateinit var bindings: Bindings
    private lateinit var choreo: Choreographer
    private lateinit var controller: Controller
    private lateinit var hintDialogMgr: HintDialogManager
    private lateinit var keyboard: Keyboard
    private lateinit var params: ProgStudyActivityParams
    private lateinit var qaProvider: QAProvider
    private lateinit var values: Values
    private lateinit var screenValues: Values.ScreenDependentValues
    private var unyt: Unyt? = null // super-progressive mode when null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        setContentView(R.layout.prog_study_activity)

        val vocab = SingletonHolder.vocab
        val stats = SingletonHolder.stats
        val kanjiIndex = SingletonHolder.kanjiIndex
        hintDialogMgr = HintDialogManager(stats)
        params = ProgStudyActivityParams.fromIntent(intent)
        bindings = Bindings(this)
        unyt = if (params.unytId.isEmpty()) null else vocab.findUnytById(params.unytId)!!
        val unyt = unyt

        values = Values(this)

        val screenSize = DeviceUtils.getScreenSize(this)
        val orient = DeviceUtils.getOrientation(this)
        screenValues = values.getScreenDependent(screenSize, orient)

        val qaDelegate = object: QAProvider.Delegate {
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
                StudyItemIterator.create(vocab, stats, unyt, HowToStudy.WORST_CONTINUOUSLY) // unyt may be null

            override fun ranOutOfWords() {
                Snackbar.make(bindings.nextBtn, R.string.study_set_restarted, Snackbar.LENGTH_SHORT).show()
            }
        }

        qaProvider = QAProvider(qaDelegate, kanjiIndex, stats)
        val state = savedInstanceState?.let { ProgStudyState.from(it) }

        if (!qaProvider.start(state)) {
            Log.e(TAG, "QAProvider failed to find first word")
            finish()
            return
        }

        bindings.toolbar.let {
            setSupportActionBar(it)
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }

        updateActionBar()

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
                    vocab.writeMyWordsUnytIfNecessary()
                    roundsUntilSave = ROUNDS_UNTIL_SAVE
                }

                updateActionBar()
            }
        }

        val ctrlDelegate = object: Controller.Delegate {
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

    private fun updateActionBar() {
        val ctx = applicationContext
        val unyt = unyt
        supportActionBar?.apply {
            if (unyt != null) {
                title = unyt.name.withSystemLang
                subtitle = qaProvider.getSummary(ctx)
            } else {
                title = qaProvider.getSummary(ctx)
                subtitle = ""
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hintDialogMgr.cancel()
        SingletonHolder.vocab.writeMyWordsUnytIfNecessary()
    }

    private fun infoBtnClicked() {
        bindings.nextBtn.hide()

        val word = qaProvider.qa.word

        // If unyt is not null, all words we show should come from that unyt.
        // If unyt is null, we need to search for the original unyt, and it may not be loaded.
        val unyt = unyt ?: SingletonHolder.vocab.findFirstUnytContainingWordWithSameFile(word)

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
        override val iconWhenCorrect = IconBuilder.getCheckmarkIconDrawable(this@ProgStudyActivity)
        override val iconWhenWrong = IconBuilder.getFlashIconDrawable(this@ProgStudyActivity)

        override val iconWhenAlmostCorrect =
            ContextCompat.getDrawable(this@ProgStudyActivity, R.drawable.ic_attention_24dp)

        override val screenSize = DeviceUtils.getScreenSize(this@ProgStudyActivity)
        override val screenOrientation = DeviceUtils.getOrientation(this@ProgStudyActivity)
        override val minTop = screenValues.minTop

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

                    QAKind.SHOW_PHRASE_ASK_NOTHING,
                    QAKind.SHOW_SENTENCE_ASK_NOTHING,
                    -> R.string.hint_when_asking_nothing

                    QAKind.SHOW_PHRASE_ASK_WORD_KANJI,
                    QAKind.SHOW_SENTENCE_ASK_WORD_KANJI,
                    -> R.string.hint_when_show_s_or_ph_ask_kanji

                    QAKind.SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
                    -> R.string.hint_when_show_ph_translation_ask_ph_kana

                    QAKind.SHOW_PHRASE_ASK_WORD_KANA,
                    QAKind.SHOW_SENTENCE_ASK_WORD_KANA,
                    -> R.string.hint_when_show_s_or_ph_ask_kana
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
        override fun createNewChip(chipText: String, iconResId: Int?, contentDescResId: Int?, size: ChipSize): Chip {
            val chipValues = screenValues.getChipValues(size)

            val textSize =
                if (size == ChipSize.NORMAL && chipText.isSmallKana()) screenValues.smallKanaFontSize
                else chipValues.fontSize

            return Chip(applicationContext).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                text = chipText
                minWidth = chipValues.minWidth.toInt()
                chipMinHeight = chipValues.minHeight
                chipStartPadding = chipValues.lrPadding
                chipEndPadding = chipStartPadding

                if (contentDescResId != null) {
                    contentDescription = ContextCompat.getString(applicationContext, contentDescResId)
                }

                if (iconResId != null) {
                    chipIcon = ContextCompat.getDrawable(applicationContext, iconResId)
                    isChipIconVisible = true
                    chipIconSize = screenValues.iconSize
                    chipStartPadding = screenValues.iconXShift
                }
            }
        }

        override fun createNewChipGroup() =
            ChipGroup(applicationContext).apply {
                chipSpacingVertical = screenValues.chipSpacing.toInt()
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

        override fun applyInputTextTransform(trf: (Keyboard.TextAndCaret) -> Unit) {
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
