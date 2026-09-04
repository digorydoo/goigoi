package io.github.digorydoo.goigoi.core.file

import kotlin.test.Test
import kotlin.test.assertEquals

internal class GoigoiFileMarkerTest {
    @Test
    fun `should have unique values`() {
        val map = mutableMapOf<UShort, GoigoiFileMarker>()
        val problems = mutableListOf<String>()

        for (marker in GoigoiFileMarker.entries) {
            val existing = map[marker.value]
            if (existing != null) problems.add("Value not unique: $existing vs $marker")
            map[marker.value] = marker
        }

        assertEquals("\n", "\n" + problems.joinToString("\n"))
    }
}
