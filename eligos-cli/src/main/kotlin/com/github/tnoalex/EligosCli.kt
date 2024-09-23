package com.github.tnoalex

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.path
import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.issues.Severity
import java.io.File
import kotlin.io.path.Path
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

@Suppress("unused")
class EligosCli : CliktCommand(name = "eligos-cli") {
    private val majorLang: String by argument(
        name = "major language",
        help = "The major language to be analyzed"
    )

    private val srcPath by argument(name = "source path", help = "The path of source files").path(
        mustExist = true,
        canBeDir = true,
        canBeFile = true,
        mustBeReadable = true
    )

    private val resultOutPath by argument("result output path", help = "The path to out put result").path(
        canBeDir = true,
        canBeFile = false,
        mustExist = true,
        mustBeWritable = true
    ).default(Path("."))

    private val withLang by option(
        "-w",
        "--with",
        help = "Secondary languages that are analyzed in collaboration with the primary language"
    )

    private val severityLevel by option(
        "-sl",
        "--severity-level",
        help = "The lowest severity level recorded during the analysis"
    ).enum<Severity> { it.name }.default(Severity.SUGGESTION)

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

    private val resultOutPrefix by option("-p", "--prefix", help = "The result file name prefix").default("")

    private val resultFormat by option(
        "-f",
        "--format",
        help = "The Presentation of results"
    ).enum<FormatterTypeEnum>(ignoreCase = true) { it.name }.default(FormatterTypeEnum.JSON)


    private val extendRulePath by option("-r", "--rules", help = "Specify the rules to use").file(
        mustExist = true,
        mustBeReadable = true
    )

    private val disableCompilerLog by option("-DC", "--disable-compiler-log", help = "Disable compiler log")
        .flag(default = true)

    private val enabledDebug by option("-D", "--debug", help = "Out put exception stack").flag(default = false)

    private val nonReport by option("-Nr", "--no-report", help = "Disable reporter (debug flag)").flag(default = false)

    private val disableAnyElse by option(
        "-dae",
        "--disable-any-else",
        help = "Disable all processors except those specified"
    ).multiple()

    private val enableAnyElse by option(
        "-eae",
        "--enable-any-else",
        help = "Enable all processors except those specified"
    ).multiple()

    override fun run() {
        val arguments =
            this::class.declaredMemberProperties.associate { it.isAccessible = true; it.name to it.getter.call(this) }
        parseArguments(arguments)
    }

    companion object {
        private val defaultJdkHome = File(System.getProperty("java.home")).toPath()
        private val defaultKotlinLib = File(CharRange::class.java.protectionDomain.codeSource.location.path).toPath()
    }
}