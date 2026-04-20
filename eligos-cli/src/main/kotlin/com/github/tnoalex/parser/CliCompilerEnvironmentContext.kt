package com.github.tnoalex.parser

import com.github.tnoalex.specs.KotlinCompilerSpec
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.local.CoreLocalFileSystem
import com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import com.intellij.pom.PomModel
import com.intellij.pom.tree.TreeAspect
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.analysis.api.KaExperimentalApi
import org.jetbrains.kotlin.analysis.api.projectStructure.KaSourceModule
import org.jetbrains.kotlin.analysis.api.standalone.buildStandaloneAnalysisAPISession
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtLibraryModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSdkModule
import org.jetbrains.kotlin.analysis.project.structure.builder.buildKtSourceModule
import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.arguments.parseCommandLineArguments
import org.jetbrains.kotlin.cli.common.arguments.validateArguments
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoots
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.common.setupLanguageVersionSettings
import org.jetbrains.kotlin.cli.jvm.config.addJavaSourceRoots
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.cli.jvm.config.jvmClasspathRoots
import org.jetbrains.kotlin.cli.jvm.configureAdvancedJvmOptions
import org.jetbrains.kotlin.cli.jvm.setupJvmSpecificArguments
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.Path

class CliCompilerEnvironmentContext(compilerSpec: KotlinCompilerSpec) :
    AutoCloseable {
    private val fileSystem: CoreLocalFileSystem = CoreLocalFileSystem()
    private val disposable: Disposable = Disposer.newDisposable()
    private val configuration: CompilerConfiguration = createCompilerConfiguration(
        listOf(compilerSpec.srcPath),
        compilerSpec.classpath,
        compilerSpec.apiVersion,
        compilerSpec.languageVersion,
        compilerSpec.jvmTarget,
        compilerSpec.jdkHome,
        compilerSpec.freeCompilerArgs,
        if (compilerSpec.disableCompilerLog) {
            object : PrintStream(
                object : OutputStream() {
                    override fun write(b: Int) {
                        // no-op
                    }
                }
            ) {}
        } else {
            System.err
        },
    )

    // This lateinit var can be changed to val if https://github.com/JetBrains/kotlin/pull/5703 is merged
    private lateinit var sourceModule: KaSourceModule

    val project: Project
    val baseDir: VirtualFile

    val allSourceFiles: List<PsiFile> by lazy {
        ktSourceFiles.filterIsInstance<PsiFile>() + javaSourceFiles
    }

    @OptIn(KaExperimentalApi::class)
    val ktSourceFiles: List<PsiFileSystemItem> get() = sourceModule.psiRoots

    val javaSourceFiles: List<PsiFile> by lazy {
        baseDir.refresh(false, true)
        val psiManager = project.getService(PsiManager::class.java)

        fun visitVirtualFile(virtualFile: VirtualFile, visitor: (file: VirtualFile) -> Unit) {
            if (virtualFile.isDirectory) {
                virtualFile.children.forEach {
                    visitVirtualFile(it, visitor)
                }
            } else {
                visitor(virtualFile)
            }
        }

        val result = mutableListOf<PsiFile>()

        visitVirtualFile(baseDir) {
            if (it.extension == "java") {
                psiManager.findFile(it)?.let { it1 ->
                    result.add(it1)
                }
            }
        }
        result
    }

    init {
        buildStandaloneAnalysisAPISession(disposable) {
            this@CliCompilerEnvironmentContext.project = project
            baseDir = CoreLocalVirtualFile(
                fileSystem,
                compilerSpec.srcPath,
                Files.readAttributes(compilerSpec.srcPath, BasicFileAttributes::class.java)
            )
            // Required for autocorrect support
            registerProjectService(TreeAspect::class.java)
            registerProjectService(PomModel::class.java, MyPomModel(project))
            configuration.putIfAbsent(CommonConfigurationKeys.MODULE_NAME, "<no module name provided>")

            buildKtModuleProvider {
                val targetPlatform =
                    JvmPlatforms.jvmPlatformByTargetVersion(configuration.jvmTarget ?: JvmTarget.DEFAULT)
                platform = targetPlatform

                val jdk = configuration.jdkHome?.let { jdkHome ->
                    buildKtSdkModule {
                        addBinaryRootsFromJdkHome(jdkHome.toPath(), isJre = false)
                        platform = targetPlatform
                        libraryName = "jdk"
                    }
                }

                val friends = configuration.friendPaths.takeIf { it.isNotEmpty() }
                    ?.let { paths ->
                        buildKtLibraryModule {
                            platform = targetPlatform
                            paths.forEach { addBinaryRoot(Path(it)) }
                            libraryName = "friendDependencies"
                        }
                    }

                val dependencies = buildKtLibraryModule {
                    platform = targetPlatform
                    addBinaryRoots(configuration.jvmClasspathRoots.map { it.toPath() })
                    libraryName = "regularDependencies"
                }

                sourceModule = buildKtSourceModule {
                    addSourceRoots(configuration.kotlinSourceRoots.map { Path(it.path) })
                    platform = targetPlatform
                    moduleName = "source"

                    jdk?.let { addRegularDependency(it) }
                    friends?.let {
                        // Friend dependencies must also be declared as regular dependencies - https://github.com/JetBrains/kotlin/commit/69cfa0498a76f0c3eec39eb06b5de70a0d06e41a
                        addFriendDependency(it)
                        addRegularDependency(it)
                    }
                    addRegularDependency(dependencies)

                    languageVersionSettings = configuration.languageVersionSettings
                }

                addModule(sourceModule)
            }
        }

    }

    override fun close() {
        Disposer.dispose(disposable)
    }

    fun resetEnvironment() {
        // todo
    }
}

/**
 * Creates a compiler configuration for the kotlin compiler with all known sources and classpath jars.
 * Be aware that if any path of [pathsToAnalyze] is a directory it is scanned for java and kotlin files.
 */
@Suppress("LongParameterList")
fun createCompilerConfiguration(
    pathsToAnalyze: List<Path>,
    classpath: List<Path>,
    apiVersion: String?,
    languageVersion: String?,
    jvmTarget: String,
    jdkHome: Path?,
    freeCompilerArgs: List<String>,
    printStream: PrintStream,
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

    val classpathFiles = classpath.map(Path::toFile)

    val jvmCompilerArguments = K2JVMCompilerArguments()

    val args = buildList {
        if (apiVersion != null) {
            add("-api-version")
            add(apiVersion)
        }
        if (languageVersion != null) {
            add("-language-version")
            add(languageVersion)
        }
        add("-jvm-target")
        add(jvmTarget)
        addAll(freeCompilerArgs)
    }

    parseCommandLineArguments(args, jvmCompilerArguments)

    validateArguments(jvmCompilerArguments.errors)?.let { throw IllegalStateException(it) }

    return CompilerConfiguration().apply {
        addJavaSourceRoots(javaFiles)
        addKotlinSourceRoots(kotlinFiles)
        addJvmClasspathRoots(classpathFiles)
        put(
            CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            PrintingMessageCollector(printStream, MessageRenderer.PLAIN_FULL_PATHS, false)
        )
        setupLanguageVersionSettings(jvmCompilerArguments)
        setupJvmSpecificArguments(jvmCompilerArguments)
        configureAdvancedJvmOptions(jvmCompilerArguments)

        if (jdkHome != null) {
            put(JVMConfigurationKeys.JDK_HOME, jdkHome.toFile())
        } else {
            put(JVMConfigurationKeys.JDK_HOME, File(System.getProperty("java.home")))
        }

        configureJdkClasspathRoots()
    }
}