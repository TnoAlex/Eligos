package com.github.tnoalex.parser

import com.github.tnoalex.events.AllFileParsedEvent
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventBus
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.slf4j.LoggerFactory


@Component(order = Short.MAX_VALUE.toInt())
class CliFileDistributor : FileDistributor {
    override val supportLanguage: List<String>
        get() = listOf("java", "kotlin")

    private lateinit var psiManager: PsiManager
    override val launchEnvironment: LaunchEnvironment
        get() = LaunchEnvironment.CLI

    override fun init() {
        with(ApplicationContext.getExactBean(CliCompilerEnvironmentContext::class.java)!!) {
            psiManager = project.getService(PsiManager::class.java)
        }
    }

    override fun dispatch() {
        val environment = ApplicationContext.getExactBean(CliCompilerEnvironmentContext::class.java)!!
        environment.environment.getSourceFiles().forEach {
            logger.info("Dispatching ${it.virtualFile.path}")
            EventBus.post(it)
        }
        val baseDir = environment.baseDir
        baseDir.refresh(false, true)
        visitVirtualFile(baseDir) {
            if (it.extension == "java") {
                psiManager.findFile(it)?.let { psi ->
                    logger.info("Dispatching ${psi.virtualFile.path}")
                    EventBus.post(psi)
                }
            }
        }
        EventBus.post(AllFileParsedEvent)
    }

    override fun virtualFileConvert(virtualFile: Any): PsiFile {
        require(virtualFile is VirtualFile)
        return psiManager.findFile(virtualFile)
            ?: throw RuntimeException("Can not find psi file with path: ${virtualFile.path}")
    }

    private fun visitVirtualFile(virtualFile: VirtualFile, visitor: (file: VirtualFile) -> Unit) {
        if (virtualFile.isDirectory) {
            virtualFile.children.forEach {
                visitVirtualFile(it, visitor)
            }
        } else {
            visitor(virtualFile)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CliFileDistributor::class.java)
    }

}