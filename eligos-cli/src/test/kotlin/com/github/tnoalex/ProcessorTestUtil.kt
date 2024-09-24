package com.github.tnoalex

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.issues.ConfidenceLevel
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.parser.CliCompilerEnvironmentContext
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.annotations.TestOnly
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

@TestOnly
fun psiFiles(): ArrayList<PsiFile> {
    val environmentContext = ApplicationContext.getExactBean(CliCompilerEnvironmentContext::class.java)!!
    val psiFiles = ArrayList<PsiFile>()
    psiFiles.addAll(environmentContext.environment.getSourceFiles())
    val psiManager = environmentContext.project.getService(PsiManager::class.java)
    visitVirtualFile(environmentContext.baseDir) {
        if (it.extension == "java") {
            psiManager.findFile(it)?.let { psi ->
                psiFiles.add(psi)
            }
        }
    }
    return psiFiles
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

@TestOnly
@JvmSynthetic
inline fun <reified T : Issue> issue(): List<T> {
    return ApplicationContext.getExactBean(Context::class.java)!!.issues.filterIsInstance<T>()
}


val contextAllowConfidenceLevelProperty by lazy {
    val property1 = Context::class.memberProperties.find { it.name == "allowConfidenceLevel" }
    @Suppress("UNCHECKED_CAST")
    (property1 as KMutableProperty1<Context, ConfidenceLevel>).also { it.isAccessible = true }
}

@TestOnly
fun Context.setConfidenceLevel(confidenceLevel: ConfidenceLevel) {
    contextAllowConfidenceLevelProperty.set(this, confidenceLevel)
}