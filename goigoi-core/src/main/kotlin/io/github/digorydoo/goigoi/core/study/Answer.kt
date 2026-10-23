package io.github.digorydoo.goigoi.core.study

enum class Answer(val intValue: Int) {
    NONE(0), CORRECT(1), WRONG(2), CORRECT_BUT_INACCURATE(3), CORRECT_EXCEPT_KANA_SIZE(4), TRIVIAL(5);

    companion object {
        fun fromIntOrNull(i: Int) =
            entries.firstOrNull { it.intValue == i }
    }
}
