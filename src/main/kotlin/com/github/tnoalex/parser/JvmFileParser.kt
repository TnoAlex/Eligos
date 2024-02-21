package com.github.tnoalex.parser

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.environment.JvmCompilerEnvironmentContext
import org.jetbrains.kotlin.com.intellij.ide.highlighter.ArchiveFileType
import org.jetbrains.kotlin.com.intellij.ide.highlighter.JavaClassFileType
import org.jetbrains.kotlin.com.intellij.ide.highlighter.JavaFileType
import org.jetbrains.kotlin.com.intellij.lang.java.JavaParserDefinition
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.parsing.KotlinParserDefinition
import java.io.File


@Component(order = Short.MAX_VALUE.toInt())
class JvmFileParser : FileParser {
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
        }
    }

    override fun parseFile(file: File): PsiFile? {
        val environmentContext = ApplicationContext.getBean(JvmCompilerEnvironmentContext::class.java).first()
        val virtualFile = environmentContext.createVirtualFile(file)
        val psiManager = environmentContext.project.getService(PsiManager::class.java)
        return psiManager.findFile(virtualFile)
    }

}