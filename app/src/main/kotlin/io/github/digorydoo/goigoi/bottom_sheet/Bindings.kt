package io.github.digorydoo.goigoi.bottom_sheet

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import io.github.digorydoo.goigoi.R

class Bindings(root: View) {
    val content = root.findViewById<LinearLayout>(R.id.content)!!
    val primaryForm = root.findViewById<TextView>(R.id.primary_form)!!
    val secondaryForm = root.findViewById<TextView>(R.id.secondary_form)!!
    val sometimesWrittenAs = root.findViewById<TextView>(R.id.sometimes_written_as)!!
    val kanjiMoreDifficult = root.findViewById<TextView>(R.id.kanji_more_difficult)!!
    val wordCategories = root.findViewById<TextView>(R.id.word_categories)!!
    val translation = root.findViewById<TextView>(R.id.translation)!!
    val hint = root.findViewById<TextView>(R.id.hint)!!
    val jlptLevel = root.findViewById<TextView>(R.id.jlpt_level)!!
    val wordStats = root.findViewById<TextView>(R.id.word_stats)!!
}
