package com.github.tnoalex.processor

import com.github.tnoalex.Analyzer
import com.github.tnoalex.config.ConfigParser
import com.github.tnoalex.formatter.json.JsonFormatter
import com.github.tnoalex.foundation.filetools.FileContainer
import com.github.tnoalex.issues.CircularReferencesIssue
import com.github.tnoalex.issues.ComplexMethodIssue
import com.github.tnoalex.issues.ExcessiveParamsIssue
import com.github.tnoalex.issues.UnusedImportIssue
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
        assertEquals("solveSCC@0", (issues[0] as ComplexMethodIssue).methodId)
        assertEquals(18, (issues[0] as ComplexMethodIssue).circleComplexity)
    }

}