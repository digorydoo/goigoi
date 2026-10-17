package io.github.digorydoo.goigoi.core.list

class UnytListItemData(
    val name: String,
    val numWords: Int,
    val studyMomentAsText: String?, // null = not studied yet
    val isMyWordsUnyt: Boolean,
    val progress: Float, // null = do not display progress
    val rating: Float, // null = do not display rating
    val asleep: Boolean, // true = not studied for a long time
) {
    constructor(): this(
        name = "",
        numWords = 0,
        studyMomentAsText = null,
        isMyWordsUnyt = false,
        progress = 0f,
        rating = 0f,
        asleep = false
    )
}
