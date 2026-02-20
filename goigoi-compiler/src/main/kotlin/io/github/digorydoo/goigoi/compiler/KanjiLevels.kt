package io.github.digorydoo.goigoi.compiler

import ch.digorydoo.kutils.cjk.JLPTLevel

class KanjiLevels {
    val n5 = mutableSetOf<Char>()
    val n4 = mutableSetOf<Char>()
    val n3 = mutableSetOf<Char>()
    val n2 = mutableSetOf<Char>()
    val n1 = mutableSetOf<Char>()
    val other = mutableSetOf<Char>()

    fun get(lvl: JLPTLevel?) = when (lvl) {
        JLPTLevel.N5 -> n5
        JLPTLevel.N4 -> n4
        JLPTLevel.N3 -> n3
        JLPTLevel.N2 -> n2
        JLPTLevel.N1 -> n1
        JLPTLevel.Nx -> other
        null -> other
    }

    fun findLevelOf(c: Char) = when {
        n5.contains(c) -> JLPTLevel.N5
        n4.contains(c) -> JLPTLevel.N4
        n3.contains(c) -> JLPTLevel.N3
        n2.contains(c) -> JLPTLevel.N2
        n1.contains(c) -> JLPTLevel.N1
        other.contains(c) -> JLPTLevel.Nx
        else -> null
    }
}
