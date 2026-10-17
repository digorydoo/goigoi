package io.github.digorydoo.goigoi.activity.splash

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import ch.digorydoo.kutils.filter.envDelay
import ch.digorydoo.kutils.flow.compose
import ch.digorydoo.kutils.math.accel
import ch.digorydoo.kutils.math.scurve
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

class AnimatedLogo(ctx: Context): Drawable() {
    private class Arc(
        var time: Float,
        var left: Float,
        var top: Float,
        var right: Float,
        var bottom: Float,
        var radialShift: Float,
        var start: Float,
        var end: Float,
        var angularShift: Float,
    )

    private val oneDip: Float = DimUtils.dpToPx(1.0f, ctx)
    private val penSize = 6.0f * oneDip
    private val bgndColour = ResUtils.getARGBFromRes(R.color.green_800, ctx)
    private val arcColour = ResUtils.getARGBFromRes(R.color.white, ctx)

    var animValue: Float = 1.0f

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}

    @Deprecated("Deprecated in Java; still needed for API < 29", ReplaceWith(""))
    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun draw(canvas: Canvas) {
        val p = Paint()
        p.isAntiAlias = true

        val funs = arrayOf(
            compose<Double>(
                { envDelay(it, 0.10, 0.35) },
                { scurve(it, 1.0) }
            ),
            compose(
                { envDelay(it, 0.0, 0.45) },
                { scurve(it, 1.0) }
            ),
            compose(
                { envDelay(it, 0.31, 0.43) },
                { accel(it, 1.6) }
            ),
            compose(
                { envDelay(it, 0.44, 0.57) },
                { accel(it, 1.7) }
            ),
            compose(
                { envDelay(it, 0.57) },
                { scurve(it, 1.5) }
            )
        )

        val (t0, t1, t2, t3, t4) = funs.map { it(animValue.toDouble()).toFloat() }

        // Draw background circle

        val bounds = copyBounds()
        bounds.inset((penSize + oneDip).toInt(), (penSize + oneDip).toInt())
        p.color = bgndColour
        p.style = Paint.Style.FILL
        val circleRadius = bounds.width() / 2.0f * t0
        canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), circleRadius, p)

        // Arcs

        p.color = arcColour
        p.style = Paint.Style.STROKE
        p.strokeWidth = penSize
        val path = Path()
        addAnimatedArc(arc1, path, t1, bounds, false)

        if (t2 > 0) {
            addAnimatedArc(arc2, path, t2, bounds, false)
        }

        if (t3 > 0) {
            addAnimatedArc(arc3, path, t3, bounds, false)
        }

        if (t4 > 0) {
            addAnimatedArc(arc4, path, t4, bounds, false)
            addAnimatedArc(arc5, path, t4, bounds, true)
            addAnimatedArc(arc6, path, t4, bounds, true)
            addAnimatedArc(arc7, path, t4, bounds, true)
        }

        canvas.drawPath(path, p)

        if (BuildConfig.DEBUG) {
            p.style = Paint.Style.FILL
            p.textAlign = Paint.Align.CENTER
            p.textSize = 1.5f * penSize
            canvas.drawText("DEBUG", bounds.exactCenterX(), bounds.bottom - 2.0f * penSize, p)
        }
    }

    companion object {
        // Circle around the logo
        private val arc1 = arrayOf(
            //  time   left  top   right bttm  shift start   end     ang
            Arc(0.00f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 270.0f, 270.0f, 0.0f),
            Arc(1.00f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 270.0f, 630.0f, 0.0f)
        )

        // Horizontal stroke of あ
        private val arc2 = arrayOf(
            //  time    left   top     right   bttm    shift  start   end     ang
            Arc(0.00f, -0.10f, 0.125f, 0.830f, 0.285f, 0.00f, 828.0f, 828.0f, 0.0f),
            Arc(1.00f, -0.10f, 0.125f, 0.830f, 0.285f, 0.00f, 759.0f, 828.0f, 0.0f)
        )

        // Vertical stroke of あ
        private val arc3 = arrayOf(
            //  time   left   top    right  bttm    shift  start    end     ang
            Arc(0.00f, 0.42f, 0.00f, 0.64f, 0.88f, 0.00f, 580.0f, 580.0f, 0.0f),
            Arc(1.00f, 0.42f, 0.00f, 0.64f, 0.88f, 0.00f, 493.0f, 580.0f, 0.0f)
        )

        // Round stroke of あ, part 1
        private val arc4 = arrayOf(
            //  time   left     top      right   bttm    shift  start   end      ang
            Arc(0.00f, -0.230f, -0.180f, 0.645f, 0.760f, 0.00f, 728.0f, 728.00f, 0.0f),
            Arc(0.28f, -0.230f, -0.180f, 0.645f, 0.760f, 0.00f, 728.0f, 783.00f, 0.0f)
        )

        // Round stroke of あ, part 2
        private val arc5 = arrayOf(
            // time   left   top    right  bttm   shift start  end     ang
            Arc(0.28f, 0.22f, 0.61f, 0.41f, 0.74f, 0.0f, 54.0f, 54.0f, 0.0f),
            Arc(0.42f, 0.22f, 0.61f, 0.41f, 0.74f, 0.0f, 54.0f, 166.0f, 0.0f)
        )

        // Round stroke of あ, part 3
        private val arc6 = arrayOf(
            //  time   left   top     right  bttm   shift start   end     ang
            Arc(0.42f, 0.22f, 0.413f, 0.94f, 0.92f, 0.0f, 180.0f, 180.0f, 0.0f),
            Arc(0.76f, 0.22f, 0.413f, 0.94f, 0.92f, 0.0f, 180.0f, 272.0f, 0.0f)
        )

        // Round stroke of あ, part 4
        private val arc7 = arrayOf(
            //  time   left   top   right  bttm  shift start   end     ang
            Arc(0.76f, 0.21f, 0.4f, 0.79f, 0.8f, 0.0f, 302.0f, 302.0f, 0.0f),
            Arc(1.00f, 0.21f, 0.4f, 0.79f, 0.8f, 0.0f, 302.0f, 436.0f, 0.0f)
        )

        private fun addAnimatedArc(arcs: Array<Arc>, path: Path, time: Float, bounds: Rect, connect: Boolean) {
            var fromArc: Arc? = null
            var toArc: Arc? = null

            for (a in arcs) {
                if (a.time <= time) {
                    fromArc = a
                } else {
                    toArc = a
                    break
                }
            }

            if (fromArc == null) {
                // This set of arcs don't show yet
                return
            }

            var x1: Float
            var y1: Float
            var x2: Float
            var y2: Float
            val start: Float
            val end: Float
            val radialShift: Float
            val angularShift: Float

            if (toArc == null) {
                // This set of arcs have completed animation.
                x1 = bounds.left + bounds.width() * fromArc.left
                y1 = bounds.top + bounds.height() * fromArc.top
                x2 = bounds.left + bounds.width() * fromArc.right
                y2 = bounds.top + bounds.height() * fromArc.bottom
                start = fromArc.start
                end = fromArc.end
                radialShift = fromArc.radialShift
                angularShift = fromArc.angularShift
            } else {
                // Interpolate between fromArc and toArc.
                val p = (time - fromArc.time) / (toArc.time - fromArc.time) // 0..1
                val q = 1.0f - p
                x1 = bounds.left + bounds.width() * (q * fromArc.left + p * toArc.left)
                y1 = bounds.top + bounds.height() * (q * fromArc.top + p * toArc.top)
                x2 = bounds.left + bounds.width() * (q * fromArc.right + p * toArc.right)
                y2 = bounds.top + bounds.height() * (q * fromArc.bottom + p * toArc.bottom)
                start = q * fromArc.start + p * toArc.start
                end = q * fromArc.end + p * toArc.end
                radialShift = q * fromArc.radialShift + p * toArc.radialShift
                angularShift = q * fromArc.angularShift + p * toArc.angularShift
            }

            if (radialShift != 0.0f) {
                val phi = 0.5f * (start + end) * Math.PI / 180.0f
                val r = 0.5f * max(bounds.width(), bounds.height())
                val xShift = (radialShift * r * cos(phi)).toFloat()
                val yShift = (radialShift * r * sin(phi)).toFloat()
                x1 += xShift
                y1 += yShift
                x2 += xShift
                y2 += yShift
            }

            var sweep = end - start

            if (sweep <= 0) {
                return
            } else if (sweep >= 360.0f) {
                sweep = 359.999f
            }

            path.arcTo(x1, y1, x2, y2, start + angularShift, sweep, !connect)
        }
    }
}
