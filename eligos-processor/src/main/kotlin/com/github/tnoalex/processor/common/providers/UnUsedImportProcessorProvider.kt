package com.github.tnoalex.processor.common.providers

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.processor.common.AbstractSpecificProcessorProvider

@Component
class UnUsedImportProcessorProvider : AbstractSpecificProcessorProvider() {
    override val commonProcessorName: String
        get() = "UnUsedImportProcessor"
    override val supportLanguage: List<Language>
        get() = listOf(JavaLanguage, KotlinLanguage)
}