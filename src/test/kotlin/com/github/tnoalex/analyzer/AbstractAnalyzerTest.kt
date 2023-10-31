package com.github.tnoalex.analyzer

import com.github.tnoalex.entity.enums.FormatterTypeEnum
import com.github.tnoalex.utils.StdOutErrWrapper
import java.io.File

abstract class AbstractAnalyzerTest(lang: String) {
    var analyzer: AbstractSmellAnalyzer? = null

    init {
        StdOutErrWrapper.init()
        SmellAnalyzerRegister.INSTANCE.init()
        analyzer = SmellAnalyzerRegister.INSTANCE.getAnalyzerByLang(lang)
    }

    fun createTestContext(
        lang: String,
        srcPath: String,
        outputName: String,
        outPath: String,
        outFormat: FormatterTypeEnum
    ) {
        analyzer!!.createAnalyticsContext(lang, File(srcPath), outputName, File(outPath), outFormat)
    }
}