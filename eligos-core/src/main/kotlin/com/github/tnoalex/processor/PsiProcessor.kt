package com.github.tnoalex.processor

import com.github.tnoalex.foundation.eventbus.EventBus
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.analysis.api.KaSession

interface PsiProcessor : BaseProcessor {
    fun process(psiFile: PsiFile)

    fun <R> analyze(action: KaSession.() -> R): R {
        return context.session!!.action()
    }

    fun registerListener() {
        EventBus.register(this)
    }

    fun unregisterListener() {
        EventBus.unregister(this)
    }
}
