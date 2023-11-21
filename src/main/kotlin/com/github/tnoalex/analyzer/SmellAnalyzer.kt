package com.github.tnoalex.analyzer

import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.foundation.common.LanguageSupportInfo

interface SmellAnalyzer : LanguageSupportInfo {
    fun analyze()

    fun createAnalyticsContext(formatter: FormatterTypeEnum)
}