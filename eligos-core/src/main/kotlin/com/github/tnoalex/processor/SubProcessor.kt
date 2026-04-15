package com.github.tnoalex.processor

import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.psi.KtFile

interface SubProcessor : BaseProcessor {
    fun process(psiFile: PsiFile, shareSpace: ShareSpace)

    fun processKaa(ktFile: KtFile, shareSpace: ShareSpace)
    {
        process(ktFile as PsiFile, shareSpace)
    }
}