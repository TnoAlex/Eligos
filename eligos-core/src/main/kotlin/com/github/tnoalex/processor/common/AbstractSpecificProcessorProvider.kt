package com.github.tnoalex.processor.common

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.language.LanguageSupportInfo
import com.github.tnoalex.foundation.cache.LRUCache
import com.github.tnoalex.foundation.language.Language
import com.github.tnoalex.processor.SubProcessor

abstract class AbstractSpecificProcessorProvider : LanguageSupportInfo {
    private val cache = LRUCache<String, SubProcessor>(4)
    abstract val commonProcessorName: String

    fun provideProcessor(language: Language): SubProcessor? {
        return locateProcessor(language)
    }

    open fun support(language: Language): Boolean {
        return language in supportLanguage || Language.AnyLanguage in supportLanguage
    }

    open fun createProcessorName(language: Language): String {
        return language.asString() + commonProcessorName
    }

    private fun locateProcessor(language: Language): SubProcessor? {
        val name = createProcessorName(language)
        return if (cache.containsKey(name)) {
            cache[name]!!
        } else {
            val processor = ApplicationContext.getBean(name) as? SubProcessor ?: return null
            cache[name] = processor
            processor
        }
    }
}