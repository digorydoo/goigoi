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
            maxHeight = 32,
            columnSpacing = 1,
            letterSpacing = 1,
            furiganaCharWidth = 5,
            furiganaXDelta = 1,
            emptyFuriganaWidth = 2,
        )
        val s = serialize(layout)
        assertEquals(
            """
            【私：わたし】x=68, y=0, h=11
            【は：】x=68, y=11, h=11
            【元：げん】x=51, y=0, h=11
            【気：き】x=51, y=11, h=11
            【に：】x=51, y=22, h=11
            【や：】x=34, y=0, h=11
            【っ：】x=34, y=11, h=11
            【て：】x=34, y=22, h=11
            【い：】x=17, y=0, h=11
            【ま：】x=17, y=11, h=11
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
            maxHeight = 50,
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

    @Test
    fun `should make the expected layout when suffix is mashita`() {
        val layout = TategakiLayout().apply {
            add(0, "殴", "なぐ")
            add(1, 'ら')
            add(2, 'れ')
            add(3, 'ま')
            add(4, 'し')
            add(5, 'た')
            add(6, '。')
            arrange(
                charWidth = 10,
                charHeight = 10,
                maxHeight = 60,
                columnSpacing = 1,
                letterSpacing = 1,
                furiganaCharWidth = 5,
                furiganaXDelta = 1,
                emptyFuriganaWidth = 2,
            )
        }
        val s = serialize(layout)
        assertEquals(
            """
            【殴：なぐ】x=13, y=0, h=11
            【ら：】x=13, y=11, h=11
            【れ：】x=13, y=22, h=11
            【ま：】x=0, y=0, h=11
            【し：】x=0, y=11, h=11
            【た：】x=0, y=22, h=11
            【。：】x=0, y=33, h=11
            """.trimIndent().trim(),
            s
        )
    }

    @Test
    fun `should insert soft break at koto wo`() {
        val layout = TategakiLayout().apply {
            add(0, 'あ')
            add(1, 'の')
            add(2, 'ひ')
            add(3, 'の')
            add(4, 'こ')
            add(5, 'と')
            add(6, 'を')
            add(7, 'お')
            add(8, 'も')
            add(9, 'い')
            add(10, 'だ')
            add(11, 'す')
            arrange(
                charWidth = 10,
                charHeight = 10,
                maxHeight = 110,
                columnSpacing = 1,
                letterSpacing = 1,
                furiganaCharWidth = 5,
                furiganaXDelta = 1,
                emptyFuriganaWidth = 2,
            )
        }
        val s = serialize(layout)
        assertEquals(
            """
            【あ：】x=13, y=0, h=11
            【の：】x=13, y=11, h=11
            【ひ：】x=13, y=22, h=11
            【の：】x=13, y=33, h=11
            【こ：】x=13, y=44, h=11
            【と：】x=13, y=55, h=11
            【を：】x=13, y=66, h=11
            【お：】x=0, y=0, h=11
            【も：】x=0, y=11, h=11
            【い：】x=0, y=22, h=11
            【だ：】x=0, y=33, h=11
            【す：】x=0, y=44, h=11
            """.trimIndent().trim(),
            s
        )
    }

    @Test
    fun `should insert soft break between ga and arimasu`() {
        val layout = TategakiLayout().apply {
            add(0, 'ち')
            add(1, 'ず')
            add(2, 'が')
            add(3, 'あ')
            add(4, 'り')
            add(5, 'ま')
            add(6, 'す')
            add(7, 'か')
            arrange(
                charWidth = 10,
                charHeight = 10,
                maxHeight = 70,
                columnSpacing = 1,
                letterSpacing = 1,
                furiganaCharWidth = 5,
                furiganaXDelta = 1,
                emptyFuriganaWidth = 2,
            )
        }
        val s = serialize(layout)
        assertEquals(
            """
            【ち：】x=13, y=0, h=11
            【ず：】x=13, y=11, h=11
            【が：】x=13, y=22, h=11
            【あ：】x=0, y=0, h=11
            【り：】x=0, y=11, h=11
            【ま：】x=0, y=22, h=11
            【す：】x=0, y=33, h=11
            【か：】x=0, y=44, h=11
            """.trimIndent().trim(),
            s
        )
    }
}
