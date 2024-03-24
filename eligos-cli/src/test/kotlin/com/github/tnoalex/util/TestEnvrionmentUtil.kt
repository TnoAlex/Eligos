package com.github.tnoalex.util

import com.github.tnoalex.Analyzer
import com.github.tnoalex.config.ConfigParser
import com.github.tnoalex.formatter.FormatterTypeEnum
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
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors

private fun buildSpec(): AnalyzerSpec {
    val kotlinCompilerSpec = KotlinCompilerSpec(
        TEST_SRC_PATH.toPath(),
        listOf(TEST_SRC_PATH.toPath()),
        defaultJdkHome,
        "1.9",
        "11",
        defaultKotlinLib,
        false
    )
    val formatterSpec = FormatterSpec(
        TEST_SRC_PATH.canonicalPath,
        TEST_SRC_PATH.toPath(),
        "",
        FormatterTypeEnum.JSON
    )
    val debugSpec = DebugSpec()
    return AnalyzerSpec(
        "kotlin",
        "java",
        null,
        kotlinCompilerSpec,
        formatterSpec,
        LaunchEnvironment.CLI,
        debugSpec
    )
}


fun initEligosEnv() {
    val analyzerSpec = buildSpec()
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
        ApplicationContext.addBeanRegisterDistributor(listOf(DefaultBeanRegisterDistributor()))
        ApplicationContext.addBeanContainerScanner(listOf(DefaultBeanContainerScanner()))
        ApplicationContext.addBeanHandlerScanner(listOf(DefaultBeanHandlerScanner()))

        ApplicationContext.addBean(
            configParser.javaClass.simpleName,
            configParser,
            SimpleSingletonBeanContainer
        )

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
    Analyzer(analyzerSpec).analyze()
}


private val TEST_SRC_PATH = File("../testData/kotlin-code-samples")
private val defaultJdkHome = File(System.getProperty("java.home")).toPath()
private val defaultKotlinLib = File(CharRange::class.java.protectionDomain.codeSource.location.path).toPath()

