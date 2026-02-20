package io.github.digorydoo.goigoi.activity.prog_study

import android.app.Activity
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.Orientation
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.ScreenSize

class Values(a: Activity) {
    val spacing = DimUtils.fromAttr(R.attr.progStudyElementSpacing, a)
    val minChipSwipeDelta = DimUtils.dpToPx(16.0f, a)
    val primaryColour = ResUtils.getARGBFromAttr(R.attr.colorPrimary, a.applicationContext)
    val keyLensVOffset = DimUtils.dpToPx(8, a)

    private val smallMinTop = DimUtils.dpToPx(16.0f, a)
    private val largeMinTop = DimUtils.dpToPx(56.0f, a)

    val tategakiMaxHeight = DimUtils.dpToPx(260.0f, a)

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

    private val smallChipFontSize = DimUtils.dpToPx(20.0f, a.applicationContext)
    private val largeChipFontSize = DimUtils.spToPx(24.0f, a.applicationContext)

    private val smallChipFontSizeForSmallKana = DimUtils.dpToPx(18.0f, a.applicationContext)
    private val largeChipFontSizeForSmallKana = DimUtils.spToPx(21.0f, a.applicationContext)

    private val tinyChipSpacing = DimUtils.dpToPx(2.0f, a.applicationContext)
    private val smallChipSpacing = DimUtils.dpToPx(4.0f, a.applicationContext)
    private val largeChipSpacing = DimUtils.dpToPx(8.0f, a.applicationContext)

    private val smallChipIconSize = DimUtils.dpToPx(24.0f, a)
    private val largeChipIconSize = DimUtils.dpToPx(28.0f, a)

    private val smallChipIconXShift = DimUtils.dpToPx(14.0f, a)
    private val largeChipIconXShift = DimUtils.dpToPx(20.0f, a)

    private val smallChipMinWidth = DimUtils.dpToPx(56.0f, a)
    private val largeChipMinWidth = DimUtils.dpToPx(64.0f, a)

    private val smallChipMinHeight = DimUtils.dpToPx(36.0f, a)
    private val largeChipMinHeight = DimUtils.dpToPx(56.0f, a)

    fun getMinTop(screenSize: ScreenSize, orientation: Orientation) = when (screenSize) {
        ScreenSize.LARGE -> largeMinTop
        ScreenSize.NORMAL -> largeMinTop
        ScreenSize.SMALL -> when (orientation) {
            Orientation.PORTRAIT -> largeMinTop
            Orientation.LANDSCAPE_LEFT -> smallMinTop
            Orientation.LANDSCAPE_RIGHT -> smallMinTop
            Orientation.UNKNOWN -> smallMinTop
        }
    }

    fun getChipFontSize(screenSize: ScreenSize, orientation: Orientation) = when (screenSize) {
        ScreenSize.LARGE -> largeChipFontSize
        ScreenSize.SMALL -> smallChipFontSize
        ScreenSize.NORMAL -> when (orientation) {
            Orientation.PORTRAIT -> largeChipFontSize
            Orientation.LANDSCAPE_LEFT -> smallChipFontSize
            Orientation.LANDSCAPE_RIGHT -> smallChipFontSize
            Orientation.UNKNOWN -> smallChipFontSize
        }
    }

    fun getChipFontSizeForSmallKana(screenSize: ScreenSize, orientation: Orientation) = when (screenSize) {
        ScreenSize.LARGE -> largeChipFontSizeForSmallKana
        ScreenSize.SMALL -> smallChipFontSizeForSmallKana
        ScreenSize.NORMAL -> when (orientation) {
            Orientation.PORTRAIT -> largeChipFontSizeForSmallKana
            Orientation.LANDSCAPE_LEFT -> smallChipFontSizeForSmallKana
            Orientation.LANDSCAPE_RIGHT -> smallChipFontSizeForSmallKana
            Orientation.UNKNOWN -> smallChipFontSizeForSmallKana
        }
    }

    fun getChipSpacing(screenSize: ScreenSize) = when (screenSize) {
        ScreenSize.LARGE -> largeChipSpacing
        ScreenSize.SMALL -> tinyChipSpacing
        ScreenSize.NORMAL -> smallChipSpacing
    }

    fun getChipIconSize(screenSize: ScreenSize, orientation: Orientation) = when (screenSize) {
        ScreenSize.LARGE -> largeChipIconSize
        ScreenSize.SMALL -> smallChipIconSize
        ScreenSize.NORMAL -> when (orientation) {
            Orientation.PORTRAIT -> largeChipIconSize
            Orientation.LANDSCAPE_LEFT -> smallChipIconSize
            Orientation.LANDSCAPE_RIGHT -> smallChipIconSize
            Orientation.UNKNOWN -> smallChipIconSize
        }
    }

    fun getChipIconXShift(screenSize: ScreenSize, orientation: Orientation) = when (screenSize) {
        ScreenSize.LARGE -> largeChipIconXShift
        ScreenSize.SMALL -> smallChipIconXShift
        ScreenSize.NORMAL -> when (orientation) {
            Orientation.PORTRAIT -> smallChipIconXShift
            Orientation.LANDSCAPE_LEFT -> largeChipIconXShift
            Orientation.LANDSCAPE_RIGHT -> largeChipIconXShift
            Orientation.UNKNOWN -> smallChipIconXShift
        }
    }

    fun getChipMinWidth(screenSize: ScreenSize, orientation: Orientation) = when (screenSize) {
        ScreenSize.LARGE -> largeChipMinWidth
        ScreenSize.SMALL -> smallChipMinWidth
        ScreenSize.NORMAL -> when (orientation) {
            Orientation.PORTRAIT -> smallChipMinWidth
            Orientation.LANDSCAPE_LEFT -> largeChipMinWidth
            Orientation.LANDSCAPE_RIGHT -> largeChipMinWidth
            Orientation.UNKNOWN -> smallChipMinWidth
        }
    }

    fun getChipMinHeight(screenSize: ScreenSize, orientation: Orientation) = when (screenSize) {
        ScreenSize.LARGE -> largeChipMinHeight
        ScreenSize.SMALL -> smallChipMinHeight
        ScreenSize.NORMAL -> when (orientation) {
            Orientation.PORTRAIT -> largeChipMinHeight
            Orientation.LANDSCAPE_LEFT -> smallChipMinHeight
            Orientation.LANDSCAPE_RIGHT -> smallChipMinHeight
            Orientation.UNKNOWN -> smallChipMinHeight
        }
    }
}
