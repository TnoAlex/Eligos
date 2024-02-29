package com.github.tnoalex.processor.kotlinjava

import com.github.tnoalex.processor.PsiProcessorWithContext

class UnclearPlatformTypeProcessor : PsiProcessorWithContext() {
    override val supportLanguage: List<String>
        get() = listOf("java","kotlin")
}