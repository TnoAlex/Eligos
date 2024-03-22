package com.github.tnoalex.processor.utils

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset

val PsiElement.startLine: Int
    get() {
        return fileDocumentManager.getDocument(this.containingFile.virtualFile)?.getLineNumber(this.startOffset)
            ?.let { it + 1 } ?: 0
    }

val PsiElement.endLine: Int
    get() {
        return fileDocumentManager.getDocument(this.containingFile.virtualFile)?.getLineNumber(this.endOffset)
            ?.let { it + 1 } ?: 0
    }

val KtFile.lineCount: Int
    get() {
        return fileDocumentManager.getDocument(this.virtualFile)?.lineCount?.let { it + 1 } ?: 0
    }

val PsiJavaFile.lineCount: Int
    get() {
        return fileDocumentManager.getDocument(this.virtualFile)?.lineCount?.let { it + 1 } ?: 0
    }

val fileDocumentManager by lazy { FileDocumentManager.getInstance() }