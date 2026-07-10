package io.github.digorydoo.goigoi.compiler

import ch.digorydoo.kutils.cjk.FuriganaIterator.MalformedFuriganaException
import io.github.digorydoo.goigoi.compiler.check.PostChecks
import io.github.digorydoo.goigoi.compiler.check.PreChecks
import io.github.digorydoo.goigoi.compiler.stats.FinalStats
import io.github.digorydoo.goigoi.compiler.stats.Stats
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
    private val vocab = GoigoiVocab()

    fun execute() {
        if (!options.quiet) {
            println("Parsing XML...")
        }

        val xmlFiles = mutableListOf<File>()
        findGoigoiXmlFiles(options.srcDir, xmlFiles)

        xmlFiles.forEachIndexed { i, file ->
            readGoigoiXml(file)
            if (options.quiet && i % 10 == 0) print(".")
        }

        if (!options.quiet) {
            println("Checking...")
        }

        PreChecks(options).check(vocab)

        if (!options.quiet) {
            Stats.printStats(vocab) // stats about see-also links will be emitted by prepare below
            println("Preparing see-also links...")
        }

        val prep = PrepWordLinks(vocab, options)
        prep.prepare() // prepares see-also links and prints statistics about those

        if (!options.quiet) {
            println("Writing vocab index...")
        }

        buildFileNames() // also defines the rank; must happen after PrepWordLinks, because links affect the rank!
        writeVocabIndex() // this writes the binary index.voc, not the JSON word index (see WordIndexWriter)
        if (options.quiet) print(".")

        writeWordVocFiles()
        if (!options.quiet) println()

        val kanjiLevels = KanjiLevels()
        val readings = mutableMapOf<String, MutableSet<String>>()

        KanjiIndexBuilder(vocab, kanjiLevels, readings, options).build()
        if (options.quiet) print(".")

        KanjiIndexWriter(options).writeFiles(
            kanjiLevels,
            readings,
            vocab.kanjiBySchoolYear,
            vocab.kanjiByFreq,
            vocab.dontConfuseKanjis,
        )
        if (options.quiet) print(".")

        PostChecks(vocab, kanjiLevels, readings, options).check()
        if (options.quiet) print(".")

        val wordIndexWriter = WordIndexWriter(vocab, options.quiet)

        options.generateWordIndexFile?.let { file ->
            wordIndexWriter.writeWordIndex(file)
            if (options.quiet) print(".")
        }

        options.generatePhraseIndexFile?.let { file ->
            wordIndexWriter.writePhraseIndex(file)
            if (options.quiet) print(".")
        }

        options.generateSentenceIndexFile?.let { file ->
            wordIndexWriter.writeSentenceIndex(file)
            if (options.quiet) print(".")
        }

        if (options.quiet) {
            println()
        } else {
            FinalStats(vocab, kanjiLevels, readings).print()
        }
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

    private fun readGoigoiXml(xmlFile: File) {
        try {
            val stream = xmlFile.inputStream()
            val parser = GoigoiXmlParser()
            parser.parse(stream, vocab, xmlFile.name)
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

    private fun buildFileNames() {
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

    private fun writeVocabIndex() {
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

    private fun writeWordVocFiles() {
        if (!options.quiet) {
            println("Writing word files...")
        }

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
}
