package io.github.digorydoo.goigoi.compiler

import ch.digorydoo.kutils.cjk.FuriganaIterator.MalformedFuriganaException
import io.github.digorydoo.goigoi.compiler.check.PostChecks
import io.github.digorydoo.goigoi.compiler.stats.FinalStats
import io.github.digorydoo.goigoi.compiler.vocab.GoigoiVocab
import io.github.digorydoo.goigoi.compiler.writer.KanjiIndexWriter
import io.github.digorydoo.kokuban.ShellCommandError
import oracle.xml.parser.v2.XMLParseException
import java.io.File
import kotlin.system.exitProcess

private fun compileGoigoi(options: Options) {
    val srcDir = File(options.srcDir)

    if (!srcDir.isDirectory) {
        throw ShellCommandError("Not a directory: ${options.srcDir}")
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

    PostChecks(vocab, kanjiLevels, readings, options).check()

    val wordIndexWriter = WordIndexWriter(vocab, options.quiet)

    if (options.generateWordIndexPath.isNotEmpty()) {
        wordIndexWriter.writeWordIndex(options.generateWordIndexPath)
    }

    if (options.generatePhraseIndexPath.isNotEmpty()) {
        wordIndexWriter.writePhraseIndex(options.generatePhraseIndexPath)
    }

    if (options.generateSentenceIndexPath.isNotEmpty()) {
        wordIndexWriter.writeSentenceIndex(options.generateSentenceIndexPath)
    }

    if (!options.quiet) {
        FinalStats(vocab, kanjiLevels, readings).print()
    }
}

private fun printStackTraceOfCause(e: Throwable) {
    val cause = e.cause ?: return

    val checkDeeper: Boolean

    when (cause) {
        is ParsingFailed, is CheckFailed -> {
            // ParsingFailed and CheckFailed may wrap other causes
            checkDeeper = true
        }
        is XMLParseException, is MalformedFuriganaException -> {
            // Message is redundant as we expect it to be wrapped by ParsingFailed
            checkDeeper = false
        }
        else -> {
            System.err.println("Caused by $cause")
            checkDeeper = false
        }
    }

    if (checkDeeper) {
        printStackTraceOfCause(cause)
    }
}

fun main(args: Array<String>) {
    try {
        val options = Options.fromCmdLine(args)
        compileGoigoi(options)
    } catch (e: ParsingFailed) {
        System.err.println("Parsing FAILED!\n${e.message}")
        printStackTraceOfCause(e)
        exitProcess(1)
    } catch (e: XMLParseException) {
        // We typically don't come here, as this happens to be thrown as generic Exception (see below)
        System.err.println("Parsing FAILED!\n${e.message}")
        exitProcess(1)
    } catch (e: CheckFailed) {
        System.err.println("Check FAILED!\n${e.message}")
        printStackTraceOfCause(e)
        exitProcess(2)
    } catch (e: ShellCommandError) {
        System.err.println(e.message?.takeIf { it.isNotEmpty() } ?: "An unknown error occurred")
        printStackTraceOfCause(e)
        exitProcess(e.exitCode)
    } catch (e: Throwable) {
        System.err.println("Error: ${e.message}")

        if (e.cause !is XMLParseException) {
            e.printStackTrace()
        }

        exitProcess(42)
    }
}
