package io.github.digorydoo.goigoi.drawable

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Paint.Cap
import android.graphics.Paint.Join
import android.graphics.Paint.Style
import android.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.filter.delay
import io.github.digorydoo.goigoi.core.welcome.DailyProgressTracker
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class BigRingDrawable(
    val value: Float, // 0..1
    private val colours: Colours,
    private val dimensions: Dimensions,
    private val centreText: String,
): AnimatedDrawable() {
    interface Colours {
        val trail: Color
        val track: Color
        val text: Color
        val checkmark: Color
        val headBg: Color
        val headFg: Color
    }

    interface Dimensions {
        val insetSizePx: Int
        val markInsetSizePx: Int
        val textSizePx: Float
    }

    private val trackPaint = Paint().apply {
        isAntiAlias = true
        color = colours.track.toArgb()
        style = Style.STROKE
    }

    private val trailPaint = Paint().apply {
        isAntiAlias = true
        style = Style.STROKE
        strokeCap = Cap.ROUND
        strokeJoin = Join.ROUND
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        style = Style.FILL // STROKE would draw the text as outline
        color = colours.text.toArgb()
        textAlign = Align.CENTER
        textSize = dimensions.textSizePx
    }

    private val markPaint = Paint().apply {
        isAntiAlias = true
        style = Style.STROKE
        color = colours.checkmark.toArgb()
        strokeCap = Cap.SQUARE
        strokeJoin = Join.MITER
    }

    private val headBgPaint = Paint().apply {
        isAntiAlias = true
        color = colours.headBg.toArgb()
        style = Style.FILL
    }

    private val headFgPaint = Paint().apply {
        isAntiAlias = true
        color = colours.headFg.toArgb()
        style = Style.STROKE
        strokeCap = Cap.SQUARE
        strokeJoin = Join.ROUND
    }

    override fun draw(canvas: Canvas) {
        val bounds = copyBounds()
        bounds.inset(dimensions.insetSizePx, dimensions.insetSizePx)

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val r = 0.5f * min(bounds.width(), bounds.height())
        val phi = max(0.0f, value * 360.0f) * animValue
        val shouldDrawCheckmark = value >= DailyProgressTracker.CHECKMARK_THRESHOLD
        var checkmarkAnimValue = 0.0f
        var ringHeadAnimValue = 0.0f

        if (shouldDrawCheckmark) {
            checkmarkAnimValue = delay(animValue, 0.8f)
            ringHeadAnimValue = delay(animValue, 0.96f)
        }

        drawRing(cx, cy, r, phi, ringHeadAnimValue, canvas)

        if (ringHeadAnimValue < 1.0f) {
            drawRingHead(1.0f - ringHeadAnimValue, cx, cy, r, phi, canvas)
        }

        if (!shouldDrawCheckmark && centreText.isNotEmpty()) {
            drawText(cx, cy, canvas)
        }

        if (checkmarkAnimValue > 0.0f) {
            bounds.inset(dimensions.markInsetSizePx, dimensions.markInsetSizePx)
            val path = Artist.makeCheckmarkPath(bounds, checkmarkAnimValue)
            markPaint.strokeWidth = 0.1f * r
            canvas.drawPath(path, markPaint)
        }
    }

    private fun drawRing(
        cx: Float,
        cy: Float,
        r: Float,
        phi: Float,
        ringHeadAnimValue: Float,
        canvas: Canvas,
    ) {
        val ringRadius = r - r * (HEAD_REL_RADIUS - RING_REL_STROKEWIDTH / 2.0f)
        val w = ringRadius * RING_REL_STROKEWIDTH
        val r2 = ringRadius - w / 2.0f

        trackPaint.strokeWidth = w * 0.8f
        canvas.drawCircle(cx, cy, r2, trackPaint)

        val left = cx - r2
        val top = cy - r2
        val right = cx + r2
        val bottom = cy + r2

        trailPaint.color = Colour.mixARGB(colours.trail.toArgb(), colours.checkmark.toArgb(), ringHeadAnimValue)
        trailPaint.strokeWidth = w
        canvas.drawArc(left, top, right, bottom, 270f, phi, false, trailPaint)
    }

    private fun drawRingHead(
        headAnimValue: Float,
        cx: Float,
        cy: Float,
        r: Float,
        phi: Float,
        canvas: Canvas,
    ) {
        var headRadius = HEAD_REL_RADIUS * r
        val psi = (phi - 90) * 2.0 * Math.PI / 360.0
        val headCx = (cx + (r - headRadius) * cos(psi)).toFloat()
        val headCy = (cy + (r - headRadius) * sin(psi)).toFloat()
        headRadius *= headAnimValue

        canvas.drawCircle(headCx, headCy, headRadius, headBgPaint)

        val chi = psi + Math.PI / 2.0
        val offset = Math.PI / 1.5f
        val arrowCx = headCx - (headRadius * 0.042 * cos(chi)).toFloat()
        val arrowCy = headCy - (headRadius * 0.042 * sin(chi)).toFloat()
        val innerRadius = headRadius * 0.42

        val path = Path().apply {
            moveTo(
                arrowCx + (innerRadius * cos(chi - offset)).toFloat(),
                arrowCy + (innerRadius * sin(chi - offset)).toFloat()
            )
            lineTo(
                arrowCx + (innerRadius * cos(chi)).toFloat(),
                arrowCy + (innerRadius * sin(chi)).toFloat()
            )
            lineTo(
                arrowCx + (innerRadius * cos(chi + offset)).toFloat(),
                arrowCy + (innerRadius * sin(chi + offset)).toFloat()
            )
        }

        headFgPaint.strokeWidth = r * 0.022f * headAnimValue
        canvas.drawPath(path, headFgPaint)
    }

    private fun drawText(cx: Float, cy: Float, canvas: Canvas) {
        val p = textPaint
        val baselineOffset = -p.ascent() - 0.5f * (-p.ascent() + p.descent())
        val lineHeight = -p.ascent() + p.descent()
        val lines = centreText.split("\n")
        val yoff = baselineOffset - (lines.size - 1) / 2.0f * lineHeight

        lines.forEachIndexed { i, line ->
            canvas.drawText(line, cx, cy + yoff + (i * lineHeight), p)
        }
    }

    companion object {
        private const val RING_REL_STROKEWIDTH = 0.125f
        private const val HEAD_REL_RADIUS = 0.12f
    }
}
