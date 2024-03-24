package com.github.tnoalex

import com.github.tnoalex.config.ConfigParser
import com.github.tnoalex.formatter.Reporter
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.container.DefaultBeanContainerScanner
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.foundation.bean.handler.DefaultBeanHandlerScanner
import com.github.tnoalex.foundation.bean.register.DefaultBeanRegisterDistributor
import com.github.tnoalex.parser.CliCompilerEnvironmentContext
import com.github.tnoalex.specs.AnalyzerSpec
import com.github.tnoalex.specs.DebugSpec
import com.github.tnoalex.specs.FormatterSpec
import com.github.tnoalex.specs.KotlinCompilerSpec
import com.github.tnoalex.utils.StdOutErrWrapper
import com.github.tnoalex.utils.creatDataClassAndFillProperty
import java.util.concurrent.Callable
import java.util.concurrent.Executors

fun parseArguments(args: Map<String, Any?>) {
    StdOutErrWrapper.init()
    val analyzerSpec = buildSpec(HashMap(args))
    initApplication(analyzerSpec)
    Analyzer(analyzerSpec).analyze()
    if (analyzerSpec.debugSpec.notAllowedReport()) return
    Reporter(analyzerSpec.formatterSpec).report()
}

private fun buildSpec(args: HashMap<String, Any?>): AnalyzerSpec {
    val debugSpec = creatDataClassAndFillProperty(args, DebugSpec::class)
    val kotlinCompilerSpec = creatDataClassAndFillProperty(args, KotlinCompilerSpec::class)
    args["kotlinCompilerSpec"] = kotlinCompilerSpec
    args["srcPathPrefix"] = kotlinCompilerSpec.srcPath.toFile().canonicalPath
    val formatterSpec = creatDataClassAndFillProperty(args, FormatterSpec::class)
    args["formatterSpec"] = formatterSpec
    args["launchEnvironment"] = LaunchEnvironment.CLI
    args["debugSpec"] = debugSpec
    val analyzerSpec = creatDataClassAndFillProperty(args, AnalyzerSpec::class)
    return analyzerSpec
}

private fun initApplication(analyzerSpec: AnalyzerSpec) {
    val executorPool = Executors.newFixedThreadPool(2)
    val compilerThread = Callable {
        Thread.currentThread().name = "compilerThread"
        val cliCompilerEnvironmentContext = CliCompilerEnvironmentContext(analyzerSpec.kotlinCompilerSpec!!)
        cliCompilerEnvironmentContext.initCompilerEnv()
        cliCompilerEnvironmentContext
    }
    val containerThread = Runnable {
        Thread.currentThread().name = "containerThread"
        val configParser = ConfigParser()
        configParser.extendRules = analyzerSpec.extendRulePath


        ApplicationContext.addBeanRegisterDistributor(listOf(DefaultBeanRegisterDistributor()))
        ApplicationContext.addBeanContainerScanner(listOf(DefaultBeanContainerScanner()))
        ApplicationContext.addBeanHandlerScanner(listOf(DefaultBeanHandlerScanner()))

        ApplicationContext.addBean(configParser.javaClass.simpleName, configParser, SimpleSingletonBeanContainer)

        ApplicationContext.init()
    }
    val future = executorPool.submit(compilerThread)
    executorPool.submit(containerThread).get()
    val cliCompilerEnvironmentContext = future.get()
    executorPool.shutdown()
    ApplicationContext.addBean(
        cliCompilerEnvironmentContext.javaClass.simpleName,
        cliCompilerEnvironmentContext,
        SimpleSingletonBeanContainer
    )
}