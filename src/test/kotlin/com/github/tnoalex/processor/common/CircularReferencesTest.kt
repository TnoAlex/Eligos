package com.github.tnoalex.processor.common

import com.github.tnoalex.AbstractTest
import com.github.tnoalex.issues.CircularReferencesIssue
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CircularReferencesTest : AbstractTest() {
    @Test
    fun findCircularReferences() {
        init(listOf("kotlin"))
        createTestContext(
            "E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\circularceferences",
            "out",
            "./"
        )
        analyzer!!.analyze()
        val issues = analyzer!!.getContextByLang("kotlin")!!.getIssuesByType(CircularReferencesIssue::class)
        assertEquals(2, issues.size)
        assertEquals(2, issues[0].affectedFiles.size)
        assertEquals(3, issues[1].affectedFiles.size)
        assertArrayEquals(
            arrayOf(
                "E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\circularceferences\\pkg1\\CircularReferences0.kt",
                "E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\circularceferences\\pkg1\\CircularReferences1.kt",
                "E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\circularceferences\\pkg1\\CircularReferences2.kt"
            ),
            issues[1].affectedFiles.toArray()
        )
    }
}