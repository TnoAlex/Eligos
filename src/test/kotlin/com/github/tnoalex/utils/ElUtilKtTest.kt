package com.github.tnoalex.utils

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ElUtilKtTest {

    @Test
    fun testInvokeElExpression() {
        val fileName = "F:/test.kt"
        val res = evaluateBooleanElExpression("\${fileName}.endsWith(\".kt\") && \${fileName}.startsWith(\"F\") ", listOf(fileName))
        assertTrue(res)
    }
}