package com.github.tnoalex.utils

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ElUtilKtTest {

    @Test
    fun testInvokeElExpression() {
        class Test {
            val name = "Bob"

            fun testEval(): Boolean {
                val test = Test()
                val age = 30
                return evaluateBooleanElExpression("\${name} == \"Bob\" && #{age} == 30", test, age)
            }
        }
        val test = Test()
        assertTrue(test.testEval())
    }
}