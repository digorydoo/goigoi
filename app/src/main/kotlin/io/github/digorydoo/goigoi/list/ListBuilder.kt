package io.github.digorydoo.goigoi.list

import android.content.Context
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.db.Topic
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Unyt.Companion.MIN_NUM_WORDS_FOR_STUDY
import io.github.digorydoo.goigoi.db.Vocabulary
import io.github.digorydoo.goigoi.drawable.BitmapPool
import io.github.digorydoo.goigoi.drawable.LargeListItemBgnd
import io.github.digorydoo.goigoi.helper.UserPrefs.WordListItemMode
import io.github.digorydoo.goigoi.utils.ScreenSize
import ch.digorydoo.kutils.cjk.JLPTLevel

/**
 * Note: The methods of this class may be called from a thread!
 */
object ListBuilder {
    private fun makeTopicListItem(topic: Topic, isFirst: Boolean, ctx: Context): TopicListItem {
        return TopicListItem(topic, ctx).apply {
            hasTopDivider = isFirst
        }
    }

    private fun makeContinueStudyingItem(ctx: Context, screenSize: ScreenSize, lrMargin: Int): AbstrListItem {
        val primaryText = "つづきへ"
        val secondaryText = ctx.getString(R.string.continue_studying)
        val bgndBitmap = BitmapPool.getFromAssets("img/btn-00-continue.webp", ctx)
        val img = LargeListItemBgnd(ctx, primaryText, secondaryText, bgndBitmap, screenSize == ScreenSize.LARGE)
        return ActionItem(primaryText, secondaryText, img, lrMargin)
    }

    fun buildWelcomeList(
        vocab: Vocabulary,
        screenSize: ScreenSize,
        lrMargin: Int,
        imageMargin: Int,
        ctx: Context,
    ): Array<AbstrListItem> {
        val items = mutableListOf<AbstrListItem>()
        items.add(HeaderItem())
        items.add(makeContinueStudyingItem(ctx, screenSize, lrMargin + imageMargin))

        var isFirst = true

        for (topic in vocab.topics) {
            val canSee = BuildConfig.DEBUG || !topic.hidden

            if (canSee) {
                items.add(makeTopicListItem(topic, isFirst, ctx))
                isFirst = false
            }
        }

        // The start button on UnytActivity tests the limit as well, but it's better to hide
        // the unyt as long as the user has not studied enough words yet.

        if (vocab.myWordsUnyt.numWordsAvailable > MIN_NUM_WORDS_FOR_STUDY) {
            items.add(
                UnytListItem(vocab.myWordsUnyt, R.drawable.ic_bubbles_24dp, ctx).apply {
                    hasTopDivider = true
                }
            )
        }

        return items.toTypedArray()
    }

    fun buildUnytsList(topic: Topic, topMargin: Int, topMarginWhenSubheader: Int, ctx: Context): Array<AbstrListItem> {
        val items = mutableListOf<AbstrListItem>(HeaderItem())
        var isFirst = true

        for (unyt in topic.unyts) {
            val subheader = unyt.subheader.withSystemLang

            if (subheader.isNotEmpty()) {
                SubheaderItem(subheader).let {
                    items.add(it)
                    it.hasTopDivider = !isFirst

                    if (isFirst) {
                        it.topMargin = topMarginWhenSubheader
                    }
                }
            }

            val badge = when {
                unyt.levels.size == 1 && unyt.levels[0] != JLPTLevel.Nx -> {
                    unyt.levels[0].toPrettyString()
                }
                unyt.levels.size == 2 && !unyt.levels.contains(JLPTLevel.Nx) -> {
                    unyt.levels.joinToString(", ") { it.toString().uppercase() }
                }
                else -> ""
            }

            UnytListItem(unyt, badge, ctx).let {
                items.add(it)

                if (isFirst && subheader.isEmpty()) {
                    it.topMargin = topMargin
                }
            }

            isFirst = false
        }

        return items.toTypedArray()
    }

    fun buildWordsList(unyt: Unyt, mode: WordListItemMode, topMargin: Int, ctx: Context): Array<AbstrListItem> {
        val items = mutableListOf<AbstrListItem>(HeaderItem())
        var isFirst = true

        unyt.forEachWord { word ->
            val hasBadge = unyt.levels.size > 1 && word.level != null && word.level != JLPTLevel.Nx

            WordListItem(word, mode, hasBadge, ctx).let {
                items.add(it)

                if (isFirst) {
                    it.topMargin = topMargin
                }
            }

            isFirst = false
        }

        return items.toTypedArray()
    }
}
