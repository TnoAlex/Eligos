package com.github.tnoalex.foundation.asttools

import com.github.tnoalex.foundation.common.LanguageSupportInfo

interface AstProcessor : LanguageSupportInfo {
    fun hookAst()
    fun removeHooks()
}