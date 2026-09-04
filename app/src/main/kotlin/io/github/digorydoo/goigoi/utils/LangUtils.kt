package io.github.digorydoo.goigoi.utils

import android.content.Context
import io.github.digorydoo.goigoi.R

object LangUtils {
    private var languageNames: Array<String>? = null
    private var languageIds: Array<String>? = null

    private fun loadLanguages(ctx: Context) {
        if (languageNames != null) {
            return  // already loaded
        }

        languageIds = ResUtils.getStringArray(R.array.language_ids, ctx)
        languageNames = ResUtils.getStringArray(R.array.language_names, ctx)
    }

    fun humanizeLangId(langId: String, ctx: Context): String {
        if (languageNames == null) {
            loadLanguages(ctx)
        }

        for (i in languageIds!!.indices) {
            if (languageIds!![i] == langId) {
                return languageNames!![i]
            }
        }

        return "?$langId"
    }
}
