package com.github.tnoalex.parser

import com.intellij.mock.MockProject
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.local.CoreLocalFileSystem
import com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.addJavaSourceRoots
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.config.*
import java.io.File
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory

class CliCompilerEnvironmentContext {

    private val disposer = Disposer.newDisposable()

    private val fileSystem: CoreLocalFileSystem = CoreLocalFileSystem()
    lateinit var project: MockProject
        private set
    lateinit var baseDir: VirtualFile
        private set

    fun initCompilerEnv(filePath: Path) {
        val jdkHome = File(System.getProperty("java.home")).toPath()
        val compilerEnvironmentContext = createCompilerConfiguration(
            listOf(filePath),
            listOf(filePath.absolutePathString()),
            jdkHome = jdkHome
        )
        createKotlinCoreEnvironment(compilerEnvironmentContext)
        baseDir = CoreLocalVirtualFile(fileSystem, filePath.toFile(), filePath.isDirectory())
    }

    private fun createKotlinCoreEnvironment(
        configuration: CompilerConfiguration = CompilerConfiguration(),
        printStream: PrintStream = System.err,
    ): KotlinCoreEnvironment {
        setIdeaIoUseFallback()
        configuration.put(
            CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            PrintingMessageCollector(printStream, MessageRenderer.PLAIN_FULL_PATHS, false)
        )
        configuration.put(CommonConfigurationKeys.MODULE_NAME, "eligos")

        val environment = KotlinCoreEnvironment.createForProduction(
            disposer,
            configuration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )

        val projectCandidate = environment.project

        project = requireNotNull(projectCandidate as? MockProject) {
            "MockProject type expected, actual - ${projectCandidate.javaClass.simpleName}"
        }
        return environment
    }

    private fun createCompilerConfiguration(
        pathsToAnalyze: List<Path>,
        classpath: List<String>,
        languageVersion: LanguageVersion = LanguageVersion.KOTLIN_1_9,
        jvmTarget: JvmTarget = JvmTarget.JVM_1_8,
        jdkHome: Path?,
    ): CompilerConfiguration {
        val javaFiles = pathsToAnalyze.flatMap { path ->
            path.toFile().walk()
                .filter { it.isFile && it.extension.equals("java", true) }
                .toList()
        }
        val kotlinFiles = pathsToAnalyze.flatMap { path ->
            path.toFile().walk()
                .filter { it.isFile }
                .filter { it.extension.equals("kt", true) || it.extension.equals("kts", true) }
                .map { it.absolutePath }
                .toList()
        }

        val classpathFiles = classpath.map { File(it) }
        val languageVersionSettings: LanguageVersionSettings = languageVersion.let {
            LanguageVersionSettingsImpl(
                languageVersion = it,
                apiVersion = ApiVersion.createByLanguageVersion(it)
            )
        }

        return CompilerConfiguration().apply {
            put(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS, languageVersionSettings)
            put(JVMConfigurationKeys.JVM_TARGET, jvmTarget)
            addJavaSourceRoots(javaFiles)
            addKotlinSourceRoots(kotlinFiles)
            addJvmClasspathRoots(classpathFiles)
            addJvmClasspathRoot(kotlinStdLibPath())
            addJvmClasspathRoot(File("."))

            jdkHome?.let { put(JVMConfigurationKeys.JDK_HOME, it.toFile()) }
            configureJdkClasspathRoots()
        }
    }

    private fun kotlinStdLibPath(): File {
        return File(CharRange::class.java.protectionDomain.codeSource.location.path)
    }
}