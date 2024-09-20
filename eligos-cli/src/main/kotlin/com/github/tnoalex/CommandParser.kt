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
import com.github.tnoalex.specs.*
import com.github.tnoalex.utils.StdOutErrWrapper
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
    val analyzerSpec: AnalyzerSpec = SpecificationBuilder(args).next(KotlinCompilerSpec::class)
        .withCurrentArtifact("kotlinCompilerSpec")
        .withPartOfCurrentArtifact("srcPathPrefix") { (it as KotlinCompilerSpec).srcPath.toFile().canonicalPath }
        .next(FormatterSpec::class)
        .withCurrentArtifact("formatterSpec")
        .next(DebugSpec::class)
        .withCurrentArtifact("debugSpec")
        .setProperty("launchEnvironment", LaunchEnvironment.CLI)
        .next(AnalyzerSpec::class)
        .build() ?: throw IllegalStateException("Arguments parser error")
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