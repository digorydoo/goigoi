package io.github.digorydoo.goigoi.db

import android.util.Log
import ch.digorydoo.kutils.cjk.IntlString
import ch.digorydoo.kutils.cjk.JLPTLevel
import ch.digorydoo.kutils.math.clamp
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.min
import kotlin.math.roundToInt

class Unyt(val id: String) {
    val name = IntlString()
    var studyLang = ""
    var hasRomaji = false
    var hasFurigana = false
    val subheader = IntlString()
    var modified = false // for myWordsUnyt

    val wordFilenames = mutableListOf<String>()
    private val theWords = mutableListOf<Word>()

    val numWordsLoaded get() = theWords.size
    val numWordsAvailable get() = wordFilenames.size

    var levels = listOf<JLPTLevel>()

    val levelOfMostDifficultWord: JLPTLevel
        get() = levels.minOfOrNull { it.toInt() }
            ?.let { JLPTLevel.fromInt(it) }
            ?: JLPTLevel.Nx // fallback if levels is empty

    // theSections and theWords are protected by a lock, because we're going to
    // load the words and sections of a unyt asynchronously.
    private val lock = ReentrantLock()

    fun forEachWord(lambda: (w: Word) -> Unit) {
        lock.lock()

        try {
            theWords.forEach {
                lambda(it)
            }
        } finally {
            lock.unlock()
        }
    }

    fun any(lambda: (w: Word) -> Boolean): Boolean {
        lock.lock()

        try {
            return theWords.any(lambda)
        } finally {
            lock.unlock()
        }
    }

    fun first(): Word {
        lock.lock()

        try {
            return theWords.first()
        } finally {
            lock.unlock()
        }
    }

    fun last(): Word {
        lock.lock()

        try {
            return theWords.last()
        } finally {
            lock.unlock()
        }
    }

    fun findWord(predicate: (w: Word) -> Boolean): Word? {
        var result: Word? = null

        forEachWord {
            if (result == null && predicate(it)) {
                result = it
            }
        }

        return result
    }

    fun findWordById(id: String) = findWord { it.id == id }

    fun add(word: Word) {
        lock.lock()

        try {
            if (!theWords.contains(word)) {
                theWords.add(word)
                modified = true
            }

            if (word.filename.isEmpty()) {
                Log.w(TAG, "The word's filename is empty!")
            } else if (!wordFilenames.contains(word.filename)) {
                wordFilenames.add(word.filename)
                modified = true
            }
        } finally {
            lock.unlock()
        }
    }

    fun add(index: Int, word: Word) {
        lock.lock()

        try {
            if (!theWords.contains(word)) {
                theWords.add(min(index, theWords.size), word)
                modified = true
            }

            if (word.filename.isEmpty()) {
                Log.w(TAG, "The word's filename is empty!")
            } else if (!wordFilenames.contains(word.filename)) {
                wordFilenames.add(min(index, wordFilenames.size), word.filename)
                modified = true
            }
        } finally {
            lock.unlock()
        }
    }

    /**
     * Should be called on the myWordsUnyt only.
     */
    fun removeAllWithSameId(word: Word) {
        lock.lock()

        try {
            if (theWords.removeAll { it.id == word.id }) {
                modified = true
            }

            if (wordFilenames.removeAll { it == word.filename }) {
                modified = true
            }
        } finally {
            lock.unlock()
        }
    }

    fun hasWordWithId(wordId: String): Boolean {
        val result: Boolean
        lock.lock()

        try {
            result = theWords.find { wordId == it.id } != null
        } finally {
            lock.unlock()
        }

        return result
    }

    fun hasWordWithFilename(filename: String): Boolean {
        val result: Boolean
        lock.lock()

        try {
            result = wordFilenames.find { filename == it } != null
        } finally {
            lock.unlock()
        }

        return result
    }

    fun hasEnoughWordsForStudy() =
        numWordsLoaded >= MIN_NUM_WORDS_FOR_STUDY

    fun averageLevelOfWords(): JLPTLevel? {
        var sum = 0
        var numWords = 0

        forEachWord {
            // Words whose level are unknown will be rated N3.
            sum += (it.level ?: JLPTLevel.N3).toInt()
            numWords++
        }

        if (numWords == 0) return null

        val avg = (sum.toFloat() / numWords).roundToInt()
        return JLPTLevel.fromInt(clamp(avg, 0, 5))
    }

    fun unload() {
        lock.lock()

        try {
            theWords.clear()
        } finally {
            lock.unlock()
        }
    }

    fun getWordsShallowClone(): List<Word> {
        val result: List<Word>
        lock.lock()

        try {
            result = mutableListOf<Word>().apply { addAll(theWords) }
        } finally {
            lock.unlock()
        }

        return result
    }

    companion object {
        private const val TAG = "Unyt"
        const val MIN_NUM_WORDS_FOR_STUDY = 5
    }
}
