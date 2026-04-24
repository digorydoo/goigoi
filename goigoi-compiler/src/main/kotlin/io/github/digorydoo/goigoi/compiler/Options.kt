package io.github.digorydoo.goigoi.compiler

import io.github.digorydoo.kokuban.OptionsBuilder
import io.github.digorydoo.kokuban.OptionsParser
import io.github.digorydoo.kokuban.ShellCommandError
import kotlin.system.exitProcess

class Options private constructor() {
    var srcDir = ""; private set
    var dstDir = ""; private set
    var generateWordIndexPath = ""; private set
    var generatePhraseIndexPath = ""; private set
    var generateSentenceIndexPath = ""; private set
    var generateKanjiIndexPath = ""; private set
    var generateReadingsIndexPath = ""; private set
    var generateSchoolYearsIndexPath = ""; private set
    var generateKanjiFreqIndexPath = ""; private set
    var generateDontConfuseIndexPath = ""; private set
    var quiet = false; private set

    private var showHelp = false

    private val defs = OptionsBuilder.build {
        addValueless("help", 'h') { showHelp = true }
        addString("input-dir", 'd') { srcDir = it }
        addString("output-dir", 'o') { dstDir = it }
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

        if (srcDir.isEmpty()) throw ShellCommandError("Missing option: input-dir")
        if (dstDir.isEmpty()) throw ShellCommandError("Missing option: output-dir")

        if (generateWordIndexPath.isNotEmpty() && !generateWordIndexPath.lowercase().endsWith(".json")) {
            throw ShellCommandError("Word index file should end in .json")
        }

        if (generatePhraseIndexPath.isNotEmpty() && !generatePhraseIndexPath.lowercase().endsWith(".json")) {
            throw ShellCommandError("Phrase index file should end in .json")
        }

        if (generateSentenceIndexPath.isNotEmpty() && !generateSentenceIndexPath.lowercase().endsWith(".json")) {
            throw ShellCommandError("Sentence index file should end in .json")
        }

        // There are no options for these, but we keep this in Options for the sake of consistency:
        generateKanjiIndexPath = "$dstDir/all-kanjis.txt"
        generateReadingsIndexPath = "$dstDir/readings.txt"
        generateSchoolYearsIndexPath = "$dstDir/schoolyears.txt"
        generateKanjiFreqIndexPath = "$dstDir/kanji-freq.txt"
        generateDontConfuseIndexPath = "$dstDir/dont-confuse.txt"
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
