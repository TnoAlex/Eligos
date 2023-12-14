package com.github.tnoalex.processor.cross.kotlinjava

import com.github.tnoalex.processor.AstProcessorWithContext

class UnclearPlatformTypeProcessor : AstProcessorWithContext() {
    override val order: Int
        get() = -1
    override val supportLanguage: List<String>
        get() = listOf("java","kotlin")
}