package com.github.tnoalex.processor.metrics

import com.github.tnoalex.AbstractTest
import com.github.tnoalex.processor.AstProcessorContainer
import com.github.tnoalex.processor.kotlin.KotlinMccabeComplexityProcessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KotlinMccabeComplexityProcessorTest : AbstractTest() {

    @Test
    fun testMccabe() {
        init(listOf("kotlin"))
//        (AstProcessorContainer.getByType(KotlinMccabeComplexityProcessor::class)
//            ?.get(0) as MccabeComplexityProcessor).finishProcess()
        createTestContext(
            "E:\\code\\depends-smell\\src\\test\\resources\\metrics-samples",
            "", "./"
        )
        val processor =
            AstProcessorContainer.getByType(KotlinMccabeComplexityProcessor::class)
        val cc = (processor!![0] as KotlinMccabeComplexityProcessor).getMccabeComplex()
        println(cc)
        assertEquals(cc["ccSample0@0"], 3)
        assertEquals(cc["ccSample1@2"], 9)
        assertEquals(cc["tarjan@1"], 6)
        assertEquals(cc["ccSample2@1"], 4)
        assertEquals(cc["ccSample3@4"], 8)
        assertEquals(cc["solveSCC@0"], 18)
    }
}