package io.github.digorydoo.goigoi.compiler

import ch.digorydoo.kutils.cjk.FuriganaIterator.MalformedFuriganaException
import io.github.digorydoo.goigoi.compiler.check.PostChecks
import io.github.digorydoo.goigoi.compiler.check.PreChecks
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiXmlParser
import io.github.digorydoo.goigoi.compiler.writer.KanjiIndexWriter
import io.github.digorydoo.goigoi.compiler.writer.VocabIndexWriter
import io.github.digorydoo.goigoi.compiler.writer.WordFileWriter
import io.github.digorydoo.goigoi.compiler.writer.WordIndexWriter
import io.github.digorydoo.kokuban.ShellCommandError
import java.io.File
import kotlin.math.floor
import kotlin.math.log10

class Command(private val options: Options) {
    private data class CheckAndPrepareResults(
        val prep: PrepWordLinks,
        val kanjiLevels: KanjiLevels,
        val readings: Map<String, Set<String>>,
    )

    fun execute() {
        val vocab = readFiles()
        val (prep, kanjiLevels, readings) = checkAndPrepare(vocab)
        writeFiles(vocab, kanjiLevels, readings)

        if (options.quiet) {
            println()
        } else {
            val stats = Stats(vocab, prep, kanjiLevels, readings)
            stats.print()
        }
    }

    private fun readFiles(): GoigoiVocab {
        logInfo("Reading XML...")

        val xmlFiles = mutableListOf<File>()
        findGoigoiXmlFiles(options.srcDir, xmlFiles)

        val vocab = GoigoiVocab()

        xmlFiles.forEachIndexed { i, file ->
            readGoigoiXml(file, vocab)
            if (options.quiet && i % 10 == 0) print(".")
        }

        return vocab
    }

    private fun findGoigoiXmlFiles(dir: File, xmlFiles: MutableList<File>) {
        dir.listFiles()!!
            .sortedBy { it.name }
            .forEach { fileOrDir ->
                if (fileOrDir.isDirectory) {
                    findGoigoiXmlFiles(fileOrDir, xmlFiles)
                } else if (fileOrDir.isFile) {
                    if (fileOrDir.name.startsWith(".")) {
                        // Files starting with a dot are silently ignored, e.g. ".DS_Store"
                    } else if (fileOrDir.extension.lowercase() != "xml") {
                        System.err.println("Warning: Ignoring non-XML file: ${fileOrDir.path}")
                    } else {
                        xmlFiles.add(fileOrDir)
                    }
                } else {
                    System.err.println(
                        "Warning: Inaccessible, or neither directory nor regular file: ${fileOrDir.path}"
                    )
                }
            }
    }

    private fun readGoigoiXml(xmlFile: File, vocab: GoigoiVocab) {
        try {
            val parser = GoigoiXmlParser()
            parser.parse(xmlFile, vocab)
        } catch (e: MalformedFuriganaException) {
            throw MalformedFuriganaException("${xmlFile.path}\n${e.message?.prependIndent("   ")}", e)
        } catch (e: ParsingFailed) {
            throw ParsingFailed("${xmlFile.path}\n${e.message?.prependIndent("   ")}", e)
        } catch (e: CheckFailed) {
            throw CheckFailed("${xmlFile.path}\n${e.message?.prependIndent("   ")}", e)
        } catch (e: Exception) {
            throw Exception("${xmlFile.path}\n${e.message?.prependIndent("   ")}", e)
        }
    }

    private fun checkAndPrepare(vocab: GoigoiVocab): CheckAndPrepareResults {
        // Invoke pre-checks

        logInfo("Checking...")
        PreChecks(options).check(vocab)

        // Prepare see-also links

        logInfo("Preparing see-also links...")
        val prep = PrepWordLinks(vocab, options)
        prep.prepare()

        // Build the kanji index

        logInfo("Building kanji index...")
        val kanjiLevels = KanjiLevels()
        val readings = mutableMapOf<String, MutableSet<String>>()
        KanjiIndexBuilder(vocab, kanjiLevels, readings, options).build()

        // Sort words and build filenames

        logInfo("Sorting words and building filenames...")
        buildFileNames(vocab) // must happen after PrepWordLinks and KanjiIndexBuilder, they affect word rank!

        // Invoke PostChecks

        logInfo("Final integrity checks...")
        PostChecks(vocab, kanjiLevels, readings).check()

        return CheckAndPrepareResults(prep, kanjiLevels, readings)
    }

    private fun buildFileNames(vocab: GoigoiVocab) {
        val sortedWords = WordSorter(vocab).getSortedWords()

        // Filenames should be short and unique, to make the VocabIndex occupy less disk space.
        // Enable the extended filename for debugging only.
        val extendedFileNames = false

        if (extendedFileNames) {
            println("Warning: Extended filenames are enabled!")
        }

        val numDigits = floor(log10(sortedWords.size.toDouble())).toInt() + 1

        sortedWords.forEachIndexed { wordIdx, word ->
            val prefix = "w${wordIdx.toString().padStart(numDigits, '0')}"
            val name = word.romaji
                .ifEmpty { word.primaryForm.raw }
                .ifEmpty { word.id }
                .trim()
                .lowercase()
                .replace(", ", "-")
                .replace(" ", "-")
                .replace("/", "")
                .replace(".", "")

            if (extendedFileNames) {
                word.fileName = "$prefix-${word.level?.toString() ?: "nx"}-${name}.voc"
            } else {
                word.fileName = "${prefix}${name}".replace("-", "").take(10) + ".voc"
            }
        }
    }

    private fun writeFiles(vocab: GoigoiVocab, kanjiLevels: KanjiLevels, readings: Map<String, Set<String>>) {
        // Write the binary index.voc

        logInfo("Writing vocab index...")
        writeVocabIndex(vocab)

        // Write kanji index

        logInfo("Writing kanji index...")
        KanjiIndexWriter(options).writeFiles(
            kanjiLevels,
            readings,
            vocab.kanjiBySchoolYear,
            vocab.kanjiByFreq,
            vocab.visuallySimilar,
        )

        // Write the binary word *.voc files

        logInfo("Writing word files...")
        writeWordVocFiles(vocab)

        // Write the JSON word index

        val wordIndexWriter = WordIndexWriter(vocab)

        options.generateWordIndexFile?.let { file ->
            logInfo("Writing ${file.name}...")
            wordIndexWriter.writeWordIndex(file)
        }

        options.generatePhraseIndexFile?.let { file ->
            logInfo("Writing ${file.name}...")
            wordIndexWriter.writePhraseIndex(file)
        }

        options.generateSentenceIndexFile?.let { file ->
            logInfo("Writing ${file.name}...")
            wordIndexWriter.writeSentenceIndex(file)
        }
    }

    private fun writeVocabIndex(vocab: GoigoiVocab) {
        val file = options.vocabIndexFile

        if (file.exists()) {
            file.delete()
        }

        if (!file.createNewFile()) {
            throw ShellCommandError("\nFailed to create file: ${file.path}")
        }

        val indexStream = file.outputStream()
        VocabIndexWriter(vocab, indexStream).write()
    }

    private fun writeWordVocFiles(vocab: GoigoiVocab) {
        var count = 0

        for (topic in vocab.topics) {
            for (unyt in topic.unyts) {
                if (!topic.hidden && !unyt.hidden) {
                    unyt.forEachVisibleWord { word, _ ->
                        writeWordVocFile(word)
                        if (options.quiet && ++count % 80 == 0) print(".")
                    }
                }
            }
        }
    }

    private fun writeWordVocFile(word: GoigoiWord) {
        require(word.fileName.isNotEmpty())
        val file = File(options.wordVocFilesDir, word.fileName)

        if (file.exists()) {
            file.delete()
        }

        if (!file.createNewFile()) {
            throw ShellCommandError("\nFailed to create file: ${file.path}")
        }

        file.outputStream().use {
            WordFileWriter(word, it).write()
        }
    }

    private fun logInfo(verboseMsg: String) {
        if (options.quiet) print(".")
        else println(verboseMsg)
    }
}
