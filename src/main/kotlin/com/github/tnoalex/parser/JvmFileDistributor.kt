package com.github.tnoalex.parser

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.environment.JvmCompilerEnvironmentContext
import com.github.tnoalex.foundation.eventbus.EventBus
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.com.intellij.psi.PsiManager


@Component(order = Short.MAX_VALUE.toInt())
class JvmFileDistributor : FileDistributor {
    override val supportLanguage: List<String>
        get() = listOf("java", "kotlin")

    private lateinit var psiManager: PsiManager

    override fun init() {
        with(ApplicationContext.getExactBean(JvmCompilerEnvironmentContext::class.java)!!) {
            psiManager = project.getService(PsiManager::class.java)
        }
    }

    override fun dispatch() {
        val baseDir = ApplicationContext.getExactBean(JvmCompilerEnvironmentContext::class.java)?.baseDir
            ?: throw RuntimeException("Can not find the project of ${this::class.java.typeName}")
        baseDir.refresh(false, true)
        visitVirtualFile(baseDir) {
            psiManager.findFile(it)?.let { psi ->
                EventBus.post(psi)
            }
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

}