package com.github.tnoalex.plugin.parser

import com.github.tnoalex.events.AllFileParsedEvent
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.bean.container.SimpleSingletonBeanContainer
import com.github.tnoalex.foundation.eventbus.EventBus
import com.github.tnoalex.parser.FileDistributor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.VirtualFileVisitor
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.references.fe10.base.KtFe10ReferenceResolutionHelper
import kotlin.io.path.Path

@Component(order = Short.MAX_VALUE.toInt())
class IdePluginFileDistributor : FileDistributor {
    override val supportLanguage: List<String>
        get() = listOf("java", "kotlin")
    override val launchEnvironment: LaunchEnvironment
        get() = LaunchEnvironment.IDE_PLUGIN

    private lateinit var psiManager: PsiManager
    private var projectFiles: VirtualFile? = null
    private var fileIndex: ProjectFileIndex? = null

    override fun init() {}

    fun initPsiManager(project: Project) {
        psiManager = project.getService(PsiManager::class.java)
        projectFiles = VirtualFileManager.getInstance().findFileByNioPath(Path(project.basePath ?: return))
        fileIndex = ProjectFileIndex.getInstance(project)
        val resolutionHelper =
            ApplicationManager.getApplication().getService(KtFe10ReferenceResolutionHelper::class.java)
        ApplicationContext.addBean(
            resolutionHelper::class.java.simpleName,
            resolutionHelper,
            SimpleSingletonBeanContainer
        )
    }

    override fun dispatch() {
        projectFiles?.refresh(true, true)
        VfsUtilCore.visitChildrenRecursively(projectFiles ?: return, object : VirtualFileVisitor<Unit>() {
            override fun visitFile(file: VirtualFile): Boolean {
                if (fileIndex!!.isExcluded(file)) return false
                if (!file.isDirectory && (file.extension == "java" || file.extension == "kt")) {
                    val psiFile = psiManager.findFile(file) ?: return true
                    EventBus.post(psiFile)
                }
                return true
            }
        })
        EventBus.post(AllFileParsedEvent)
    }

    override fun virtualFileConvert(virtualFile: Any): Any {
        require(virtualFile is VirtualFile)
        return psiManager.findFile(virtualFile)
            ?: throw RuntimeException("Can not find psi file with path: ${virtualFile.path}")
    }
}