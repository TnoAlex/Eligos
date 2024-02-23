package com.github.tnoalex.foundation.environment

import com.github.tnoalex.foundation.bean.Component
import org.jetbrains.kotlin.com.intellij.codeInsight.folding.CodeFoldingSettings
import org.jetbrains.kotlin.com.intellij.concurrency.JobLauncher
import org.jetbrains.kotlin.com.intellij.core.*
import org.jetbrains.kotlin.com.intellij.lang.*
import org.jetbrains.kotlin.com.intellij.lang.impl.PsiBuilderFactoryImpl
import org.jetbrains.kotlin.com.intellij.lang.jvm.facade.JvmFacade
import org.jetbrains.kotlin.com.intellij.lang.jvm.facade.JvmFacadeImpl
import org.jetbrains.kotlin.com.intellij.mock.*
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.application.ApplicationInfo
import org.jetbrains.kotlin.com.intellij.openapi.application.ApplicationManager
import org.jetbrains.kotlin.com.intellij.openapi.application.impl.ApplicationInfoImpl
import org.jetbrains.kotlin.com.intellij.openapi.command.CommandProcessor
import org.jetbrains.kotlin.com.intellij.openapi.command.impl.CoreCommandProcessor
import org.jetbrains.kotlin.com.intellij.openapi.editor.impl.DocumentImpl
import org.jetbrains.kotlin.com.intellij.openapi.extensions.ExtensionPoint
import org.jetbrains.kotlin.com.intellij.openapi.extensions.ExtensionPointName
import org.jetbrains.kotlin.com.intellij.openapi.extensions.Extensions
import org.jetbrains.kotlin.com.intellij.openapi.extensions.ExtensionsArea
import org.jetbrains.kotlin.com.intellij.openapi.fileEditor.FileDocumentManager
import org.jetbrains.kotlin.com.intellij.openapi.fileTypes.FileType
import org.jetbrains.kotlin.com.intellij.openapi.progress.ProgressManager
import org.jetbrains.kotlin.com.intellij.openapi.progress.impl.CoreProgressManager
import org.jetbrains.kotlin.com.intellij.openapi.project.DumbService
import org.jetbrains.kotlin.com.intellij.openapi.project.DumbUtil
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.roots.FileIndexFacade
import org.jetbrains.kotlin.com.intellij.openapi.roots.LanguageLevelProjectExtension
import org.jetbrains.kotlin.com.intellij.openapi.roots.PackageIndex
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.util.registry.Registry
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFileManagerListener
import org.jetbrains.kotlin.com.intellij.openapi.vfs.encoding.EncodingManager
import org.jetbrains.kotlin.com.intellij.openapi.vfs.impl.CoreVirtualFilePointerManager
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalFileSystem
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalVirtualFile
import org.jetbrains.kotlin.com.intellij.openapi.vfs.pointers.VirtualFilePointerManager
import org.jetbrains.kotlin.com.intellij.psi.*
import org.jetbrains.kotlin.com.intellij.psi.codeStyle.JavaCodeStyleManager
import org.jetbrains.kotlin.com.intellij.psi.codeStyle.JavaCodeStyleSettingsFacade
import org.jetbrains.kotlin.com.intellij.psi.controlFlow.ControlFlowFactory
import org.jetbrains.kotlin.com.intellij.psi.impl.*
import org.jetbrains.kotlin.com.intellij.psi.impl.file.PsiDirectoryFactory
import org.jetbrains.kotlin.com.intellij.psi.impl.file.PsiDirectoryFactoryImpl
import org.jetbrains.kotlin.com.intellij.psi.impl.file.impl.JavaFileManager
import org.jetbrains.kotlin.com.intellij.psi.impl.source.resolve.JavaResolveCache
import org.jetbrains.kotlin.com.intellij.psi.impl.source.resolve.PsiResolveHelperImpl
import org.jetbrains.kotlin.com.intellij.psi.impl.source.resolve.ResolveCache
import org.jetbrains.kotlin.com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import org.jetbrains.kotlin.com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistryImpl
import org.jetbrains.kotlin.com.intellij.psi.search.ProjectScopeBuilder
import org.jetbrains.kotlin.com.intellij.psi.stubs.CoreStubTreeLoader
import org.jetbrains.kotlin.com.intellij.psi.stubs.StubTreeLoader
import org.jetbrains.kotlin.com.intellij.psi.util.CachedValuesManager
import org.jetbrains.kotlin.com.intellij.util.CachedValuesManagerImpl
import org.jetbrains.kotlin.com.intellij.util.KeyedLazyInstanceEP
import org.jetbrains.kotlin.com.intellij.util.graph.GraphAlgorithms
import org.jetbrains.kotlin.com.intellij.util.graph.impl.GraphAlgorithmsImpl
import java.io.File
import java.lang.reflect.Modifier

@Component(order = Int.MAX_VALUE)
class JvmCompilerEnvironmentContext : CompilerEnvironmentContext {
    val disposer = Disposer.newDisposable()
    val psiApplication get() = ApplicationManager.getApplication()

    val fileSystem = CoreLocalFileSystem()
    private val coreFileTypeRegistry = CoreFileTypeRegistry()
    val project = JvmMockProject(disposer)

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
        addExtension(
            Extensions.getRootArea(),
            ExtensionPointName<Any?>("org.jetbrains.kotlin.com.intellij.virtualFileManagerListener").name,
            VirtualFileManagerListener::class.java
        )
        addExtension(
            Extensions.getRootArea(),
            ExtensionPointName<Any?>("org.jetbrains.kotlin.com.intellij.virtualFileSystem").name,
            KeyedLazyInstanceEP::class.java
        )
        application.registerService(PsiManager::class.java,PsiManagerImpl(project))
        application.registerService(EncodingManager::class.java, CoreEncodingRegistry())
        application.registerService(VirtualFilePointerManager::class.java, CoreVirtualFilePointerManager())
        application.registerService(DefaultASTFactory::class.java, DefaultASTFactoryImpl())
        application.registerService(PsiBuilderFactory::class.java, PsiBuilderFactoryImpl())
        application.registerService(ReferenceProvidersRegistry::class.java, ReferenceProvidersRegistryImpl())
        application.registerService(StubTreeLoader::class.java, CoreStubTreeLoader())
        application.registerService(PsiReferenceService::class.java, PsiReferenceServiceImpl())
        application.registerService(ProgressManager::class.java, CoreProgressManager())
        application.registerService(JobLauncher::class.java, object : JobLauncher() {})
        application.registerService(CodeFoldingSettings::class.java, CodeFoldingSettings())
        application.registerService(CommandProcessor::class.java, CoreCommandProcessor())
        application.registerService(GraphAlgorithms::class.java, GraphAlgorithmsImpl())
        application.registerService(ApplicationInfo::class.java, ApplicationInfoImpl::class.java)
    }

    override fun initPsiProject() {
        val fileIndexFacade = MockFileIndexFacade(project)
        project.registerService(FileIndexFacade::class.java, fileIndexFacade)

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
        project.registerService(JvmPsiConversionHelper::class.java, JvmPsiConversionHelperImpl())
        project.registerService(PsiElementFactory::class.java, PsiElementFactoryImpl(project))

        project.registerService(JavaPsiFacade::class.java, JavaPsiFacadeImpl(project))
        project.registerService(ResolveScopeManager::class.java, MockResolveScopeManager(project))
        project.registerService(ProjectScopeBuilder::class.java, CoreProjectScopeBuilder(project, fileIndexFacade))
        project.registerService(DumbService::class.java, MockDumbService(project))
        project.registerService(DumbUtil::class.java, MockDumbUtil())

        project.registerService(ResolveCache::class.java, ResolveCache(project))
        project.registerService(PsiResolveHelper::class.java, PsiResolveHelperImpl(project))
        project.registerService(LanguageLevelProjectExtension::class.java, CoreLanguageLevelProjectExtension())
        project.registerService(JavaResolveCache::class.java, JavaResolveCache(project))
        project.registerService(ControlFlowFactory::class.java, ControlFlowFactory(project))
        project.registerService(PackageIndex::class.java,CorePackageIndex())
        project.registerService(JavaFileManager::class.java, CoreJavaFileManager(psiManager))
        project.registerService(JvmFacade::class.java, JvmFacadeImpl(project, project.messageBus))


        Registry.get("psi.sleep.in.validity.check").setValue(false)
        Registry.get("psi.incremental.reparse.depth.limit").setValue("6")
    }

    override fun createVirtualFile(file: File): VirtualFile {
        return CoreLocalVirtualFile(fileSystem, file)
    }

    fun setProjectDir(dir: File) {
        val baseDir = CoreLocalVirtualFile(fileSystem, dir)
        project.baseDir = baseDir
    }

    private fun <T : Any> addExtension(area: ExtensionsArea, name: String, aClass: Class<out T>) {
        if (!area.hasExtensionPoint(name)) {
            val kind =
                if (!aClass.isInterface && !Modifier.isAbstract(aClass.modifiers)) ExtensionPoint.Kind.BEAN_CLASS else ExtensionPoint.Kind.INTERFACE
            area.registerExtensionPoint(name, aClass.name, kind, false)
        }
    }


    class JvmMockPsiDocumentManager(project: Project) : PsiDocumentManagerBase(project)
}