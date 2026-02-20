package io.github.digorydoo.goigoi.compiler

import io.github.digorydoo.goigoi.compiler.check.FinalChecks
import io.github.digorydoo.goigoi.compiler.stats.FinalStats
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import io.github.digorydoo.goigoi.compiler.writer.KanjiIndexWriter
import io.github.digorydoo.kokuban.ShellCommandError
import java.io.File
import kotlin.system.exitProcess

fun compileGoigoi(options: Options) {
    if (options.srcDir.isEmpty()) {
        throw ShellCommandError("The input directory has not been set.")
    }

    val srcDir = File(options.srcDir)

    if (!srcDir.isDirectory) {
        throw ShellCommandError("Not a directory: ${options.srcDir}")
    }

    if (options.dstDir.isEmpty()) {
        throw ShellCommandError("The output directory has not been set.")
    }

    val dstDir = File(options.dstDir)

    if (!dstDir.isDirectory) {
        throw ShellCommandError("Not a directory: ${options.dstDir}")
    }

    val vocab = GoigoiVocab()
    GoigoiVocabBuilder(vocab, options).build(srcDir)

    if (!options.quiet) {
        println()
    }

    val kanjiLevels = KanjiLevels()
    val readings = mutableMapOf<String, MutableSet<String>>()
    KanjiIndexBuilder(vocab, kanjiLevels, readings, options).build()

    KanjiIndexWriter(options).writeFiles(
        kanjiLevels,
        readings,
        vocab.kanjiBySchoolYear,
        vocab.kanjiByFreq,
        vocab.dontConfuseKanjis,
    )

    FinalChecks(vocab, kanjiLevels, readings, options).check()

    if (!options.quiet) {
        FinalStats(vocab, kanjiLevels, readings).print()
    }
}

fun main(args: Array<String>) {
    try {
        val options = Options.fromCmdLine(args)
        compileGoigoi(options)
    } catch (e: ParsingFailed) {
        System.err.println("Parsing FAILED")
        e.prettyPrint()
        exitProcess(1)
    } catch (e: CheckFailed) {
        System.err.println("Check FAILED")
        e.prettyPrint()
        exitProcess(2)
    } catch (e: ShellCommandError) {
        System.err.println(e.message)
        exitProcess(e.exitCode)
    } catch (e: Throwable) {
        System.err.println("***EXCEPTION")
        e.printStackTrace()
        exitProcess(42)
    }
}
