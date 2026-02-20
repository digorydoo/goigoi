package io.github.digorydoo.goigoi.furigana

import android.text.SpannableString
import android.text.Spanned
import ch.digorydoo.kutils.cjk.FuriganaIterator
import ch.digorydoo.kutils.cjk.FuriganaString

object FuriganaBuilder {
    fun buildSpan(text: String) = buildSpan(text, FuriganaSpan.Options())

    fun buildSpan(text: String, canSeeFurigana: Boolean): SpannableString {
        val options = FuriganaSpan.Options(canSeeFurigana = canSeeFurigana)
        return buildSpan(text, options)
    }

    fun buildSpan(text: String, options: FuriganaSpan.Options): SpannableString {
        val span = SpannableString(text)
        val it = FuriganaIterator(text)

        while (it.hasNext()) {
            val r = it.next()
            span.setSpan(
                FuriganaSpan(r.primaryText, r.secondaryText, options),
                r.range.first,
                r.range.last + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return span
    }
}

fun FuriganaString.buildSpan() =
    FuriganaBuilder.buildSpan(raw)

fun FuriganaString.buildSpan(options: FuriganaSpan.Options) =
    FuriganaBuilder.buildSpan(raw, options)
