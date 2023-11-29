package com.github.tnoalex.processor.kotlin

import com.github.tnoalex.AbstractTest
import org.junit.jupiter.api.Test

class BaseInfoProcessorTest : AbstractTest() {
    @Test
    fun testEnterFile() {
        init(listOf("kotlin"))
        createTestContext(
            "E:\\code\\depends-smell\\src\\test\\resources\\processor-samples",
            "", "./"
        )
    }
}