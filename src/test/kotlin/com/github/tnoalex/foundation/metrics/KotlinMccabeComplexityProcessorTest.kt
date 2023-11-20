package com.github.tnoalex.foundation.metrics

import com.github.tnoalex.foundation.algorithm.metrics.KotlinMccabeComplexityProcessor
import com.github.tnoalex.foundation.asttools.AstProcessorContainer
import com.github.tnoalex.foundation.asttools.kotlin.KotlinAstParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KotlinMccabeComplexityProcessorTest {

    @Test
    fun testMccabe() {
        val processor =
            AstProcessorContainer.getByType(KotlinMccabeComplexityProcessor::class)
        KotlinAstParser.parseAst("E:\\code\\depends-smell\\src\\test\\resources\\metrics-samples\\MccabeSample0.kt")
        val cc = (processor!![0] as KotlinMccabeComplexityProcessor).getMccabeComplex()
        assertEquals(cc["ccSample0@0"], 3)
        assertEquals(cc["ccSample1@2"], 9)
        assertEquals(cc["ccSample2@1"], 4)
        assertEquals(cc["ccSample3@4"], 5)
    }
}