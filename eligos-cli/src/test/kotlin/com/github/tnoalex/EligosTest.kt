package com.github.tnoalex

import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.issues.Issue
import com.github.tnoalex.issues.common.CircularReferencesIssue
import com.github.tnoalex.issues.common.ExcessiveParamsIssue
import com.github.tnoalex.issues.common.UnusedImportIssue
import com.github.tnoalex.util.initEligosEnv
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@DisplayName("Eligos Processor Test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EligosTest {
    private lateinit var context: Context

    @BeforeAll
    fun initTestEnv() {
        initEligosEnv()
        context = ApplicationContext.getExactBean(Context::class.java)!!
    }

    @DisplayName("Circular References Test")
    @Test
    fun testCircularReferences() {
        val issues = issues<CircularReferencesIssue>()
        assertEquals(2, issues.size)
        assertNotNull(issues.find { it.affectedFiles.size == 2 })
        assertNotNull(issues.find { it.affectedFiles.size == 3 })
    }

    @DisplayName("Excessive Parameters Test")
    @Test
    fun testExcessiveParameters() {
        val issues = issues<ExcessiveParamsIssue>()
        assertEquals(3, issues.size)
        assertNotNull(issues.find { it.functionFqName == "test" && it.arity == 7 })
        assertNotNull(issues.find { it.functionFqName == "toomanyparams.pkg0.TooManyParams0.funP0" && it.arity == 8 })
    }

    @DisplayName("Unused Import Test")
    @Test
    fun testUnusedImport() {
        val issues = issues<UnusedImportIssue>()
        assertEquals(4, issues.size)
        assertNotNull(issues.find { it.unusedImports.size == 3 })
        assertNotNull(issues.find {
            it.unusedImports == listOf(
                "unusedimport.pkg2.*",
                "java.util.*",
                "java.util.jar.*"
            )
        })
    }

    private inline fun <reified T : Issue> issues(): List<T> {
        return context.issues.filterIsInstance<T>()
    }
}