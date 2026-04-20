package com.github.tnoalex

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.parser.CliCompilerEnvironmentContext
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.annotations.TestOnly

@TestOnly
fun psiFiles(): List<PsiFile> {
    val environmentContext = ApplicationContext.getExactBean(CliCompilerEnvironmentContext::class.java)!!
    return environmentContext.allSourceFiles
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
