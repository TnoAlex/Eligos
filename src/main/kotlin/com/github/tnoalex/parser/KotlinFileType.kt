package com.github.tnoalex.parser

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.util.NotNullLazyValue
import org.jetbrains.kotlin.idea.KotlinIconProviderService

class KotlinFileType private constructor() : LanguageFileType(KotlinLanguage.INSTANCE) {
    private val myIcon = NotNullLazyValue.lazy {
        KotlinIconProviderService.getInstance().fileIcon
    }

    override fun getName() = "KOTLIN"

    override fun getDescription() = "Kotlin"

    override fun getDefaultExtension() = DEFAULT_EXTENSION

    override fun getIcon() = myIcon.value

    companion object {
        @JvmStatic
        val DEFAULT_EXTENSION = "kt"

        @JvmStatic
        val DOT_DEFAULT_EXTENSION = ".kt"

        @JvmStatic
        val INSTANCE = KotlinFileType()
    }
}