package com.github.tnoalex

import com.github.tnoalex.events.AllFileParsedEvent
import com.github.tnoalex.foundation.EligosBeforeAllTestExtension
import com.github.tnoalex.foundation.RequireTestProcessor
import com.github.tnoalex.issues.common.CircularReferencesIssue
import com.github.tnoalex.issues.common.ExcessiveParamsIssue
import com.github.tnoalex.issues.common.UnusedImportIssue
import com.github.tnoalex.processor.common.CircularReferencesProcessor
import com.github.tnoalex.processor.common.TooManyParametersProcessor
import com.github.tnoalex.processor.common.UnUsedImportProcessor
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import java.util.*

@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(EligosBeforeAllTestExtension::class)
class CommonProcessorTest {

    @RequireTestProcessor("resources@toomanyParams")
    fun testTooManyParameters(processor: TooManyParametersProcessor) {
        psiFiles().forEach { psiFile ->
            processor.process(psiFile)
        }
        val excessiveParamsIssues = issue<ExcessiveParamsIssue>()
        assertEquals(3, excessiveParamsIssues.size)
        assertArrayEquals(
            arrayOf(8, 6),
            excessiveParamsIssues.firstOrNull {
                it.functionSignature == "toomanyparams.pkg0.TooManyParams0.funP0(p0,p1,p2,p3,p4,p5,p6,p7)"
            }?.let { arrayOf(it.arity, it.startLine) }
        )
        assertArrayEquals(
            arrayOf(8, 4),
            excessiveParamsIssues.firstOrNull {
                it.functionSignature == "toomanyparams.pkg1.funP1(p0,p1,p2,p3,p4,p5,p6,p7)"
            }?.let { arrayOf(it.arity, it.startLine) }
        )
    }

    @RequireTestProcessor("resources@circularRceferences")
    fun testCircularReferences(processor: CircularReferencesProcessor) {
        psiFiles().forEach { psiFile ->
            processor.process(psiFile)
        }
        processor.resultGeneration(AllFileParsedEvent)
        val circularReferencesIssues = issue<CircularReferencesIssue>()
        assertEquals(2, circularReferencesIssues.size)
        var result = circularReferencesIssues.firstOrNull {
            it.affectedFiles.all { f -> f.contains("pkg0") }
        }?.refMatrix?.second?.toTypedArray()
        result?.let { Arrays.sort(it, Comparator.comparing { list -> list.joinToString() }) }
        assertArrayEquals(
            arrayOf(arrayListOf(0, 1), arrayListOf(1, 0)),
            result
        )
        result = circularReferencesIssues.firstOrNull {
            it.affectedFiles.all { f -> f.contains("pkg1") }
        }?.refMatrix?.second?.toTypedArray()
        result?.let { Arrays.sort(it, Comparator.comparing { list -> list.joinToString() }) }
        assertArrayEquals(
            arrayOf(arrayListOf(0, 0, 1), arrayListOf(0, 1, 0), arrayListOf(1, 0, 0)),
            result
        )
    }

    @RequireTestProcessor("resources@unusedImport")
    fun testUnUsedImport(processor: UnUsedImportProcessor) {
        psiFiles().forEach { psiFile ->
            processor.process(psiFile)
        }
        val unusedImportIssues = issue<UnusedImportIssue>()
        assertEquals(3, unusedImportIssues.size)
        assertArrayEquals(
            arrayOf("java.util.concurrent.*;"),
            unusedImportIssues.firstOrNull {
                it.affectedFiles.all { f -> f.endsWith("UnUsedImportJava.java") }
            }?.unusedImports?.toTypedArray()
        )
        assertArrayEquals(
            arrayOf("unusedimport.pkg2.*", "java.util.*", "java.util.jar.*"),
            unusedImportIssues.firstOrNull {
                it.affectedFiles.all { f -> f.endsWith("UnusedImport0.kt") }
            }?.unusedImports?.toTypedArray()
        )
    }
}