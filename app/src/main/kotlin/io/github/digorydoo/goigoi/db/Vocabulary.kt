package io.github.digorydoo.goigoi.db

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import io.github.digorydoo.goigoi.BuildConfig
import io.github.digorydoo.goigoi.file.MyWordsUnytFileReader
import io.github.digorydoo.goigoi.file.MyWordsUnytFileWriter
import io.github.digorydoo.goigoi.file.VocabIndexReader
import io.github.digorydoo.goigoi.file.WordFileReader
import io.github.digorydoo.goigoi.stats.Stats
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class Vocabulary private constructor() {
    private val theTopics = mutableListOf<Topic>()
    val topics get() = theTopics.iterator()

    private val _allWordFilenames = mutableListOf<String>()
    val allWordFilenames: List<String> get() = _allWordFilenames

    val myWordsUnyt = Unyt(id = MY_WORDS_UNYT_ID)

    @Suppress("KotlinConstantConditions")
    private val vocabDir: String
        get() = when (BuildConfig.FLAVOR) {
            "japanese_free" -> "voc_ja"
            "french_free" -> "voc_fr"
            else -> throw RuntimeException("Unhandled build flavor: ${BuildConfig.FLAVOR}")
        }

    fun createNewTopic(id: String): Topic {
        if (BuildConfig.DEBUG) {
            require(findTopicById(id) == null) { "Topic id not unique: $id" }
        }

        return Topic(id).also { theTopics.add(it) }
    }

    fun createNewUnyt(topic: Topic, id: String): Unyt {
        if (BuildConfig.DEBUG) {
            require(findUnytById(id) == null) { "Unyt id not unique: $id" }
        }

        return Unyt(id).also { topic.add(it) }
    }

    fun setWordFilenames(filenames: List<String>) {
        _allWordFilenames.clear()
        _allWordFilenames.addAll(filenames)
        _allWordFilenames.sort() // filenames start with the super progressive index
    }

    fun findTopicById(id: String) = theTopics.find { u -> u.id == id }

    fun findUnytById(unytId: String?): Unyt? {
        val id = unytId ?: return null

        if (id == myWordsUnyt.id) {
            return myWordsUnyt
        }

        for (t in theTopics) {
            t.findUnytById(id)?.let { return it }
        }

        return null
    }

    /**
     * Use this function when the unyt may not be loaded. An unloaded unyt doesn't know the wordIds, but it always
     * knows the filenames.
     */
    fun findFirstUnytContainingWordWithSameFile(word: Word): Unyt =
        findUnyt { it.hasWordWithFilename(word.filename) }
            ?: run {
                Log.w(TAG, "Cannot find original unyt of word $word, passing back myWordsUnyt instead")
                myWordsUnyt
            }

    private fun findUnyt(predicate: (u: Unyt) -> Boolean): Unyt? {
        for (topic in theTopics) {
            val u = topic.findUnyt(predicate)
            if (u != null) return u
        }

        return null
    }

    fun findWordById(wordId: String): Word? {
        for (t in theTopics) {
            t.findWordById(wordId)?.let { return it }
        }

        // If the caller is showing myWordsUnyt, the original unyt may not be loaded, so let's have a look here, too.
        return myWordsUnyt.findWordById(wordId)
    }

    fun loadUnytIfNecessary(unyt: Unyt, ctx: Context) {
        if (unyt.numWordsAvailable > 0 && unyt.numWordsLoaded == 0) {
            Log.d(TAG, "Loading unyt including words: ${unyt.name.en}")

            unyt.wordFilenames.forEach { filename ->
                val word = loadWordFile(filename, ctx)

                if (word != null) {
                    unyt.add(word)
                } else {
                    Log.e(TAG, "Failed to load word file: $filename")
                }
            }
        }
    }

    fun loadWordFile(filename: String, ctx: Context): Word? {
        val word = Word()
        word.filename = filename
        val path = "$vocabDir/word/$filename"

        try {
            ctx.assets.open(path).use { stream ->
                WordFileReader(stream, word).read()
            }
            return word
        } catch (_: IOException) {
            return null
        }
    }

    private fun loadVocab(ctx: Context) {
        val path = "$vocabDir/index.voc"

        try {
            ctx.assets.open(path).use { stream ->
                VocabIndexReader(stream, this).read()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("Failed to load vocab: $path")
        }

        loadOrCreateMyWordsUnyt(ctx)
    }

    private fun loadOrCreateMyWordsUnyt(ctx: Context) {
        myWordsUnyt.apply {
            name.en = "My words"
            name.de = "Meine Wörter"
            name.fr = "Mes mots"
            name.it = "Parole mie"

            @Suppress("KotlinConstantConditions")
            when (BuildConfig.FLAVOR) {
                "japanese_free" -> {
                    studyLang = "ja"
                    hasRomaji = true
                    hasFurigana = true
                }
                "french_free" -> {
                    studyLang = "fr"
                    hasRomaji = false
                    hasFurigana = false
                }
                else -> throw RuntimeException("Unhandled build flavor: ${BuildConfig.FLAVOR}")
            }
        }

        try {
            ctx.openFileInput(MY_WORDS_UNYT_FILE_NAME).use { stream ->
                loadMyWordsUnyt(stream, ctx)
            }
        } catch (_: FileNotFoundException) {
            Log.d(TAG, "My words unyt does not exist")
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}\n${e.stackTraceToString()}")
        }
    }

    private fun loadMyWordsUnyt(stream: InputStream, ctx: Context) {
        Log.d(TAG, "Reading my words unyt file")
        MyWordsUnytFileReader(myWordsUnyt, stream).read()

        if (myWordsUnyt.wordFilenames.isEmpty()) {
            Log.w(TAG, "Loaded an empty my words unyt")
        } else {
            val toBeRemoved = mutableListOf<String>()

            myWordsUnyt.wordFilenames.forEach { filename ->
                val word = loadWordFile(filename, ctx)

                if (word != null) {
                    myWordsUnyt.add(word)
                } else {
                    // This can happen if myWordsUnyt contains the filename of a word that no longer exists.
                    Log.w(TAG, "Removing word from my words unyt as we failed to load it: $filename")
                    toBeRemoved.add(filename)
                }
            }

            // Actually remove the words only if some words could be loaded. If all of them failed to load,
            // there is probably a bug (during development), and we don't want to lose the list.

            if (toBeRemoved.size < myWordsUnyt.wordFilenames.size) {
                myWordsUnyt.wordFilenames.removeAll(toBeRemoved)
            }

            Log.d(TAG, "My words unyt: avail=${myWordsUnyt.numWordsAvailable}, loaded=${myWordsUnyt.numWordsLoaded}")
        }
    }

    fun writeMyWordsUnytIfNecessary(ctx: Context) {
        if (!myWordsUnyt.modified) return

        try {
            Log.d(TAG, "Writing my words unyt file")
            ctx.openFileOutput(MY_WORDS_UNYT_FILE_NAME, MODE_PRIVATE).use {
                MyWordsUnytFileWriter(myWordsUnyt, it).write()
            }
            myWordsUnyt.modified = false

            val stats = Stats.getSingleton(ctx)
            stats.setUnytStudyMoment(myWordsUnyt)
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}\n${e.stackTraceToString()}")
        }
    }

    companion object {
        private var singleton: Vocabulary? = null
        private const val TAG = "Vocabulary"
        private const val MY_WORDS_UNYT_FILE_NAME = "my_words_unyt.voc"
        private const val MY_WORDS_UNYT_ID = "__my_words_unyt__"

        fun getSingleton(ctx: Context) =
            singleton ?: Vocabulary().also {
                singleton = it
                it.loadVocab(ctx)
            }

        fun hasSingleton() = singleton != null
    }
}
