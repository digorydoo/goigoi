package io.github.digorydoo.goigoi.bottom_sheet

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.R
import io.github.digorydoo.goigoi.db.KanjiIndex
import io.github.digorydoo.goigoi.db.Phrase
import io.github.digorydoo.goigoi.db.Unyt
import io.github.digorydoo.goigoi.db.Word
import io.github.digorydoo.goigoi.db.WordLink
import io.github.digorydoo.goigoi.dialog.WordCtxMenu
import io.github.digorydoo.goigoi.furigana.FuriganaBuilder
import io.github.digorydoo.goigoi.furigana.buildSpan
import io.github.digorydoo.goigoi.helper.MyLayoutInflater
import io.github.digorydoo.goigoi.stats.Stats
import io.github.digorydoo.goigoi.utils.LangUtils
import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.cjk.Unicode
import java.util.Locale
import kotlin.math.roundToInt

class SheetContent(
    private val bindings: Bindings,
    private val word: Word,
    private val unyt: Unyt,
    private val delegate: Delegate,
) {
    interface Delegate: WordCtxMenu.Callback {
        fun dismissSheet()
        fun showInBrowser(uri: Uri)
    }

    fun insertContent(inflater: LayoutInflater, ctx: Context) {
        val myInflater = MyLayoutInflater(inflater, bindings.content)
        val kanjiIndex = KanjiIndex.getSingleton(ctx)

        insertBasicWordInfo(ctx, kanjiIndex)
        insertWordStats(ctx)
        insertPhrasesAndSentences(myInflater)
        insertDictionaryLinks(ctx, myInflater)
        insertKanjiDetailsLinks(ctx, myInflater)
        insertSeeAlso(ctx, myInflater)
        insertWordCtxMenuItems(ctx, myInflater)
        insertOtherLanguages(ctx, myInflater)

        if (BuildConfig.DEBUG) {
            insertDebugInfo(ctx, myInflater)
        }
    }

    private fun insertBasicWordInfo(ctx: Context, kanjiIndex: KanjiIndex) {
        val showKanaAsPrimary = when {
            word.kana.isEmpty() -> false
            word.kana == word.primaryForm.raw -> false
            word.usuallyInKana -> true
            else -> false
        }

        bindings.primaryForm.text = when {
            word.primaryForm.isEmpty() -> "?"
            showKanaAsPrimary -> word.kana
            else -> word.primaryForm.buildSpan()
        }

        bindings.secondaryForm.apply {
            if (word.romaji.isEmpty()) {
                visibility = View.GONE
            } else {
                text = word.romaji
            }
        }

        bindings.sometimesWrittenAs.apply {
            if (showKanaAsPrimary && word.usuallyInKana) {
                val tmpl = ctx.getString(R.string.sometimes_with_kanji)
                text = tmpl.replace("\${kanji}", word.kanji)
            } else {
                visibility = View.GONE
            }
        }

        bindings.kanjiMoreDifficult.apply {
            when {
                word.usuallyInKana || word.kanji == word.kana -> visibility = View.GONE
                else -> {
                    val unytLevel = unyt.levelOfMostDifficultWord
                    when (unytLevel) {
                        JLPTLevel.Nx, JLPTLevel.N1 -> visibility = View.GONE
                        else -> {
                            val kanjiLevel = kanjiIndex.levelOfMostDifficultKanji(word.kanji) ?: JLPTLevel.N5
                            when {
                                !kanjiLevel.isMoreDifficultThan(unytLevel) -> visibility = View.GONE
                                else -> {
                                    text = ctx.getString(R.string.kanji_more_difficult)
                                        .replace("\${level}", unytLevel.toPrettyString())
                                        .replace("\${kana}", word.kana)
                                }
                            }
                        }
                    }
                }
            }
        }

        bindings.translation.apply {
            text = word.translation.withSystemLang
                .takeIf { it.isNotEmpty() }
                ?: "?"
        }

        bindings.hint.apply {
            word.hintsWithSystemLang.let { hint ->
                if (hint.isEmpty()) {
                    visibility = View.GONE
                } else {
                    text = hint
                }
            }
        }

        bindings.jlptLevel.apply {
            val level = word.level
            if (level == null || level == JLPTLevel.Nx) {
                visibility = View.GONE
            } else {
                text = level.toPrettyString()
            }
        }

        word.cats.joinToString(" ") { "#${it.text}" }
            .let { cats ->
                if (cats.isEmpty()) {
                    bindings.wordCategories.visibility = View.GONE
                } else {
                    bindings.wordCategories.text = cats
                }
            }
    }

    private fun insertWordStats(ctx: Context) {
        val stats = Stats.getSingleton(ctx)
        val seenCount = stats.getWordTotalSeenCount(word)
        val progress = stats.getWordStudyProgress(word)
        val rating = stats.getWordTotalRating(word)

        var txt: String

        if (seenCount <= 0) {
            txt = ctx.getString(R.string.not_studied_yet)
        } else {
            txt = ctx.getString(R.string.studied_n_times)
            txt = txt.replace("\${N}", "" + seenCount)

            if (progress < 1.0f) {
                txt += "\n${ctx.getString(R.string.progress)}:"
                txt += " ${(100.0f * progress).roundToInt()}%"
            } else {
                txt += "\n${ctx.getString(R.string.rating)}:"
                txt += " ${(100.0f * rating).roundToInt()}%"
            }
        }

        bindings.wordStats.text = txt
    }

    private fun insertWordCtxMenuItems(ctx: Context, inflater: MyLayoutInflater) {
        inflater.insertSubheader(R.string.functions)

        val menu = WordCtxMenu(unyt, word).apply {
            createItems(ctx)
            callback = delegate
        }

        menu.items.forEach { item ->
            inflater.insertItem(item.imgResId, item.text) {
                delegate.dismissSheet()
                menu.doAction(item.action, ctx)
            }
        }
    }

    private fun insertDictionaryLinks(ctx: Context, inflater: MyLayoutInflater) {
        DictionaryLinksBuilder(word, unyt.studyLang).build(ctx, inflater) { uri ->
            delegate.showInBrowser(uri)
        }
    }

    private fun insertKanjiDetailsLinks(ctx: Context, inflater: MyLayoutInflater) {
        KanjiDetailsLinksBuilder(word).build(ctx, inflater) { uri ->
            delegate.showInBrowser(uri)
        }
    }

    private fun insertPhrasesAndSentences(inflater: MyLayoutInflater) {
        if (word.phrases.isEmpty() && word.sentences.isEmpty()) {
            return
        }

        inflater.insertSubheader(R.string.examples)

        word.phrases.forEach { insert1Phrase(it, inflater) }
        word.sentences.forEach { insert1Phrase(it, inflater) }
    }

    private fun insert1Phrase(ph: Phrase, inflater: MyLayoutInflater) {
        inflater.insertItemWithFurigana(
            ph.primaryForm.buildSpan(),
            ph.translation.withSystemLang,
            FuriganaBuilder.buildSpan(ph.explanation.withSystemLang)
        )
    }

    private fun insertSeeAlso(ctx: Context, inflater: MyLayoutInflater) {
        if (word.links.isEmpty()) {
            return
        }

        val systemLang = Locale.getDefault().isO3Language ?: "" // eng, ger
        val similarMeaning = ctx.getString(R.string.similar_meaning)
        var didEmitSubheader = false

        // De-duplicate links as compileGoigoi de-duplicates them only within the same kind
        val linksEmitted = mutableSetOf<String>()

        word.links.forEach { link ->
            if (linksEmitted.contains(link.wordId)) {
                Log.d(TAG, "Not showing duplicate link to ${link.wordId}: ${link.kind}")
            } else {
                val text = when (link.kind) {
                    WordLink.Kind.SAME_READING -> ctx.getString(R.string.same_reading)
                    WordLink.Kind.SAME_KANJI -> ctx.getString(R.string.same_kanji)
                    WordLink.Kind.SAME_EN_TRANSLATION -> if (systemLang == "eng") similarMeaning else null
                    WordLink.Kind.SAME_DE_TRANSLATION -> if (systemLang == "ger") similarMeaning else null
                    WordLink.Kind.CLOSELY_RELATED -> ctx.getString(R.string.closely_related)
                    WordLink.Kind.KEEP_APART -> null
                    WordLink.Kind.TRANSITIVE_VERB -> ctx.getString(R.string.transitive_verb)
                    WordLink.Kind.INTRANSITIVE_VERB -> ctx.getString(R.string.intransitive_verb)
                    WordLink.Kind.NOUN -> ctx.getString(R.string.noun)
                    WordLink.Kind.VERB -> ctx.getString(R.string.verb)
                    WordLink.Kind.ADJECTIVE -> ctx.getString(R.string.adjective)
                    WordLink.Kind.ANTONYM -> ctx.getString(R.string.antonym)
                    null -> null
                }

                if (text != null) {
                    if (!didEmitSubheader) {
                        inflater.insertSubheader(R.string.see_also)
                        didEmitSubheader = true
                    }

                    inflater.insertItemWithFurigana(
                        FuriganaBuilder.buildSpan(link.primaryForm),
                        "${link.translation.withSystemLang} ${Unicode.MIDDLE_DOT} $text"
                    )
                    linksEmitted.add(link.wordId)
                }
            }
        }
    }

    private fun insertOtherLanguages(ctx: Context, inflater: MyLayoutInflater) {
        class Triplet(
            val translation: String,
            val hint: String,
            val langId: String,
        )

        val sysTranslation = word.translation.withSystemLang

        val languages = word.translation.availableLanguages()
            .map { langId ->
                Triplet(
                    word.translation.withLanguage(langId),
                    word.hintsWithLanguage(langId),
                    langId
                )
            }
            .filter { it.translation != sysTranslation }

        if (languages.isNotEmpty()) {
            inflater.insertSubheader(R.string.other_languages)

            languages.forEach { lang ->
                inflater.insertItem(
                    lang.translation,
                    arrayOf(LangUtils.humanizeLangId(lang.langId, ctx), lang.hint)
                        .filter { it.isNotEmpty() }
                        .joinToString(" ${Unicode.MIDDLE_DOT} "),
                )
            }
        }
    }

    private fun insertDebugInfo(ctx: Context, inflater: MyLayoutInflater) {
        val stats = Stats.getSingleton(ctx)
        inflater.insertSubheader("DEBUG")
        inflater.insertItemMultiLine("Word", stats.getDebugInfo(word))
        inflater.insertItemMultiLine("Unyt", "${unyt.name.en}\n" + stats.getDebugInfo(unyt))
    }

    companion object {
        private const val TAG = "SheetContent"
    }
}
