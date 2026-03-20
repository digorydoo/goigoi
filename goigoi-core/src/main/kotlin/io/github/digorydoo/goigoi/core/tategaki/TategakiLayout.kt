package io.github.digorydoo.goigoi.core.tategaki

import ch.digorydoo.kutils.cjk.isCJKNotKana
import ch.digorydoo.kutils.cjk.isHiragana
import ch.digorydoo.kutils.cjk.isKatakana
import ch.digorydoo.kutils.vector.Vector2i
import kotlin.math.max

class TategakiLayout {
    class Element(val startIdx: Int, val text: String, val furigana: String) {
        var x = 0
        var y = 0
        var minHeight = 0
        var trimmableHeight = 0
        val fullHeight get() = minHeight + trimmableHeight
        var softBreakAfter = false // will be set during each arrangement phase
        var forceSoftBreakAfter = false // will be set once when elements are added
    }

    private val elements = mutableListOf<Element>()

    fun clear() = elements.clear()
    fun isEmpty() = elements.isEmpty()
    fun forEachElement(lambda: (Element) -> Unit) = elements.forEach(lambda)

    fun add(idx: Int, char: Char) {
        if (char in WHITESPACE) {
            // We silently drop whitespace, but we force a soft break.
            elements.lastOrNull()?.forceSoftBreakAfter = true
        } else {
            elements.add(Element(idx, "$char", ""))
        }
    }

    fun add(idx: Int, text: String, furigana: String) {
        elements.add(Element(idx, text, furigana))
    }

    fun arrange(
        charWidth: Int,
        charHeight: Int,
        maxHeight: Int,
        columnSpacing: Int,
        letterSpacing: Int,
        furiganaCharWidth: Int,
        furiganaXDelta: Int,
        emptyFuriganaWidth: Int,
    ): Vector2i {
        val lastElWithFurigana = elements.lastOrNull { it.furigana.isNotEmpty() }

        var nextColumnWidth = 0 // will be determined later
        var firstColumn = true
        var furiganaSeen = false
        var left = if (elements.isEmpty()) 0 else -charWidth
        var top = 0
        var pastMaxColumnHeight = 0
        var softBreakAfterIdx = -1
        var lastBreakBeforeIdx = 0
        var elementIdx = 0

        elements.forEach { measure(it, charHeight, letterSpacing) }
        determineSoftBreaks()

        while (elementIdx < elements.size) {
            var element = elements[elementIdx]

            val breakNeeded = top > 0 && top + element.minHeight > maxHeight

            if (breakNeeded) {
                if (softBreakAfterIdx > 0) {
                    elementIdx = softBreakAfterIdx + 1 // go back to previous element
                    element = elements[elementIdx]
                }

                val trimmable = elements.foldIndexed(0) { i, result, e ->
                    @Suppress("EmptyRange") // linter bug
                    result + (if (i in lastBreakBeforeIdx ..< elementIdx) e.trimmableHeight else 0)
                }

                if (firstColumn) {
                    nextColumnWidth = when {
                        lastElWithFurigana == null -> {
                            // No column has any furigana
                            charWidth + emptyFuriganaWidth + columnSpacing
                        }
                        element.startIdx > lastElWithFurigana.startIdx -> {
                            // This column had furigana, but none of the columns that follow have any.
                            charWidth + emptyFuriganaWidth + columnSpacing
                        }
                        else -> {
                            // Furigana still follows, so columns distances must be slightly larger.
                            charWidth + furiganaXDelta + furiganaCharWidth + columnSpacing
                        }
                    }
                }

                pastMaxColumnHeight = max(pastMaxColumnHeight, top - trimmable)
                left -= nextColumnWidth
                top = 0
                firstColumn = false
                softBreakAfterIdx = -1
                lastBreakBeforeIdx = elementIdx
            } else if (element.softBreakAfter && elementIdx - lastBreakBeforeIdx + 1 >= MIN_CHARS_FOR_SOFT_BREAK) {
                softBreakAfterIdx = elementIdx
            }

            if (!furiganaSeen) {
                if (element.furigana.isNotEmpty()) {
                    furiganaSeen = true

                    if (firstColumn) {
                        // Push elements further to the left to make room for the furigana.
                        val deltaX = furiganaXDelta + furiganaCharWidth
                        left -= deltaX
                        for (e in elements) {
                            if (e == element) break
                            e.x -= deltaX
                        }
                    }
                }
            }

            element.x = left
            element.y = top
            top += element.fullHeight
            elementIdx++
        }

        val trimmable = elements.foldIndexed(0) { i, result, e ->
            result + (if (i >= lastBreakBeforeIdx) e.trimmableHeight else 0)
        }

        val newHeight = max(pastMaxColumnHeight, top - trimmable)

        val newWidth = -left
        val originX = -left
        val originY = 0

        elements.forEach {
            it.x += originX
            it.y += originY
        }

        return Vector2i(newWidth, newHeight)
    }

    private fun measure(element: Element, charHeight: Int, spacing: Int) {
        var trimmable = 0
        var height = 0

        element.text.forEach { c ->
            when (c) {
                '\n', '\r' -> {
                    // forced break not implemented and should probably have its own element
                    trimmable = 0
                }
                ' ' -> {
                    val h = charHeight / 2 + spacing // normal (half-height) space
                    height += h
                    trimmable = h
                }
                '　' -> {
                    val h = charHeight + spacing // full-height space
                    height += h
                    trimmable = h
                }
                '、', '。' -> {
                    height += charHeight + spacing // will occupy full height if not at end of column
                    trimmable = charHeight / 2 - spacing
                }
                else -> {
                    height += charHeight + spacing
                    trimmable = spacing
                }
            }
        }

        require(height >= trimmable)
        element.trimmableHeight = trimmable
        element.minHeight = height - trimmable
    }

    private fun determineSoftBreaks() {
        elements.forEachIndexed { i, element ->
            val lastCharOfThisEl = element.text.lastOrNull() ?: Char(0)
            val nextEl1 = elements.getOrNull(i + 1)?.text
            val nextEl2 = elements.getOrNull(i + 2)?.text
            val nextEl3 = elements.getOrNull(i + 3)?.text
            val nextChar1 = nextEl1?.firstOrNull() ?: Char(0)
            val nextChar2 = if (nextEl1?.length != 1) Char(0) else nextEl2?.firstOrNull() ?: Char(0)
            val nextChar3 = if (nextEl2?.length != 1) Char(0) else nextEl3?.firstOrNull() ?: Char(0)

            element.softBreakAfter = element.forceSoftBreakAfter ||
                lastCharOfThisEl in ALWAYS_SOFT_BREAK_AFTER ||
                nextChar1 in ALWAYS_SOFT_BREAK_BEFORE ||
                nextChar2 in NEVER_AT_START_OF_COLUMN ||
                (lastCharOfThisEl == 'て' && nextChar1 != 'て') ||
                (lastCharOfThisEl.isKatakana() && (nextChar1.isHiragana() || nextChar1.isCJKNotKana())) ||
                (nextChar1 == 'で' && nextChar2 == 'す' && nextChar3 == '。') ||
                (lastCharOfThisEl.isHiragana() && lastCharOfThisEl != 'お' && lastCharOfThisEl != 'ご' &&
                    (nextChar1 !in NEVER_AT_START_OF_COLUMN) &&
                    (nextChar1.isCJKNotKana() || nextChar1.isKatakana()))
        }
    }

    companion object {
        private const val MIN_CHARS_FOR_SOFT_BREAK = 2
        private const val ALWAYS_SOFT_BREAK_AFTER = "を、。：；!！?？・」』】〕）"
        private const val ALWAYS_SOFT_BREAK_BEFORE = "「『【〔（"
        private const val NEVER_AT_START_OF_COLUMN = "、。!！?？"
        private const val WHITESPACE = " \t　\n\r"
    }
}
