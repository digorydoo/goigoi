package io.github.digorydoo.goigoi.study

enum class Answer(val intValue: Int) {
    NONE(0), CORRECT(1), WRONG(2), CORRECT_EXCEPT_KANA_SIZE(3), TRIVIAL(4), SKIP(5);

    companion object {
        fun fromIntOrNull(i: Int) =
            entries.firstOrNull { it.intValue == i }
    }
}
