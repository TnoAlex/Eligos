package com.github.tnoalex.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class RulerParserTest {
    @Test
    fun testRuleParser() {
        ConfigParser.parserRules(File("E:\\code\\depends-smell\\src\\test\\resources\\rules-sample\\rules.yaml"))
        assertEquals((ConfigContainer.getByType(FunctionConfig::class) as FunctionConfig).arity, 5)
    }
}