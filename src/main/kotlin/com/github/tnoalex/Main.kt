package com.github.tnoalex

import com.github.tnoalex.foundation.filetools.FileHelper
import com.github.tnoalex.parser.*
import com.intellij.codeInsight.ContainerProvider
import com.intellij.codeInsight.JavaContainerProvider
import com.intellij.core.CoreApplicationEnvironment
import com.intellij.core.CoreFileTypeRegistry
import com.intellij.ide.highlighter.ArchiveFileType
import com.intellij.ide.highlighter.JavaClassFileType
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.*
import com.intellij.lang.java.JavaLanguage
import com.intellij.lang.java.JavaParserDefinition
import com.intellij.mock.MockApplication
import com.intellij.mock.MockFileDocumentManagerImpl
import com.intellij.mock.MockFileIndexFacade
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.extensions.ExtensionPoint
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.extensions.Extensions
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeExtension
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.fileTypes.PlainTextParserDefinition
import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.local.CoreLocalFileSystem
import com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import com.intellij.psi.*
import com.intellij.psi.impl.LanguageConstantExpressionEvaluator
import com.intellij.psi.impl.PsiExpressionEvaluator
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.compiled.ClassFileStubBuilder
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.intellij.psi.impl.file.PsiDirectoryFactoryImpl
import com.intellij.psi.impl.source.tree.JavaASTFactory
import com.intellij.psi.impl.source.tree.PlainTextASTFactory
import com.intellij.psi.stubs.BinaryFileStubBuilder
import com.intellij.psi.stubs.BinaryFileStubBuilders
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.CachedValuesManagerImpl
import org.jetbrains.kotlin.com.intellij.lang.ASTFactory
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.LanguageVersion
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
    val path = File("E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\unclearPlatformType").toPath()
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

    val virtualFile = CoreLocalVirtualFile(fileSystem, path)
    val project = EligosMockProject(null, Disposer.newDisposable())

    project.baseDir = virtualFile

    project.registerService(FileIndexFacade::class.java, MockFileIndexFacade(project))
    val psiManager = PsiManagerImpl(project)

    project.registerService(PsiManager::class.java, psiManager)

    project.registerService(PsiDirectoryFactory::class.java, PsiDirectoryFactoryImpl(project))
    project.registerService(PsiFileFactory::class.java, PsiFileFactoryImpl(psiManager))
    project.registerService(CachedValuesManager::class.java, CachedValuesManagerImpl(project))


    Registry.get("psi.sleep.in.validity.check").setValue(false)


    val fileHelper = FileHelper()
    fileHelper.setFileInfo(path.toFile(), null, null)
    val psis = ArrayList<PsiFile?>()
    fileHelper.visitSourcesFile { file ->
        psis.add(psiManager.findFile(CoreLocalVirtualFile(fileSystem, file.toPath())))
    }

    val dir = psiManager.findDirectory(virtualFile)

    val parser = KtParser(kotlinCoreEnvironment)
    val ktFiles = ArrayList<KtFile>()

    fileHelper.visitSourcesFile { file ->
        ktFiles.add(parser.parseKotlinFile(file.toPath()))
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
