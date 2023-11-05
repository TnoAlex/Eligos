package com.github.tnoalex.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import com.github.tnoalex.analyzer.SmellAnalyzerRegister
import com.github.tnoalex.entity.enums.FormatterTypeEnum
import com.github.tnoalex.rules.RulerParser
import org.slf4j.LoggerFactory
import kotlin.io.path.Path
import kotlin.system.exitProcess

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
    ).enum<FormatterTypeEnum> { it.name }.default(FormatterTypeEnum.JSON)

    private val extendRules by option("-r", "--rules", help = "Specify the rules to use").file(
        mustExist = true,
        mustBeReadable = true
    )

    override fun run() {
        val analyzer = SmellAnalyzerRegister.INSTANCE.getAnalyzerByLang(lang)
        if (analyzer == null) {
            logger.error("Not support language:${lang}")
            logger.error("Supported languages are:${SmellAnalyzerRegister.INSTANCE.getAllSupportedLanguages()}")
            exitProcess(-1)
        }
        RulerParser.parserRules(extendRules)
        analyzer.createAnalyticsContext(lang, srcPath.toFile(), outputName, outPath.toFile(), outFormat)
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CommandParser::class.java)
    }
}