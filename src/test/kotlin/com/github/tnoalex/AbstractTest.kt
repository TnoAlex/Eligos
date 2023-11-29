package com.github.tnoalex

import com.github.tnoalex.config.ConfigParser
import com.github.tnoalex.formatter.json.JsonFormatter
import com.github.tnoalex.foundation.filetools.FileContainer
import com.github.tnoalex.utils.StdOutErrWrapper
import depends.LangRegister
import java.io.File

abstract class AbstractTest {
    var analyzer: Analyzer? = null

    fun init(lang: List<String?>) {
        StdOutErrWrapper.init()
        ConfigParser.parserRules(null)
        LangRegister.register()
        analyzer = Analyzer(JsonFormatter(), lang)
    }

    fun createTestContext(
        srcPath: String,
        outputName: String,
        outPath: String
    ) {
        FileContainer.initFileContainer(File(srcPath), File(outPath), outputName)
    }
}