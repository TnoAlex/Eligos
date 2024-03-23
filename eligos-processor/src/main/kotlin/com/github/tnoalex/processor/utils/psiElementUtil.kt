package com.github.tnoalex.processor.utils

import com.intellij.psi.PsiElement

internal val PsiElement.filePath
    get() = this.containingFile.virtualFile.path