package io.github.digorydoo.goigoi.helper

import android.content.Context
import androidx.core.content.edit

class UserPrefs private constructor(ctx: Context) {
    enum class WordListItemMode { SHOW_KANA, SHOW_ROMAJI, SHOW_TRANSLATION }

    private val prefsFile = ctx.getSharedPreferences(PREFS_FILE_ID, Context.MODE_PRIVATE)

    var wordListItemMode: WordListItemMode
        get() = when (getValue(WORD_LIST_ITEM_MODE)) {
            "s" -> WordListItemMode.SHOW_KANA
            "r" -> WordListItemMode.SHOW_ROMAJI
            "t" -> WordListItemMode.SHOW_TRANSLATION
            else -> WordListItemMode.SHOW_KANA
        }
        set(newMode) {
            val s = when (newMode) {
                WordListItemMode.SHOW_KANA -> "s"
                WordListItemMode.SHOW_ROMAJI -> "r"
                WordListItemMode.SHOW_TRANSLATION -> "t"
            }

            setValue(WORD_LIST_ITEM_MODE, s)
        }

    var darkMode: Boolean
        get() = getBoolean(DARK_MODE) ?: false
        set(b) = setBoolean(DARK_MODE, b)

    private fun getValue(key: String): String? {
        return prefsFile.getString(key, null)
    }

    private fun setValue(key: String, value: String?) {
        prefsFile.edit {
            putString(key, value)
        }
    }

    private fun getBoolean(key: String): Boolean? {
        val s = getValue(key)
        return when (s) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }

    private fun setBoolean(key: String, b: Boolean) {
        setValue(key, if (b) "true" else "false")
    }

    companion object {
        private const val PREFS_FILE_ID = "prefs"
        private const val DARK_MODE = "darkMode"
        private const val WORD_LIST_ITEM_MODE = "wordListItemMode"

        private var singleton: UserPrefs? = null

        fun getSingleton(ctx: Context) =
            singleton ?: UserPrefs(ctx).also { singleton = it }

        fun hasSingleton() = singleton != null
    }
}
