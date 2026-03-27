package io.github.digorydoo.goigoi.core.tategaki

import ch.digorydoo.kutils.cjk.isCJKNotKana
import ch.digorydoo.kutils.cjk.isHiragana
import ch.digorydoo.kutils.cjk.isKatakana
import ch.digorydoo.kutils.vector.Vector2i
import kotlin.math.max

class TategakiLayout {
    enum class SoftBreak {
        NONE, // no soft break here
        NORMAL, // normal soft break
        FIXED, // normal soft break, and does not change when arrange() is called again
        WEAK, // weak soft break
    }

    class Element(val startIdx: Int, val text: String, val furigana: String) {
        var x = 0
        var y = 0
        var minHeight = 0
        var trimmableHeight = 0
        val fullHeight get() = minHeight + trimmableHeight
        var softBreakAfter = SoftBreak.NONE
    }

    private val elements = mutableListOf<Element>()

    fun clear() = elements.clear()
    fun isEmpty() = elements.isEmpty()
    fun forEachElement(lambda: (Element) -> Unit) = elements.forEach(lambda)

    fun add(idx: Int, char: Char) {
        if (char in WHITESPACE) {
            // Silently drop whitespace, but add a soft break.
            elements.lastOrNull()?.softBreakAfter = SoftBreak.FIXED
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
        var softBreakAfterIdx = -1
        var softBreakIsWeak = true
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

                left -= nextColumnWidth
                top = 0
                firstColumn = false
                softBreakAfterIdx = -1
                softBreakIsWeak = true
                lastBreakBeforeIdx = elementIdx
            } else if (elementIdx - lastBreakBeforeIdx + 1 >= MIN_CHARS_FOR_SOFT_BREAK) {
                when (element.softBreakAfter) {
                    SoftBreak.NONE -> Unit
                    SoftBreak.NORMAL, SoftBreak.FIXED -> {
                        // Make this the new position for a soft break.
                        softBreakAfterIdx = elementIdx
                        softBreakIsWeak = false
                    }
                    SoftBreak.WEAK -> {
                        // Make this the new position for a soft break unless there is a non-weak soft break already.
                        if (softBreakIsWeak) {
                            softBreakAfterIdx = elementIdx
                        }
                    }
                }
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

        val newWidth = -left
        val originX = -left
        val originY = 0
        var newHeight = 0

        elements.forEach {
            it.x += originX
            it.y += originY
            newHeight = max(newHeight, it.y + it.minHeight)
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
            if (element.softBreakAfter == SoftBreak.FIXED) {
                return@forEachIndexed // fixed soft breaks never change
            }

            val lastCharOfThisEl = element.text.lastOrNull() ?: Char(0)
            val prevEl1 = elements.getOrNull(i - 1)?.text

            val prevChar1 =
                if (element.text.length > 1) element.text[element.text.length - 2]
                else prevEl1?.lastOrNull() ?: Char(0)

            val nextEl1 = elements.getOrNull(i + 1)?.text
            val nextEl2 = elements.getOrNull(i + 2)?.text
            val nextEl3 = elements.getOrNull(i + 3)?.text
            val nextEl4 = elements.getOrNull(i + 4)?.text

            val nextChar1 = nextEl1?.firstOrNull() ?: Char(0)
            val nextChar2 = if (nextEl1?.length != 1) Char(0) else nextEl2?.firstOrNull() ?: Char(0)
            val nextChar3 = if (nextEl2?.length != 1) Char(0) else nextEl3?.firstOrNull() ?: Char(0)
            val nextChar4 = if (nextEl3?.length != 1) Char(0) else nextEl4?.firstOrNull() ?: Char(0)

            val normalBreak = lastCharOfThisEl in ALWAYS_SOFT_BREAK_AFTER ||
                nextChar1 in ALWAYS_SOFT_BREAK_BEFORE ||
                (nextChar1 == 'こ' && nextChar2 == 'と' && nextChar3 == Char(0)) ||
                (nextChar1 == 'で' && nextChar2 == 'す' && nextChar3 == '。') ||
                (nextChar1 == 'い' && nextChar2 == 'ま' && nextChar3 == 'す' && nextChar4 == '。') ||
                (nextChar1 == 'し' && nextChar2 == 'っ' && nextChar3 == 'か' && nextChar4 == 'り') ||
                (nextChar1 == 'な' && nextChar2 == 'さ' && nextChar3 == 'い' && nextChar4 == '。') ||
                (nextChar1 == 'ほ' && nextChar2 == 'し' && nextChar3 == 'い' && nextChar4 == '。') ||
                (nextChar1 == 'ま' && nextChar2 == 'し' && nextChar3 == 'た' && nextChar4 == '。') ||
                (nextChar1 == 'ま' && nextChar2 == 'せ' && nextChar3 == 'ん' && nextChar4 == '。') ||
                (prevChar1 == '全' && lastCharOfThisEl == '部') ||
                (lastCharOfThisEl == 'て' && (nextChar1 != 'て' && nextChar1 != 'は')) ||
                (lastCharOfThisEl == 'も' && nextChar1 == 'い' && nextChar2 == 'い') ||
                (lastCharOfThisEl == 'は' && nextChar1 == 'い' && nextChar2 == 'け' && nextChar3 == 'な' &&
                    nextChar4 == 'い') ||
                (lastCharOfThisEl != 'ま' && nextChar1 == 'し' && nextChar2 == 'た' && nextChar3.isCJKNotKana()) ||
                (lastCharOfThisEl.isKatakana() && nextChar1.isCJKNotKana()) ||
                (lastCharOfThisEl.isHiragana() && lastCharOfThisEl != 'お' && lastCharOfThisEl != 'ご' &&
                    (nextChar1 !in NEVER_AT_START_OF_COLUMN) &&
                    (nextChar1.isCJKNotKana() || nextChar1.isKatakana()))

            element.softBreakAfter = when {
                normalBreak -> SoftBreak.NORMAL
                nextChar2 in NEVER_AT_START_OF_COLUMN ||
                    (lastCharOfThisEl.isKatakana() && nextChar1.isHiragana())
                -> SoftBreak.WEAK
                else -> SoftBreak.NONE
            }
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
