package io.github.digorydoo.goigoi.view.tategaki

import ch.digorydoo.kutils.vector.Vector2i
import kotlin.math.max

class TategakiLayout {
    class Element(val startIdx: Int, val text: String, val furigana: String) {
        var x = 0
        var y = 0
        var minHeight = 0
        var trimmableHeight = 0
        val fullHeight get() = minHeight + trimmableHeight
    }

    private val elements = mutableListOf<Element>()

    fun clear() = elements.clear()
    fun isEmpty() = elements.isEmpty()
    fun forEachElement(lambda: (Element) -> Unit) = elements.forEach(lambda)

    fun add(idx: Int, char: Char) {
        if (isVisible(char)) {
            elements.add(Element(idx, "$char", ""))
        }
    }

    fun add(idx: Int, text: String, furigana: String) {
        elements.add(Element(idx, text, furigana))
    }

    fun arrangeElements(
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
        var thisColumnTrimmable = 0
        var pastMaxColumnHeight = 0

        elements.forEach { element ->
            measure(element, charHeight, letterSpacing) // sets minHeight and trimmableHeight
            val breakHere = top > 0 && top + element.minHeight > maxHeight

            if (breakHere) {
                if (firstColumn) {
                    if (lastElWithFurigana == null) {
                        // No column has any furigana
                        nextColumnWidth = charWidth + emptyFuriganaWidth + columnSpacing
                    } else if (element.startIdx > lastElWithFurigana.startIdx) {
                        // This column had furigana, but none of the columns that follow have any.
                        nextColumnWidth = charWidth + emptyFuriganaWidth + columnSpacing
                    } else {
                        // Furigana still follows, so columns distances must be slightly larger.
                        nextColumnWidth = charWidth + furiganaXDelta + furiganaCharWidth + columnSpacing
                    }
                }

                pastMaxColumnHeight = max(pastMaxColumnHeight, top - thisColumnTrimmable)
                left -= nextColumnWidth
                top = 0
                firstColumn = false
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
            thisColumnTrimmable = element.trimmableHeight
        }

        val newHeight = max(pastMaxColumnHeight, top - thisColumnTrimmable)

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

    companion object {
        /**
         * Even though TategakiView can handle spaces, it generally looks better if we omit them.
         */
        private fun isVisible(c: Char) = when (c) {
            ' ', '　' -> false
            else -> true
        }
    }
}
