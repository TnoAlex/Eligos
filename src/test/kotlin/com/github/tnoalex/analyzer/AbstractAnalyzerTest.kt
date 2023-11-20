package com.github.tnoalex.analyzer

import com.github.tnoalex.analyzer.singlelang.SingleLangAbstractSmellAnalyzer
import com.github.tnoalex.analyzer.singlelang.SingleSmellAnalyzerContainer
import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.foundation.filetools.FileContainer
import com.github.tnoalex.rules.RulerParser
import com.github.tnoalex.utils.StdOutErrWrapper
import java.io.File

abstract class AbstractAnalyzerTest(lang: String) {
    var analyzer: SingleLangAbstractSmellAnalyzer? = null

    init {
        StdOutErrWrapper.init()
        RulerParser.parserRules(null)
        analyzer = SingleSmellAnalyzerContainer.getByKey(lang)
    }

    fun createTestContext(
        lang: String,
        srcPath: String,
        outputName: String,
        outPath: String,
        outFormat: FormatterTypeEnum
    ) {
        FileContainer.initFileContainer(File(srcPath), File(outPath), outputName)
        analyzer!!.createAnalyticsContext(lang, outFormat)
    }
}