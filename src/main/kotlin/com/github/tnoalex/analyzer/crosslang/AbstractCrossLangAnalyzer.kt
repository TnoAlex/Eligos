package com.github.tnoalex.analyzer.crosslang

import com.github.tnoalex.analyzer.SmellAnalyzer
import com.github.tnoalex.analyzer.singlelang.SingleSmellAnalyzerContainer
import com.github.tnoalex.formatter.FormatterTypeEnum

abstract class AbstractCrossLangAnalyzer : SmellAnalyzer {
    override fun analyze() {

    }

    override fun createAnalyticsContext(formatter: FormatterTypeEnum) {
        supportLanguage.forEach {
            SingleSmellAnalyzerContainer.getByKey(it)!!.createAnalyticsContext(formatter)
            SingleSmellAnalyzerContainer.getByKey(it)!!.analyze()
        }
    }
}