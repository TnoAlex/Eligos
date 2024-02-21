package com.github.tnoalex

import com.github.tnoalex.foundation.filetools.FileHelper
import com.github.tnoalex.parser.*
import org.jetbrains.kotlin.com.intellij.codeInsight.ContainerProvider
import org.jetbrains.kotlin.com.intellij.codeInsight.JavaContainerProvider
import org.jetbrains.kotlin.com.intellij.core.CoreApplicationEnvironment
import org.jetbrains.kotlin.com.intellij.core.CoreFileTypeRegistry
import org.jetbrains.kotlin.com.intellij.ide.highlighter.ArchiveFileType
import org.jetbrains.kotlin.com.intellij.ide.highlighter.JavaClassFileType
import org.jetbrains.kotlin.com.intellij.ide.highlighter.JavaFileType
import org.jetbrains.kotlin.com.intellij.lang.*
import org.jetbrains.kotlin.com.intellij.lang.java.JavaLanguage
import org.jetbrains.kotlin.com.intellij.lang.java.JavaParserDefinition
import org.jetbrains.kotlin.com.intellij.mock.MockApplication
import org.jetbrains.kotlin.com.intellij.mock.MockFileDocumentManagerImpl
import org.jetbrains.kotlin.com.intellij.mock.MockFileIndexFacade
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.application.ApplicationManager
import org.jetbrains.kotlin.com.intellij.openapi.editor.impl.DocumentImpl
import org.jetbrains.kotlin.com.intellij.openapi.extensions.ExtensionPoint
import org.jetbrains.kotlin.com.intellij.openapi.extensions.ExtensionPointName
import org.jetbrains.kotlin.com.intellij.openapi.extensions.Extensions
import org.jetbrains.kotlin.com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.kotlin.com.intellij.openapi.fileTypes.FileType
import org.jetbrains.kotlin.com.intellij.openapi.fileTypes.FileTypeExtension
import org.jetbrains.kotlin.com.intellij.openapi.fileTypes.PlainTextLanguage
import org.jetbrains.kotlin.com.intellij.openapi.fileTypes.PlainTextParserDefinition
import org.jetbrains.kotlin.com.intellij.openapi.roots.FileIndexFacade
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.util.registry.Registry
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalFileSystem
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import org.jetbrains.kotlin.com.intellij.psi.*
import org.jetbrains.kotlin.com.intellij.psi.impl.compiled.ClassFileStubBuilder
import org.jetbrains.kotlin.com.intellij.psi.impl.file.PsiDirectoryFactory
import org.jetbrains.kotlin.com.intellij.psi.impl.file.PsiDirectoryFactoryImpl
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.JavaASTFactory
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PlainTextASTFactory
import org.jetbrains.kotlin.com.intellij.psi.stubs.BinaryFileStubBuilder
import org.jetbrains.kotlin.com.intellij.psi.stubs.BinaryFileStubBuilders
import org.jetbrains.kotlin.com.intellij.psi.util.CachedValuesManager
import org.jetbrains.kotlin.com.intellij.util.CachedValuesManagerImpl
import org.jetbrains.kotlin.com.intellij.lang.ASTFactory
import org.jetbrains.kotlin.com.intellij.psi.impl.*
import org.jetbrains.kotlin.com.intellij.util.CachedValuesFactory
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.parsing.KotlinParserDefinition
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import kotlin.io.path.Path


//fun main(args: Array<String>) {
//    initApplication()
//    showBanner()
//    LangRegister.register()
//    CommandParser().main(args)
//}
//
//private fun showBanner() {
//    val inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("banner.txt")
//    val banner = StringBuilder()
//    if (inputStream != null) {
//        val scanner = Scanner(inputStream)
//        while (scanner.hasNextLine()) {
//            banner.append(scanner.nextLine()).append("\n")
//        }
//        println(banner.toString())
//    }
//}
//
//private fun initApplication(){
//    ApplicationContext.addBeanRegisterDistributor(listOf(DefaultBeanRegisterDistributor))
//    ApplicationContext.init()
//}


fun main() {
    val path = File("src\\test\\resources\\kotlin-code-samples\\unclearPlatformType").toPath()
    val compilerConfiguration =
        createCompilerConfiguration(
            listOf(path),
            listOf(path.toString()),
            LanguageVersion.KOTLIN_1_9,
            JvmTarget.JVM_1_8,
            Path(System.getProperty("java.home"))
        )
    val kotlinCoreEnvironment =
        createKotlinCoreEnvironment(
            compilerConfiguration,
            printStream = System.err
        )
    val coreFileTypeRegistry = CoreFileTypeRegistry()
    val disposer = Disposer.newDisposable()
    val application = MockApplication(disposer)

    application.registerService(FileDocumentManager::class.java, MockFileDocumentManagerImpl(
        null
    ) { chars: CharSequence ->
        DocumentImpl(
            chars
        )
    }
    )
    coreFileTypeRegistry.registerFileType(JavaClassFileType.INSTANCE, "class")
    coreFileTypeRegistry.registerFileType(ArchiveFileType.INSTANCE, "jar;zip")
    coreFileTypeRegistry.registerFileType(JavaFileType.INSTANCE, "java")
    coreFileTypeRegistry.registerFileType(KotlinFileType.INSTANCE, "kt")
    ApplicationManager.setApplication(application, { coreFileTypeRegistry }, disposer)


    addExplicitExtension(LanguageASTFactory.INSTANCE, PlainTextLanguage.INSTANCE, PlainTextASTFactory(), disposer)
    registerParserDefinition(PlainTextParserDefinition(), disposer)
    addExplicitExtension<FileViewProviderFactory>(
        FileTypeFileViewProviders.INSTANCE,
        JavaClassFileType.INSTANCE,
        ClassFileViewProviderFactory(),
        disposer
    )
    addExplicitExtension<BinaryFileStubBuilder>(
        BinaryFileStubBuilders.INSTANCE,
        JavaClassFileType.INSTANCE,
        ClassFileStubBuilder(),
        disposer
    )

    addExplicitExtension(LanguageASTFactory.INSTANCE, JavaLanguage.INSTANCE, JavaASTFactory(), disposer)
    registerParserDefinition(JavaParserDefinition(), disposer)
    registerParserDefinition(KotlinParserDefinition(), disposer)
    addExplicitExtension(
        LanguageConstantExpressionEvaluator.INSTANCE,
        JavaLanguage.INSTANCE,
        PsiExpressionEvaluator(),
        disposer
    )



    CoreApplicationEnvironment.registerApplicationExtensionPoint(
        ContainerProvider.EP_NAME,
        ContainerProvider::class.java
    )
    addExtension<ContainerProvider>(ContainerProvider.EP_NAME, JavaContainerProvider(), disposer)


    val fileSystem = CoreLocalFileSystem()

    val virtualFile = CoreLocalVirtualFile(fileSystem, path.toFile())
    val project = EligosMockProject(null, Disposer.newDisposable())

//    project.baseDir = virtualFile

    project.registerService(FileIndexFacade::class.java, MockFileIndexFacade(project))
    val psiManager = PsiManagerImpl(project)

    project.registerService(PsiManager::class.java, psiManager)

    project.registerService(PsiDirectoryFactory::class.java, PsiDirectoryFactoryImpl(project))
    project.registerService(PsiFileFactory::class.java, PsiFileFactoryImpl(psiManager))
    project.registerService(CachedValuesManager::class.java, CachedValuesManagerImpl(project, PsiCachedValuesFactory(project)))


    Registry.get("psi.sleep.in.validity.check").setValue(false)


    val fileHelper = FileHelper()
    fileHelper.setFileInfo(path.toFile(), null, null)
    val psis = ArrayList<PsiFile?>()
    fileHelper.visitSourcesFile { file ->
        psis.add(psiManager.findFile(CoreLocalVirtualFile(fileSystem, file)))
    }

    println()
}

fun registerParserDefinition(definition: ParserDefinition, disposable: Disposable) {
    addExplicitExtension<ParserDefinition>(
        LanguageParserDefinitions.INSTANCE,
        definition.fileNodeType.language,
        definition,
        disposable
    )
}

fun <T : Any> addExplicitExtension(
    instance: LanguageExtension<T>,
    language: Language,
    obj: T,
    parentDisposable: Disposable
) {
    instance.addExplicitExtension(language, obj, parentDisposable)
}

fun <T : Any> addExplicitExtension(instance: FileTypeExtension<T>, fileType: FileType, obj: T, disposable: Disposable) {
    instance.addExplicitExtension(fileType, obj, disposable)
}

fun <T : Any> addExtension(name: ExtensionPointName<T>, extension: T, disposable: Disposable) {
    val extensionPoint: ExtensionPoint<T> = Extensions.getRootArea().getExtensionPoint(name)
    extensionPoint.registerExtension(extension, disposable)
}
