package io.github.digorydoo.goigoi.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import io.github.digorydoo.goigoi.R

class AspectRatioLayout: FrameLayout {
    private var relWidth = 0
    private var relHeight = 0

    constructor(ctx: Context, attrs: AttributeSet): super(ctx, attrs) {
        setup(ctx, attrs)
    }

    constructor(ctx: Context, attrs: AttributeSet, defStyle: Int): super(ctx, attrs, defStyle) {
        setup(ctx, attrs)
    }

    private fun setup(context: Context, attrs: AttributeSet) {
        context.withStyledAttributes(attrs, R.styleable.AspectRatioLayout) {
            relWidth = getInt(R.styleable.AspectRatioLayout_aspectRatioWidth, DEFAULT_REL_WIDTH)
            relHeight = getInt(R.styleable.AspectRatioLayout_aspectRatioHeight, DEFAULT_REL_HEIGHT)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = width * relHeight / relWidth
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

    companion object {
        private const val DEFAULT_REL_WIDTH = 4
        private const val DEFAULT_REL_HEIGHT = 3
    }
}
