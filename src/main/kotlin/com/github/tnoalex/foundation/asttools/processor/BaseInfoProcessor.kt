package com.github.tnoalex.foundation.asttools.processor

class BaseInfoProcessor : AstProcessor {
    override val order: Int
        get() = Int.MAX_VALUE

    override fun hookAst() {
        TODO("Not yet implemented")
    }

    override fun removeHooks() {
        TODO("Not yet implemented")
    }

    override val supportLanguage: List<String>
        get() = listOf("kotlin", "java")
}