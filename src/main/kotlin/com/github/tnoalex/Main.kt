package com.github.tnoalex

import com.github.tnoalex.foundation.filetools.FileHelper
import com.github.tnoalex.parser.*
import com.intellij.core.CoreFileTypeRegistry
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.mock.MockApplication
import com.intellij.mock.MockFileDocumentManagerImpl
import com.intellij.mock.MockFileIndexFacade
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.local.CoreLocalFileSystem
import com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.PsiFileFactoryImpl
import com.intellij.psi.impl.PsiManagerImpl
import com.intellij.psi.impl.file.PsiDirectoryFactory
import com.intellij.psi.impl.file.PsiDirectoryFactoryImpl
import org.jetbrains.kotlin.config.JvmTarget
import org.jetbrains.kotlin.config.LanguageVersion
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
    coreFileTypeRegistry.registerFileType(JavaFileType.INSTANCE, "java")
    coreFileTypeRegistry.registerFileType(KotlinFileType.INSTANCE, "kt")
    ApplicationManager.setApplication(application, { coreFileTypeRegistry }, disposer)


    val fileSystem = CoreLocalFileSystem()

    val virtualFile = CoreLocalVirtualFile(fileSystem, path)
    val project = EligosMockProject(null, Disposer.newDisposable())

    project.baseDir = virtualFile

    project.registerService(FileIndexFacade::class.java, MockFileIndexFacade(project))
    val psiManager = PsiManagerImpl(project)

    project.registerService(PsiManager::class.java, psiManager)

    project.registerService(PsiDirectoryFactory::class.java, PsiDirectoryFactoryImpl(project))
    project.registerService(PsiFileFactory::class.java, PsiFileFactoryImpl(psiManager))


    Registry.get("psi.sleep.in.validity.check").setValue(false)

    val psiFileFactory = PsiFileFactory.getInstance(project)

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