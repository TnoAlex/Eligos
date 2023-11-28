package com.github.tnoalex.analyzer.kotlin

import com.github.tnoalex.AbstractTest
import com.github.tnoalex.formatter.FormatterTypeEnum
import org.junit.jupiter.api.Test

class CircularReferencesTest : AbstractTest("kotlin") {
    @Test
    fun findCircularReferences() {
        init()
        createTestContext(
            "E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\circularceferences",
            "out",
            "./",
            FormatterTypeEnum.JSON
        )
        analyzer!!.findCircularReferences()
    }
}