package com.github.tnoalex.foundation.language

/**
 * Identifies the languages supported by the language-sensitive component in the analyzer,
 * which when its value is 'any', indicating that it supports all languages
 */
interface LanguageSupportInfo {
    val supportLanguage: List<Language>
        get() = listOf(Language.AnyLanguage)
}