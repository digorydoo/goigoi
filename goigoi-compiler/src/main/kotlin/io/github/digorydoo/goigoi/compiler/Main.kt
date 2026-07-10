package io.github.digorydoo.goigoi.compiler

import ch.digorydoo.kutils.cjk.FuriganaIterator.MalformedFuriganaException
import io.github.digorydoo.kokuban.ShellCommandError
import oracle.xml.parser.v2.XMLParseException
import kotlin.system.exitProcess

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
        Command(options).execute()
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
