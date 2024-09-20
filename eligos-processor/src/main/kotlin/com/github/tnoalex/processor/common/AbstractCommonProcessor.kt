package com.github.tnoalex.processor.common

import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.processor.IssueProcessor
import com.github.tnoalex.processor.ShareSpace
import com.intellij.psi.PsiFile

abstract class AbstractCommonProcessor : IssueProcessor {
    protected abstract var processorProvider: AbstractSpecificProcessorProvider

    protected fun invokeSpecificProcessor(psiFile: PsiFile) {
        val language = Language.createFromString(psiFile.language.displayName) ?: return
        if (processorProvider.support(language)) {
            processorProvider.provideProcessor(language)?.process(psiFile, createShearSpace())
        }
    }

    protected abstract fun createShearSpace(): ShareSpace
}



