package io.github.digorydoo.goigoi.activity.prog_study

import android.app.Activity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.view.FloatingActionBtn
import io.github.digorydoo.goigoi.view.TextWithCaret
import io.github.digorydoo.goigoi.view.tategaki.TategakiView

class Bindings(a: Activity) {
    val rootView = a.findViewById<ViewGroup>(R.id.root_view)!!
    val toolbar = a.findViewById<Toolbar>(R.id.my_toolbar)!!
    val nextBtn = a.findViewById<FloatingActionBtn>(R.id.next_btn)!!
    val whatToDoTextView = a.findViewById<TextView>(R.id.what_to_do)!!
    val questionInDefaultFontTextView = a.findViewById<TextView>(R.id.question_in_default_font)!!
    val questionInHiraganaFontTextView = a.findViewById<TextView>(R.id.question_in_hiragana_font)!!
    val questionInKatakanaFontTextView = a.findViewById<TextView>(R.id.question_in_katakana_font)!!
    val questionInPencilFontTextView = a.findViewById<TextView>(R.id.question_in_pencil_font)!!
    val questionInCalligraphyFontTategakiView = a.findViewById<TategakiView>(R.id.question_in_calligraphy_font)!!
    val questionHintTextView = a.findViewById<TextView>(R.id.question_hint_text)!!
    val inputTextView = a.findViewById<TextWithCaret>(R.id.input_text)!!
    val correctedTextView = a.findViewById<TextView>(R.id.corrected_text)!!
    val revealedKanjiOrKanaTextView = a.findViewById<TextView>(R.id.revealed_kanji_or_kana)!!
    val revealedTranslationTextView = a.findViewById<TextView>(R.id.revealed_translation)!!
    val revealedHintTextView = a.findViewById<TextView>(R.id.revealed_hint)!!
    val explanationRow = a.findViewById<ViewGroup>(R.id.explanation_row)!!
    val explanationTextView = a.findViewById<TextView>(R.id.explanation_text)!!
    val keyboardView = a.findViewById<ViewGroup>(R.id.keyboard)!!
    val infoBtn = a.findViewById<ImageView>(R.id.info_btn)!!
    val stateIconView = a.findViewById<ImageView>(R.id.state_icon)!!
    val keyLensImageView = a.findViewById<ImageView>(R.id.key_lens)!!
}
