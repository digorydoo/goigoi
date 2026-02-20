package io.github.digorydoo.goigoi.activity.welcome

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.drawable.AnimatedDrawable
import io.github.digorydoo.goigoi.drawable.Artist
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import ch.digorydoo.kutils.colour.Colour
import ch.digorydoo.kutils.filter.delay
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class BigRing(
    ctx: Context,
    val value: Float, // 0..1
    private val centreText: String,
): AnimatedDrawable() {
    private val trailColour = ResUtils.getARGBFromAttr(R.attr.bigRingTrailColour, ctx)
    private val checkmarkColour = ResUtils.getARGBFromRes(R.color.green_800, ctx)

    private val insetSize = DimUtils.dpToPx(1, ctx)
    private val markInsetSize = DimUtils.dpToPx(48, ctx)

    private val trackPaint = Paint().apply {
        isAntiAlias = true
        color = ResUtils.getARGBFromAttr(R.attr.bigRingTrackColour, ctx)
        style = Paint.Style.STROKE
    }

    private val trailPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL // STROKE would draw the text as outline
        color = ResUtils.getARGBFromAttr(R.attr.bigRingTextColour, ctx)
        textAlign = Paint.Align.CENTER
        textSize = DimUtils.dpToPx(17.0f, ctx)
    }

    private val markPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = checkmarkColour
        strokeCap = Paint.Cap.SQUARE
        strokeJoin = Paint.Join.MITER
    }

    private val headBgPaint = Paint().apply {
        isAntiAlias = true
        color = ResUtils.getARGBFromRes(R.color.green_800, ctx)
        style = Paint.Style.FILL
    }

    private val headFgPaint = Paint().apply {
        isAntiAlias = true
        color = ResUtils.getARGBFromRes(R.color.white, ctx)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.SQUARE
        strokeJoin = Paint.Join.ROUND
    }

    override fun draw(canvas: Canvas) {
        val bounds = copyBounds()
        bounds.inset(insetSize, insetSize)

        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val r = 0.5f * min(bounds.width(), bounds.height())
        val phi = max(0.0f, value * 360.0f) * animValue
        val shouldDrawCheckmark = value >= CHECKMARK_THRESHOLD
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
            bounds.inset(markInsetSize, markInsetSize)
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

        trailPaint.color = Colour.mixARGB(trailColour, checkmarkColour, ringHeadAnimValue)
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
        @Suppress("unused")
        private const val TAG = "BigRing"

        const val CHECKMARK_THRESHOLD = 0.98f

        private const val RING_REL_STROKEWIDTH = 0.125f
        private const val HEAD_REL_RADIUS = 0.12f
    }
}
