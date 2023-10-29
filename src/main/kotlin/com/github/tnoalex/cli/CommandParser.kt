package com.github.tnoalex.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.switch
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.path
import com.github.tnoalex.analyzer.SmellAnalyzerRegister
import com.github.tnoalex.analyzer.SmellAnalyzerScanner
import com.github.tnoalex.formatter.OutputFormatterType
import kotlin.io.path.Path

@SmellAnalyzerScanner("com.github.tnoalex.analyzer")
class CommandParser : CliktCommand() {

    private val lang: String by argument(
        name = "lang",
        help = "The language of project files"
    ).choice(*SmellAnalyzerRegister.INSTANCE.getAllSupportedLanguages().toTypedArray(), ignoreCase = true)

    private val srcPath by argument(name = "srcPath", help = "The source path").path(
        mustExist = true,
        canBeDir = true,
        canBeFile = true,
        mustBeReadable = true
    )

    private val outputName by argument(name = "outputName", help = "The result file name")

    private val outPath by option("-d", "--outDir", help = "The result output path").path(
        mustExist = true,
        canBeFile = false,
        canBeDir = true,
        mustBeWritable = true
    ).default(Path("."))

    private val outFormat by option(
        "-f",
        "--format",
        help = "The Presentation of results"
    ).enum<OutputFormatterType> { it.name }.default(OutputFormatterType.JSON)

    override fun run() {

    }
}