package com.github.tnoalex.processor.kotlin.withJava

import com.github.tnoalex.processor.PsiProcessor


class UnclearPlatformTypeProcessor : PsiProcessor {
    override val supportLanguage: List<String>
        get() = listOf("java", "kotlin")
}