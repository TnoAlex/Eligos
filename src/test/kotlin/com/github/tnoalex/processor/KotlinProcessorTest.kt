package com.github.tnoalex.processor

import com.github.tnoalex.Analyzer
import com.github.tnoalex.formatter.json.JsonFormatter
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.bean.register.DefaultBeanRegisterDistributor
import com.github.tnoalex.foundation.environment.JvmCompilerEnvironmentContext
import com.github.tnoalex.foundation.filetools.FileHelper
import com.github.tnoalex.issues.*
import com.github.tnoalex.utils.StdOutErrWrapper
import depends.LangRegister
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KotlinProcessorTest {
    private var analyzer = Analyzer(JsonFormatter(), listOf("kotlin", "java"))
    private val systemOut = System.out

    init {
        StdOutErrWrapper.init()
        LangRegister.register()
        ApplicationContext.addBeanRegisterDistributor(listOf(DefaultBeanRegisterDistributor))
        ApplicationContext.init()
        ApplicationContext.getExactBean(JvmCompilerEnvironmentContext::class.java)
            ?.initCompilerEnv(File(".\\src\\test\\resources\\kotlin-code-samples\\unusedimport").toPath())
        ApplicationContext.getBean(FileHelper::class.java)[0].setFileInfo(
            File(".\\src\\test\\resources\\kotlin-code-samples\\unusedimport"),
            File("./"), "out"
        )
        analyzer.analyze()
    }

    @Test
    fun findCircularReferences() {
        val issues = analyzer.context.getIssuesByType(CircularReferencesIssue::class)
        assertEquals(2, issues.size)
        assertNotNull(issues.find { it.affectedFiles.size == 2 })
        assertNotNull(issues.find { it.affectedFiles.size == 3 })
        assertArrayEquals(
            arrayOf(
                ".\\src\\test\\resources\\kotlin-code-samples\\circularceferences\\pkg1\\CircularReferences0.kt",
                ".\\src\\test\\resources\\kotlin-code-samples\\circularceferences\\pkg1\\CircularReferences1.kt",
                ".\\src\\test\\resources\\kotlin-code-samples\\circularceferences\\pkg1\\CircularReferences2.kt"
            ),
            issues[1].affectedFiles.toArray()
        )
    }

    @Test
    fun tesFindTooManyParameters() {
        val issues = analyzer.context.getIssuesByType(ExcessiveParamsIssue::class)
        assertEquals(3, issues.size)
        assertTrue(issues.map { it as ExcessiveParamsIssue }.find { it.arity == 8 } != null)
    }

    @Test
    fun testUnUsedImport() {
        val issues = analyzer.context.getIssuesByType(UnusedImportIssue::class)
        assertEquals(2, issues.size)
        assertNotNull(issues.find { it.affectedFiles.size == 2 })
        assertNotNull(issues.find { it.affectedFiles.size == 3 })
    }

    @Test
    fun testMccabe() {
        val issues = analyzer.context.getIssuesByType(ComplexMethodIssue::class)
        assertEquals(1, issues.size)
        assertEquals("solveSCC()", (issues[0] as ComplexMethodIssue).methodSignature)
        assertEquals(12, (issues[0] as ComplexMethodIssue).circleComplexity)
    }

    @Test
    fun testOptimizedTailRecursion() {
        val issues = analyzer.context.getIssuesByType(OptimizedTailRecursionIssue::class)
        assertEquals(2, issues.size)
        assertNotNull(issues.find { (it as OptimizedTailRecursionIssue).functionSignature == "factorial0(n:Int,acc:Int=1)" })
        assertNotNull(issues.find { (it as OptimizedTailRecursionIssue).functionSignature == "factorial4(n:Int,acc:Int=1)" })
    }

    @Test
    fun testImplicitSingleExprFunction() {
        val issues = analyzer.context.getIssuesByType(ImplicitSingleExprFunctionIssue::class)
        assertEquals(1, issues.size)
        assertEquals("test0()", (issues[0] as ImplicitSingleExprFunctionIssue).functionSignature)
    }
}