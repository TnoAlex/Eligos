package com.github.tnoalex.analyzer

import com.github.tnoalex.formatter.FormatterFactory
import com.github.tnoalex.formatter.FormatterTypeEnum
import java.io.File


abstract class AbstractSmellAnalyzer(val supportedLanguages: String) {
    private var context: AnalyzerContext? = null
    abstract fun analyze()

    fun createAnalyticsContext(
        language: String,
        sourcePath: File,
        outputName: String?,
        outputPath: File,
        formatter: FormatterTypeEnum
    ) {
        context = AnalyzerContext(
            language,
            sourcePath,
            outputName ?: "${language}_analysis_result",
            outputPath,
            FormatterFactory.getFormatter(formatter)
        )
    }
}