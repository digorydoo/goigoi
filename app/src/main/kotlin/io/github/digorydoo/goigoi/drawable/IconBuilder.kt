package io.github.digorydoo.goigoi.drawable

import android.content.Context
import androidx.compose.ui.graphics.Color
import ch.digorydoo.kutils.utils.Moment
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.core.db.Unyt
import io.github.digorydoo.goigoi.core.db.Word
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils
import io.github.digorydoo.goigoi.utils.SingletonHolder
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object IconBuilder {
    const val DAYS_BEFORE_ZZZ = 99

    fun makeWordIcon(ctx: Context, word: Word): AnimatedDrawable {
        val stats = SingletonHolder.stats
        val progress = stats.getWordStudyProgress(word)

        if (progress < 1.0f) {
            return getRingIconDrawable(ctx, RingIconDrawable.Variant.CIRCULAR, progress)
        }

        val rating = stats.getWordTotalRating(word)
        return getBubbleDrawable(ctx, BubbleIconDrawable.Variant.CIRCULAR, rating)
    }

    fun makeUnytIcon(ctx: Context, unyt: Unyt): AnimatedDrawable {
        val stats = SingletonHolder.stats

        var progress = stats.getUnytStudyProgress(unyt)

        if (progress == 0.0f && unyt.numWordsLoaded == 0 && unyt.numWordsAvailable > 0) {
            // To compute the progress, we need to load the words of the unyt first. However, we
            // must avoid doing this for all unyts of a topic when a user navigates to a topic that
            // he has never studied yet, as this would have a performance impact. Luckily, a unyt
            // will still have a non-null study moment when its cache was invalidated after one of
            // its words' stats have changed, so we load those only.

            if (stats.getUnytStudyMoment(unyt) != null) {
                val vocab = SingletonHolder.vocab
                vocab.loadUnytIfNecessary(unyt)
                progress = stats.getUnytStudyProgress(unyt)
            }
        }

        if (progress < 1.0f) {
            return getRingIconDrawable(ctx, RingIconDrawable.Variant.DIAMOND, progress)
        }

        val studyDat = stats.getUnytStudyMoment(unyt)
        val minDat = Moment.now() - DAYS_BEFORE_ZZZ.toDuration(DurationUnit.DAYS)

        if (studyDat != null && studyDat < minDat) {
            return getZzzIconDrawable(ctx)
        }

        val rating = stats.getUnytRating(unyt)
        return getBubbleDrawable(ctx, BubbleIconDrawable.Variant.DIAMOND, rating)
    }

    private fun getRingIconDrawable(
        ctx: Context,
        variant: RingIconDrawable.Variant,
        progress: Float,
    ): RingIconDrawable {
        val colours = object: RingIconDrawable.Colours {
            override val trail = Color(ResUtils.getARGBFromAttr(R.attr.ringTrailColour, ctx))
            override val track = Color(ResUtils.getARGBFromAttr(R.attr.ringTrackColour, ctx))
        }
        val dims = object: RingIconDrawable.Dimensions {
            override val circularInsetSizePx = DimUtils.dpToPx(1, ctx)
            override val minStrokeWidthPx = DimUtils.dpToPx(3.0f, ctx)
        }
        return RingIconDrawable(variant, progress, colours, dims)
    }

    private fun getBubbleDrawable(
        ctx: Context,
        variant: BubbleIconDrawable.Variant,
        rating: Float,
    ): BubbleIconDrawable {
        val colours = object: BubbleIconDrawable.Colours {
            override val poorRating = Color(ResUtils.getARGBFromAttr(R.attr.bubbleFgColour, ctx))
            override val goodRating = Color(ResUtils.getARGBFromRes(R.color.green_800, ctx))
            override val background = Color(ResUtils.getARGBFromAttr(R.attr.bubbleBgColour, ctx))
            override val outline = Color(ResUtils.getARGBFromAttr(R.attr.bubbleOutlineColour, ctx))
        }
        val dims = object: BubbleIconDrawable.Dimensions {
            override val circularInsetPx = DimUtils.dpToPx(2, ctx)
            override val minBubbleSizePx = DimUtils.dpToPx(2.0f, ctx)
            override val outlineStrokeWidthPx = DimUtils.dpToPx(1.0f, ctx)
        }
        return BubbleIconDrawable(variant, rating, colours, dims)
    }

    fun getCheckmarkIconDrawable(ctx: Context): CheckmarkIconDrawable {
        val colours = object: CheckmarkIconDrawable.Colours {
            override val background = Color(ResUtils.getARGBFromRes(R.color.green_800, ctx))
            override val mark = Color(ResUtils.getARGBFromRes(R.color.white, ctx))
        }
        val dims = object: CheckmarkIconDrawable.Dimensions {
            override val insetPx = DimUtils.dpToPx(1, ctx)
            override val markMinSizePx = DimUtils.dpToPx(23, ctx)
        }
        return CheckmarkIconDrawable(colours, dims)
    }

    fun getFabIconDrawable(ctx: Context, iconName: FabIconDrawable.IconName): FabIconDrawable {
        val colours = object: FabIconDrawable.Colours {
            override val normal = Color(ResUtils.getARGBFromRes(R.color.green_800, ctx))
            override val pressed = Color(ResUtils.getARGBFromRes(R.color.green_700, ctx))
            override val shim = Color(ResUtils.getARGBFromAttr(R.attr.fabShimColour, ctx))
            override val glow = Color(ResUtils.getARGBFromAttr(R.attr.fabGlowColour, ctx))
            override val icon = Color(ResUtils.getARGBFromRes(R.color.white, ctx))
        }
        val dims = object: FabIconDrawable.Dimensions {
            override val shimWidthPx = DimUtils.dpToPx(8, ctx)
            override val glowRadiusPx = DimUtils.dpToPx(8.0f, ctx)
            override val outlinedIconStrokeWidthPx = DimUtils.dpToPx(2.5f, ctx)
        }
        return FabIconDrawable(iconName, colours, dims)
    }

    fun getFlashIconDrawable(ctx: Context): FlashIconDrawable {
        val colours = object: FlashIconDrawable.Colours {
            override val background = Color(ResUtils.getARGBFromAttr(R.attr.flashBgColour, ctx))
            override val foreground = Color(ResUtils.getARGBFromAttr(R.attr.flashFgColour, ctx))
        }
        val dims = object: FlashIconDrawable.Dimensions {
            override val insetPx = DimUtils.dpToPx(1, ctx)
        }
        return FlashIconDrawable(colours, dims)
    }

    fun getSheetHeadDrawable(ctx: Context): SheetHeadDrawable {
        val colours = object: SheetHeadDrawable.Colours {
            override val appBar = Color(ResUtils.getARGBFromAttr(R.attr.appBarColour, ctx))
        }
        val dims = object: SheetHeadDrawable.Dimensions {
            override val cornerSizePx = DimUtils.dpToPx(24, ctx)
            override val headHeightPx = cornerSizePx + DimUtils.dpToPx(8, ctx)
        }
        return SheetHeadDrawable(colours, dims)
    }

    fun getZzzIconDrawable(ctx: Context): ZzzIconDrawable {
        val colours = object: ZzzIconDrawable.Colours {
            override val background = Color(ResUtils.getARGBFromAttr(R.attr.zzzBgColour, ctx))
            override val foreground = Color(ResUtils.getARGBFromAttr(R.attr.zzzFgColour, ctx))
        }
        val dims = object: ZzzIconDrawable.Dimensions {
            override val insetPx = DimUtils.dpToPx(1, ctx)
        }
        return ZzzIconDrawable(colours, dims)
    }
}
