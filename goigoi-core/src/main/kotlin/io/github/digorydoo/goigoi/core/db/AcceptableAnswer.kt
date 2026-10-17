package io.github.digorydoo.goigoi.core.db

class AcceptableAnswer(
    var answer: String, // kanji and/or kana; not a FuriganaString
    var inaccurate: Boolean, // true=we'll accept the answer, but tell the user it's inaccurate
    var interchangeable: Boolean, // true=allow using this variant in phrases and sentences, too
) {
    constructor(): this("", false, false)
}
