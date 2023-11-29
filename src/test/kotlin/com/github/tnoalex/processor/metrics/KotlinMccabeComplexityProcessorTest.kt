package com.github.tnoalex.processor.metrics

import com.github.tnoalex.AbstractTest
import com.github.tnoalex.issues.ComplexMethodIssue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KotlinMccabeComplexityProcessorTest : AbstractTest() {

    @Test
    fun testMccabe() {
        init(listOf("kotlin"))
        createTestContext(
            "E:\\code\\depends-smell\\src\\test\\resources\\metrics-samples",
            "", "./"
        )
        analyzer!!.analyze()
        val issues = analyzer!!.getContextByLang("kotlin")!!.getIssuesByType(ComplexMethodIssue::class)
        assertEquals(1,issues.size)
        assertEquals("solveSCC@0",(issues[0] as ComplexMethodIssue).methodId)
        assertEquals(18,(issues[0] as ComplexMethodIssue).circleComplexity)
    }
}