package io.github.digorydoo.goigoi.activity.prog_study.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.core.prog_study.QAKind.*
import io.github.digorydoo.goigoi.core.study.Answer
import io.github.digorydoo.goigoi.providers.GoigoiTheme

@Composable
fun InstructionHint(model: ProgStudyActivityModel, modifier: Modifier = Modifier) {
    val mode = model.presentationMode.collectAsState().value
    val qaKind = model.qaKind.collectAsState().value
    val acceptableAnswers = model.acceptableAnswers.collectAsState().value
    val answerCorrectness = model.answerCorrectness.collectAsState().value

    val shownInstructionResId = remember { mutableIntStateOf(-1) }

    SideEffect(qaKind, mode, acceptableAnswers, answerCorrectness) {
        val questionInstructionResId = when (qaKind) {
            SHOW_KANJI_ASK_KANA -> R.string.hint_when_show_kanji_ask_kana

            SHOW_KANA_ASK_KANJI -> when (acceptableAnswers.firstOrNull()?.length == 1) {
                true -> R.string.hint_when_show_kana_ask_kanji_singular
                false -> R.string.hint_when_show_kana_ask_kanji_plural
            }

            SHOW_ROMAJI_ASK_KANA -> R.string.hint_when_show_romaji_ask_kana
            SHOW_TRANSLATION_ASK_KANA -> R.string.hint_when_show_translation_ask_kana

            SHOW_TRANSLATION_ASK_KANJI_AMONG_SIMILAR,
            -> R.string.hint_when_show_translation_ask_kanji_among_similar

            SHOW_TRANSLATION_ASK_KANJI_AMONG_WORDS,
            -> R.string.hint_when_show_translation_ask_kanji_among_words

            SHOW_WORD_ASK_NOTHING -> R.string.hint_when_new_word

            SHOW_PHRASE_ASK_NOTHING,
            SHOW_SENTENCE_ASK_NOTHING,
            -> R.string.hint_when_asking_nothing

            SHOW_PHRASE_ASK_WORD_KANJI,
            SHOW_SENTENCE_ASK_WORD_KANJI,
            -> R.string.hint_when_show_s_or_ph_ask_kanji

            SHOW_PHRASE_TRANSLATION_ASK_PHRASE_KANA,
            -> R.string.hint_when_show_ph_translation_ask_ph_kana

            SHOW_PHRASE_ASK_WORD_KANA,
            SHOW_SENTENCE_ASK_WORD_KANA,
            -> R.string.hint_when_show_s_or_ph_ask_kana
        }

        val newInstructionResId = when (mode) {
            PresentationMode.BEFORE_QUESTION,
            PresentationMode.QUESTION,
            -> questionInstructionResId

            PresentationMode.ANSWER_CHECK -> when (answerCorrectness) {
                Answer.CORRECT -> R.string.answer_correct
                Answer.WRONG -> R.string.answer_wrong
                Answer.CORRECT_EXCEPT_KANA_SIZE -> R.string.answer_correct_except_kana_size
                Answer.TRIVIAL, Answer.NONE, Answer.SKIP -> null
            }

            else -> null
        }

        newInstructionResId?.let { shownInstructionResId.intValue = it }
    }

    val text = shownInstructionResId.intValue
        .takeIf { it >= 0 }
        ?.let { stringResource(it) }

    AnimatedElement(modifier = modifier, visible = mode != PresentationMode.NOTHING) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = (text ?: "") + "\n\n",
            style = GoigoiTheme.typography.hint,
            color = GoigoiTheme.colours.secondaryOnBackground,
            // textAlign = TextAlign.Center,

            // By adding two newlines and then limiting to two lines we make sure the height will always exactly match
            // the height of two lines.
            maxLines = 2,
        )
    }
}
