package io.github.digorydoo.goigoi.core.tategaki

import kotlin.test.Test
import kotlin.test.assertEquals

internal class TategakiLayoutTest {
    private fun addSomeChars(layout: TategakiLayout) {
        layout.apply {
            add(0, "私", "わたし")
            add(1, 'は')
            add(2, "元", "げん")
            add(3, "気", "き")
            add(4, 'に')
            add(5, ' ')
            add(6, 'や')
            add(7, 'っ')
            add(8, 'て')
            add(9, 'い')
            add(10, 'ま')
            add(11, 'す')
            add(12, '。')
        }
    }

    private fun serialize(layout: TategakiLayout): String {
        var result = ""
        layout.forEachElement {
            result += "【${it.text}：${it.furigana}】x=${it.x}, y=${it.y}, h=${it.fullHeight}\n"
        }
        return result.trim()
    }

    @Test
    fun `should arrange chars in a single column when there is room`() {
        val layout = TategakiLayout()
        addSomeChars(layout)
        layout.arrange(
            charWidth = 10,
            charHeight = 10,
            maxHeight = 1000,
            columnSpacing = 1,
            letterSpacing = 1,
            furiganaCharWidth = 5,
            furiganaXDelta = 1,
            emptyFuriganaWidth = 2,
        )
        val s = serialize(layout)
        assertEquals(
            """
            【私：わたし】x=0, y=0, h=11
            【は：】x=0, y=11, h=11
            【元：げん】x=0, y=22, h=11
            【気：き】x=0, y=33, h=11
            【に：】x=0, y=44, h=11
            【や：】x=0, y=55, h=11
            【っ：】x=0, y=66, h=11
            【て：】x=0, y=77, h=11
            【い：】x=0, y=88, h=11
            【ま：】x=0, y=99, h=11
            【す：】x=0, y=110, h=11
            【。：】x=0, y=121, h=11
            """.trimIndent().trim(),
            s
        )
    }

    @Test
    fun `should not put the punctuation on the start of a new column`() {
        val layout = TategakiLayout()
        addSomeChars(layout)
        layout.arrange(
            charWidth = 10,
            charHeight = 10,
            maxHeight = 120,
            columnSpacing = 1,
            letterSpacing = 1,
            furiganaCharWidth = 5,
            furiganaXDelta = 1,
            emptyFuriganaWidth = 2,
        )
        val s = serialize(layout)
        assertEquals(
            """
            【私：わたし】x=13, y=0, h=11
            【は：】x=13, y=11, h=11
            【元：げん】x=13, y=22, h=11
            【気：き】x=13, y=33, h=11
            【に：】x=13, y=44, h=11
            【や：】x=13, y=55, h=11
            【っ：】x=13, y=66, h=11
            【て：】x=13, y=77, h=11
            【い：】x=13, y=88, h=11
            【ま：】x=13, y=99, h=11
            【す：】x=0, y=0, h=11
            【。：】x=0, y=11, h=11
            """.trimIndent().trim(),
            s
        )
    }

    @Test
    fun `should use implicit soft break at space`() {
        val layout = TategakiLayout()
        addSomeChars(layout)
        layout.arrange(
            charWidth = 10,
            charHeight = 10,
            maxHeight = 98,
            columnSpacing = 1,
            letterSpacing = 1,
            furiganaCharWidth = 5,
            furiganaXDelta = 1,
            emptyFuriganaWidth = 2,
        )
        val s = serialize(layout)
        assertEquals(
            """
            【私：わたし】x=13, y=0, h=11
            【は：】x=13, y=11, h=11
            【元：げん】x=13, y=22, h=11
            【気：き】x=13, y=33, h=11
            【に：】x=13, y=44, h=11
            【や：】x=0, y=0, h=11
            【っ：】x=0, y=11, h=11
            【て：】x=0, y=22, h=11
            【い：】x=0, y=33, h=11
            【ま：】x=0, y=44, h=11
            【す：】x=0, y=55, h=11
            【。：】x=0, y=66, h=11
            """.trimIndent().trim(),
            s
        )
    }

    @Test
    fun `should use implicit soft break at kana-kanji transition`() {
        val layout = TategakiLayout()
        addSomeChars(layout)
        layout.arrange(
            charWidth = 10,
            charHeight = 10,
            maxHeight = 42,
            columnSpacing = 1,
            letterSpacing = 1,
            furiganaCharWidth = 5,
            furiganaXDelta = 1,
            emptyFuriganaWidth = 2,
        )
        val s = serialize(layout)
        assertEquals(
            """
            【私：わたし】x=51, y=0, h=11
            【は：】x=51, y=11, h=11
            【元：げん】x=34, y=0, h=11
            【気：き】x=34, y=11, h=11
            【に：】x=34, y=22, h=11
            【や：】x=17, y=0, h=11
            【っ：】x=17, y=11, h=11
            【て：】x=17, y=22, h=11
            【い：】x=0, y=0, h=11
            【ま：】x=0, y=11, h=11
            【す：】x=0, y=22, h=11
            【。：】x=0, y=33, h=11
            """.trimIndent().trim(),
            s
        )
    }
}
