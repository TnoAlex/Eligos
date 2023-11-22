package com.github.tnoalex.foundation.asttools.processor

import com.github.tnoalex.foundation.common.LanguageSupportInfo

interface AstProcessor : LanguageSupportInfo {
    val order: Int
        get() = 0

    fun hookAst()
    fun removeHooks()
}