package com.github.tnoalex.parser

import com.intellij.lang.Language
import com.intellij.lang.jvm.JvmLanguage

class KotlinLanguage private constructor() :
    Language("KOTLIN", "text/x-kotlin-source", "text/kotlin", "application/x-kotlin", "text/x-kotlin"), JvmLanguage {

    override fun getDisplayName() = "Kotlin"
    override fun isCaseSensitive() = true

    companion object {
        @JvmStatic
        val INSTANCE = KotlinLanguage()
    }
}