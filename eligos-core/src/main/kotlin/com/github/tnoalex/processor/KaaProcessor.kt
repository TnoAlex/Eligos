package com.github.tnoalex.processor

import com.github.tnoalex.processor.BaseProcessor
import com.github.tnoalex.foundation.eventbus.EventBus
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtFile

interface KaaProcessor : BaseProcessor {
    fun process(ktFile: KtFile)
    
    fun registerListener() {
        EventBus.register(this)
    }
    
    fun unregisterListener() {
        EventBus.unregister(this)
    }
}