package com.github.tnoalex.parser

import com.github.tnoalex.Context
import com.github.tnoalex.events.AllFileParsedEvent
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.LaunchEnvironment
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.eventbus.EventBus
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.analysis.api.analyze
import org.slf4j.LoggerFactory


@Component(order = Short.MAX_VALUE.toInt())
class CliFileDistributor : FileDistributor {
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage, KotlinLanguage)

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
        analyze(environment.module) {
            val context = ApplicationContext.getExactBean(Context::class.java)!!
            context.session = this
            environment.ktSourceFiles.forEach {
                logger.debug("Dispatching Kotlin File: ${it.virtualFile.path}")
                EventBus.post(it)
            }
            environment.javaSourceFiles.forEach {
                logger.debug("Dispatching Java File: ${it.virtualFile.path}")
                EventBus.post(it)
            }
            EventBus.post(AllFileParsedEvent)
            context.session = null
        }
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