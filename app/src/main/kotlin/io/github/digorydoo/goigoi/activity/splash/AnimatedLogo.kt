package io.github.digorydoo.goigoi.activity.splash

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.drawable.AnimatedDrawable
import io.github.digorydoo.goigoi.drawable.Artist
import io.github.digorydoo.goigoi.drawable.Artist.Arc
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import ch.digorydoo.kutils.filter.delay
import ch.digorydoo.kutils.flow.compose
import ch.digorydoo.kutils.math.accel
import ch.digorydoo.kutils.math.scurve

class AnimatedLogo(ctx: Context): AnimatedDrawable() {
    private val oneDip: Float = DimUtils.dpToPx(1.0f, ctx)
    private val penSize = 6.0f * oneDip
    private val bgndColour = ResUtils.getARGBFromRes(R.color.green_800, ctx)
    private val arcColour = ResUtils.getARGBFromRes(R.color.white, ctx)

    override fun draw(canvas: Canvas) {
        val p = Paint()
        p.isAntiAlias = true

        val funs = arrayOf(
            compose<Double>(
                { delay(it, 0.10, 0.35) },
                { scurve(it, 1.0) }
            ),
            compose(
                { delay(it, 0.0, 0.45) },
                { scurve(it, 1.0) }
            ),
            compose(
                { delay(it, 0.31, 0.43) },
                { accel(it, 1.6) }
            ),
            compose(
                { delay(it, 0.44, 0.57) },
                { accel(it, 1.7) }
            ),
            compose(
                { delay(it, 0.57) },
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
        Artist.addAnimatedArc(arc1, path, t1, bounds, false)

        if (t2 > 0) {
            Artist.addAnimatedArc(arc2, path, t2, bounds, false)
        }

        if (t3 > 0) {
            Artist.addAnimatedArc(arc3, path, t3, bounds, false)
        }

        if (t4 > 0) {
            Artist.addAnimatedArc(arc4, path, t4, bounds, false)
            Artist.addAnimatedArc(arc5, path, t4, bounds, true)
            Artist.addAnimatedArc(arc6, path, t4, bounds, true)
            Artist.addAnimatedArc(arc7, path, t4, bounds, true)
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
        @Suppress("unused")
        private const val TAG = "AnimatedLogo"

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
    }
}
