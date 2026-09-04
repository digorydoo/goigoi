package io.github.digorydoo.goigoi.activity.prog_study

import android.app.Activity
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.activity.prog_study.keyboard.Keyboard.ChipSize
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.ScreenSize

class Values(a: Activity) {
    class ChipValues(val fontSize: Float, val minWidth: Float, val minHeight: Float, val lrPadding: Float)

    class ScreenDependentValues(
        val xxLargeChip: ChipValues,
        val xLargeChip: ChipValues,
        val largeChip: ChipValues,
        val normalChip: ChipValues,
        val smallKanaFontSize: Float,
        val iconSize: Float,
        val iconXShift: Float,
        val chipSpacing: Float,
        val minTop: Float,
    ) {
        fun getChipValues(size: ChipSize) = when (size) {
            ChipSize.XXLARGE -> xxLargeChip
            ChipSize.XLARGE -> xLargeChip
            ChipSize.LARGE -> largeChip
            ChipSize.NORMAL -> normalChip
        }
    }

    private val largeScreen = ScreenDependentValues(
        xxLargeChip = ChipValues(
            fontSize = DimUtils.dpToPx(64.0f, a),
            minWidth = DimUtils.dpToPx(102.0f, a),
            minHeight = DimUtils.dpToPx(102.0f, a),
            lrPadding = DimUtils.dpToPx(32.0f, a),
        ),
        xLargeChip = ChipValues(
            fontSize = DimUtils.dpToPx(48.0f, a),
            minWidth = DimUtils.dpToPx(64.0f, a),
            minHeight = DimUtils.dpToPx(80.0f, a),
            lrPadding = DimUtils.dpToPx(24.0f, a),
        ),
        largeChip = ChipValues(
            fontSize = DimUtils.dpToPx(36.0f, a),
            minWidth = DimUtils.dpToPx(64.0f, a),
            minHeight = DimUtils.dpToPx(72.0f, a),
            lrPadding = DimUtils.dpToPx(20.0f, a),
        ),
        normalChip = ChipValues(
            fontSize = DimUtils.dpToPx(24.0f, a),
            minWidth = DimUtils.dpToPx(64.0f, a),
            minHeight = DimUtils.dpToPx(56.0f, a),
            lrPadding = DimUtils.dpToPx(16.0f, a),
        ),
        smallKanaFontSize = DimUtils.dpToPx(21.0f, a),
        iconSize = DimUtils.dpToPx(28.0f, a),
        iconXShift = DimUtils.dpToPx(20.0f, a),
        chipSpacing = DimUtils.dpToPx(8.0f, a),
        minTop = DimUtils.dpToPx(56.0f, a),
    )

    private val smallScreenPortrait = ScreenDependentValues(
        xxLargeChip = ChipValues(
            fontSize = DimUtils.dpToPx(38.0f, a),
            minWidth = DimUtils.dpToPx(64.0f, a),
            minHeight = DimUtils.dpToPx(48.0f, a),
            lrPadding = DimUtils.dpToPx(20.0f, a),
        ),
        xLargeChip = ChipValues(
            fontSize = DimUtils.dpToPx(32.0f, a),
            minWidth = DimUtils.dpToPx(56.0f, a),
            minHeight = DimUtils.dpToPx(40.0f, a),
            lrPadding = DimUtils.dpToPx(16.0f, a),
        ),
        largeChip = ChipValues(
            fontSize = DimUtils.dpToPx(26.0f, a),
            minWidth = DimUtils.dpToPx(56.0f, a),
            minHeight = DimUtils.dpToPx(38.0f, a),
            lrPadding = DimUtils.dpToPx(12.0f, a),
        ),
        normalChip = ChipValues(
            fontSize = DimUtils.dpToPx(20.0f, a),
            minWidth = DimUtils.dpToPx(56.0f, a),
            minHeight = DimUtils.dpToPx(36.0f, a),
            lrPadding = DimUtils.dpToPx(8.0f, a),
        ),
        smallKanaFontSize = DimUtils.dpToPx(18.0f, a),
        iconSize = DimUtils.dpToPx(24.0f, a),
        iconXShift = DimUtils.dpToPx(14.0f, a),
        chipSpacing = DimUtils.dpToPx(2.0f, a),
        minTop = DimUtils.dpToPx(56.0f, a),
    )

    private val smallScreenLandscape = ScreenDependentValues(
        xxLargeChip = smallScreenPortrait.xxLargeChip,
        xLargeChip = smallScreenPortrait.xLargeChip,
        largeChip = smallScreenPortrait.largeChip,
        normalChip = smallScreenPortrait.normalChip,
        smallKanaFontSize = smallScreenPortrait.smallKanaFontSize,
        iconSize = smallScreenPortrait.iconSize,
        iconXShift = smallScreenPortrait.iconXShift,
        chipSpacing = smallScreenPortrait.chipSpacing,
        minTop = DimUtils.dpToPx(16.0f, a),
    )

    private val normalScreenPortrait = ScreenDependentValues(
        xxLargeChip = ChipValues(
            fontSize = DimUtils.dpToPx(64.0f, a),
            minWidth = DimUtils.dpToPx(102.0f, a),
            minHeight = DimUtils.dpToPx(102.0f, a),
            lrPadding = DimUtils.dpToPx(32.0f, a),
        ),
        xLargeChip = ChipValues(
            fontSize = DimUtils.dpToPx(42.0f, a),
            minWidth = DimUtils.dpToPx(76.0f, a),
            minHeight = DimUtils.dpToPx(76.0f, a),
            lrPadding = DimUtils.dpToPx(24.0f, a),
        ),
        largeChip = ChipValues(
            fontSize = DimUtils.dpToPx(36.0f, a),
            minWidth = DimUtils.dpToPx(64.0f, a),
            minHeight = DimUtils.dpToPx(64.0f, a),
            lrPadding = DimUtils.dpToPx(20.0f, a),
        ),
        normalChip = ChipValues(
            fontSize = DimUtils.dpToPx(24.0f, a),
            minWidth = DimUtils.dpToPx(56.0f, a),
            minHeight = DimUtils.dpToPx(56.0f, a),
            lrPadding = DimUtils.dpToPx(16.0f, a),
        ),
        smallKanaFontSize = largeScreen.smallKanaFontSize,
        iconSize = largeScreen.iconSize,
        iconXShift = smallScreenPortrait.iconXShift,
        chipSpacing = DimUtils.dpToPx(4.0f, a),
        minTop = largeScreen.minTop,
    )

    private val normalScreenLandscape = ScreenDependentValues(
        xxLargeChip = smallScreenPortrait.xxLargeChip,
        xLargeChip = smallScreenPortrait.xLargeChip,
        largeChip = smallScreenPortrait.largeChip,
        normalChip = smallScreenPortrait.normalChip,
        smallKanaFontSize = smallScreenPortrait.smallKanaFontSize,
        iconSize = smallScreenPortrait.iconSize,
        iconXShift = DimUtils.dpToPx(16.0f, a),
        chipSpacing = normalScreenPortrait.chipSpacing,
        minTop = smallScreenLandscape.minTop,
    )

    val elementSpacing = DimUtils.fromAttr(R.attr.progStudyElementSpacing, a)
    val minChipSwipeDelta = DimUtils.dpToPx(16.0f, a)
    val primaryColour = ResUtils.getARGBFromAttr(R.attr.colorPrimary, a.applicationContext)
    val keyLensVOffset = DimUtils.dpToPx(8, a)

    val bottomMargin = DimUtils.dpToPx(24.0f, a)
    val tategakiMaxHeight = DimUtils.dpToPx(340.0f, a)

    // Note that these sizes must not be too large, because we may need additional vertical space when
    // hasCombinedReading furigana elements cause early breaks!
    val len1QuestionTextSize = DimUtils.dpToPx(84.0f, a)
    val len2QuestionTextSize = DimUtils.dpToPx(63.0f, a)
    val len3QuestionTextSize = DimUtils.dpToPx(52.0f, a)
    val len4QuestionTextSize = DimUtils.dpToPx(40.0f, a)
    val len5QuestionTextSize = DimUtils.dpToPx(32.0f, a)
    val len6QuestionTextSize = DimUtils.dpToPx(27.0f, a)
    val len7QuestionTextSize = DimUtils.dpToPx(40.0f, a) // 3+4 on both my Samsung and Nexus 4.95inch
    val len8QuestionTextSize = DimUtils.dpToPx(40.0f, a) // 4+4
    val len9QuestionTextSize = DimUtils.dpToPx(32.0f, a) // 4+5
    val len10QuestionTextSize = DimUtils.dpToPx(32.0f, a) // 5+5
    val len11QuestionTextSize = DimUtils.dpToPx(27.0f, a) // 5+6
    val len12QuestionTextSize = DimUtils.dpToPx(27.0f, a) // 6+6
    val len13QuestionTextSize = DimUtils.dpToPx(32.0f, a) // 3+5+5
    val len14QuestionTextSize = DimUtils.dpToPx(32.0f, a) // 4+5+5
    val len15QuestionTextSize = DimUtils.dpToPx(27.0f, a) // 3+6+6
    val len16QuestionTextSize = DimUtils.dpToPx(27.0f, a) // 4+6+6
    val len17QuestionTextSize = DimUtils.dpToPx(27.0f, a) // 5+6+6
    val len18QuestionTextSize = DimUtils.dpToPx(27.0f, a) // 6+6+6
    val len19QuestionTextSize = DimUtils.dpToPx(23.0f, a) // 5+7+7
    val len20QuestionTextSize = DimUtils.dpToPx(23.0f, a) // 6+7+7
    val len21QuestionTextSize = DimUtils.dpToPx(23.0f, a) // 7+7+7
    val minQuestionTextSize = DimUtils.dpToPx(22.0f, a)

    val largeHintTextSize = DimUtils.dpToPx(28.0f, a)
    val mediumHintTextSize = DimUtils.dpToPx(22.0f, a)
    val smallHintTextSize = DimUtils.dpToPx(16.0f, a)
    val minHintTextSize = DimUtils.dpToPx(14.0f, a)

    fun getScreenDependent(screenSize: ScreenSize, orientation: Orientation) =
        when (screenSize) {
            ScreenSize.LARGE -> largeScreen
            ScreenSize.NORMAL -> when (orientation) {
                Orientation.PORTRAIT -> normalScreenPortrait
                else -> normalScreenLandscape
            }
            ScreenSize.SMALL -> when (orientation) {
                Orientation.PORTRAIT -> smallScreenPortrait
                else -> smallScreenLandscape
            }
        }
}
