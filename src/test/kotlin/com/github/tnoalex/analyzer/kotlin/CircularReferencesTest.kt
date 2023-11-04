package com.github.tnoalex.analyzer.kotlin

import com.github.tnoalex.analyzer.AbstractAnalyzerTest
import com.github.tnoalex.entity.enums.FormatterTypeEnum
import org.junit.jupiter.api.Test

class CircularReferencesTest : AbstractAnalyzerTest("kotlin") {
    @Test
    fun findCircularReferences() {
        super.createTestContext(
            "kotlin",
            "E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\circularceferences",
            "out",
            "./",
            FormatterTypeEnum.JSON
        )
        analyzer!!.findCircularReferences()
    }
}