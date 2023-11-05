package com.github.tnoalex.rules

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class RulerParserTest {
    @Test
    fun testRuleParser() {
        RulerParser.parserRules(File("E:\\code\\depends-smell\\src\\test\\resources\\rules-sample\\rules.yaml"))
        assertEquals((RuleContainer.INSTANT.getRuleByType(FunctionRule::class) as FunctionRule).arity, 5)
    }
}