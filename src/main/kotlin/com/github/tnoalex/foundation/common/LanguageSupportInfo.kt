package com.github.tnoalex.foundation.common

/**
 * Identifies the languages supported by the language-sensitive component in the analyzer,
 * which when its value is 'any', indicating that it supports all languages
 */
interface LanguageSupportInfo {
    val supportLanguage: List<String>
        get() = listOf("any")
}