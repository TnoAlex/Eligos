package com.github.tnoalex.processor.common.providers

import com.github.tnoalex.foundation.bean.Component
import com.github.tnoalex.foundation.language.JavaLanguage
import com.github.tnoalex.foundation.language.KotlinLanguage
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.processor.common.AbstractSpecificProcessorProvider

@Component
class TooManyParametersProcessorProvider : AbstractSpecificProcessorProvider() {
    override val commonProcessorName: String
        get() = "TooManyParametersProcessor"
    override val supportLanguage: List<Language>
        get() = listOf(KotlinLanguage, JavaLanguage)
}