package com.github.tnoalex.processor.utils

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset

val PsiElement.startLine
    get() = lazy {
        fileDocumentManager.getDocument(this.containingFile.virtualFile)?.getLineNumber(this.startOffset) ?: -1
    }.value

val PsiElement.endLine
    get() = lazy {
        fileDocumentManager.getDocument(this.containingFile.virtualFile)?.getLineNumber(this.endOffset) ?: -1
    }.value

val fileDocumentManager by lazy { FileDocumentManager.getInstance() }