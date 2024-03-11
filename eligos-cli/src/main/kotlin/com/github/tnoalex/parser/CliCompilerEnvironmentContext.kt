package com.github.tnoalex.parser


import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.specs.KotlinCompilerSpec
import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.extensions.ExtensionPoint
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.local.CoreLocalFileSystem
import com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import org.jetbrains.kotlin.analysis.api.descriptors.references.ReadWriteAccessCheckerDescriptorsImpl
import org.jetbrains.kotlin.analysis.api.impl.base.references.HLApiReferenceProviderService
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.NoScopeRecordCliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.cli.jvm.config.addJavaSourceRoots
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.idea.references.KotlinReferenceProviderContributor
import org.jetbrains.kotlin.idea.references.ReadWriteAccessChecker
import org.jetbrains.kotlin.plugin.references.SimpleNameReferenceExtension
import org.jetbrains.kotlin.psi.KotlinReferenceProvidersService
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.references.fe10.KtFe10SimpleNameReference
import org.jetbrains.kotlin.references.fe10.base.DummyKtFe10ReferenceResolutionHelper
import org.jetbrains.kotlin.references.fe10.base.KtFe10KotlinReferenceProviderContributor
import org.jetbrains.kotlin.references.fe10.base.KtFe10ReferenceResolutionHelper
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import org.slf4j.LoggerFactory
import java.io.File
import java.io.PrintStream
import kotlin.io.path.isDirectory

class CliCompilerEnvironmentContext(private val compilerSpec: KotlinCompilerSpec) {
    private val disposer = Disposer.newDisposable()

    private val fileSystem: CoreLocalFileSystem = CoreLocalFileSystem()
    lateinit var project: MockProject
        private set
    lateinit var baseDir: VirtualFile
        private set
    private lateinit var bindingContext: BindingContext

    lateinit var environment: KotlinCoreEnvironment
        private set

    fun initCompilerEnv() {
        val compilerEnvironmentContext = createCompilerConfiguration()
        environment = createKotlinCoreEnvironment(compilerEnvironmentContext)
        baseDir = CoreLocalVirtualFile(fileSystem, compilerSpec.srcPath.toFile(), compilerSpec.srcPath.isDirectory())
        bindingContext = generateBindingContext(
            environment,
            environment.getSourceFiles()
        )
        val application = ApplicationManager.getApplication()
        val resolutionHelper = DummyKtFe10ReferenceResolutionHelper(bindingContext)
        (application as MockApplication).registerService(
            KtFe10ReferenceResolutionHelper::class.java,
            resolutionHelper
        )
        ApplicationContext.addBean(
            resolutionHelper.javaClass.simpleName,
            resolutionHelper,
            SimpleSingletonBeanContainer
        )
    }

    private fun createKotlinCoreEnvironment(
        configuration: CompilerConfiguration = CompilerConfiguration(),
        printStream: PrintStream = System.err,
    ): KotlinCoreEnvironment {
        logger.info("Create kotlin core environment")
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

        project.registerService(
            KotlinReferenceProviderContributor::class.java,
            KtFe10KotlinReferenceProviderContributor::class.java
        )

        project.registerService(KotlinReferenceProvidersService::class.java, HLApiReferenceProviderService(project))
        project.registerService(ReadWriteAccessChecker::class.java, ReadWriteAccessCheckerDescriptorsImpl())
        return environment
    }

    private fun createCompilerConfiguration(): CompilerConfiguration {
        logger.info("Create compiler environment")
        val javaFiles = with(compilerSpec.srcPath) {
            toFile().walk()
                .filter { it.isFile && it.extension.equals("java", true) }
                .toList()
        }
        val kotlinFiles = with(compilerSpec.srcPath) {
            toFile().walk()
                .filter { it.isFile }
                .filter { it.extension.equals("kt", true) || it.extension.equals("kts", true) }
                .map { it.absolutePath }
                .toList()
        }

        val classpathFiles = ArrayList<File>()
        with(compilerSpec) {
            classPath.forEach {
                classpathFiles.add(it.toFile())
            }
            classpathFiles.add(srcPath.toFile())
            classpathFiles.add(compilerSpec.kotlinStdLibPath.toFile())
        }
        val languageVersionSettings: LanguageVersionSettings =
            LanguageVersion.fromVersionString(compilerSpec.kotlinVersion)!!.let {
                LanguageVersionSettingsImpl(
                    languageVersion = it,
                    apiVersion = ApiVersion.createByLanguageVersion(it)
                )
            }

        return CompilerConfiguration().apply {
            put(CommonConfigurationKeys.LANGUAGE_VERSION_SETTINGS, languageVersionSettings)
            put(JVMConfigurationKeys.JVM_TARGET, JvmTarget.fromString(compilerSpec.jvmTarget)!!)
            addJavaSourceRoots(javaFiles)
            addKotlinSourceRoots(kotlinFiles)
            addJvmClasspathRoots(classpathFiles)
            addJvmClasspathRoot(File("."))

            put(JVMConfigurationKeys.JDK_HOME, compilerSpec.jdkHome.toFile())
            configureJdkClasspathRoots()
        }
    }

    class MyMessageCollector : MessageCollector by MessageCollector.NONE {
        override fun report(
            severity: CompilerMessageSeverity,
            message: String,
            location: CompilerMessageSourceLocation?
        ) {
            print(message)
        }
    }

    private fun generateBindingContext(
        environment: KotlinCoreEnvironment,
        files: List<KtFile>
    ): BindingContext {
        logger.info("Analyzing... Please waiting")
        val analyzer = AnalyzerWithCompilerReport(
            MyMessageCollector(),
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

    companion object{
        private val logger = LoggerFactory.getLogger(CliCompilerEnvironmentContext::class.java)
    }
}