package com.github.tnoalex.analyzer.kotlin

import com.github.tnoalex.analyzer.AbstractAnalyzerTest
import com.github.tnoalex.formatter.FormatterTypeEnum
import org.junit.jupiter.api.Test

class UnUsedImportTest : AbstractAnalyzerTest("kotlin") {

    @Test
    fun testUnUsedImport() {
        init()
        super.createTestContext(
            "E:\\code\\Depends-smell\\src\\test\\resources\\kotlin-code-samples\\unusedimport",
            "out",
            "./",
            FormatterTypeEnum.JSON
        )
        analyzer!!.findUselessImport()
    }
}