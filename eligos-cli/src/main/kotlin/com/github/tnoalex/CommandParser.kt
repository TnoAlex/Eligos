package com.github.tnoalex

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import com.github.tnoalex.config.ConfigParser
import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.formatter.Reporter
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.container.DefaultBeanContainerScanner
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.foundation.bean.handler.DefaultBeanHandlerScanner
import com.github.tnoalex.foundation.bean.register.DefaultBeanRegisterDistributor
import com.github.tnoalex.parser.CliCompilerEnvironmentContext
import com.github.tnoalex.specs.AnalyzerSpec
import com.github.tnoalex.specs.FormatterSpec
import com.github.tnoalex.specs.KotlinCompilerSpec
import com.github.tnoalex.utils.StdOutErrWrapper
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.pathString

private val defaultJdkHome = File(System.getProperty("java.home")).toPath()
private val defaultKotlinLib = File(CharRange::class.java.protectionDomain.codeSource.location.path).toPath()

class CommandParser : CliktCommand() {
    private val majorLang: String by argument(
        name = "major language",
        help = "The major language to be analyzed"
    )

    private val withLang by option(
        "-w",
        "--with",
        help = "Secondary languages that are analyzed in collaboration with the primary language"
    )

    private val srcPath by argument(name = "source path", help = "The path of source files").path(
        mustExist = true,
        canBeDir = true,
        canBeFile = true,
        mustBeReadable = true
    )

    private val classPath by option(
        "-ecp",
        "--class-path",
        help = "The classpath of the project to be analyzed. " +
                "(Default is source path and '.'," +
                "If your project has external jar dependencies, add the paths of them)"
    ).path(
        mustExist = true,
        canBeDir = true,
        mustBeReadable = true,
        canBeFile = true
    ).multiple()


    private val jdkHome by option(
        "-jh",
        "--jdk-home",
        help = "The path of 'JAVA_HOME'. (Default is current jvm's base dir)"
    ).path(
        mustExist = true,
        mustBeReadable = true,
        canBeFile = false,
        canBeDir = true
    ).default(defaultJdkHome)

    private val kotlinVersion by option(
        "-kv",
        "--kotlin-v",
        help = "The version of kotlin in the project"
    ).default("1.9")

    private val jvmTarget by option("-jt", "--jvm-target", help = "The target of project's bytecode").default("1.8")

    private val kotlinStdLibPath by option(
        "-kl",
        "--kotlin-lib",
        help = "The path of kotlin-std-lib. (Default is current kotlin lib's path)"
    ).path(
        mustExist = true,
        canBeDir = true,
        canBeFile = true
    ).default(defaultKotlinLib)

    private val resultOutPath by argument("result output path", help = "The path to out put result").path(
        canBeDir = true,
        canBeFile = false,
        mustExist = true,
        mustBeWritable = true
    ).default(Path("."))

    private val resultPrefix by option("-p", "--prefix", help = "The result file name prefix").default("")

    private val resultFormat by option(
        "-f",
        "--format",
        help = "The Presentation of results"
    ).enum<FormatterTypeEnum> { it.name }.default(FormatterTypeEnum.JSON)


    private val extendRules by option("-r", "--rules", help = "Specify the rules to use").file(
        mustExist = true,
        mustBeReadable = true
    )

    private val debug by option("-D", "--debug", help = "Out put exception stack").flag(default = false)

    override fun run() {
        val analyzerSpec = buildSpec()
        initApplication(analyzerSpec)
        StdOutErrWrapper.init()
        Analyzer(analyzerSpec).analyze()
        Reporter(analyzerSpec.formatterSpec).report()
    }

    private fun buildSpec(): AnalyzerSpec {
        val kotlinCompilerSpec = KotlinCompilerSpec(
            srcPath,
            classPath,
            jdkHome,
            kotlinVersion,
            jvmTarget,
            kotlinStdLibPath
        )
        val formatterSpec = FormatterSpec(
            srcPath.pathString,
            resultOutPath,
            resultPrefix,
            resultFormat
        )
        return AnalyzerSpec(
            majorLang,
            withLang,
            extendRules,
            debug,
            kotlinCompilerSpec,
            formatterSpec,
            LaunchEnvironment.CLI
        )
    }

    private fun initApplication(analyzerSpec: AnalyzerSpec) {
        val cliCompilerEnvironmentContext = CliCompilerEnvironmentContext(analyzerSpec.kotlinCompilerSpec!!)
        cliCompilerEnvironmentContext.initCompilerEnv()

        val configParser = ConfigParser()
        configParser.extendRules = extendRules

        ApplicationContext.addBeanRegisterDistributor(listOf(DefaultBeanRegisterDistributor()))
        ApplicationContext.addBeanContainerScanner(listOf(DefaultBeanContainerScanner()))
        ApplicationContext.addBeanHandlerScanner(listOf(DefaultBeanHandlerScanner()))

        ApplicationContext.addBean(configParser.javaClass.simpleName, configParser, SimpleSingletonBeanContainer)

        ApplicationContext.addBean(
            cliCompilerEnvironmentContext.javaClass.simpleName,
            cliCompilerEnvironmentContext,
            SimpleSingletonBeanContainer
        )
        let { }
        ApplicationContext.init()
    }
}