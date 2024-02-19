package com.github.tnoalex

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import com.github.tnoalex.config.ConfigParser
import com.github.tnoalex.formatter.FormatterFactory
import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.filetools.FileHelper
import com.github.tnoalex.utils.StdOutErrWrapper

class CommandParser : CliktCommand() {

    private val lang: String by argument(
        name = "lang",
        help = "The language of project files"
    )

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
        StdOutErrWrapper.init()
        ApplicationContext.getBean(FileHelper::class.java)[0].setFileInfo(
            srcPath.toFile(),
            outPath?.toFile(),
            outputPrefix
        )
        ApplicationContext.getBean(ConfigParser::class.java)[0].extendRules = extendRules
        Analyzer(
            FormatterFactory.getFormatter(outFormat) ?: throw RuntimeException("Unsupported  formatter"),
            listOf(lang, crossLang)
        )
    }
}