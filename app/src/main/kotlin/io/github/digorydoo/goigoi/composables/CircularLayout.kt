package io.github.digorydoo.goigoi.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

enum class CircularAlignment { INSIDE, CENTRE, OUTSIDE }

@Composable
fun CircularLayout(
    modifier: Modifier = Modifier,
    startAngle: Double = 0.0,
    spacing: Double = PI / 90.0,
    align: CircularAlignment = CircularAlignment.CENTRE,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        val maxWidth = constraints.maxWidth
        val maxHeight = constraints.maxHeight

        // At least one dimension must be bounded
        require(maxWidth < Constraints.Infinity || maxHeight < Constraints.Infinity)
        val layoutSize = minOf(maxWidth, maxHeight)

        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0, maxWidth = maxWidth, maxHeight = maxHeight)
        val placeables = measurables.map { it.measure(childConstraints) }
        val childRadii = placeables.map { maxOf(it.width, it.height) / 2f }
        val maxChildRadius = childRadii.maxOf { it }
        val centreRadius = (layoutSize / 2 - maxChildRadius).coerceAtLeast(0f)
        val centreCircum = 2 * PI * centreRadius
        val spacingArc = spacing * centreRadius

        layout(layoutSize, layoutSize) {
            var phi = -startAngle // CCW is natural if Y pointed upwards

            placeables.forEach { placeable ->
                val childRadius = maxOf(placeable.width, placeable.height) / 2

                val placeRadius = when (align) {
                    CircularAlignment.INSIDE -> centreRadius - maxChildRadius + childRadius
                    CircularAlignment.CENTRE -> centreRadius
                    CircularAlignment.OUTSIDE -> centreRadius + maxChildRadius - childRadius
                }

                val cx = (layoutSize / 2 + placeRadius * cos(phi)).roundToInt()
                val cy = (layoutSize / 2 + placeRadius * sin(phi)).roundToInt()

                placeable.placeRelative(x = cx - childRadius, y = cy - childRadius)

                phi -= 2 * PI * (2 * childRadius + spacingArc) / centreCircum
            }
        }
    }
}
