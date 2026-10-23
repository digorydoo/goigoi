package io.github.digorydoo.goigoi.legacy

import android.text.SpannableString
import android.text.Spanned
import ch.digorydoo.kutils.cjk.FuriganaIterator
import ch.digorydoo.kutils.cjk.FuriganaString

fun String.buildSpan(options: FuriganaSpan.Options = FuriganaSpan.Options()): SpannableString {
    val span = SpannableString(this)
    val it = FuriganaIterator(this)

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

fun String.buildSpan(canSeeFurigana: Boolean) = buildSpan(FuriganaSpan.Options(canSeeFurigana = canSeeFurigana))
fun FuriganaString.buildSpan() = raw.buildSpan()
fun FuriganaString.buildSpan(options: FuriganaSpan.Options) = raw.buildSpan(options)
