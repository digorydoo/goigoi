package io.github.digorydoo.goigoi.helper

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.utils.DimUtils
import io.github.digorydoo.goigoi.utils.ResUtils

class MyLayoutInflater(private val inflater: LayoutInflater, private val root: ViewGroup) {
    fun insertSubheader(strResId: Int) {
        val ctx = root.context ?: return
        val str = ctx.getString(strResId)
        insertSubheader(str)
    }

    fun insertSubheader(caption: String) {
        val group = inflater.inflate(R.layout.bottom_sheet_subheader, root, false)
        root.addView(group)

        group.findViewById<TextView>(R.id.label).apply {
            text = caption
        }
    }

    @Suppress("unused")
    fun insertItem(primaryText: String) {
        val group = inflater.inflate(R.layout.my_list_item_1, root, false)
        root.addView(group)

        group.findViewById<TextView>(R.id.primary_text).apply {
            text = primaryText
        }
    }

    fun insertItem(primaryText: String, secondaryText: String) {
        val group = inflater.inflate(R.layout.my_list_item_2, root, false)
        root.addView(group)

        group.findViewById<TextView>(R.id.primary_text).apply {
            text = primaryText
        }

        group.findViewById<TextView>(R.id.secondary_text).apply {
            text = secondaryText
        }
    }

    fun insertItemMultiLine(primaryText: String, secondaryText: String) {
        val group = inflater.inflate(R.layout.my_list_item_2_multiline, root, false)
        root.addView(group)

        group.findViewById<TextView>(R.id.primary_text).apply {
            text = primaryText
        }

        group.findViewById<TextView>(R.id.secondary_text).apply {
            text = secondaryText
        }
    }

    fun insertItemWithFurigana(primaryText: CharSequence, secondaryText: String) {
        val group = inflater.inflate(R.layout.my_list_item_2_furigana, root, false)
        root.addView(group)

        group.findViewById<TextView>(R.id.primary_text).apply {
            text = primaryText
        }

        group.findViewById<TextView>(R.id.secondary_text).apply {
            text = secondaryText
        }
    }

    fun insertItemWithFurigana(primaryText: CharSequence, secondaryText: String, tertiaryText: CharSequence) {
        if (tertiaryText.isEmpty()) {
            insertItemWithFurigana(primaryText, secondaryText)
        } else {
            val group = inflater.inflate(R.layout.my_list_item_3_furigana, root, false)
            root.addView(group)

            group.findViewById<TextView>(R.id.primary_text).apply {
                text = primaryText
            }

            group.findViewById<TextView>(R.id.secondary_text).apply {
                text = secondaryText
            }

            group.findViewById<TextView>(R.id.tertiary_text).apply {
                text = tertiaryText
            }
        }
    }

    fun insertItem(imgResId: Int, primaryText: String, onClick: () -> Unit) {
        val ctx = root.context
        val group = inflater.inflate(R.layout.my_list_item_1, root, false)
        group.setOnClickListener { onClick() }
        root.addView(group)

        val distanceToText = DimUtils.dpToPx(32, ctx)
        val imgMarginLeft = DimUtils.dpToPx(8, ctx)
        val imgSize = DimUtils.dpToPx(32, ctx)

        val imgDrw = ContextCompat.getDrawable(ctx, imgResId)?.apply {
            bounds = Rect(imgMarginLeft, 0, imgSize + imgMarginLeft, imgSize)
        }

        val tintList = ResUtils.getARGBFromAttr(R.attr.decorativeIconTintColour, ctx)
            .let { ColorStateList.valueOf(it) }

        group.findViewById<TextView>(R.id.primary_text).apply {
            text = primaryText
            setCompoundDrawables(imgDrw, null, null, null)
            TextViewCompat.setCompoundDrawableTintMode(this, PorterDuff.Mode.SRC_IN)
            TextViewCompat.setCompoundDrawableTintList(this, tintList)
            compoundDrawablePadding = distanceToText
        }
    }

    @Suppress("unused")
    fun insertItem(imgResId: Int, primaryText: String, secondaryText: String, onClick: () -> Unit) {
        val ctx = root.context
        val group = inflater.inflate(R.layout.my_list_item_2_drw, root, false)
        group.setOnClickListener { onClick() }
        root.addView(group)

        val tintList = ResUtils.getARGBFromAttr(R.attr.decorativeIconTintColour, ctx)
            .let { ColorStateList.valueOf(it) }

        val drw = ContextCompat.getDrawable(ctx, imgResId)?.apply {
            setTintList(tintList)
            setTintMode(PorterDuff.Mode.SRC_IN)
        }

        group.findViewById<ImageView>(R.id.image_view).apply {
            setImageDrawable(drw)
        }

        group.findViewById<TextView>(R.id.primary_text).apply {
            text = primaryText
        }

        group.findViewById<TextView>(R.id.secondary_text).apply {
            text = secondaryText
        }
    }

    fun insertItem(kanjiAsIcon: Char, primaryText: String, secondaryText: String, onClick: () -> Unit) {
        val group = inflater.inflate(R.layout.my_list_item_2_kanjiicon, root, false)
        group.setOnClickListener { onClick() }
        root.addView(group)

        group.findViewById<TextView>(R.id.kanji_icon).apply {
            text = "$kanjiAsIcon"
        }

        group.findViewById<TextView>(R.id.primary_text).apply {
            text = primaryText
        }

        group.findViewById<TextView>(R.id.secondary_text).apply {
            text = secondaryText
        }
    }
}
