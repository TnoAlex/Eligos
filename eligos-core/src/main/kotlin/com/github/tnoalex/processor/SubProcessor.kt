package com.github.tnoalex.processor

import com.intellij.psi.PsiFile

interface SubProcessor : BaseProcessor {
    fun process(psiFile: PsiFile, shareSpace: ShareSpace)
}