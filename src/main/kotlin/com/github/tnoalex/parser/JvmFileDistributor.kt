package com.github.tnoalex.parser

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.environment.JvmCompilerEnvironmentContext
import com.github.tnoalex.foundation.eventbus.EventBus
import org.jetbrains.kotlin.com.intellij.ide.highlighter.ArchiveFileType
import org.jetbrains.kotlin.com.intellij.ide.highlighter.JavaClassFileType
import org.jetbrains.kotlin.com.intellij.ide.highlighter.JavaFileType
import org.jetbrains.kotlin.com.intellij.lang.java.JavaParserDefinition
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.parsing.KotlinParserDefinition


@Component(order = Short.MAX_VALUE.toInt())
class JvmFileDistributor : FileDistributor {
    private val psiManager: PsiManager

    init {
        val environmentContext = ApplicationContext.getBean(JvmCompilerEnvironmentContext::class.java).first()
        with(environmentContext) {
            initPsiApplication()
            arrayOf(JavaParserDefinition(), KotlinParserDefinition()).forEach {
                registerParserDefinition(it, disposer)
            }
            mapOf(
                JavaClassFileType.INSTANCE to "class",
                JavaFileType.INSTANCE to "java",
                KotlinFileType.INSTANCE to "kt",
                ArchiveFileType.INSTANCE to "jar;zip"
            ).forEach { (k, v) ->
                registerCoreFileType(k, v)
            }
            initPsiProject()
            psiManager = psiApplication.getService(PsiManager::class.java)
        }
    }

    override fun dispatch() {
        val project = ApplicationContext.getExactBean(JvmCompilerEnvironmentContext::class.java)?.project
            ?: throw RuntimeException("Can not find the project of ${this::class.java.typeName}")
        project.projectFile.refresh(false, true)
        visitVirtualFile(project.projectFile) {
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