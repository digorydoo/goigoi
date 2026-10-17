package io.github.digorydoo.goigoi.activity.prog_study

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import io.github.digorydoo.goigoi.activity.prog_study.composables.ProgStudyScreen
import io.github.digorydoo.goigoi.activity.prog_study.utils.ProgStudyState
import io.github.digorydoo.goigoi.activity.prog_study.utils.restoreState
import io.github.digorydoo.goigoi.activity.prog_study.utils.saveState
import io.github.digorydoo.goigoi.composables.bottom_sheet.word_info.WordInfoBottomSheetModel
import io.github.digorydoo.goigoi.composables.providers.DevicePropsProvider
import io.github.digorydoo.goigoi.composables.providers.GoigoiTheme
import io.github.digorydoo.goigoi.core.db.Vocabulary
import io.github.digorydoo.goigoi.core.prog_study.FixedKeysProvider
import io.github.digorydoo.goigoi.core.prog_study.KeyActionHandler
import io.github.digorydoo.goigoi.core.prog_study.QAFactory
import io.github.digorydoo.goigoi.core.prog_study.QAProvider
import io.github.digorydoo.goigoi.core.prog_study.RoundsTracker
import io.github.digorydoo.goigoi.core.study.AnswerChecker
import io.github.digorydoo.goigoi.core.study.AnswerCommentator
import io.github.digorydoo.goigoi.core.study.StudyItemIterator
import io.github.digorydoo.goigoi.core.study.StudyItemIterator.HowToStudy
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.getOrCreateSingletons

class ProgStudyActivity: ComponentActivity() {
    private lateinit var qaProvider: QAProvider
    private lateinit var model: ProgStudyActivityModel
    private lateinit var studyItemIterator: StudyItemIterator
    private lateinit var rounds: RoundsTracker
    private lateinit var vocab: Vocabulary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ResUtils.setActivityTheme(this)
        enableEdgeToEdge()

        val singletons = getOrCreateSingletons()
        val kanjiIndex = singletons.kanjiIndex
        val prefs = singletons.prefs
        val stats = singletons.stats
        vocab = singletons.vocab

        val fixedKeysProvider = FixedKeysProvider(vocab.myWordsUnyt, kanjiIndex)
        val keyActionHandler = KeyActionHandler()
        val answerChecker = AnswerChecker()
        val answerCommentator = AnswerCommentator()
        val qaFactory = QAFactory(stats)
        val bottomSheetModel = WordInfoBottomSheetModel(kanjiIndex, stats)

        studyItemIterator = StudyItemIterator.create(vocab, stats, null, HowToStudy.WORST_CONTINUOUSLY)
        rounds = RoundsTracker()
        qaProvider = QAProvider(studyItemIterator, kanjiIndex, rounds, stats, vocab)

        val state = savedInstanceState?.let { ProgStudyState.from(it) }

        if (state != null) {
            studyItemIterator.restoreState(state.studyItemIteratorState)
            rounds.restoreState(state)

            qaProvider.start(
                qaFactory.createQA(
                    studyItemIterator.curWord,
                    state.qaKind,
                    state.qaIndex,
                    state.showFuriganaWithQuestion
                )
            )
        } else {
            qaProvider.start(null)
        }

        model = ProgStudyActivityModel(
            qaProvider,
            studyItemIterator,
            answerChecker,
            answerCommentator,
            fixedKeysProvider,
            keyActionHandler,
            stats,
            vocab,
            lifecycleScope
        )

        model.showNextQuestion(initial = true)

        setContent {
            DevicePropsProvider(this) {
                GoigoiTheme(prefs.theme) {
                    ProgStudyScreen(model, bottomSheetModel, onBack = { finish() })
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        vocab.writeMyWordsUnytIfNecessary()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val state = ProgStudyState()
        state.answer = model.answerCorrectness.value

        // FIXME restoring state after answer was given does not properly work
        val qa = qaProvider.qa

        studyItemIterator.saveState(state.studyItemIteratorState)
        rounds.saveState(state)

        state.qaKind = qa.kind
        state.qaIndex = qa.index
        state.showFuriganaWithQuestion = qa.showFuriganaWithQuestion
        state.writeTo(outState)
    }
}
