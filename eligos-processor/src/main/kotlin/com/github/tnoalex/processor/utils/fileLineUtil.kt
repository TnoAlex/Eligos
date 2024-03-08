package com.github.tnoalex.processor.utils

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset

val PsiElement.startLine
    get() = lazy {
        fileDocumentManager.getDocument(this.containingFile.virtualFile)?.getLineNumber(this.startOffset)?.and(1) ?: -1
    }.value

val PsiElement.endLine
    get() = lazy {
        fileDocumentManager.getDocument(this.containingFile.virtualFile)?.getLineNumber(this.endOffset)?.and(1) ?: -1
    }.value

val KtFile.lineCount
    get() = lazy {
        fileDocumentManager.getDocument(this.virtualFile)?.lineCount ?: 0
    }.value

val PsiJavaFile.lineCount
    get() = lazy {
        fileDocumentManager.getDocument(this.virtualFile)?.lineCount ?: 0
    }.value

val fileDocumentManager by lazy { FileDocumentManager.getInstance() }