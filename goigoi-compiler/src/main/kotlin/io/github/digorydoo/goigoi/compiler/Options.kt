package io.github.digorydoo.goigoi.compiler

import io.github.digorydoo.kokuban.OptionsBuilder
import io.github.digorydoo.kokuban.OptionsParser
import io.github.digorydoo.kokuban.ShellCommandError
import java.io.File
import kotlin.system.exitProcess

class Options private constructor() {
    private var srcPath = ""
    private var dstPath = ""

    lateinit var srcDir: File

    private var generateWordIndexPath = ""
    private var generatePhraseIndexPath = ""
    private var generateSentenceIndexPath = ""

    var generateWordIndexFile: File? = null; private set
    var generatePhraseIndexFile: File? = null; private set
    var generateSentenceIndexFile: File? = null; private set

    lateinit var wordVocFilesDir: File; private set
    lateinit var vocabIndexFile: File; private set
    lateinit var generateKanjiIndexFile: File; private set
    lateinit var generateReadingsIndexFile: File; private set
    lateinit var generateSchoolYearsIndexFile: File; private set
    lateinit var generateKanjiFreqIndexFile: File; private set
    lateinit var generateDontConfuseIndexFile: File; private set

    var quiet = false; private set
    private var showHelp = false

    private val defs = OptionsBuilder.build {
        addValueless("help", 'h') { showHelp = true }
        addString("input-dir", 'd') { srcPath = it }
        addString("output-dir", 'o') { dstPath = it }
        addString("word-index", 'i') { generateWordIndexPath = it }
        addString("phrase-index", 'p') { generatePhraseIndexPath = it }
        addString("sentence-index", 's') { generateSentenceIndexPath = it }
        addValueless("quiet", 'q') { quiet = true }
    }

    private fun parse(args: Array<String>) {
        OptionsParser(defs).parse(args, allowExtraArgs = false)

        if (showHelp) {
            printUsage()
            exitProcess(0)
        }

        if (srcPath.isEmpty()) throw ShellCommandError("Missing option: input-dir")
        if (dstPath.isEmpty()) throw ShellCommandError("Missing option: output-dir")

        srcDir = File(srcPath)

        if (!srcDir.isDirectory) {
            throw ShellCommandError("Not a directory: $srcPath")
        }

        val dstDir = File(dstPath)

        if (!dstDir.isDirectory) {
            throw ShellCommandError("Not a directory: $dstPath")
        }

        generateWordIndexFile = when {
            generateWordIndexPath.isEmpty() -> null
            generateWordIndexPath.lowercase().endsWith(".json") -> File(generateWordIndexPath)
            else -> throw ShellCommandError("Word index file should end in .json")
        }

        generatePhraseIndexFile = when {
            generatePhraseIndexPath.isEmpty() -> null
            generatePhraseIndexPath.lowercase().endsWith(".json") -> File(generatePhraseIndexPath)
            else -> throw ShellCommandError("Phrase index file should end in .json")
        }

        generateSentenceIndexFile = when {
            generateSentenceIndexPath.isEmpty() -> null
            generateSentenceIndexPath.lowercase().endsWith(".json") -> File(generateSentenceIndexPath)
            else -> throw ShellCommandError("Sentence index file should end in .json")
        }

        // There are no options for these, but we keep this in Options for the sake of consistency:
        wordVocFilesDir = File(dstDir, "word")
        vocabIndexFile = File(dstDir, "index.voc")
        generateKanjiIndexFile = File(dstDir, "all-kanjis.txt")
        generateReadingsIndexFile = File(dstDir, "readings.txt")
        generateSchoolYearsIndexFile = File(dstDir, "schoolyears.txt")
        generateKanjiFreqIndexFile = File(dstDir, "kanji-freq.txt")
        generateDontConfuseIndexFile = File(dstDir, "dont-confuse.txt")

        if (!wordVocFilesDir.exists()) {
            if (!wordVocFilesDir.mkdir()) {
                throw ShellCommandError("Failed to create directory: ${wordVocFilesDir.path}")
            }
        }
    }

    private fun printUsage() {
        println("USAGE: compile-goigoi <options>")
        println("<options> is one or more of:\n")

        defs.apply {
            get("help").apply { helpBody = "Print this usage guide." }
            get("input-dir").apply {
                valueTypeHint = "<path>"
                helpBody = "Set the input directory."
            }
            get("output-dir").apply {
                valueTypeHint = "<path>"
                helpBody = "Set the output directory for the generated voc files."
            }
            get("word-index").apply {
                valueTypeHint = "<path>"
                helpBody = "If specified, a word index file will be created at the specified path."
            }
            get("phrase-index").apply {
                valueTypeHint = "<path>"
                helpBody = "If specified, a phrase index file will be created at the specified path."
            }
            get("sentence-index").apply {
                valueTypeHint = "<path>"
                helpBody = "If specified, a sentence index file will be created at the specified path."
            }
            get("quiet").apply { helpBody = "Don't write anything to stdout except stats and errors." }
        }

        println(defs.makeHelpText())
        println("Example:")
        println("   $ ./compile-goigoi.sh -d=goigoi-xml/voc_ja -o=app/src/main/assets/voc_ja")
    }

    companion object {
        fun fromCmdLine(args: Array<String>) =
            Options().apply { parse(args) }
    }
}
