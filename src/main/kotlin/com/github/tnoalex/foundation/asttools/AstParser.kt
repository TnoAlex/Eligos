package com.github.tnoalex.foundation.asttools

import com.github.tnoalex.foundation.common.LanguageSupportInfo

/**
 * Make sure that the subclasses of this class are global singletons and can be loaded by the classloader at initialization
 *
 * If you use kotlin to implement this interface, we recommend that you use `object` to declare a class
 */
interface AstParser : LanguageSupportInfo {
    fun parseAst(fileName: String)
}