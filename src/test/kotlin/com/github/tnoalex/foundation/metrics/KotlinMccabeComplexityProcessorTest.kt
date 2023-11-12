package com.github.tnoalex.foundation.metrics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KotlinMccabeComplexityProcessorTest {
    private val processor = KotlinMccabeComplexityProcessor()

    @Test
    fun testMccabe() {
        processor.processFile("E:\\code\\depends-smell\\src\\test\\resources\\metrics-samples\\MccabeSample0.kt")
        val cc = processor.getMccabeComplex()
        assertEquals(cc["ccSample0@0"], 3)
        assertEquals(cc["ccSample1@2"], 9)
        assertEquals(cc["ccSample2@1"], 4)
        assertEquals(cc["ccSample3@4"], 5)
    }
}