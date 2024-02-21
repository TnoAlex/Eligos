package com.github.tnoalex.foundation.environment

import com.github.tnoalex.foundation.bean.Component
import org.jetbrains.kotlin.com.intellij.core.CoreEncodingProjectManager
import org.jetbrains.kotlin.com.intellij.core.CoreFileTypeRegistry
import org.jetbrains.kotlin.com.intellij.core.CoreJavaPsiImplementationHelper
import org.jetbrains.kotlin.com.intellij.lang.LanguageParserDefinitions
import org.jetbrains.kotlin.com.intellij.lang.ParserDefinition
import org.jetbrains.kotlin.com.intellij.lang.PsiBuilderFactory
import org.jetbrains.kotlin.com.intellij.lang.impl.PsiBuilderFactoryImpl
import org.jetbrains.kotlin.com.intellij.mock.MockApplication
import org.jetbrains.kotlin.com.intellij.mock.MockFileDocumentManagerImpl
import org.jetbrains.kotlin.com.intellij.mock.MockFileIndexFacade
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.application.ApplicationManager
import org.jetbrains.kotlin.com.intellij.openapi.editor.impl.DocumentImpl
import org.jetbrains.kotlin.com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.kotlin.com.intellij.openapi.fileTypes.FileType
import org.jetbrains.kotlin.com.intellij.openapi.progress.ProgressManager
import org.jetbrains.kotlin.com.intellij.openapi.progress.impl.CoreProgressManager
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.roots.FileIndexFacade
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.util.registry.Registry
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.com.intellij.openapi.vfs.encoding.EncodingManager
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalFileSystem
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import org.jetbrains.kotlin.com.intellij.psi.JavaPsiFacade
import org.jetbrains.kotlin.com.intellij.psi.JvmPsiConversionHelper
import org.jetbrains.kotlin.com.intellij.psi.PsiDocumentManager
import org.jetbrains.kotlin.com.intellij.psi.PsiElementFactory
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.psi.impl.*
import org.jetbrains.kotlin.com.intellij.psi.impl.file.PsiDirectoryFactory
import org.jetbrains.kotlin.com.intellij.psi.impl.file.PsiDirectoryFactoryImpl
import org.jetbrains.kotlin.com.intellij.psi.stubs.CoreStubTreeLoader
import org.jetbrains.kotlin.com.intellij.psi.stubs.StubTreeLoader
import org.jetbrains.kotlin.com.intellij.psi.util.CachedValuesManager
import org.jetbrains.kotlin.com.intellij.util.CachedValuesManagerImpl
import java.io.File

@Component(order = Int.MAX_VALUE)
class JvmCompilerEnvironmentContext : CompilerEnvironmentContext {
    val disposer = Disposer.newDisposable()
    val project = MockProject(null, disposer)
    val psiApplication get() = ApplicationManager.getApplication()

    private val fileSystem = CoreLocalFileSystem()
    private val coreFileTypeRegistry = CoreFileTypeRegistry()

    override fun registerCoreFileType(fileType: Any, extension: String) {
        require(fileType is FileType) { "Unsupported file type:‘${fileType::class.qualifiedName}}’" }
        coreFileTypeRegistry.registerFileType(fileType, extension)
    }

    override fun registerParserDefinition(definition: Any, disposable: Any) {
        require(definition is ParserDefinition) { "Unsupported definition type:‘${definition::class.qualifiedName}}’" }
        require(disposable is Disposable) { "Unsupported disposable type:‘${disposable::class.qualifiedName}}’" }

        LanguageParserDefinitions.INSTANCE.addExplicitExtension(
            definition.fileNodeType.language,
            definition,
            disposable
        )
    }

    override fun initPsiApplication() {
        val application = MockApplication(disposer)
        application.registerService(
            FileDocumentManager::class.java, MockFileDocumentManagerImpl(
                null
            ) { chars: CharSequence ->
                DocumentImpl(
                    chars
                )
            }
        )
        ApplicationManager.setApplication(application, { coreFileTypeRegistry }, disposer)

        application.registerService(PsiManager::class.java, PsiManagerImpl(project))
        application.registerService(EncodingManager::class.java, CoreEncodingProjectManager())
        application.registerService(StubTreeLoader::class.java, CoreStubTreeLoader())
        application.registerService(PsiBuilderFactory::class.java, PsiBuilderFactoryImpl())
        application.registerService(ProgressManager::class.java, CoreProgressManager())
    }

    override fun initPsiProject() {
        project.registerService(FileIndexFacade::class.java, MockFileIndexFacade(project))

        val psiManager = psiApplication.getService(PsiManager::class.java)
        project.registerService(PsiManager::class.java, psiManager)
        project.registerService(PsiDirectoryFactory::class.java, PsiDirectoryFactoryImpl(project))
        project.registerService(PsiFileFactory::class.java, PsiFileFactoryImpl(psiManager))
        project.registerService(
            CachedValuesManager::class.java,
            CachedValuesManagerImpl(project, PsiCachedValuesFactory(project))
        )
        val psiDocumentManager = JvmMockPsiDocumentManager(project)
        project.registerService(PsiDocumentManager::class.java, psiDocumentManager)
        (psiApplication as MockApplication).registerService(PsiDocumentManager::class.java, psiDocumentManager)

        project.registerService(JavaPsiImplementationHelper::class.java, CoreJavaPsiImplementationHelper(project))
        project.registerService(JvmPsiConversionHelper::class.java,JvmPsiConversionHelperImpl())
        project.registerService(PsiElementFactory::class.java,PsiElementFactoryImpl(project))
        project.registerService(JavaPsiFacade::class.java, JavaPsiFacadeImpl(project))

        Registry.get("psi.sleep.in.validity.check").setValue(false)
        Registry.get("psi.incremental.reparse.depth.limit").setValue("6")
    }

    override fun createVirtualFile(file: File): VirtualFile {
        return CoreLocalVirtualFile(fileSystem, file)
    }

    class JvmMockPsiDocumentManager(project: Project) : PsiDocumentManagerBase(project)
}