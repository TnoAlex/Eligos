package com.github.tnoalex.foundation.asttools.processor.kotlin

import com.github.tnoalex.AbstractTest
import com.github.tnoalex.formatter.FormatterTypeEnum
import org.junit.jupiter.api.Test

class BaseInfoProcessorTest : AbstractTest("kotlin") {
    @Test
    fun testEnterFile() {
        init()
        createTestContext(
            "E:\\code\\depends-smell\\src\\test\\resources\\processor-samples",
            "", "./", FormatterTypeEnum.JSON
        )
    }
}