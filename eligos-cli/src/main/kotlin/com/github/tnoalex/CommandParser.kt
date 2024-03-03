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
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.container.DefaultBeanContainerScanner
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.foundation.bean.handler.DefaultBeanHandlerScanner
import com.github.tnoalex.foundation.bean.register.DefaultBeanRegisterDistributor
import com.github.tnoalex.foundation.filetools.FileHelper
import com.github.tnoalex.parser.CliCompilerEnvironmentContext
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
        initApplication()
        Analyzer(
            FormatterFactory.getFormatter(outFormat) ?: throw RuntimeException("Unsupported  formatter"),
            listOf(lang, crossLang),
            LaunchEnvironment.CLI
        ).analyze()
    }

    private fun initApplication() {
        val fileHelper = FileHelper()
        fileHelper.setFileInfo(
            srcPath.toFile(),
            outPath?.toFile(),
            outputPrefix
        )

        val cliCompilerEnvironmentContext = CliCompilerEnvironmentContext()
        cliCompilerEnvironmentContext.initCompilerEnv(srcPath)

        val configParser = ConfigParser()
        configParser.extendRules = extendRules

        ApplicationContext.addBeanRegisterDistributor(listOf(DefaultBeanRegisterDistributor()))
        ApplicationContext.addBeanContainerScanner(listOf(DefaultBeanContainerScanner()))
        ApplicationContext.addBeanHandlerScanner(listOf(DefaultBeanHandlerScanner()))

        ApplicationContext.addBean(fileHelper.javaClass.simpleName, fileHelper, SimpleSingletonBeanContainer)
        ApplicationContext.addBean(configParser.javaClass.simpleName, configParser, SimpleSingletonBeanContainer)
        ApplicationContext.addBean(
            cliCompilerEnvironmentContext.javaClass.simpleName,
            cliCompilerEnvironmentContext,
            SimpleSingletonBeanContainer
        )
        ApplicationContext.init()
    }
}