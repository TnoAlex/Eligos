package com.github.tnoalex.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import com.github.tnoalex.analyzer.SmellAnalyzer
import com.github.tnoalex.analyzer.crosslang.CrossLangSmellAnalyzerContainer
import com.github.tnoalex.analyzer.singlelang.SingleSmellAnalyzerContainer
import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.foundation.filetools.FileContainer
import com.github.tnoalex.rules.RulerParser
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class CommandParser : CliktCommand() {

    private val lang: String by argument(
        name = "lang",
        help = "The language of project files"
    ).choice(*SingleSmellAnalyzerContainer.getKeys().toTypedArray(), ignoreCase = true)

    private val srcPath by argument(name = "srcPath", help = "The source path").path(
        mustExist = true,
        canBeDir = true,
        canBeFile = true,
        mustBeReadable = true
    )

    private val crossLang by option("--with", help = "The Languages of cross-analysis")

    private val outputPrefix by option("-p", "--prefix", help = "The result file name prefix")

    private val outPath by option("-d", "--outDir", help = "The result output path").path(
        mustExist = true,
        canBeFile = false,
        canBeDir = true,
        mustBeWritable = true
    )

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
        val analyzer: SmellAnalyzer? = if (crossLang == null) {
            SingleSmellAnalyzerContainer.getByKey(lang)
        } else {
            CrossLangSmellAnalyzerContainer.getByKey(setOf(lang, crossLang!!))
        }

        if (analyzer == null) {
            if (crossLang == null) {
                logger.error("Not support language:${lang}")
                logger.error("Supported languages are:${SingleSmellAnalyzerContainer.getKeys()}")
            } else {
                logger.error("Not support cross language:${lang}-${crossLang}")
                logger.error("Supported languages are:${CrossLangSmellAnalyzerContainer.getKeys()}")
            }
            exitProcess(-1)
        }
        FileContainer.initFileContainer(srcPath.toFile(), outPath?.toFile(), outputPrefix)
        RulerParser.parserRules(extendRules)
        analyzer.createAnalyticsContext(outFormat)
        analyzer.analyze()
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(CommandParser::class.java)
    }
}