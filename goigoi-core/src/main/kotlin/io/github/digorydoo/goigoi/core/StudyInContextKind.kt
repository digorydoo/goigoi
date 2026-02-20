package io.github.digorydoo.goigoi.core

enum class StudyInContextKind(val id: Int) {
    NOT_REQUIRED(0), // word may be studied without context
    PREFERRED(1), // word without context should be shown less frequently
    REQUIRED(2); // word should never be shown without context

    companion object {
        fun fromInt(id: Int): StudyInContextKind? =
            entries.find { it.id == id }
    }
}
