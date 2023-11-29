package com.github.tnoalex.processor.common

import com.github.tnoalex.AbstractTest
import com.github.tnoalex.issues.UnusedImportIssue
import org.junit.jupiter.api.Test

class UnUsedImportTest : AbstractTest() {

    @Test
    fun testUnUsedImport() {
        init(listOf("kotlin"))
        super.createTestContext(
            "E:\\code\\Depends-smell\\src\\test\\resources\\kotlin-code-samples\\unusedimport",
            "out",
            "./"
        )
        analyzer!!.analyze()
        val issues = analyzer!!.getContextByLang("kotlin")!!.getIssuesByType(UnusedImportIssue::class)
    }
}