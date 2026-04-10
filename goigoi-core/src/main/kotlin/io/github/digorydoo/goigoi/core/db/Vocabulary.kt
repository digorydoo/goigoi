package io.github.digorydoo.goigoi.core.db

import ch.digorydoo.kutils.logging.Log
import io.github.digorydoo.goigoi.core.file.AssetsAccessor
import io.github.digorydoo.goigoi.core.file.MyWordsUnytFileReader
import io.github.digorydoo.goigoi.core.file.MyWordsUnytFileWriter
import io.github.digorydoo.goigoi.core.file.VocabIndexReader
import io.github.digorydoo.goigoi.core.file.WordFileReader
import io.github.digorydoo.goigoi.core.stats.Stats
import io.github.digorydoo.goigoi.core.utils.Flavour
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

class Vocabulary(private val flavour: Flavour, private val assets: AssetsAccessor, private val stats: Stats) {
    private val theTopics = mutableListOf<Topic>()
    val topics get() = theTopics.iterator()

    private val _allWordFilenames = mutableListOf<String>()
    val allWordFilenames: List<String> get() = _allWordFilenames

    val myWordsUnyt = Unyt(id = MY_WORDS_UNYT_ID)

    fun createNewTopic(id: String): Topic {
        // FIXME BuildConfig not available
        // if (BuildConfig.DEBUG) {
        //     require(findTopicById(id) == null) { "Topic id not unique: $id" }
        // }

        return Topic(id).also { theTopics.add(it) }
    }

    fun createNewUnyt(topic: Topic, id: String): Unyt {
        // FIXME BuildConfig not available
        // if (BuildConfig.DEBUG) {
        //     require(findUnytById(id) == null) { "Unyt id not unique: $id" }
        // }

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
                Log.warn(TAG, "Cannot find original unyt of word $word, passing back myWordsUnyt instead")
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

    fun loadUnytIfNecessary(unyt: Unyt) {
        if (unyt.numWordsAvailable > 0 && unyt.numWordsLoaded == 0) {
            Log.debug(TAG, "Loading unyt including words: ${unyt.name.en}")

            unyt.wordFilenames.forEach { filename ->
                val word = loadWordFile(filename)

                if (word != null) {
                    unyt.add(word)
                } else {
                    Log.error(TAG, "Failed to load word file: $filename")
                }
            }
        }
    }

    fun loadWordFile(filename: String): Word? {
        val word = Word()
        word.filename = filename
        val path = "voc_${flavour.studyLang}/word/$filename"

        try {
            assets.useAsset(path) { stream ->
                WordFileReader(stream, word).read()
            }
            return word
        } catch (_: IOException) {
            return null
        }
    }

    fun loadVocab() {
        require(_allWordFilenames.isEmpty())
        val path = "voc_${flavour.studyLang}/index.voc"

        try {
            assets.useAsset(path) { stream ->
                VocabIndexReader(stream, this).read()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw RuntimeException("Failed to load vocab: $path")
        }

        loadOrCreateMyWordsUnyt()
    }

    private fun loadOrCreateMyWordsUnyt() {
        myWordsUnyt.apply {
            name.en = "My words"
            name.de = "Meine Wörter"
            name.fr = "Mes mots"
            name.it = "Parole mie"
            studyLang = flavour.studyLang
            hasRomaji = flavour == Flavour.JAPANESE
            hasFurigana = flavour == Flavour.JAPANESE
        }

        try {
            assets.usePrivateFileInput(MY_WORDS_UNYT_FILE_NAME) { stream ->
                loadMyWordsUnyt(stream)
            }
        } catch (_: FileNotFoundException) {
            Log.debug(TAG, "My words unyt does not exist")
        } catch (e: Exception) {
            Log.error(TAG, "Error: $e")
        }
    }

    private fun loadMyWordsUnyt(stream: InputStream) {
        Log.debug(TAG, "Reading my words unyt file")
        MyWordsUnytFileReader(myWordsUnyt, stream).read()

        if (myWordsUnyt.wordFilenames.isEmpty()) {
            Log.warn(TAG, "Loaded an empty my words unyt")
        } else {
            val toBeRemoved = mutableListOf<String>()

            myWordsUnyt.wordFilenames.forEach { filename ->
                val word = loadWordFile(filename)

                if (word != null) {
                    myWordsUnyt.add(word)
                } else {
                    // This can happen if myWordsUnyt contains the filename of a word that no longer exists.
                    Log.warn(TAG, "Removing word from my words unyt as we failed to load it: $filename")
                    toBeRemoved.add(filename)
                }
            }

            // Actually remove the words only if some words could be loaded. If all of them failed to load,
            // there is probably a bug (during development), and we don't want to lose the list.

            if (toBeRemoved.size < myWordsUnyt.wordFilenames.size) {
                myWordsUnyt.wordFilenames.removeAll(toBeRemoved)
            }

            Log.debug(TAG, "My Words: avail=${myWordsUnyt.numWordsAvailable}, loaded=${myWordsUnyt.numWordsLoaded}")
        }
    }

    fun writeMyWordsUnytIfNecessary() {
        if (!myWordsUnyt.modified) return

        try {
            Log.debug(TAG, "Writing my words unyt file")
            assets.usePrivateFileOutput(MY_WORDS_UNYT_FILE_NAME) {
                MyWordsUnytFileWriter(myWordsUnyt, it).write()
            }
            myWordsUnyt.modified = false
            stats.setUnytStudyMoment(myWordsUnyt)
        } catch (e: Exception) {
            Log.error(TAG, "Error: ${e.message}\n${e.stackTraceToString()}")
        }
    }

    companion object {
        private val TAG = Log.Tag("Vocabulary")
        private const val MY_WORDS_UNYT_FILE_NAME = "my_words_unyt.voc"
        private const val MY_WORDS_UNYT_ID = "__my_words_unyt__"
    }
}
