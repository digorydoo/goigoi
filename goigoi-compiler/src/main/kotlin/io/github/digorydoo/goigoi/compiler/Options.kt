package io.github.digorydoo.goigoi.compiler

import io.github.digorydoo.kokuban.OptionsBuilder
import io.github.digorydoo.kokuban.OptionsParser
import kotlin.system.exitProcess

class Options private constructor() {
    var srcDir = ""; private set
    var dstDir = ""; private set
    var overwrite = false; private set
    var quiet = false; private set

    private var showHelp = false

    private val defs = OptionsBuilder.build {
        addValueless("help", 'h') { showHelp = true }
        addString("input-dir", 'd') { srcDir = it }
        addString("output-dir", 'o') { dstDir = it }
        addBoolean("overwrite", 'w') { overwrite = it }
        addValueless("quiet", 'q') { quiet = true }
    }

    private fun parse(args: Array<String>) {
        OptionsParser(defs).parse(args, allowExtraArgs = false)

        if (showHelp) {
            printUsage()
            exitProcess(0)
        }
    }

    private fun printUsage() {
        println("USAGE: compile-goigoi <options>")
        println("<options> is one or more of:\n")

        defs.apply {
            get("help").apply { helpBody = "Print this usage guide." }
            get("input-dir").apply { valueTypeHint = "<path>"; helpBody = "Set the input directory." }
            get("output-dir").apply { valueTypeHint = "<path>"; helpBody = "Set the output directory." }
            get("overwrite").apply { helpBody = "Overwrite existing files." }
            get("quiet").apply { helpBody = "Don't write anything to stdout except stats and errors." }
        }

        println(defs.makeHelpText())
        println("Example:")
        println("   $ ./goigoi-compile.sh -d=data/goigoi-xml/voc_ja -o=data/goigoi-data/voc_ja")
    }

    companion object {
        fun fromCmdLine(args: Array<String>) =
            Options().apply { parse(args) }
    }
}
