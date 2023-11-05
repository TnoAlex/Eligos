package com.github.tnoalex.analyzer.kotlin

import com.github.tnoalex.analyzer.AbstractAnalyzerTest
import com.github.tnoalex.entity.enums.FormatterTypeEnum
import org.junit.jupiter.api.Test

class TooManyParametersTest : AbstractAnalyzerTest("kotlin") {

    @Test
    fun tesFindTooManyParameters() {
        super.createTestContext(
            "kotlin",
            "E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\toomanyparams",
            "out",
            "./",
            FormatterTypeEnum.JSON
        )
        analyzer!!.findTooManyParameters()
    }
}