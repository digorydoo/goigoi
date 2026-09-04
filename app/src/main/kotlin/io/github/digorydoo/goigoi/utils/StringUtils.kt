package io.github.digorydoo.goigoi.utils

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import ch.digorydoo.kutils.cjk.IntlString
import ch.digorydoo.kutils.colour.Colour
import io.github.digorydoo.goigoi.BuildConfig

@Suppress("KotlinConstantConditions")
val IntlString.withStudyLang
    get() = when (BuildConfig.FLAVOR) {
        "japanese_free" -> ja
        "french_free" -> fr
        else -> throw RuntimeException("Unhandled build flavor: ${BuildConfig.FLAVOR}")
    }

fun SpannableStringBuilder.appendStyled(
    style: Int,
    flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
    buildText: SpannableStringBuilder.() -> CharSequence,
): SpannableStringBuilder {
    val start = length
    val text = SpannableStringBuilder().buildText()
    append(text)
    setSpan(StyleSpan(style), start, length, flags)
    return this
}

fun SpannableStringBuilder.appendColoured(
    fg: Colour,
    flags: Int = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
    buildText: SpannableStringBuilder.() -> CharSequence,
): SpannableStringBuilder {
    val start = length
    val text = SpannableStringBuilder().buildText()
    append(text)
    setSpan(ForegroundColorSpan(fg.toARGB()), start, length, flags)
    return this
}
