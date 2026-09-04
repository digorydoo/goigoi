package io.github.digorydoo.goigoi.drawable

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.ScreenSize
import kotlin.math.min

class HintBalloonDrawable(
    private val anchorX: Int,
    private val dir: Direction,
    private val shadowMargin: Rect,
    private val colours: Colours,
    private val dimensions: Dimensions,
): AnimatedDrawable() {
    enum class Direction { UPWARDS, DOWNWARDS }

    interface Colours {
        val background: Color
        val outline: Color
        val shadow: Color
    }

    interface Dimensions {
        val cornerSizePx: Float
        val tipWidthPx: Float
        val tipHeightPx: Float
        val shadowRadiusPx: Float
        val shadowDyPx: Float
        val outlineStrokeWidthPx: Float
    }

    // @see https://developer.android.com/guide/topics/graphics/hardware-accel.html
    private val canUseShadowLayer = true // OK since API level 28

    private val bgndPaint = Paint().apply {
        isAntiAlias = true
        color = colours.background.toArgb()
        style = Paint.Style.FILL
    }

    private val outlinePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = dimensions.outlineStrokeWidthPx
        color = colours.outline.toArgb()
    }

    override fun draw(canvas: Canvas) {
        val bounds = animatedBounds
        val x1 = bounds.left.toFloat() + shadowMargin.left
        val x2 = bounds.right.toFloat() - shadowMargin.right
        val y1 = bounds.top.toFloat() + shadowMargin.top
        val y2 = bounds.bottom.toFloat() - shadowMargin.bottom

        var ax1 = anchorX - dimensions.tipWidthPx / 2
        var ax2 = anchorX + dimensions.tipWidthPx / 2

        if (ax1 < x1 + dimensions.cornerSizePx) {
            val dx = x1 + dimensions.cornerSizePx - ax1
            ax1 += dx
            ax2 += dx
        } else if (ax2 > x2 - dimensions.cornerSizePx) {
            val dx = ax2 - (x2 - dimensions.cornerSizePx)
            ax1 -= dx
            ax2 -= dx
        }

        val x1c = x1 + dimensions.cornerSizePx
        val x2c = x2 - dimensions.cornerSizePx
        val y1c = y1 + dimensions.cornerSizePx
        val y2c = y2 - dimensions.cornerSizePx

        val path = Path().apply {
            when (dir) {
                Direction.DOWNWARDS -> {
                    arcTo(x1, y1 + dimensions.tipHeightPx, x1c, y1c + dimensions.tipHeightPx, 270.0f, -90.0f, false)
                    arcTo(x1, y2c, x1c, y2, 180.0f, -90.0f, false)
                    arcTo(x2c, y2c, x2, y2, 90.0f, -90.0f, false)
                    arcTo(x2c, y1 + dimensions.tipHeightPx, x2, y1c + dimensions.tipHeightPx, 0.0f, -90.0f, false)
                    lineTo(ax2, y1 + dimensions.tipHeightPx)
                    lineTo((ax1 + ax2) / 2, y1)
                    lineTo(ax1, y1 + dimensions.tipHeightPx)
                }
                else -> {
                    arcTo(x1, y1, x1c, y1c, 270.0f, -90.0f, false)
                    arcTo(x1, y2c - dimensions.tipHeightPx, x1c, y2 - dimensions.tipHeightPx, 180.0f, -90.0f, false)
                    lineTo(ax1, y2 - dimensions.tipHeightPx)
                    lineTo((ax1 + ax2) / 2, y2)
                    lineTo(ax2, y2 - dimensions.tipHeightPx)
                    arcTo(x2c, y2c - dimensions.tipHeightPx, x2, y2 - dimensions.tipHeightPx, 90.0f, -90.0f, false)
                    arcTo(x2c, y1, x2, y1c, 0.0f, -90.0f, false)
                }
            }
            close()
        }

        if (canUseShadowLayer) {
            bgndPaint.setShadowLayer(dimensions.shadowRadiusPx, 0.0f, dimensions.shadowDyPx, colours.shadow.toArgb())
            canvas.drawPath(path, bgndPaint)
            bgndPaint.clearShadowLayer()
        } else {
            // When we can't use a shadow layer, we draw an outline instead.
            canvas.drawPath(path, bgndPaint)
            canvas.drawPath(path, outlinePaint)
        }
    }

    companion object {
        @Suppress("unused")
        private const val TAG = "HintBalloon"

        const val TIP_HEIGHT = 12 // dp
        private const val TIP_DISTANCE = 4 // dp
        private const val TEXT_MARGIN = 16 // dp
        private const val BALLOON_MARGIN_NORMAL = 32 // dp
        private const val BALLOON_MARGIN_SMALL = 0 // dp
        const val SHADOW_MARGIN = 12 // dp
        const val SHADOW_MARGIN_TIP_TOP = 2 // dp
        const val SHADOW_MARGIN_TIP_BOTTOM = 8 // dp
        private const val ANCHOR_MAX_DISTANCE = 48 // dp
        private const val BALLOON_MAX_WIDTH = 352 // dp

        private const val TEXT_SIZE_NORMAL = 19.0f // sp
        private const val TEXT_SIZE_SMALL = 17.0f // sp

        private fun getAnchorBounds(anchorView: View, rootView: View): Rect {
            return IntArray(2).let {
                rootView.getLocationOnScreen(it)
                val rx = it[0]
                val ry = it[1]
                anchorView.getLocationOnScreen(it)
                val ax = it[0] - rx
                val ay = it[1] - ry
                Rect(ax, ay, ax + anchorView.width, ay + anchorView.height)
            }
        }

        fun create(
            hintText: CharSequence,
            anchorView: View,
            anchorView2: View?,
            rootView: RelativeLayout,
            screenSize: ScreenSize,
            ctx: Context,
        ) {
            val r = getAnchorBounds(anchorView, rootView)
            val allowOverlap: Boolean

            if (anchorView2 != null) {
                // When a second anchor is given, we take the union of their bounding boxes.
                r.union(getAnchorBounds(anchorView2, rootView))
                allowOverlap = false
            } else {
                if (anchorView is TextView) {
                    // Apply anchorMaxDistance since the TextView may be much wider than its text!
                    val anchorMaxDistance = DimUtils.dpToPx(ANCHOR_MAX_DISTANCE, ctx)
                    r.right = min(r.right, r.left + anchorMaxDistance)
                }

                // The hint of the BigRing should be allowed to overlap
                allowOverlap = anchorView !is TextView
            }

            create(hintText, r, rootView, allowOverlap, screenSize, ctx)
        }

        fun create(
            hintText: CharSequence,
            anchor: Rect,
            rootView: RelativeLayout,
            allowOverlap: Boolean,
            screenSize: ScreenSize,
            ctx: Context,
        ) {
            val dir = if ((anchor.top + anchor.bottom) / 2 < (rootView.height / 2)) {
                Direction.DOWNWARDS
            } else {
                Direction.UPWARDS
            }

            val tipHeight = DimUtils.dpToPx(TIP_HEIGHT, ctx)
            val tipDistance = DimUtils.dpToPx(TIP_DISTANCE, ctx)
            val textMargin = DimUtils.dpToPx(TEXT_MARGIN, ctx)
            val balloonMaxWidth = DimUtils.dpToPx(BALLOON_MAX_WIDTH, ctx)

            val shadowMargin = DimUtils.dpToPx(SHADOW_MARGIN, ctx)
                .let { Rect(it, it, it, it) }
                .apply {
                    if (dir == Direction.DOWNWARDS) {
                        top = DimUtils.dpToPx(SHADOW_MARGIN_TIP_TOP, ctx)
                    } else {
                        bottom = DimUtils.dpToPx(SHADOW_MARGIN_TIP_BOTTOM, ctx)
                    }
                }

            val balloonMargin = when (screenSize) {
                ScreenSize.SMALL -> DimUtils.dpToPx(BALLOON_MARGIN_SMALL, ctx)
                else -> DimUtils.dpToPx(BALLOON_MARGIN_NORMAL, ctx)
            }

            val anchorX = (anchor.left + anchor.right) / 2

            val anchorY = if (dir == Direction.DOWNWARDS) {
                (if (allowOverlap) {
                    (0.3f * anchor.top + 0.7f * anchor.bottom).toInt()
                } else {
                    anchor.bottom + tipDistance
                }) - shadowMargin.top
            } else {
                (if (allowOverlap) {
                    (0.7f * anchor.top + 0.3f * anchor.bottom).toInt()
                } else {
                    anchor.top - tipDistance
                }) + shadowMargin.bottom
            }

            var balloonLeft: Int
            var balloonRight: Int

            if (rootView.width - balloonMargin * 2 < balloonMaxWidth) {
                balloonLeft = balloonMargin
                balloonRight = rootView.width - balloonMargin
            } else {
                balloonLeft = anchorX - balloonMaxWidth / 2
                balloonRight = balloonLeft + balloonMaxWidth

                if (balloonLeft < balloonMargin) {
                    val d = balloonMargin - balloonLeft
                    balloonLeft += d
                    balloonRight += d
                } else if (balloonRight > rootView.width - balloonMargin) {
                    val d = balloonRight - (rootView.width - balloonMargin)
                    balloonLeft -= d
                    balloonRight -= d
                }
            }

            val textView = TextView(ctx).apply {
                text = hintText
                setTextSize(
                    TypedValue.COMPLEX_UNIT_SP,
                    when (screenSize) {
                        ScreenSize.SMALL -> TEXT_SIZE_SMALL
                        else -> TEXT_SIZE_NORMAL
                    }
                )
                layoutParams = FrameLayout.LayoutParams(0, 0).apply {
                    width = FrameLayout.LayoutParams.WRAP_CONTENT
                    height = FrameLayout.LayoutParams.WRAP_CONTENT
                    leftMargin = textMargin + shadowMargin.left
                    rightMargin = textMargin + shadowMargin.right
                    topMargin = textMargin + shadowMargin.top
                    bottomMargin = textMargin + shadowMargin.bottom

                    if (dir == Direction.DOWNWARDS) {
                        topMargin += tipHeight
                    } else {
                        bottomMargin += tipHeight
                    }
                }
            }

            val colours = object: Colours {
                override val background = Color(ResUtils.getARGBFromAttr(R.attr.hintBalloonBgColour, ctx))
                override val outline = Color(ResUtils.getARGBFromAttr(R.attr.hintBalloonOutlineColour, ctx))
                override val shadow = Color(0.0f, 0.0f, 0.0f, 0.24f)
            }

            val dimensions = object: Dimensions {
                override val cornerSizePx = DimUtils.dpToPx(16.0f, ctx)
                override val tipWidthPx = DimUtils.dpToPx(12.0f, ctx)
                override val tipHeightPx = DimUtils.dpToPx(TIP_HEIGHT.toFloat(), ctx)
                override val shadowRadiusPx = DimUtils.dpToPx(6.4f, ctx)
                override val shadowDyPx = DimUtils.dpToPx(3.0f, ctx)
                override val outlineStrokeWidthPx = DimUtils.dpToPx(2.0f, ctx)
            }

            val balloon = FrameLayout(ctx).apply {
                addView(textView)
                background = HintBalloonDrawable(anchorX - balloonLeft, dir, shadowMargin, colours, dimensions)

                // Using translation allows us to move the balloon to a position that sticks out of
                // the screen size, which may happen on small screens. This is better than
                // truncating the size of the balloon and clipping the text inside.

                translationX = balloonLeft.toFloat()
                translationY = when (dir) {
                    Direction.DOWNWARDS -> anchorY.toFloat()
                    else -> anchorY.toFloat() - rootView.height
                }

                layoutParams = FrameLayout.LayoutParams(0, 0).apply {
                    width = balloonRight - balloonLeft
                    height = FrameLayout.LayoutParams.WRAP_CONTENT

                    // We don't know how tall the balloon is going to be. When the tip is at the
                    // bottom, we move the entire balloon to the bottom of the screen and then
                    // translate in the negative way.

                    gravity = when (dir) {
                        Direction.DOWNWARDS -> Gravity.TOP
                        else -> Gravity.BOTTOM
                    }
                }
            }

            val backdrop = FrameLayout(ctx).apply {
                addView(balloon)
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT
                )
                setOnClickListener { rootView.removeView(this) }
            }

            rootView.addView(backdrop)
            backdrop.requestLayout()
        }
    }
}
