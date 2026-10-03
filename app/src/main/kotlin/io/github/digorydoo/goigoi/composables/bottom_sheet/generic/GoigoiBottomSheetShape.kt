package io.github.digorydoo.goigoi.composables.bottom_sheet.generic

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline.Generic
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import ch.digorydoo.kutils.math.clamp

class GoigoiBottomSheetShape(private val cornerSizePx: Int, private val topSpacePx: Int): Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
        Generic(
            Path().apply {
                addRoundRect(
                    RoundRect(
                        left = 0f,
                        top = clamp(topSpacePx.toFloat(), 0f, size.height),
                        right = size.width,
                        bottom = size.height,
                        topLeftCornerRadius = CornerRadius(cornerSizePx.toFloat()),
                        topRightCornerRadius = CornerRadius(cornerSizePx.toFloat())
                    )
                )
            }
        )
}
