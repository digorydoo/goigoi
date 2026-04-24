package io.github.digorydoo.goigoi.compiler

import io.github.digorydoo.goigoi.compiler.check.check
import io.github.digorydoo.goigoi.compiler.stats.Stats
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiWord
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiXmlParser
import io.github.digorydoo.goigoi.compiler.writer.VocabIndexWriter
import io.github.digorydoo.goigoi.compiler.writer.WordFileWriter
import io.github.digorydoo.kokuban.ShellCommandError
import java.io.File
import kotlin.math.floor
import kotlin.math.log10

class GoigoiVocabBuilder(private val vocab: GoigoiVocab, private val options: Options) {
    fun build(srcDir: File) {
        if (!options.quiet) {
            println("Parsing XML...")
        }

        readGoigoiXmls(srcDir.listFiles()!!)

        buildFileNames() // creates file names for *.voc files, ignoring original XML number prefixes

        if (!options.quiet) {
            println("Checking vocab...")
        }

        vocab.check() // checks constraints that cannot be checked at XML reading time

        if (!options.quiet) {
            Stats.printStats(vocab) // stats about see-also links will be emitted by prepare below
            println("Preparing see-also links...")
        }

        val prep = PrepWordLinks(vocab, options)
        prep.prepare() // prepares see-also links and prints statistics about those

        if (!options.quiet) {
            println("Writing vocab index...")
        }

        writeVocabIndex()

        if (!options.quiet) {
            println("Writing word files...")
        }

        val wordDir = File("${options.dstDir}/$WORD_DIR")

        if (!wordDir.exists()) {
            if (!wordDir.mkdir()) {
                throw ShellCommandError("Failed to create directory: ${wordDir.path}")
            }
        }

        for (topic in vocab.topics) {
            for (unyt in topic.unyts) {
                if (!topic.hidden && !unyt.hidden) {
                    if (options.quiet) {
                        print(".")
                    }

                    unyt.forEachVisibleWord { word, _ ->
                        writeWord(word)
                    }
                }
            }
        }

        if (options.quiet) {
            println() // dots are printed in quiet mode, so terminate the line here
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

    private fun readGoigoiXmls(srcFiles: Array<File>) {
        srcFiles.sortedBy { it.name }
            .forEach { fileOrDir ->
                if (fileOrDir.isDirectory) {
                    readGoigoiXmls(fileOrDir.listFiles()!!)
                } else if (fileOrDir.isFile) {
                    if (fileOrDir.name.startsWith(".")) {
                        // Files starting with a dot are silently ignored, e.g. ".DS_Store"
                    } else if (fileOrDir.extension.lowercase() != "xml") {
                        System.err.println("Warning: Ignoring non-XML file: ${fileOrDir.path}")
                    } else {
                        readGoigoiXml(fileOrDir)
                    }
                } else {
                    System.err.println("Warning: Inaccessible, or neither directory nor regular file: ${fileOrDir.path}")
                }
            }
    }

    private fun readGoigoiXml(xmlFile: File) {
        if (options.quiet) {
            print(".")
        }

        try {
            val stream = xmlFile.inputStream()
            val parser = GoigoiXmlParser()
            parser.parse(stream, vocab)
        } catch (e: ParsingFailed) {
            throw ParsingFailed("${xmlFile.path}\n   ${e.message}", e)
        } catch (e: CheckFailed) {
            throw CheckFailed("${xmlFile.path}\n   ${e.message}", e)
        } catch (e: Exception) {
            throw RuntimeException("${xmlFile.path}\n   ${e.message}")
        }
    }

    private fun writeVocabIndex() {
        val indexFname = "${options.dstDir}/index.voc" // FIXME path should come from Options like the rest

        if (options.quiet) {
            print(".")
        }

        val indexFile = File(indexFname)

        if (indexFile.exists()) {
            indexFile.delete()
        }

        if (!indexFile.createNewFile()) {
            throw ShellCommandError("\nFailed to create file: ${indexFile.path}")
        }

        val indexStream = indexFile.outputStream()
        VocabIndexWriter(vocab, indexStream).write()
    }

    private fun writeWord(word: GoigoiWord) {
        val fname = "${options.dstDir}/$WORD_DIR/${word.fileName}"

        val file = File(fname)

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

    companion object {
        private const val WORD_DIR = "word"
    }
}
