package com.github.tnoalex.foundation

import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.foundation.bean.inject.InjectBean
import com.github.tnoalex.foundation.bean.register.DefaultBeanRegister
import com.github.tnoalex.parser.CliCompilerEnvironmentContext
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.specs.KotlinCompilerSpec
import com.github.tnoalex.utils.getMutablePropertiesAnnotateWith
import com.github.tnoalex.utils.getPropertyAnnotation
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.io.File
import java.nio.file.Paths
import java.util.*
import java.util.stream.Stream
import kotlin.reflect.KClass

class EligosTestArgumentsProvider : ArgumentsProvider {
    private val beanRegister = DefaultBeanRegister()

    @Suppress("UNCHECKED_CAST")
    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
        val testMethod = context.requiredTestMethod
        val testAnnotation = testMethod.getAnnotation(RequireTestProcessor::class.java)
        val testResource = resolveTestResources(testAnnotation.testResourcePath)
        val injectedBeans = testAnnotation.injectedBeans
        require(testMethod.parameters.size == 1)
        val requiredProcessor = testMethod.parameters[0].type.kotlin as KClass<IssueProcessor>
        createDependenciesForRequiredProcessor(injectedBeans)
        createMockCompilerEnv(testResource)
        createMockContext(requiredProcessor)
        return Stream.of(Arguments.of(ApplicationContext.getExactBean(requiredProcessor.java)))
    }

    private fun createDependenciesForRequiredProcessor(injectedBeans: Array<KClass<*>>) {
        if (injectedBeans.isEmpty()) return
        injectedBeans.forEach {
            beanRegister.registerBean(
                it.simpleName!!,
                it.java,
                SimpleSingletonBeanContainer
            )
        }
    }

    private fun createMockContext(processorClazz: KClass<out IssueProcessor>) {
        beanRegister.registerBean(
            processorClazz.simpleName!!,
            processorClazz.java,
            SimpleSingletonBeanContainer
        )
    }

    private fun createMockCompilerEnv(sourcePath: String) {
        val compilerEnv = KotlinCompilerSpec(
            Paths.get(sourcePath),
            listOf(Paths.get(sourcePath)),
            defaultJdkHome,
            kotlinStdLibPath = defaultKotlinLib,
            disableCompilerLog = true
        )
        val context = CliCompilerEnvironmentContext(compilerEnv).also { it.initCompilerEnv() }
        ApplicationContext.addBean(
            context.javaClass.simpleName,
            context,
            SimpleSingletonBeanContainer
        )
    }

    private fun resolveTestResources(resourcePath: String): String {
        if (resourcePath.startsWith("resources@")) {
            val resourcesPath = resourcePath.removePrefix("resources@")
            var path = Thread.currentThread().contextClassLoader.getResource(resourcesPath)?.path
            isWindows.ifTrue {
                path = path?.removePrefix("/")
            }
            return path ?: return resourcesPath
        } else if (resourcePath.startsWith("file@")) {
            return resourcePath.removePrefix("file@")
        } else {
            throw RuntimeException("Can not load test resources from $resourcePath")
        }
    }

    companion object {
        private val defaultJdkHome = File(System.getProperty("java.home")).toPath()
        private val defaultKotlinLib = File(CharRange::class.java.protectionDomain.codeSource.location.path).toPath()
        private val isWindows = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows")
    }
}