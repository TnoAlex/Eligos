package com.github.tnoalex.analyzer

import com.github.tnoalex.analyzer.singlelang.AbstractSingleLangAnalyzer
import com.github.tnoalex.analyzer.singlelang.kotlin.KotlinSmellAnalyzer
import com.github.tnoalex.formatter.FormatterTypeEnum
import com.github.tnoalex.foundation.filetools.FileContainer
import com.github.tnoalex.rules.RulerParser
import com.github.tnoalex.utils.StdOutErrWrapper
import depends.LangRegister
import java.io.File

abstract class AbstractAnalyzerTest(private val lang: String) {
    var analyzer: AbstractSingleLangAnalyzer? = null


    fun init() {
        StdOutErrWrapper.init()
        RulerParser.parserRules(null)
        LangRegister.register()
        analyzer = KotlinSmellAnalyzer()
    }

    fun createTestContext(
        srcPath: String,
        outputName: String,
        outPath: String,
        outFormat: FormatterTypeEnum
    ) {
        FileContainer.initFileContainer(File(srcPath), File(outPath), outputName)
        analyzer!!.createAnalyticsContext(outFormat)
    }
}