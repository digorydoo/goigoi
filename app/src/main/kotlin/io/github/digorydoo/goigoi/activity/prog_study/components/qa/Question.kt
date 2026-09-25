package io.github.digorydoo.goigoi.activity.prog_study.components.qa

import android.graphics.Typeface
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import ch.digorydoo.kutils.cjk.hasCJKIgnoringKana
import ch.digorydoo.kutils.cjk.hasCJKOrKana
import ch.digorydoo.kutils.cjk.hasKana
import ch.digorydoo.kutils.cjk.isHiragana
import ch.digorydoo.kutils.cjk.isKatakana
import ch.digorydoo.kutils.math.clamp
import ch.digorydoo.kutils.math.lerp
import ch.digorydoo.kutils.utils.OneOf
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel
import io.github.digorydoo.goigoi.activity.prog_study.ProgStudyActivityModel.PresentationMode
import io.github.digorydoo.goigoi.activity.prog_study.components.AnimatedElement
import io.github.digorydoo.goigoi.legacy.spannable.DEFAULT_FURIGANA_OPACITY
import io.github.digorydoo.goigoi.legacy.spannable.FuriganaSpan
import io.github.digorydoo.goigoi.legacy.spannable.buildSpan
import io.github.digorydoo.goigoi.legacy.view.TategakiView
import io.github.digorydoo.goigoi.providers.DeviceProps
import io.github.digorydoo.goigoi.providers.GoigoiTheme
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ScreenSize

private const val MAX_LENGTH_FOR_CALLIGRAPHY_LARGE = 30
private const val MAX_LENGTH_FOR_CALLIGRAPHY_MEDIUM = 21
private const val MAX_LENGTH_FOR_CALLIGRAPHY_SMALL = 8

private const val DEFAULT_FONT_FURIGANA_REL_V_OFFSET = 0.3f
private const val PENCIL_FONT_FURIGANA_REL_V_OFFSET = -0.1f

private enum class FontType { DEFAULT, BOLD_HIRAGANA, BOLD_KATAKANA, PENCIL, CALLIGRAPHY }

private fun determineFontType(
    text: String,
    doesNotAskAnything: Boolean,
    screenSize: ScreenSize,
    orientation: Orientation,
): FontType {
    if (text.isHiragana()) {
        return when (doesNotAskAnything) {
            true -> FontType.BOLD_HIRAGANA
            false -> FontType.PENCIL
        }
    }

    if (text.isKatakana()) {
        return when (doesNotAskAnything) {
            true -> FontType.BOLD_KATAKANA
            false -> FontType.PENCIL
        }
    }

    if (!doesNotAskAnything) {
        return FontType.DEFAULT // question is neither trivial nor kana-only
    }

    if (!text.hasCJKOrKana()) {
        return FontType.DEFAULT // question is in English or other system language
    }

    if (text.contains('〜')) {
        // Our calligraphy font doesn't have this glyph, unfortunately. Android would display the glyph in the
        // system font, which looks OK, but let's just use our pencil font, which has the glyph.
        return FontType.PENCIL
    }

    val maxLenForCalligraphy = when (screenSize) {
        ScreenSize.SMALL -> MAX_LENGTH_FOR_CALLIGRAPHY_SMALL
        ScreenSize.NORMAL -> when (orientation) {
            Orientation.PORTRAIT -> MAX_LENGTH_FOR_CALLIGRAPHY_MEDIUM
            else -> MAX_LENGTH_FOR_CALLIGRAPHY_SMALL
        }
        ScreenSize.LARGE -> MAX_LENGTH_FOR_CALLIGRAPHY_LARGE
    }

    return when {
        text.length <= maxLenForCalligraphy -> FontType.CALLIGRAPHY
        else -> FontType.PENCIL
    }
}

private fun determineFontSizePx(
    text: String,
    fontType: FontType,
    density: Density,
    screenSize: ScreenSize,
    orientation: Orientation,
): Float {
    val len = text.length

    // Note that these sizes must not be too large, because we may need additional vertical space when
    // hasCombinedReading furigana elements cause early breaks!
    val len1FontSize = with(density) { 84.dp.toPx() }
    val len2FontSize = with(density) { 63.dp.toPx() }
    val len3FontSize = with(density) { 52.dp.toPx() }
    val len4FontSize = with(density) { 40.dp.toPx() }
    val len5FontSize = with(density) { 32.dp.toPx() }
    val len6FontSize = with(density) { 27.dp.toPx() }
    val len7FontSize = with(density) { 40.dp.toPx() } // 3+4 on both my Samsung and Nexus 4.95inch
    val len8FontSize = with(density) { 40.dp.toPx() } // 4+4
    val len9FontSize = with(density) { 32.dp.toPx() } // 4+5
    val len10FontSize = with(density) { 32.dp.toPx() } // 5+5
    val len11FontSize = with(density) { 27.dp.toPx() } // 5+6
    val len12FontSize = with(density) { 27.dp.toPx() } // 6+6
    val len13FontSize = with(density) { 32.dp.toPx() } // 3+5+5
    val len14FontSize = with(density) { 32.dp.toPx() } // 4+5+5
    val len15FontSize = with(density) { 27.dp.toPx() } // 3+6+6
    val len16FontSize = with(density) { 27.dp.toPx() } // 4+6+6
    val len17FontSize = with(density) { 27.dp.toPx() } // 5+6+6
    val len18FontSize = with(density) { 27.dp.toPx() } // 6+6+6
    val len19FontSize = with(density) { 23.dp.toPx() } // 5+7+7
    val len20FontSize = with(density) { 23.dp.toPx() } // 6+7+7
    val len21FontSize = with(density) { 23.dp.toPx() } // 7+7+7
    val minFontSize = with(density) { 22.dp.toPx() }

    var size = when (len) {
        0, 1 -> len1FontSize
        2 -> len2FontSize
        3 -> len3FontSize
        4 -> len4FontSize
        5 -> len5FontSize
        6 -> len6FontSize
        7 -> len7FontSize
        8 -> len8FontSize
        9 -> len9FontSize
        10 -> len10FontSize
        11 -> len11FontSize
        12 -> len12FontSize
        13 -> len13FontSize
        14 -> len14FontSize
        15 -> len15FontSize
        16 -> len16FontSize
        17 -> len17FontSize
        18 -> len18FontSize
        19 -> len19FontSize
        20 -> len20FontSize
        else -> {
            // len >= 21
            lerp(len21FontSize, minFontSize, clamp((len - 21) / 25.0f))
        }
    }

    size *= when (fontType) {
        FontType.CALLIGRAPHY -> 1.5f
        FontType.BOLD_KATAKANA -> 1.1f
        FontType.BOLD_HIRAGANA -> 1.0f
        FontType.PENCIL -> 1.1f
        FontType.DEFAULT -> 1.0f
    }

    val hasCJKIgnoringKana = text.hasCJKIgnoringKana()
    val hasKana = text.hasKana()

    val maxSize = when (fontType) {
        FontType.CALLIGRAPHY -> Float.POSITIVE_INFINITY
        FontType.BOLD_KATAKANA -> len3FontSize
        FontType.BOLD_HIRAGANA -> len4FontSize
        FontType.PENCIL -> len4FontSize
        FontType.DEFAULT -> when {
            hasCJKIgnoringKana -> len3FontSize
            hasKana -> len4FontSize
            else -> len6FontSize
        }
    }

    val minSize = when (fontType) {
        FontType.PENCIL -> len18FontSize
        FontType.DEFAULT -> when {
            hasCJKIgnoringKana -> len16FontSize
            hasKana -> len18FontSize
            else -> minFontSize
        }
        else -> minFontSize
    }

    size = clamp(size, minSize, maxSize)

    size *= when (screenSize) {
        ScreenSize.SMALL -> 0.8f
        ScreenSize.NORMAL -> when (orientation) {
            Orientation.PORTRAIT -> 1.0f
            else -> 0.8f
        }
        ScreenSize.LARGE -> 1.1f
    }

    return size
}

@Composable
fun Question(model: ProgStudyActivityModel, outerContainerMaxIdealHeight: Dp) {
    val colours = GoigoiTheme.colours
    val pmode = model.presentationMode.collectAsState().value
    val qaKind = model.qaKind.collectAsState().value
    val question = model.question.collectAsState().value
    val showFurigana = model.showFurigana.collectAsState().value
    val questionAfterReveal = model.questionAfterReveal.collectAsState().value
    val screenSize = DeviceProps.size
    val orientation = DeviceProps.orientation
    val context = LocalContext.current
    val density = LocalDensity.current
    val maxFuriganaSizePx = with(density) { 16.dp.toPx() }

    val questionAfterRevealWithoutFurigana = when (questionAfterReveal) {
        is OneOf.First -> questionAfterReveal.first
        is OneOf.Second -> questionAfterReveal.second.kanji
    }

    val fontType = remember(questionAfterRevealWithoutFurigana, qaKind.doesNotAskAnything, screenSize, orientation) {
        determineFontType(questionAfterRevealWithoutFurigana, qaKind.doesNotAskAnything, screenSize, orientation)
    }

    // For compose, we'd use FontFamily(Font(R.font.xy)), but since we're using AndroidView, we do it like this.
    val typeface = remember(fontType) {
        when (fontType) {
            FontType.DEFAULT -> Typeface.DEFAULT
            FontType.BOLD_HIRAGANA -> ResourcesCompat.getFont(context, R.font.mochiy_pop_one_hiragana_stripped)
            FontType.BOLD_KATAKANA -> ResourcesCompat.getFont(context, R.font.dela_gothic_one_katakana_stripped)
            FontType.PENCIL -> ResourcesCompat.getFont(context, R.font.zen_kurenaido)
            FontType.CALLIGRAPHY -> ResourcesCompat.getFont(context, R.font.epson_tai_xing_shu_tib_2)
        }
    }

    val fontSizePx = remember(questionAfterRevealWithoutFurigana, fontType, density, screenSize, orientation) {
        determineFontSizePx(questionAfterRevealWithoutFurigana, fontType, density, screenSize, orientation)
    }

    val furiganaRelVOffset = when (fontType) {
        FontType.CALLIGRAPHY -> 0.0f // furigana handled by TategakiView itself, offset is ignored
        FontType.PENCIL -> PENCIL_FONT_FURIGANA_REL_V_OFFSET
        FontType.BOLD_HIRAGANA -> 0.0f // hiragana-only word not expected to have furigana
        FontType.BOLD_KATAKANA -> 0.0f // katakana-only word not expected to have furigana
        FontType.DEFAULT -> DEFAULT_FONT_FURIGANA_REL_V_OFFSET
    }

    val questionText = remember(question, furiganaRelVOffset, showFurigana, pmode) {
        when (question) {
            is OneOf.First -> question.first
            is OneOf.Second -> question.second.buildSpan(
                if (fontType == FontType.CALLIGRAPHY) {
                    FuriganaSpan.Options(
                        relVOffset = furiganaRelVOffset,
                        fontSizeMax = maxFuriganaSizePx,
                        canSeeFurigana = showFurigana,
                        // opacity is currently ignored by TategakiView
                    )
                } else {
                    FuriganaSpan.Options(
                        relVOffset = furiganaRelVOffset,
                        fontSizeMax = maxFuriganaSizePx,
                        // We conceal furigana with opacity rather than using canSeeFurigana in order to save the space
                        // for when it will be revealed later. Note that this may also widen the space occupied by
                        // kanjis whose furigana is long, which can look odd sometimes. FIXME keep?
                        canSeeFurigana = true,
                        opacity = if (showFurigana) DEFAULT_FURIGANA_OPACITY else 0f
                    )
                }
            )
        }
    }

    // Enter animation could differ based on questionText (but only when solo, i.e. when answer is trivial):
    // - Hiragana only: swipe from left to right
    // - Katakana only: flash zoom, starting from double size
    // - Tategaki: Roll from the top
    // - Sentence: character by character
    AnimatedElement(visible = pmode != PresentationMode.NOTHING && pmode != PresentationMode.BEFORE_QUESTION) {
        when (fontType) {
            FontType.CALLIGRAPHY -> {
                // The outerContainerMaxIdealHeight is the maximum content height before the ScrollContainer starts
                // scrolling. Limit the height further to avoid very high tategaki text on large screens.
                val tategakiMaxHeight = minOf(outerContainerMaxIdealHeight, 340.dp)

                // println("max=" + with(density) { 340.dp.toPx() }) // 1020px, which is also what was used previously

                AndroidView(
                    factory = { ctx -> TategakiView(ctx, typeface, colours.onBackground.toArgb()) },
                    update = { view ->
                        view.apply {
                            textSizePx = fontSizePx
                            viewMaxHeightPx = with(density) { tategakiMaxHeight.roundToPx() }
                            // Not ideal: We pass furigana down as SpannableString, and TategakiView has to parse it
                            // again. We should pass the FuriganaString itself.
                            setText(questionText)
                        }
                    }
                )
            }
            else -> {
                // The text may be a SpannableString, so we use AndroidView.
                AndroidView(
                    factory = { ctx -> TextView(ctx) },
                    update = { view ->
                        view.apply {
                            text = questionText
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSizePx)
                            setTextColor(colours.onBackground.toArgb())
                            setTypeface(typeface)
                        }
                    }
                )
            }
        }
    }
}
