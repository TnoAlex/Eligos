package com.github.tnoalex.analyzer

import com.github.tnoalex.entity.enums.FormatterTypeEnum
import com.github.tnoalex.foundation.filetools.FileContainer
import com.github.tnoalex.rules.RulerParser
import com.github.tnoalex.utils.StdOutErrWrapper
import java.io.File

abstract class AbstractAnalyzerTest(lang: String) {
    var analyzer: AbstractSmellAnalyzer? = null

    init {
        StdOutErrWrapper.init()
        SmellAnalyzerRegister.INSTANCE.init()
        RulerParser.parserRules(null)
        analyzer = SmellAnalyzerRegister.INSTANCE.getAnalyzerByLang(lang)
    }

    fun createTestContext(
        lang: String,
        srcPath: String,
        outputName: String,
        outPath: String,
        outFormat: FormatterTypeEnum
    ) {
        analyzer!!.createAnalyticsContext(lang, outFormat)
        FileContainer.initFileContainer(File(srcPath), File(outPath), outputName)
    }
}