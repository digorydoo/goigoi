package io.github.digorydoo.goigoi.db

import ch.digorydoo.kutils.cjk.IntlString

class Topic(val id: String) {
    val name = IntlString()
    var hidden = false

    private val theUnyts = mutableListOf<Unyt>()
    val unyts get() = theUnyts.iterator()
    val size get() = theUnyts.size

    fun add(u: Unyt) = theUnyts.add(u)
    fun remove(u: Unyt) = theUnyts.remove(u)
    fun has(u: Unyt) = theUnyts.find { u == it } != null
    fun findUnytById(id: String) = theUnyts.find { u -> u.id == id }

    fun findUnyt(predicate: (u: Unyt) -> Boolean): Unyt? =
        theUnyts.find(predicate)

    fun findWordById(id: String): Word? {
        for (u in theUnyts) {
            u.findWordById(id)?.let { return it }
        }
        return null
    }
}
