package com.github.tnoalex.processor

import com.github.tnoalex.Context
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.language.LanguageSupportInfo

interface BaseProcessor : LanguageSupportInfo {
    val context: Context
        get() = ApplicationContext.getExactBean(Context::class.java)!!
}