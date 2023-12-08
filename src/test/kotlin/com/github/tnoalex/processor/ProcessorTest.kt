package com.github.tnoalex.processor

import com.github.tnoalex.Analyzer
import com.github.tnoalex.config.ConfigParser
import com.github.tnoalex.formatter.json.JsonFormatter
import com.github.tnoalex.foundation.filetools.FileContainer
import com.github.tnoalex.issues.*
import com.github.tnoalex.utils.StdOutErrWrapper
import depends.LangRegister
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProcessorTest {
    private var analyzer = Analyzer(JsonFormatter(), listOf("kotlin"))

    init {
        StdOutErrWrapper.init()
        ConfigParser.parserRules(null)
        LangRegister.register()

        FileContainer.initFileContainer(
            File("E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples"),
            File("./"), "out"
        )
        analyzer.analyze()
    }

    @Test
    fun findCircularReferences() {
        val issues = analyzer.getContextByLang("kotlin")!!.getIssuesByType(CircularReferencesIssue::class)
        assertEquals(2, issues.size)
        assertNotNull(issues.find { it.affectedFiles.size == 2 })
        assertNotNull(issues.find { it.affectedFiles.size == 3 })
        assertArrayEquals(
            arrayOf(
                "E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\circularceferences\\pkg1\\CircularReferences0.kt",
                "E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\circularceferences\\pkg1\\CircularReferences1.kt",
                "E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\circularceferences\\pkg1\\CircularReferences2.kt"
            ),
            issues[1].affectedFiles.toArray()
        )
    }

    @Test
    fun tesFindTooManyParameters() {
        val issues = analyzer.getContextByLang("kotlin")!!.getIssuesByType(ExcessiveParamsIssue::class)
        assertEquals(2, issues.size)
        assertEquals(8, (issues[0] as ExcessiveParamsIssue).arity)
    }

    @Test
    fun testUnUsedImport() {
        val issues = analyzer.getContextByLang("kotlin")!!.getIssuesByType(UnusedImportIssue::class)
        assertEquals(2, issues.size)
        assertNotNull(issues.find { it.affectedFiles.size == 2 })
        assertNotNull(issues.find { it.affectedFiles.size == 3 })
    }

    @Test
    fun testMccabe() {
        val issues = analyzer.getContextByLang("kotlin")!!.getIssuesByType(ComplexMethodIssue::class)
        assertEquals(1, issues.size)
        assertEquals("solveSCC()", (issues[0] as ComplexMethodIssue).methodSignature)
        assertEquals(12, (issues[0] as ComplexMethodIssue).circleComplexity)
    }

    @Test
    fun testOptimizedTailRecursion() {
        val issues = analyzer.getContextByLang("kotlin")!!.getIssuesByType(OptimizedTailRecursionIssue::class)
        assertEquals(2, issues.size)
        assertNotNull(issues.find { (it as OptimizedTailRecursionIssue).functionSignature == "factorial0(n:Int,acc:Int=1)" })
        assertNotNull(issues.find { (it as OptimizedTailRecursionIssue).functionSignature == "factorial4(n:Int,acc:Int=1)" })
    }

    @Test
    fun testImplicitSingleExprFunction() {
        val issues = analyzer.getContextByLang("kotlin")!!.getIssuesByType(ImplicitSingleExprFunctionIssue::class)
        assertEquals(1, issues.size)
        assertEquals("test0()", (issues[0] as ImplicitSingleExprFunctionIssue).functionSignature)
    }
}