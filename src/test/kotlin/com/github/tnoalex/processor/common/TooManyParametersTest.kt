package com.github.tnoalex.processor.common

import com.github.tnoalex.AbstractTest
import com.github.tnoalex.issues.ExcessiveParamsIssue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TooManyParametersTest : AbstractTest() {

    @Test
    fun tesFindTooManyParameters() {
        init(listOf("kotlin"))
        super.createTestContext(
            "E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\toomanyparams",
            "out",
            "./"
        )
        analyzer!!.analyze()
        val issues = analyzer!!.getContextByLang("kotlin")!!.getIssuesByType(ExcessiveParamsIssue::class)
        assertEquals(2,issues.size)
        assertEquals(8,(issues[0] as ExcessiveParamsIssue).arity)
    }
}