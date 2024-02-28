package com.github.tnoalex.foundation.environment

import com.github.tnoalex.foundation.bean.Component
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.jvm.compiler.*
import org.jetbrains.kotlin.cli.jvm.config.addJavaSourceRoots
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalFileSystem
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.psi.KotlinReferenceProvidersService
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lazy.ResolveSession
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import java.io.File
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory

@Component(order = Int.MAX_VALUE)
class JvmCompilerEnvironmentContext : CompilerEnvironmentContext {
    override val supportLanguage: List<String>
        get() = listOf("java", "kotlin")

    private val disposer = Disposer.newDisposable()

    private val fileSystem: CoreLocalFileSystem = CoreLocalFileSystem()
    lateinit var project: MockProject
        private set
    lateinit var baseDir: VirtualFile
        private set
    lateinit var bindingContext: BindingContext
        private set

    override fun initCompilerEnv(filePath: Path) {
        val jdkHome = File(System.getProperty("java.home")).toPath()
        val compilerEnvironmentContext = createCompilerConfiguration(
            listOf(filePath),
            listOf(filePath.absolutePathString()),
            jdkHome = jdkHome
        )
        val kotlinCoreEnvironment = createKotlinCoreEnvironment(compilerEnvironmentContext)
        baseDir = CoreLocalVirtualFile(fileSystem, filePath.toFile(), filePath.isDirectory())
        val analysisResult = KotlinToJVMBytecodeCompiler.analyze(kotlinCoreEnvironment)
        bindingContext = analysisResult!!.bindingContext
        /*bindingContext = generateBindingContext(
            kotlinCoreEnvironment,
            listOf(filePath.absolutePathString()),
            kotlinCoreEnvironment.getSourceFiles()
        )*/
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

        project.registerService(KotlinReferenceProvidersService::class.java)

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

    /*private fun kotlinxCoroutinesCorePath(): File {
        return File(CoroutineScope::class.java.protectionDomain.codeSource.location.path)
    }*/

    private fun generateBindingContext(
        environment: KotlinCoreEnvironment,
        classpath: List<String>,
        files: List<KtFile>
    ): BindingContext {
        if (classpath.isEmpty()) {
            return BindingContext.EMPTY
        }

        val messageCollector = object : MessageCollector by MessageCollector.NONE {
            override fun report(
                severity: CompilerMessageSeverity,
                message: String,
                location: CompilerMessageSourceLocation?
            ) {
                println(message)
            }
        }

        val analyzer = AnalyzerWithCompilerReport(
            messageCollector,
            environment.configuration.languageVersionSettings,
            false,
        )
        analyzer.analyzeAndReport(files) {
            TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                environment.project,
                files,
                NoScopeRecordCliBindingTrace(),
                environment.configuration,
                environment::createPackagePartProvider,
                ::FileBasedDeclarationProviderFactory
            )
        }

        return analyzer.analysisResult.bindingContext
    }
}