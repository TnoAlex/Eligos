package com.github.tnoalex.analyzer.kotlin

import com.github.tnoalex.AbstractTest
import com.github.tnoalex.formatter.FormatterTypeEnum
import org.junit.jupiter.api.Test

class TooManyParametersTest : AbstractTest("kotlin") {

    @Test
    fun tesFindTooManyParameters() {
        init()
        super.createTestContext(
            "E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\toomanyparams",
            "out",
            "./",
            FormatterTypeEnum.JSON
        )
        analyzer!!.findTooManyParameters()
    }
}