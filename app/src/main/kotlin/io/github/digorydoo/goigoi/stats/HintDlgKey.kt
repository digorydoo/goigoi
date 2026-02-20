package io.github.digorydoo.goigoi.stats

enum class HintDlgKey(val key: String) {
    RATE_US_REQUEST_SHOWN("rate-us"),
    FIRST_TIME_EXTENDED_KEYBOARD_SHOWN("extkb-1st"),
    FIRST_TIME_EXTKB_SMALL_KANA("extkb-smallkana"),
    FIRST_TIME_EXTKB_DAKUTEN_HANDAKUTEN("extkb-dakuten");

    override fun toString() = key
}
