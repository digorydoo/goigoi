package io.github.digorydoo.goigoi.activity.flipthru.fragment

import android.os.Bundle

class FlipThruFragmentParams(
    val unytId: String,
    val wordId: String,
    val phraseIdx: Int,
    val sentenceIdx: Int,
    val studyPrimaryForm: Boolean,
    val studyRomaji: Boolean,
    val studyFurigana: Boolean,
    val studyTranslation: Boolean,
) {
    fun toBundle() =
        Bundle().apply {
            putString(UNYT_ID_KEY, unytId)
            putString(WORD_ID_KEY, wordId)
            putInt(PHRASE_IDX_KEY, phraseIdx)
            putInt(SENTENCE_IDX_KEY, sentenceIdx)
            putBoolean(STUDY_PRIMARY_FORM_KEY, studyPrimaryForm)
            putBoolean(STUDY_ROMAJI_KEY, studyRomaji)
            putBoolean(STUDY_FURIGANA_KEY, studyFurigana)
            putBoolean(STUDY_TRANSLATION_KEY, studyTranslation)
        }

    companion object {
        private const val UNYT_ID_KEY = "unytId"
        private const val WORD_ID_KEY = "wordId"
        private const val PHRASE_IDX_KEY = "phraseIdx"
        private const val SENTENCE_IDX_KEY = "sentenceIdx"
        private const val STUDY_PRIMARY_FORM_KEY = "studyPrimaryForm"
        private const val STUDY_ROMAJI_KEY = "studyRomaji"
        private const val STUDY_FURIGANA_KEY = "studyFurigana"
        private const val STUDY_TRANSLATION_KEY = "studyTranslation"

        fun fromBundle(bundle: Bundle) = FlipThruFragmentParams(
            unytId = bundle.getString(UNYT_ID_KEY) ?: "",
            wordId = bundle.getString(WORD_ID_KEY) ?: "",
            phraseIdx = bundle.getInt(PHRASE_IDX_KEY, -1),
            sentenceIdx = bundle.getInt(SENTENCE_IDX_KEY, -1),
            studyPrimaryForm = bundle.getBoolean(STUDY_PRIMARY_FORM_KEY, true),
            studyRomaji = bundle.getBoolean(STUDY_ROMAJI_KEY, false),
            studyFurigana = bundle.getBoolean(STUDY_FURIGANA_KEY, true),
            studyTranslation = bundle.getBoolean(STUDY_TRANSLATION_KEY, false),
        )
    }
}
