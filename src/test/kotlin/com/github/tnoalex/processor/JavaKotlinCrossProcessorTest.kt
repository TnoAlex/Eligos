package com.github.tnoalex.processor

import com.github.tnoalex.Analyzer
import com.github.tnoalex.formatter.json.JsonFormatter
import com.github.tnoalex.foundation.ApplicationContext
import com.github.tnoalex.foundation.filetools.FileHelper
import com.github.tnoalex.issues.ImproperInternalExposedIssue
import com.github.tnoalex.utils.StdOutErrWrapper
import depends.LangRegister
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JavaKotlinCrossProcessorTest {
    private var analyzer = Analyzer(JsonFormatter(), listOf("kotlin", "java"))

    init {
        StdOutErrWrapper.init()
        LangRegister.register()

        ApplicationContext.getBean(FileHelper::class.java)[0].setFileInfo(
            File("E:\\code\\depends-smell\\src\\test\\resources\\kotlin-code-samples\\kotlininternal"),
            File("./"), "out"
        )
        analyzer.analyze()
    }

    @Test
    fun testInternalExtendsOrImplements() {
        val issues = analyzer.getContext().getIssuesByType(ImproperInternalExposedIssue::class)
        assertEquals(4, issues.size)
        val issue = issues.map { it as ImproperInternalExposedIssue }
            .find { it.javaClassElement.qualifiedName == "internaltest.java.UseInternalInJava0" }
        assertNotNull(issues)
        assertEquals("internaltest.kotlin.InternalOpenClassInKotlin", issue!!.kotlinClassElement.qualifiedName)
        assertEquals("Extend", issue.relation)
    }
}