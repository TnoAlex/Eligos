package com.github.tnoalex.processor

import com.github.tnoalex.foundation.eventbus.EventBus
import com.github.tnoalex.foundation.language.Language
import com.intellij.psi.PsiFile

interface PsiProcessor : BaseProcessor {
    fun process(psiFile: PsiFile)

    fun registerListener() {
        EventBus.register(this)
    }

    fun unregisterListener() {
        EventBus.unregister(this)
    }
}