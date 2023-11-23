package com.github.tnoalex.foundation.asttools.processor

class BaseInfoProcessor : AstProcessor {
    override val order: Int
        get() = Int.MAX_VALUE

    override fun registerListener() {
        TODO("Not yet implemented")
    }

    override fun unregisterListener() {
        TODO("Not yet implemented")
    }

    override val supportLanguage: List<String>
        get() = listOf("kotlin", "java")
}